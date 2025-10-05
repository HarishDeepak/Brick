package com.smartbrick.nfc

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityEvent
import androidx.preference.PreferenceManager
import com.smartbrick.nfc.utils.NotificationHelper
import kotlinx.coroutines.*

class AppBlockingService : AccessibilityService() {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var notificationHelper: NotificationHelper
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onServiceConnected() {
        super.onServiceConnected()

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        notificationHelper = NotificationHelper(this)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            packageNames = null // Monitor all packages
        }

        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            packageName?.let { checkAppAccess(it) }
        }
    }

    private fun checkAppAccess(packageName: String) {
        val isBricked = sharedPrefs.getBoolean("is_bricked", false)
        if (!isBricked) return

        val blockedApps = getBlockedApps()
        if (blockedApps.contains(packageName) && packageName != "com.smartbrick.nfc") {
            blockApp(packageName)
        }
    }

    private fun blockApp(packageName: String) {
        // Show blocking overlay
        val intent = Intent(this, BlockingOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("blocked_package", packageName)
        }
        startActivity(intent)

        // Return to home screen
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    private fun getBlockedApps(): Set<String> {
        return sharedPrefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
