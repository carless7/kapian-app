package com.example.app_android

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.app_android.navigation.AppNavigation
import com.example.app_android.nfc_utils.createNdefMessage
import com.example.app_android.nfc_utils.setHceSharedContent
import com.example.app_android.ui.theme.KapianTheme
import com.example.app_android.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {
    private lateinit var sharedViewModel: SharedViewModel
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        initializeNfc()

        setContent {
            KapianTheme {
                AppNavigation()
            }
        }
    }

    private fun initializeNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Log.d(TAG, "NFC is not available on this device")
            Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_LONG).show()
        } else if (!nfcAdapter!!.isEnabled) {
            Log.d(TAG, "NFC is not enabled")
            Toast.makeText(this, "Please enable NFC in settings", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        } else {
            Log.d(TAG, "NFC is available and enabled")
        }

        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
    }

    fun setHceContent(content: String) {
        if (nfcAdapter?.isEnabled == true) {
            try {
                // Pass the content to our HCE service
                setHceSharedContent(content)
                Log.d(TAG, "HCE content set successfully: $content")
                Toast.makeText(this, "Ready to share via NFC", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error setting HCE content", e)
                Toast.makeText(this, "NFC Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e(TAG, "NFC is not enabled")
            Toast.makeText(this, "Please enable NFC to share content", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "Intent received: ${intent.action}")

        // Handle incoming NFC intents if needed
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            // Process incoming NDEF messages if needed
            // This would be for receiving data, not for HCE
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Enabling foreground dispatch")
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Disabling foreground dispatch")
        nfcAdapter?.disableForegroundDispatch(this)
    }
}