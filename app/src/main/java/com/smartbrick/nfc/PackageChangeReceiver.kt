package com.smartbrick.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager

class PackageChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return

        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                // New app installed - add to available apps list
                handleNewAppInstalled(context, packageName)
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                // App uninstalled - remove from blocked apps list
                handleAppUninstalled(context, packageName)
            }
        }
    }

    private fun handleNewAppInstalled(context: Context, packageName: String) {
        try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)

            // Only handle user apps, not system apps
            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                // Optionally notify user about new app
                // Could add to a "new apps" list for review
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // App not found
        }
    }

    private fun handleAppUninstalled(context: Context, packageName: String) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val blockedApps = sharedPrefs.getStringSet("blocked_apps", mutableSetOf())?.toMutableSet()

        if (blockedApps?.remove(packageName) == true) {
            sharedPrefs.edit()
                .putStringSet("blocked_apps", blockedApps)
                .apply()
        }
    }
}
