package app.wimt.cheese.meta

import android.os.Parcel
import android.os.Parcelable
import com.mapbox.mapboxsdk.geometry.LatLng

/**
 * Cheezy Treasures are such a delight
 */
class CheesyTreasure(val location: LatLng?, val note: String?) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(LatLng::class.java.classLoader),
        parcel.readString()
    )

    // For simplicities sake, we will just assume all cheesy treasures have unique notes
    fun equals(other: CheesyTreasure): Boolean {
        return note == other.note
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(location, flags)
        parcel.writeString(note)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CheesyTreasure> {
        override fun createFromParcel(parcel: Parcel): CheesyTreasure {
            return CheesyTreasure(parcel)
        }

        override fun newArray(size: Int): Array<CheesyTreasure?> {
            return arrayOfNulls(size)
        }
    }
}