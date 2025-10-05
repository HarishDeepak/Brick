package com.smartbrick.nfc

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.*
import java.util.concurrent.TimeUnit

class BrickService : Service() {

    private lateinit var sharedPrefs: SharedPreferences
    private var startTime: Long = 0

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "brick_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTime = System.currentTimeMillis()
        startForeground(NOTIFICATION_ID, createNotification())

        // Schedule automatic unbrick if enabled
        scheduleAutoUnbrick()

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Smart Brick Active")
            .setContentText("Your phone is bricked. Tap your NFC tag to unbrick.")
            .setSmallIcon(R.drawable.ic_brick)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Brick Service",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows when your phone is bricked"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleAutoUnbrick() {
        val autoUnbrickMinutes = sharedPrefs.getLong("auto_unbrick_minutes", 0)
        if (autoUnbrickMinutes > 0) {
            val workRequest = OneTimeWorkRequestBuilder<AutoUnbrickWorker>()
                .setInitialDelay(autoUnbrickMinutes, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(this).enqueue(workRequest)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

class AutoUnbrickWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        // Auto unbrick
        sharedPrefs.edit()
            .putBoolean("is_bricked", false)
            .putLong("last_state_change", System.currentTimeMillis())
            .apply()

        // Stop brick service
        val serviceIntent = Intent(applicationContext, BrickService::class.java)
        applicationContext.stopService(serviceIntent)

        return Result.success()
    }
}
