package app.wimt.cheese.service.io

import android.os.Handler
import android.os.Message
import android.os.Messenger

/**
 * Handles comms between service and any connected clients - within the service
 */
class ServiceRequestManager(private val handler: Handler) {

    //list of all connected/bound service clients
    private var clients: MutableMap<Int, Messenger>? = mutableMapOf()

    //used for communication between services and bound clients
    val messenger = Messenger(handler)

    fun register(id: Int, client: Messenger) {
        clients?.set(id, client)
    }

    fun unregister(id: Int) {
        clients?.remove(id)
    }

    fun detach() {
        handler.removeCallbacksAndMessages(null)
        clients?.clear()
    }

    fun message(msg: Message) {
        clients?.keys?.forEach { key ->
            sendMessage(clients?.get(key)!!, msg)
        }
    }

    fun hasClients(): Boolean {
        return !clients.isNullOrEmpty()
    }

    private fun sendMessage(sender: Messenger, msg: Message) {
        try {
            sender.send(msg)
        } catch (e: Throwable) {
            //TODO ..this should mostly not happen??
        }
    }


}