package app.wimt.cheese.service.io

import android.os.Handler
import android.os.Message
import app.wimt.cheese.Constants
import app.wimt.cheese.meta.CheesyTreasure
import timber.log.Timber

/**
 * Handles all incoming comms to the service i.e. client -> service requests, using {@link ServiceRequest}
 */
class ServiceRequestHandler (private val contract: ServiceRequestContract) : Handler() {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            Command.Request.CONNECT -> {
                Timber.d("CONNECT")
                if (msg.replyTo != null) {
                    contract.connect(msg.obj as Int,msg.replyTo)
                }
            }
            Command.Request.DISCONNECT -> {
                Timber.d("DISCONNECT")
                if (msg.replyTo != null) {
                    contract.disconnect(msg.obj as Int)
                }
            }
            Command.Request.SHUTDOWN -> {
                Timber.d("STOP_UPDATES")
                contract.detach()
            }
            Command.Request.MONITOR -> {
                Timber.d("MONITOR")
                contract.monitor()
            }
            Command.Request.MARKER_ADD -> {
                Timber.d("MARKER_ADD")
                val item = msg.data.get(Constants.ARGS_MARKER) as CheesyTreasure
                contract.addMarker(item)
            }
            Command.Request.MARKER_REMOVE -> {
                Timber.d("MARKER_REMOVE")
                val item = msg.data.get(Constants.ARGS_MARKER) as CheesyTreasure
                contract.removeMarker(item)
            }
            Command.Request.CLEAR_NEAR -> {
                Timber.d("CLEAR_NEAR")
                contract.clearNear()
            }
            else -> super.handleMessage(msg)
        }
    }

}