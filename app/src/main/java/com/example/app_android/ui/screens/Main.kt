package com.example.app_android.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app_android.R
import com.example.app_android.viewmodel.SharedViewModel

@Composable
fun MainScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedContact = sharedViewModel.selectedContact
    var hasContactPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
    ) }

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

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactPermission = isGranted
        if (isGranted) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            contactPickerLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Contact permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
    val nfcAvailable = nfcAdapter != null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFFFF)
    ) {
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
                Text(text = stringResource(R.string.seleced_contaxt), style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(8.dp))

                SelectionContainer {
                    Text(
                        text = selectedContact ?: stringResource(R.string.no_cred),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (hasContactPermission) {
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                    contactPickerLauncher.launch(intent)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF165FBD))
        ) {
            Text(stringResource(R.string.select_contact))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!nfcAvailable) {
                    Toast.makeText(context, context.getString(R.string.no_NFC), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (!nfcAdapter!!.isEnabled) {
                    Toast.makeText(context, context.getString(R.string.settings_NFC), Toast.LENGTH_SHORT).show()
                    return@Button
                }

                Toast.makeText(context, context.getString(R.string.hold_NFC), Toast.LENGTH_SHORT).show()
            },
            enabled = selectedContact != null && nfcAvailable,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF165FBD))
        ) {
            Text(stringResource(R.string.share_NFC))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("settings") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF165FBD))
        ) {
            Text(stringResource(R.string.go_set))
        }
    }
    }
}
