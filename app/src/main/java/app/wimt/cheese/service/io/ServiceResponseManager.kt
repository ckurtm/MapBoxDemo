package app.wimt.cheese.service.io

import android.os.Handler
import android.os.Message
import android.os.Messenger

/**
 * Handles comms between service and any connected clients - within the client
 */
class ServiceResponseManager(private val handler: Handler) {
    private var bound = false
    private val clientID = 7685
    var client: Messenger? = Messenger(handler)

    var service: Messenger? = null
     set(value) {
         field = value
         bound = value != null
     }

    fun message(msg: Message) {
        msg.obj = clientID
        msg.replyTo = client
        service?.send(msg)
    }

    fun detach(){
        handler.removeCallbacksAndMessages(null)
        client = null
        service = null
        bound = false
    }
}