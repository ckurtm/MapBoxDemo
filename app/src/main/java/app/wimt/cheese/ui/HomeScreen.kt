package app.wimt.cheese.ui

import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import app.wimt.cheese.Constants
import app.wimt.cheese.Constants.PERMISSIONS.PERMISSION_ENTRIES
import app.wimt.cheese.geo.CurrentLocationViewModel
import app.wimt.cheese.meta.CheesyTreasure
import app.wimt.cheese.permissions.PermissionsHandler
import app.wimt.cheese.service.ServiceCommunicator
import app.wimt.cheese.service.io.ServiceResponseContract
import app.wimt.cheese.testing.EspressoIdlingResource
import app.wimt.cheese.ui.widget.LCAMapView
import app.wimt.cheese.ui.widget.PinView
import com.example.tes.R
import com.google.android.gms.common.api.ResolvableApiException
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import kotlinx.android.synthetic.main.activity_home.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber


class HomeScreen : AppCompatActivity(), EasyPermissions.RationaleCallbacks {

    @VisibleForTesting
    val markers = mutableMapOf<CheesyTreasure, MarkerView>()
    private lateinit var mapView: LCAMapView
    private var map: MapboxMap? = null
    private var markerManager: MarkerViewManager? = null
    private lateinit var permissions: PermissionsHandler
    private var mylocationView: MarkerView? = null
    private val model by viewModels<CurrentLocationViewModel>()

    private val comms = ServiceCommunicator(this, this, object : ServiceResponseContract {

        override fun onLocationChange(location: Location) {
            model.location.postValue(location)
        }

        override fun onLocationError(e: Exception) {
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(
                        this@HomeScreen,
                        Constants.REQUEST_CHECK_SETTIGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    //TODO Ignore the error...maybe
                }
            }
        }

        override fun onMarkerAdded(marker: CheesyTreasure) {
            renderCheeseMarker(marker)
        }

        override fun onMarkerRemoved(marker: CheesyTreasure) {
            val item = markers[marker]
            if (item != null) {
                markerManager?.removeMarker(item)
            }
        }

        override fun onMarkers(markers: List<CheesyTreasure>) {
            Timber.d("onMarkers ${markers.size}")
            markers.forEach { renderCheeseMarker(it) }
        }

        override fun onMarkersNear(entries: List<CheesyTreasure>) {
            Timber.d("onMarkersNear: ${entries.size}")
            //TODO could probably have a queue for showing found notes here.
            if (entries.isNotEmpty()) {
                removeCheesyNote(entries.first())
            }
        }

    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_home)
        mapView = findViewById(R.id.map)

        comms.mocking = intent.getBooleanExtra(Constants.MOCK_MODE,false)
        comms.mockLocation = intent.getParcelableExtra(Constants.SERVICE_MOCK_LOCATION)

        /**
         * if you really want to stop the app then u need to click the onScreen close button
         */
        findViewById<View>(R.id.close).setOnClickListener {
            comms.shutdown()
            finish()
        }

        mapView.onCreate(savedInstanceState)
        mapView.attachLifeCycle(this)

        permissions = PermissionsHandler(this, null,
            object : PermissionsHandler.PermissionsCallBack {
                override fun onGranted() {
                    initializeMap()
                }

                override fun onDenied() {
                    goodBye()
                }
            })

        initializeMap()

        model.location.observe(this, Observer {
            renderLocationMarker(it)
        })
    }

    override fun onStop() {
        if (mylocationView != null) {
            markerManager?.removeMarker(mylocationView!!)
        }
        markers.clear()
        super.onStop()
    }

    fun goodBye() {
        comms.shutdown()
        Toast.makeText(this@HomeScreen, "Good Bye", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, grant: IntArray) {
        super.onRequestPermissionsResult(code, perms, grant)
        this.permissions.onRequestPermissionsResult(code, perms, grant)
    }

    @AfterPermissionGranted(Constants.PERMISSIONS.ACCESS_FINE_LOCATION)
    private fun initializeMap() {

        EspressoIdlingResource.increment()
        if (EasyPermissions.hasPermissions(this, *PERMISSION_ENTRIES)) {
            mapView.getMapAsync { mapbox ->
                mapbox.setStyle(Style.MAPBOX_STREETS) {
                    map = mapbox
                    markerManager = MarkerViewManager(mapView, mapbox)
                    setupLongPressListener()
                    comms.startLocationMonitoring()
                }
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.location_rationale),
                Constants.PERMISSIONS.ACCESS_FINE_LOCATION,
                *PERMISSION_ENTRIES
            )
        }

    }

    private fun setupLongPressListener() {
        map?.addOnMapLongClickListener {
            createCheesyNote(it)
            true
        }
    }


    override fun onResume() {
        super.onResume()
        initializeMap()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun createCheesyNote(point: LatLng) {
        val note = CheesyDialog(this) { note ->
            EspressoIdlingResource.increment()
            comms.addCheeseMarker(CheesyTreasure(point, note))
        }
        note.show()
    }

    private fun removeCheesyNote(item: CheesyTreasure) {
        EspressoIdlingResource.increment()
        val note = CheesyFoundDialog(this, item) {
            comms.removeCheeseMarker(item)
        }
        note.show()
        comms.clearNear()
    }

    private fun renderCheeseMarker(marker: CheesyTreasure) {
        val cheeseView = layoutInflater.inflate(R.layout.marker_cheese, root_view, false)
        val markerView = MarkerView(marker.location!!, cheeseView)
        markerManager?.addMarker(markerView)
        markers[marker] = markerView
        EspressoIdlingResource.decrement()
    }

    private fun renderLocationMarker(location: Location) {
        if (mylocationView != null) {
            markerManager?.removeMarker(mylocationView!!)
        }
        val pinView =
            layoutInflater.inflate(R.layout.marker_location, root_view, false) as PinView
        mylocationView =
            MarkerView(LatLng(location.latitude, location.longitude), pinView)
        markerManager?.addMarker(mylocationView!!)
        val position: CameraPosition = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(16.0)
            .tilt(3.0)
            .build()
        map?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000)
        root_view.postDelayed({ EspressoIdlingResource.decrement() }, 200)
    }

    override fun onRationaleDenied(requestCode: Int) {
        goodBye()
    }

    override fun onRationaleAccepted(requestCode: Int) {
    }

}