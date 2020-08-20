package app.wimt.cheese.service.io

import android.os.Messenger
import app.wimt.cheese.meta.CheesyTreasure

/**
 * This is the contract between the service and bound client on what the service can be asked to todo
 */
interface ServiceRequestContract {
    fun connect(id:Int,client:Messenger)
    fun disconnect(id:Int)
    fun detach()
    fun monitor()
    fun addMarker(item: CheesyTreasure)
    fun removeMarker(item: CheesyTreasure)
    fun clearNear()
}