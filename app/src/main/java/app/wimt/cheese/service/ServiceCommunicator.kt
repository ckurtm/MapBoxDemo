package app.wimt.cheese.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import app.wimt.cheese.Constants
import app.wimt.cheese.meta.CheesyTreasure
import app.wimt.cheese.service.io.Command
import app.wimt.cheese.service.io.ServiceResponseContract
import app.wimt.cheese.service.io.ServiceResponseHandler
import app.wimt.cheese.service.io.ServiceResponseManager
import timber.log.Timber

/**
 * This communicates with the service, from a client UI
 */
class ServiceCommunicator(
    private val ctx: Context, owner: LifecycleOwner,
    client: ServiceResponseContract
) : LifecycleObserver {



    init {
        //register as an observer of screen lifecycle events
        attachLifeCycle(owner)
    }

    var mocking = false
    var mockLocation: Location? = null

    private var responseManager: ServiceResponseManager? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            responseManager = ServiceResponseManager(
                ServiceResponseHandler(client)
            )
            responseManager?.service = Messenger(service)
            Timber.d("CONNECTED")
            responseManager?.message(Message.obtain(null, Command.Request.CONNECT))
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            responseManager?.service = null
            Timber.d("DISCONNECTED")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun bind() {
        //this makes sure the service is a hybrid of bound and started
        val intent = Intent(ctx.applicationContext, CheesyService::class.java)
        if(mocking) {
            intent.action = Constants.SERVICE_ACTION_MODE
            if(mockLocation != null){
                intent.putExtra(Constants.SERVICE_MOCK_LOCATION,mockLocation)
            }
        }
        ctx.startService(intent)

        //bind this client to the service
        Intent(ctx, CheesyService::class.java).also { intent ->
            ctx.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }


    /**
     * cleanup the service comms anf unbind when the client is exited
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun unbind() {
        responseManager?.message(Message.obtain(null, Command.Request.DISCONNECT))
        ctx.unbindService(connection)
        responseManager?.detach()
        responseManager = null
    }

    /**
     * tells the service that this client says goodbye
     */
    fun shutdown() {
        responseManager?.message(Message.obtain(null, Command.Request.SHUTDOWN))
    }

    /**
     * ask the service to start monitoring for location changes
     */
    fun startLocationMonitoring() {
        responseManager?.message(Message.obtain(null, Command.Request.MONITOR))
    }

    fun addCheeseMarker(item: CheesyTreasure) {
        val msg = Message.obtain(null, Command.Request.MARKER_ADD)
        msg.data = bundleOf(Constants.ARGS_MARKER to item)
        responseManager?.message(msg)
    }

    fun removeCheeseMarker(item: CheesyTreasure) {
        val msg = Message.obtain(null, Command.Request.MARKER_REMOVE)
        msg.data = bundleOf(Constants.ARGS_MARKER to item)
        responseManager?.message(msg)
    }


    fun clearNear() {
        val msg = Message.obtain(null, Command.Request.CLEAR_NEAR)
        responseManager?.message(msg)
    }

    /**
     * attach this to the ui life cycle.
     */
    private fun attachLifeCycle(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

}