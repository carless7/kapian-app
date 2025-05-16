package com.example.app_android

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.*
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.app_android.navigation.AppNavigation
import com.example.app_android.viewmodel.SharedViewModel
import com.example.app_android.ui.theme.KapianTheme


class MainActivity : ComponentActivity() {

    private lateinit var sharedViewModel: SharedViewModel
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            KapianTheme { // Envoltem l'aplicació amb el tema personalitzat
                AppNavigation()
            }
        }
    }
    /*
    override fun onResume() {
        super.onResume()
        setupNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    private fun setupNfcForegroundDispatch() {
        if (nfcAdapter == null || !nfcAdapter!!.isEnabled) return

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        // Set up intent filters for better NFC handling
        val intentFilters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                addDataType("text/plain")
                addDataType("text/x-vcard")
            },
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )

        // Set up tech lists
        val techLists = arrayOf(arrayOf(Ndef::class.java.name))

        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {

            val tag: Tag? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java) // API 33+
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            tag?.let {
                val contactData = sharedViewModel.selectedCard
                if (contactData != null) {
                    val success = writeToNfc(it, contactData)
                    val message = if (success) "Contact shared successfully via NFC"
                    else "Failed to write to NFC tag"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No contact selected to share", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun writeToNfc(tag: Tag, contactData: String): Boolean {
        val ndef = Ndef.get(tag) ?: return false

        try {
            ndef.connect()

            if (!ndef.isWritable) {
                Toast.makeText(this, "NFC tag is read-only", Toast.LENGTH_SHORT).show()
                return false
            }

            val lines = contactData.split("\n")
            val name = lines[0]
            val phone = if (lines.size > 1) lines[1] else ""

            val vCardData = """
                BEGIN:VCARD
                VERSION:3.0
                N:${name};;;
                FN:${name}
                TEL;TYPE=CELL:${phone}
                END:VCARD
            """.trimIndent()

            val vCardRecord = NdefRecord.createMime(
                "text/x-vcard",
                vCardData.toByteArray()
            )

            val ndefMessage = NdefMessage(arrayOf(vCardRecord))

            if (ndef.maxSize < ndefMessage.byteArrayLength) {
                Toast.makeText(this, "NFC tag doesn't have enough space", Toast.LENGTH_SHORT).show()
                return false
            }

            ndef.writeNdefMessage(ndefMessage)
            ndef.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error writing to NFC tag: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }*/
}