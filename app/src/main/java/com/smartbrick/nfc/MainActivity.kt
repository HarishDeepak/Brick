package com.smartbrick.nfc

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.smartbrick.nfc.databinding.ActivityMainBinding
import com.smartbrick.nfc.fragments.*
import com.smartbrick.nfc.utils.PermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var permissionHelper: PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionHelper = PermissionHelper(this)

        // Initialize NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Setup bottom navigation
        setupBottomNavigation()

        // Check and request permissions
        checkPermissions()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_apps -> {
                    loadFragment(AppsFragment())
                    true
                }
                R.id.nav_nfc -> {
                    loadFragment(NFCFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun checkPermissions() {
        // Check NFC availability
        if (nfcAdapter == null) {
            // Device doesn't support NFC
            return
        }

        // Request accessibility service permission
        if (!permissionHelper.isAccessibilityServiceEnabled()) {
            permissionHelper.requestAccessibilityPermission()
        }

        // Request device admin permission
        if (!permissionHelper.isDeviceAdminEnabled()) {
            permissionHelper.requestDeviceAdminPermission()
        }

        // Request usage stats permission
        if (!permissionHelper.hasUsageStatsPermission()) {
            permissionHelper.requestUsageStatsPermission()
        }

        // Request system alert window permission
        if (!permissionHelper.canDrawOverlays()) {
            permissionHelper.requestOverlayPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableForegroundDispatch(this, null, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent?.action) {
            // Handle NFC tag
            NFCHandler.handleNFCIntent(intent, this)
        }
    }
}
