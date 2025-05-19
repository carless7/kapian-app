package com.example.app_android.nfc_utils

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import java.nio.charset.Charset

fun createNdefMessage(content: String): NdefMessage {
    val mimeRecord = NdefRecord.createMime(
        "application/com.example.app_android",
        content.toByteArray(Charset.forName("UTF-8"))
    )
    val uriRecord = NdefRecord.createUri(content)

    val langBytes = "en".toByteArray(Charset.forName("US-ASCII"))
    val textBytes = content.toByteArray(Charset.forName("UTF-8"))

    val textLength = langBytes.size
    val payloadLength = textLength + textBytes.size
    val payload = ByteArray(1 + payloadLength)

    payload[0] = textLength.toByte()

    System.arraycopy(langBytes, 0, payload, 1, langBytes.size)
    System.arraycopy(textBytes, 0, payload, 1 + textLength, textBytes.size)

    val textRecord = NdefRecord(
        NdefRecord.TNF_WELL_KNOWN,
        NdefRecord.RTD_TEXT,
        ByteArray(0),
        payload
    )

    return NdefMessage(arrayOf(uriRecord, mimeRecord, textRecord))
}

fun setHceSharedContent(content: String) {
    HceService.setSharedContent(content)
}