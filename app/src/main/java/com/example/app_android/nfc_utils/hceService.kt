package com.example.app_android.nfc_utils

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.nio.charset.Charset

class HceService : HostApduService() {
    companion object {
        private const val TAG = "HceService"
        private val SELECT_APDU_HEADER = byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte())
        private val APP_AID = byteArrayOf(0xF0.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte())
        private val SW_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val SW_FAILURE = byteArrayOf(0x6A.toByte(), 0x82.toByte())
        private var sharedContent: String = ""

        fun setSharedContent(content: String) {
            sharedContent = content
            Log.d(TAG, "Content set for HCE sharing: $sharedContent")
        }
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Log.d(TAG, "Received APDU: ${commandApdu.toHex()}")

        if (isSelectAidApdu(commandApdu)) {
            Log.d(TAG, "SELECT AID command received")
            return respondToSelectAid()
        }

        Log.d(TAG, "Data request command received")
        return respondWithSharedContent()
    }

    private fun isSelectAidApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 4 + APP_AID.size + 1 &&
                apdu[0] == SELECT_APDU_HEADER[0] &&
                apdu[1] == SELECT_APDU_HEADER[1] &&
                apdu[2] == SELECT_APDU_HEADER[2] &&
                apdu[3] == SELECT_APDU_HEADER[3] &&
                apdu[4] == APP_AID.size.toByte() &&
                apdu.sliceArray(5 until 5 + APP_AID.size).contentEquals(APP_AID)
    }

    private fun respondToSelectAid(): ByteArray {
        Log.d(TAG, "Responding to SELECT AID command")
        return SW_SUCCESS
    }

    private fun respondWithSharedContent(): ByteArray {
        if (sharedContent.isEmpty()) {
            Log.d(TAG, "No content available to share")
            return SW_FAILURE
        }

        val contentBytes = sharedContent.toByteArray(Charset.forName("UTF-8"))
        val response = ByteArray(contentBytes.size + SW_SUCCESS.size)

        System.arraycopy(contentBytes, 0, response, 0, contentBytes.size)
        System.arraycopy(SW_SUCCESS, 0, response, contentBytes.size, SW_SUCCESS.size)

        Log.d(TAG, "Responding with shared content: $sharedContent")
        return response
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02X".format(it) }
    }
}