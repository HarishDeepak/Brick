package com.smartbrick.nfc.models

data class NFCTag(
    val id: String,
    val name: String,
    val location: String,
    val color: String,
    val isRegistered: Boolean = false,
    val lastUsed: Long = 0
)
