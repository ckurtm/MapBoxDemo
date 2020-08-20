package app.wimt.cheese.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.wimt.cheese.ui.HomeScreen
import com.example.tes.BuildConfig
import com.example.tes.R

/**
 * abstracts out all notification details
 */
class ServiceNotification(private val context: Context) {
    private val channelId = "${BuildConfig.APPLICATION_ID}_channel"
    private val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    val notificationId = 1299

    init {
        setupChannel()
    }

    private fun setupChannel() {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            manager.createNotificationChannel(channel)
        }
    }

    fun message(title: String, clazz: Class<*>) {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, clazz), 0
        )
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.white_outline_logo)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
        manager.notify(notificationId, builder.build())
    }


    fun notification():Notification {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, HomeScreen::class.java), 0
        )
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.white_outline_logo)
            .setContentTitle(context.getString(R.string.searching))
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    fun message(title: String, intent:Intent) {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
           intent, 0
        )
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.white_outline_logo)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
        manager.notify(notificationId, builder.build())
    }

    fun detach() {
        manager.cancel(notificationId)
    }

}