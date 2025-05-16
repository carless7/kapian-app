package com.example.app_android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app_android.R
import com.example.app_android.ui.components.KapianButton
import com.example.app_android.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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
            }
            else -> {}
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
            onLogin = { viewModel.login(email, password) },
            onRegister = { navController.navigate("register") },
            isLoading = loginState is LoginViewModel.LoginState.Loading
        )
    }
}

@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String,
    onLogin: () -> Unit,
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
            contentDescription = "Kapian Logo",
            modifier = Modifier.size(300.dp)
        )

        Text(
            text = stringResource(R.string.login),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginForm(
            email = email,
            onEmailChange = onEmailChange,
            password = password,
            onPasswordChange = onPasswordChange,
            errorMessage = errorMessage
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginButtons(
            onLogin = onLogin,
            onRegister = onRegister,
            isLoading = isLoading
        )
    }
}

@Composable
private fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(stringResource(R.string.password)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    )

    if (errorMessage.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun LoginButtons(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    isLoading: Boolean
) {
    KapianButton(
        text = stringResource(R.string.login),
        onClick = onLogin,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    )

    Spacer(modifier = Modifier.height(8.dp))

    KapianButton(
        text = stringResource(R.string.register),
        onClick = onRegister,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    )

    if (isLoading) {
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}