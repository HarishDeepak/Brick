package com.smartbrick.nfc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class BlockingOverlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make this a fullscreen overlay
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Get the blocked package name
        val blockedPackage = intent.getStringExtra("blocked_package") ?: "Unknown App"

        // Show blocking message and redirect to home
        showBlockingMessage(blockedPackage)

        // Finish this activity immediately and go to home
        finish()
        goToHome()
    }

    private fun showBlockingMessage(packageName: String) {
        // Could show a toast or brief overlay here
        // For now, just silently block and redirect
    }

    private fun goToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(homeIntent)
    }
}
