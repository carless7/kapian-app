package com.example.app_android.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.app_android.R
import com.example.app_android.ui.viewmodels.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settingsScreen(navController: NavController, sharedViewModel: SharedViewModel = viewModel()) {
    var tempSelectedImageUri by remember { mutableStateOf<Uri?>(sharedViewModel.selectedImageUri.value) }
    val isUsingResource = sharedViewModel.isUsingResource.value
    val resourceDrawableId = sharedViewModel.resourceDrawableId.value

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            tempSelectedImageUri = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuration") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back"
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
            if (tempSelectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(tempSelectedImageUri),
                    contentDescription = "Image selected",
                    modifier = Modifier.size(150.dp)
                )
            } else if (isUsingResource) {
                Image(
                    painter = painterResource(id = resourceDrawableId),
                    contentDescription = "Default image",
                    modifier = Modifier.size(150.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Change card")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                tempSelectedImageUri = null
                sharedViewModel.resetToDefaultImage()
            }) {
                Text("Reset to Default")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                tempSelectedImageUri?.let {
                    sharedViewModel.setSelectedImage(it)
                }
                navController.popBackStack()
            }) {
                Text("Apply")
            }
        }
    }
}