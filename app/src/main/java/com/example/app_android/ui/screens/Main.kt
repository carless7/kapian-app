package com.example.app_android.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.app_android.R
import com.example.app_android.ui.viewmodels.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mainScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = viewModel()
) {
    val context = LocalContext.current

    // Get values from the ViewModel
    val isUsingResource = sharedViewModel.isUsingResource.value
    val selectedImageUri = sharedViewModel.selectedImageUri.value
    val resourceDrawableId = sharedViewModel.resourceDrawableId.value

    // Check if NFC is available on this device
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val nfcAvailable = remember { nfcAdapter != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.config),
                            contentDescription = "Configuration"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display either the selected image URI or the default resource image
            Image(
                painter = if (isUsingResource)
                    painterResource(id = resourceDrawableId)
                else
                    rememberAsyncImagePainter(selectedImageUri),
                contentDescription = "Image",
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val imageUri = if (isUsingResource) {
                        Uri.parse("android.resource://${context.packageName}/$resourceDrawableId")
                    } else {
                        selectedImageUri
                    }

                    if (imageUri != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_STREAM, imageUri)
                            type = "image/*"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        val chooser = Intent.createChooser(shareIntent, "Share Image")
                        
                        try {
                            context.startActivity(chooser)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error sharing image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "No image available to share", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Share Image")
            }

            if (!nfcAvailable) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("NFC not available on this device",
                    color = androidx.compose.ui.graphics.Color.Red)
            }
        }
    }
}