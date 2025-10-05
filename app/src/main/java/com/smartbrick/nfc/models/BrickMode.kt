package com.smartbrick.nfc.models

data class BrickMode(
    val name: String,
    val description: String,
    val icon: String,
    val blockedApps: List<String>,
    val isCustom: Boolean = false
)
