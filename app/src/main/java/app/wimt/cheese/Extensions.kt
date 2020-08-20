package app.wimt.cheese

import app.wimt.cheese.meta.CheesyTreasure
import com.google.android.gms.location.Geofence

//convinience extension to find a matching Cheese from a given geofence
fun Geofence.toCheese(from: List<CheesyTreasure>): CheesyTreasure? {
    for (item in from) {
        if (item.note.equals(this.requestId, true))
            return item
    }
    return null
}