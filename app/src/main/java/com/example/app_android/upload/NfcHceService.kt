package com.example.app_android.upload

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class NfcHceService : HostApduService() {
    companion object {
        private var urlToShare: String? = null
        private const val TAG = "NfcHceService"

        fun startService(context: android.content.Context, url: String) {
            urlToShare = url
            context.startService(Intent(context, NfcHceService::class.java))
        }

        fun stopService(context: android.content.Context) {
            urlToShare = null
            context.stopService(Intent(context, NfcHceService::class.java))
        }
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        return urlToShare?.toByteArray(Charsets.UTF_8) ?: byteArrayOf()
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }
}