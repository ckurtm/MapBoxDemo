package app.wimt.cheese.geo

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * remeber current location on screen rotations..not really necessary as service remembers all of this.
 */
class CurrentLocationViewModel : ViewModel() {
    var location:MutableLiveData<Location> = MutableLiveData()
}