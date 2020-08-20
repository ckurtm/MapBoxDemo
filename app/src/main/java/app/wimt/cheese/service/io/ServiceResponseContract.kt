package app.wimt.cheese.service.io

import android.location.Location
import app.wimt.cheese.meta.CheesyTreasure

/**
 * This is the contract between the service and bound client on what the client can expect from the service
 */
interface ServiceResponseContract {
    fun onLocationChange(location: Location)
    fun onLocationError(e: Exception)
    fun onMarkerAdded(marker: CheesyTreasure)
    fun onMarkerRemoved(marker: CheesyTreasure)
    fun onMarkers(markers: List<CheesyTreasure>)
    fun onMarkersNear(entries:List<CheesyTreasure>)
}