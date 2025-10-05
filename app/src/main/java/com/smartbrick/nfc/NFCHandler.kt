package com.smartbrick.nfc

import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*

object NFCHandler {

    fun handleNFCIntent(intent: Intent, context: Context) {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        tag?.let { 
            CoroutineScope(Dispatchers.IO).launch {
                readNFCTag(it, context)
            }
        }
    }

    private suspend fun readNFCTag(tag: Tag, context: Context) {
        try {
            val ndef = Ndef.get(tag)
            ndef?.let {
                it.connect()
                val ndefMessage = it.ndefMessage
                if (ndefMessage != null) {
                    val record = ndefMessage.records[0]
                    val payload = String(record.payload)

                    if (payload.contains("SMARTBRICK")) {
                        toggleBrickState(context)
                    }
                }
                it.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun writeNFCTag(tag: Tag, tagId: String): Boolean {
        return try {
            val message = "SMARTBRICK_TAG_$tagId"
            val ndefRecord = NdefRecord.createTextRecord("en", message)
            val ndefMessage = NdefMessage(arrayOf(ndefRecord))

            val ndef = Ndef.get(tag)
            ndef?.let {
                it.connect()
                it.writeNdefMessage(ndefMessage)
                it.close()
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun toggleBrickState(context: Context) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val currentState = sharedPrefs.getBoolean("is_bricked", false)

        sharedPrefs.edit()
            .putBoolean("is_bricked", !currentState)
            .putLong("last_state_change", System.currentTimeMillis())
            .apply()

        // Start/stop brick service
        val serviceIntent = Intent(context, BrickService::class.java)
        if (!currentState) {
            context.startService(serviceIntent)
        } else {
            context.stopService(serviceIntent)
        }
    }
}
