package com.example.app_android.ui.screens

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app_android.viewmodel.SharedViewModel

@Composable
fun MainScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedContact = sharedViewModel.selectedContact

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { contactUri ->
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Email.ADDRESS
                )

                context.contentResolver.query(
                    contactUri, projection, null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                        val name = if (nameIndex != -1) cursor.getString(nameIndex) else "Unknown"
                        val number = if (numberIndex != -1) cursor.getString(numberIndex) else ""

                        val contactInfo = "$name\n$number"
                        sharedViewModel.setContact(contactInfo)
                    }
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Selected Contact:", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(8.dp))

                SelectionContainer {
                    Text(
                        text = selectedContact ?: "No contact selected",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                contactPickerLauncher.launch(intent)
            }
        ) {
            Text("Select Contact")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val nfcTag: Tag? = null
                val success = writeNfcTag(nfcTag, selectedContact ?: "")
                Toast.makeText(
                    context,
                    if (success) "NFC tag written!" else "Failed to write NFC tag",
                    Toast.LENGTH_SHORT
                ).show()
            },
            enabled = selectedContact != null
        ) {
            Text("Share via NFC")
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("settings") }) {
            Text("Go to Settings")
        }
    }
} 

private fun createTextRecord(text: String): NdefRecord {
    val languageCode = "en".toByteArray()
    val textBytes = text.toByteArray()
    val payload = ByteArray(1 + languageCode.size + textBytes.size)

    payload[0] = languageCode.size.toByte()
    System.arraycopy(languageCode, 0, payload, 1, languageCode.size)
    System.arraycopy(textBytes, 0, payload, 1 + languageCode.size, textBytes.size)

    return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
}

private fun writeNfcTag(tag: Tag?, data: String): Boolean {
    if (tag == null) return false
    try {
        val ndef = Ndef.get(tag) ?: return false
        ndef.connect()
        val message = NdefMessage(arrayOf(createTextRecord(data)))
        if (ndef.isWritable) {
            ndef.writeNdefMessage(message)
            ndef.close()
            return true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

