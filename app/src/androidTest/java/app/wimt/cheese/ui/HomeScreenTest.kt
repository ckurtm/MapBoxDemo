package app.wimt.cheese.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.location.Location
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import app.wimt.cheese.Constants

import app.wimt.cheese.service.CheesyService
import com.example.tes.R
import junit.framework.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeScreenTest {

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Test
    fun map_IsDisplayed_whenAppLaunched() {
        ActivityScenario.launch(HomeScreen::class.java)
        onView(withId(R.id.map)).check(matches(isDisplayed()))
    }

    @Test
    fun close_IsDisplayed_whenAppLaunched() {
        ActivityScenario.launch(HomeScreen::class.java)
        onView(withId(R.id.close)).check(matches(isDisplayed()))
    }

    @Test
    fun appClosed_whenCloseIsClicked() {
        val activityScenario: ActivityScenario<*> = ActivityScenario.launch(HomeScreen::class.java)
        onView(withId(R.id.close)).perform(click())
        assertTrue(activityScenario.state.isAtLeast(Lifecycle.State.DESTROYED))
    }

    @Test
    fun addMarkersDialogIsShown_onLongPress() {
         ActivityScenario.launch(HomeScreen::class.java)
        SystemClock.sleep(1500)
        onView(withId(R.id.map)).perform(ViewActions.longClick())
        onView(withId(R.id.noteText)).check(matches(isDisplayed()))
    }

    @Test
    fun addedCheese_isShown_OnMap() {
        val activityScenario: ActivityScenario<*>  = ActivityScenario.launch(HomeScreen::class.java)
        SystemClock.sleep(1000)
        onView(withId(R.id.map))
            .perform(ViewActions.swipeUp())
        addMarker("cheese text marker 1")
        onView(withId(R.id.map)).perform(ViewActions.swipeDown())
        addMarker("other marker")
        SystemClock.sleep(2000)
        activityScenario.onActivity {
            val markers = (it as HomeScreen).markers
            assertTrue(markers.size > 2)
        }
    }

    @Test
    fun addedMarker_Survives_OrientationChanges() {
        val activityScenario: ActivityScenario<*>  = ActivityScenario.launch<HomeScreen>(mockedIntent())
        setLocation(createLocation(-26.0542, 27.9726))
        addMarker("marker 1")
        onView(withId(R.id.map)).perform(ViewActions.swipeRight())
        addMarker("marker 2")

        activityScenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        SystemClock.sleep(2000)
        activityScenario.onActivity {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        SystemClock.sleep(2000)
        activityScenario.onActivity {
            val markers = (it as HomeScreen).markers
            assertTrue(markers.size == 2)
        }
    }


    private fun addMarker(text: String){
        onView(withId(R.id.map)).perform(ViewActions.longClick())
        onView(withId(R.id.noteText)).check(matches(isDisplayed()))
            .perform(ViewActions.typeText(text), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.saveCheeseButton)).perform(click())
    }


    private fun mockedIntent():Intent {
        val intent = Intent(
            getInstrumentation().targetContext,
            HomeScreen::class.java
        )
        intent.putExtra(Constants.MOCK_MODE, true)
        return intent
    }

    private fun createLocation(latitude: Double, longitude: Double):Location{
        val location = Location("fused")
        location.latitude = latitude
        location.longitude = longitude
        location.accuracy = 3.0f
        return location
    }

    private fun setLocation(location: Location){
        val intent = Intent(
            getInstrumentation().targetContext,
            CheesyService::class.java
        )
        intent.action = Constants.SERVICE_ACTION_MOCK_LOCATION
        intent.putExtra(Constants.SERVICE_MOCK_LOCATION, location)
        getInstrumentation().targetContext.startService(intent)
    }

}