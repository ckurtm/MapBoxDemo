package app.wimt.cheese.geo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


/**
 * Listens for device location changes and broadcasts theses to the @attached CurrentLocationCallback
 */
class DeviceLocationManager(private val ctx: Context, val callback: CurrentLocationCallback) {

    /**
     * callback events that this class can broadcast to whoever is listening
     */
    interface CurrentLocationCallback {
        fun onLocationChanged(location: Location)
        fun onLocationError(e: Exception)
    }

    @SuppressLint("MissingPermission")
    private val client = LocationServices.getFusedLocationProviderClient(ctx)

    private val request: LocationRequest by lazy {
        LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            result ?: return
            for (location in result.locations) {
                callback.onLocationChanged(location)
            }
        }
    }

    /**
     * tells the system we want to start listening for location changes, but we first make
     * sure location settings are convinient for our use case.
     */
    @SuppressLint("MissingPermission")
    fun startMonitoring() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(ctx)
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            client.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        callback.onLocationChanged(location)
                    }
                }

            startLocationUpdates()
        }
        task.addOnFailureListener { exception ->
            callback.onLocationError(exception)
        }
    }

    /**
     * tells the system we want to start listening for location changes
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        client.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun mockMode(enabled: Boolean){
        client.setMockMode(enabled)
    }

    @SuppressLint("MissingPermission")
    fun mock(location: Location){
        client.setMockLocation(location)
    }

    fun stopLocationUpdates() {
        client.removeLocationUpdates(locationCallback)
    }
}