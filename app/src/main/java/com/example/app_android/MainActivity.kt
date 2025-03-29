package com.example.app_android

import android.app.PendingIntent
import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.app_android.navigation.AppNavigation
import com.example.app_android.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {

    private lateinit var sharedViewModel: SharedViewModel
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            AppNavigation()
        }
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED || intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag: Tag? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java) // API 33+
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            tag?.let {
                sharedViewModel.selectedContact?.let { contact ->
                    writeToNfc(it, contact)
                }
            }
        }
    }

    private fun writeToNfc(tag: Tag, data: String) {
        val ndef = Ndef.get(tag)
        ndef?.let {
            try {
                it.connect()
                val ndefRecord = NdefRecord.createMime("text/plain", data.toByteArray())
                val ndefMessage = NdefMessage(arrayOf(ndefRecord))
                it.writeNdefMessage(ndefMessage)
                it.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
