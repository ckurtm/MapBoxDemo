package app.wimt.cheese.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import androidx.annotation.VisibleForTesting
import androidx.core.os.bundleOf
import app.wimt.cheese.Constants
import app.wimt.cheese.geo.DeviceLocationManager
import app.wimt.cheese.geo.GeofenceManager
import app.wimt.cheese.meta.CheesyTreasure
import app.wimt.cheese.service.io.Command
import app.wimt.cheese.service.io.ServiceRequestContract
import app.wimt.cheese.service.io.ServiceRequestHandler
import app.wimt.cheese.service.io.ServiceRequestManager
import app.wimt.cheese.toCheese
import app.wimt.cheese.ui.HomeScreen
import com.example.tes.R
import com.google.android.gms.location.Geofence
import timber.log.Timber

/**
 * handles most of the app functionality i.e.
 *  - Listens for device location changes and notifies whatever client is bound of these changes
 *  - keeps a record of cheese markers added to map
 *  - listens for geofence events that are mapped to the cheese markers
 *  - tells any client that binds to it about the available map markers and current device location
 *  - allows bound clients to add or remove markers through a system notification or via the clients Messenger
 *
 *  N.B. one caveat, if the user changes permissions whilst the app is already started then this does not handle it gracefully..i think, but should be able to fix, just think it was out of scope
 *  for this.
 */
class CheesyService : Service(), ServiceRequestContract {

    private lateinit var notification: ServiceNotification //notifications feature of the app
    private lateinit var location: DeviceLocationManager //location feature of the app
    private lateinit var geofence: GeofenceManager //geofence feature of the app

    //a repo of markers, this could be changed to any other persisted storage if need be, should be trivial
    private val markers = mutableListOf<CheesyTreasure>()

    /**
     * if near any markes this will be populated.
     */
    private val markers_near = mutableListOf<CheesyTreasure>()

    //this is the commmunication feature of the app and handles comms between app and bound clients
    @VisibleForTesting
    private var requestManager: ServiceRequestManager? =
        ServiceRequestManager(
            ServiceRequestHandler(this)
        )

    override fun onCreate() {
        super.onCreate()
        notification = ServiceNotification(this)
        location =
            DeviceLocationManager(this, object : DeviceLocationManager.CurrentLocationCallback {
                override fun onLocationChanged(location: Location) {
                    Timber.d("onLocationChanged")
                    requestManager?.message(
                        Message.obtain(null, Command.Response.LOCATION, location)
                    )
                }

                override fun onLocationError(e: Exception) {
                    Timber.d("onLocationError")
                    requestManager?.message(
                        Message.obtain(null, Command.Response.LOCATION_ERROR, e)
                    )
                }
            })

        geofence = GeofenceManager(this, object : GeofenceManager.GeofenceCallback {
            override fun onGeofenceAdded() {
                Timber.d("onGeoFenceAdded: ")
            }

            override fun onGeofenceError(e: Exception) {
                Timber.d("onGeoFenceError: ")
                //TODO probably pass this to client or somethign...
            }

            override fun onGeofenceEnter(entries: List<Geofence>) {
                Timber.d("onGeofenceEnter: $entries ")
                //if we have a bound client then we tell them to show a dialog else we show a system notification
                if (requestManager?.hasClients() == true) {
                    //show notification on the UI
                    val msg = Message.obtain(null, Command.Response.MARKERS_NEAR)
                    val treasures = mutableListOf<CheesyTreasure>()
                    entries.forEach {
                        val entry = it.toCheese(markers)
                        if (entry != null) {
                            treasures.add(entry)
                        }
                    }
                    msg.data = bundleOf(Constants.ARGS_MARKER_ITEMS to treasures)
                    requestManager?.message(msg)
                } else {
                    //show as a system notification
                    val items = ArrayList<CheesyTreasure>()
                    entries.forEach {
                        val entry = it.toCheese(markers)
                        if (entry != null) {
                            items.add(entry)
                        }
                    }
                    markers_near.clear()
                    markers_near.addAll(items)
                    val intent = Intent(applicationContext, HomeScreen::class.java)
                    notification.message(applicationContext.getString(R.string.found), intent)
                }
            }

            override fun onGeofenceExit(exits: List<Geofence>) {
                Timber.d("onGeofenceExit: $exits")
                //say we notify the user about a found cheese and then we exit that before the
                // user actions the notification, this could be used to auto hide that notification..but i think its out of scope
            }

        })
    }

    override fun onUnbind(intent: Intent?): Boolean {
        requestManager?.detach() // this is because we only assume one client, if we ever had more than one , then maybe reconsider this.
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && Constants.SERVICE_ACTION_GEOFENCE.equals(intent.action, true)) {
            geofence.processEvent(intent)
        }
        if (intent != null && Constants.SERVICE_ACTION_MODE.equals(intent.action, true)) {
            val mocking = intent.getBooleanExtra(Constants.MOCK_MODE, false)
            location.mockMode(mocking)
            val mockLocation = intent.getParcelableExtra<Location>(Constants.SERVICE_MOCK_LOCATION)
            if (mockLocation != null) {
                location.mock(mockLocation)
            }
        }
        if (intent != null && Constants.SERVICE_ACTION_MOCK_LOCATION.equals(intent.action, true)) {
            val mockLoc = intent.getParcelableExtra<Location>(Constants.SERVICE_MOCK_LOCATION)
            if (mockLoc != null) {
                location.mock(mockLoc)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return requestManager?.messenger?.binder
    }

    override fun onDestroy() {
        requestManager?.detach()
        requestManager = null
        notification.detach()
        location.stopLocationUpdates()
        geofence.shutdown()
        super.onDestroy()
    }

    override fun connect(id: Int, client: Messenger) {
        Timber.d("CONNECT: ")
        stopForeground(true)
        requestManager?.register(id, client)
        //if we have any nearby markers then show them to client
        if (markers_near.isNotEmpty()) {
            val msg = Message.obtain(null, Command.Response.MARKERS_NEAR)
            msg.data = bundleOf(Constants.ARGS_MARKER_ITEMS to markers_near)
            requestManager?.message(msg)
//            markers_near.clear()
        }
    }

    override fun disconnect(id: Int) {
        startForeground(notification.notificationId, notification.notification())
        requestManager?.unregister(id)
    }

    override fun detach() {
        requestManager?.detach()
        stopSelf()
    }

    /**
     * start monitoring for location changes & tell whoever is connected about the current known markers
     */
    override fun monitor() {
        Timber.d("MONITOR: ")
        location.startMonitoring()
        val msg = Message.obtain(null, Command.Response.MARKERS)
        msg.data = bundleOf(Constants.ARGS_MARKER_ITEMS to markers)
        requestManager?.message(msg)
    }

    override fun addMarker(item: CheesyTreasure) {
        markers.add(item)
        geofence.add(item)
        val msg = Message.obtain(null, Command.Response.MARKER_ADDED)
        msg.data = bundleOf(Constants.ARGS_MARKER to item)
        requestManager?.message(msg)
    }

    override fun removeMarker(item: CheesyTreasure) {
        markers.remove(item)
        geofence.remove(item)
        val msg = Message.obtain(null, Command.Response.MARKER_REMOVED)
        msg.data = bundleOf(Constants.ARGS_MARKER to item)
        requestManager?.message(msg)
    }

    override fun clearNear() {
        markers_near.clear()
    }

}