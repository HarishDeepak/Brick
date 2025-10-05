package com.smartbrick.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // Restart brick service if phone was bricked before reboot
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                val wasBricked = sharedPrefs.getBoolean("is_bricked", false)

                if (wasBricked) {
                    val serviceIntent = Intent(context, BrickService::class.java)
                    context.startForegroundService(serviceIntent)
                }
            }
        }
    }
}
