package com.smartbrick.nfc.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.smartbrick.nfc.BrickDeviceAdminReceiver

class PermissionHelper(private val activity: Activity) {

    private val devicePolicyManager = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val componentName = ComponentName(activity, BrickDeviceAdminReceiver::class.java)

    fun isAccessibilityServiceEnabled(): Boolean {
        val settingValue = Settings.Secure.getString(
            activity.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return settingValue?.contains("com.smartbrick.nfc/.AppBlockingService") == true
    }

    fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        activity.startActivity(intent)
    }

    fun isDeviceAdminEnabled(): Boolean {
        return devicePolicyManager.isAdminActive(componentName)
    }

    fun requestDeviceAdminPermission() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
                "Smart Brick needs device admin access to prevent unauthorized uninstallation.")
        }
        activity.startActivityForResult(intent, REQUEST_CODE_DEVICE_ADMIN)
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = activity.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            activity.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        activity.startActivity(intent)
    }

    fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(activity)
        } else {
            true
        }
    }

    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivityForResult(intent, REQUEST_CODE_OVERLAY)
        }
    }

    companion object {
        const val REQUEST_CODE_DEVICE_ADMIN = 1001
        const val REQUEST_CODE_OVERLAY = 1002
    }
}
