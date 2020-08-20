package app.wimt.cheese

import androidx.multidex.MultiDexApplication
import timber.log.Timber

/**
 * this instance mostly just because of multidex & i like using timber for logging.
 */
class App: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}