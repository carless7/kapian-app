package com.example.app_android.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.app_android.R
import com.example.app_android.notifications.FcmTokenManager
import com.example.app_android.upload.NfcHceService
import com.example.app_android.viewmodel.UploadState
import com.example.app_android.viewmodel.UploadViewmodel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: UploadViewmodel
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadImage(it)
        } ?: run {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val downloadUrl by viewModel.downloadUrl.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isNfcActive by remember { mutableStateOf(false) }

    fun toggleNfcSharing(url: String) {
        if (isNfcActive) {
            NfcHceService.stopService(context)
            isNfcActive = false
            Toast.makeText(context, "NFC sharing stopped", Toast.LENGTH_SHORT).show()
        } else {
            NfcHceService.startService(context, url)
            isNfcActive = true
            Toast.makeText(context, "NFC sharing activated", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchStoredImageUrl()
        FcmTokenManager.refreshAndSaveToken()
    }

    DisposableEffect(Unit) {
        onDispose {
            NfcHceService.stopService(context)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.height(100.dp),
                title = {},
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.config),
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uploadState == UploadState.SUCCESS && downloadUrl != null -> {
                        GlideImage(
                            model = downloadUrl,
                            contentDescription = "Uploaded image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    uploadState == UploadState.LOADING -> {
                        CircularProgressIndicator()
                    }
                    else -> {
                        Icon(
                            painter = painterResource(id = R.drawable.text_placeholder),
                            contentDescription = "Image placeholder",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (uploadState) {
                UploadState.LOADING -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Uploading: ${uploadProgress.toInt()}%")
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                UploadState.SUCCESS -> {
                    downloadUrl?.let { url ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Card Loaded!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                UploadState.ERROR -> {
                    errorMessage?.let { message ->
                        Text(
                            "Error: $message",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    Text("Select an image to upload")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    enabled = uploadState != UploadState.LOADING
                ) {
                    Text("Choose Image")
                }

                Button(
                    onClick = {
                        downloadUrl?.let { url ->
                            toggleNfcSharing(url)
                        } ?: run {
                            Toast.makeText(context, "No image to share", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = uploadState == UploadState.SUCCESS,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNfcActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo_kapian),
                            contentDescription = "NFC",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isNfcActive) "Sharing..." else "Share via NFC")
                    }
                }
            }

            if (uploadState == UploadState.SUCCESS && downloadUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text("Preview card")
                }
            }

            if (isNfcActive) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Hold device back-to-back with another NFC device",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}