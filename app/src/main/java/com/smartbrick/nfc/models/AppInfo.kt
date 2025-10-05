package com.smartbrick.nfc.models

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    var isBlocked: Boolean = false,
    val category: String = "Other"
)
