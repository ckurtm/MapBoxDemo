package app.wimt.cheese.service

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class CheesyServiceTest {
    @get:Rule
    val serviceRule = ServiceTestRule()

    @Test
    @Throws(TimeoutException::class)
    fun canBindToService() {
        val serviceIntent = Intent(ApplicationProvider.getApplicationContext<Context>(),
            CheesyService::class.java
        )
        val binder = serviceRule.bindService(serviceIntent)
        assertThat(binder, CoreMatchers.`is`(notNullValue()))
    }


}