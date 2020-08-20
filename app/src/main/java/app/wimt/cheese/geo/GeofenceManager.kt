package app.wimt.cheese.geo

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.wimt.cheese.Constants
import app.wimt.cheese.meta.CheesyTreasure
import app.wimt.cheese.service.CheesyService
import com.google.android.gms.location.*

/**
 * handles the geofencing functionality
 */
class GeofenceManager(private val context: Context, private val callback: GeofenceCallback) {

    interface GeofenceCallback {
        fun onGeofenceAdded()
        fun onGeofenceError(e: Exception)
        fun onGeofenceEnter(entries:List<Geofence>)
        fun onGeofenceExit(exits:List<Geofence>)
    }

    private val client = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, CheesyService::class.java)
        intent.action = Constants.SERVICE_ACTION_GEOFENCE
        PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    /**
     * adds a geofence
     */
    @SuppressLint("MissingPermission") //already done from the service
    fun add(item: CheesyTreasure) {
        client?.addGeofences(getGeofencingRequest(item), geofencePendingIntent)?.run {
            addOnSuccessListener {
                callback.onGeofenceAdded()
            }
            addOnFailureListener {
                callback.onGeofenceError(it)
            }
        }
    }

    /**
     * removes a geofence
     */
    fun remove(item: CheesyTreasure) {
        client.removeGeofences(listOf("${item.hashCode()}"))
    }

    /**
     * creates a Geofence from Cheese
     */
    private fun create(item: CheesyTreasure): Geofence {
        return Geofence.Builder()
            .setRequestId(item.note)
            .setCircularRegion(
                item.location!!.latitude,
                item.location.longitude,
                Constants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(-1)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER
                        or Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build()
    }

    private fun getGeofencingRequest(item: CheesyTreasure): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(listOf(create(item)))
        }.build()
    }


    /**
     * translates all incoming geofence events into something the CheesyService would understand
     */
    fun processEvent(intent:Intent){
        val event = GeofencingEvent.fromIntent(intent)
        if (event.hasError()) {
            callback.onGeofenceError(Exception("geofence error ${event.errorCode}"))
            return
        }
        // Get the transition type.
        val transition = event.geofenceTransition
        // Test that the reported transition was of interest.
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val items = event.triggeringGeofences
            if(transition == Geofence.GEOFENCE_TRANSITION_ENTER){
                callback.onGeofenceEnter(items)
            }else{
                callback.onGeofenceExit(items)
            }
        } else {
            callback.onGeofenceError(Exception("geofence error $transition"))
        }
    }


    fun shutdown(){
        client.removeGeofences(geofencePendingIntent)
    }


}