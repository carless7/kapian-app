package com.example.app_android.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_android.R
import com.example.app_android.helper.GoogleAuthHelper
import com.example.app_android.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleAuthHelper = remember { GoogleAuthHelper(context) }

    LaunchedEffect(Unit) {
        try {
            googleAuthHelper.setupGoogleSignIn()
        } catch (e: Exception) {
            errorMessage = "Failed to initialize Google Sign-In"
            Log.e("GoogleSignIn", "Initialization error", e)
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)

                coroutineScope.launch {
                    try {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    navController.navigate("main") { popUpTo(0) }
                                } else {
                                    errorMessage = authTask.exception?.message
                                        ?: "Google authentication failed"
                                }
                            }
                    } catch (e: Exception) {
                        errorMessage = "Failed to authenticate: ${e.localizedMessage}"
                    }
                }
            } else {
                errorMessage = "Google sign-in was cancelled"
            }
        } catch (e: Exception) {
            errorMessage = when (e) {
                is ApiException -> "Google sign-in error (${e.statusCode})"
                else -> "Unexpected error: ${e.localizedMessage}"
            }
        }
    }

    val loginState by viewModel.loginState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is LoginViewModel.LoginState.Error -> {
                errorMessage = (loginState as LoginViewModel.LoginState.Error).message
                    ?: context.getString(R.string.error_unknown)
            }
            else -> {}
        }
    }

    @Composable
    fun LoginContent(
        modifier: Modifier = Modifier,
        email: String,
        onEmailChange: (String) -> Unit,
        password: String,
        onPasswordChange: (String) -> Unit,
        errorMessage: String,
        onLogin: () -> Unit,
        onGoogleLogin: () -> Unit,
        onRegister: () -> Unit,
        isLoading: Boolean
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_name),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.login),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.login))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onGoogleLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.login_with_google))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(R.string.dont_have_account),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        LoginContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            errorMessage = errorMessage,
            onLogin = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = context.getString(R.string.error_empty_fields)
                } else {
                    viewModel.login(email, password)
                }
            },
            onGoogleLogin = { googleAuthHelper.signIn(googleSignInLauncher) },
            onRegister = { navController.navigate("register") },
            isLoading = loginState is LoginViewModel.LoginState.Loading
        )
    }
}