package app.wimt.cheese.service.io

import android.location.Location
import android.os.Handler
import android.os.Message
import app.wimt.cheese.Constants
import app.wimt.cheese.meta.CheesyTreasure

/**
 * Handles all incoming comms to the client i.e. service -> client requests, using {@link ServiceResponse}
 */
class ServiceResponseHandler(private val contract: ServiceResponseContract) : Handler() {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            Command.Response.LOCATION -> contract.onLocationChange(msg.obj as Location)
            Command.Response.MARKER_ADDED -> {
                contract.onMarkerAdded(msg.data[Constants.ARGS_MARKER] as CheesyTreasure)
            }
            Command.Response.MARKER_REMOVED-> contract.onMarkerRemoved(msg.data[Constants.ARGS_MARKER] as CheesyTreasure)
            Command.Response.MARKERS-> contract.onMarkers(msg.data[Constants.ARGS_MARKER_ITEMS] as List<CheesyTreasure>)
            Command.Response.MARKERS_NEAR-> contract.onMarkersNear(msg.data[Constants.ARGS_MARKER_ITEMS] as List<CheesyTreasure>)
            else -> super.handleMessage(msg)
        }
    }
}