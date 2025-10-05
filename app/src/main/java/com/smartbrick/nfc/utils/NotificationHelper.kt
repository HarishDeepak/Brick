package com.smartbrick.nfc.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.smartbrick.nfc.MainActivity
import com.smartbrick.nfc.R

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "smart_brick_channel"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Smart Brick Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for Smart Brick app blocking status"
        }

        notificationManager.createNotificationChannel(channel)
    }

    fun showBrickedNotification() {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Smart Brick Active")
            .setContentText("Your phone is bricked. Tap your NFC tag to unbrick.")
            .setSmallIcon(R.drawable.ic_brick)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showUnbrickedNotification() {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Smart Brick Inactive")
            .setContentText("Your phone is unbricked. All apps are now accessible.")
            .setSmallIcon(R.drawable.ic_brick)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotifications() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
