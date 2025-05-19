package com.example.app_android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.ComponentActivity
import androidx.navigation.NavController
import com.example.app_android.R
import com.example.app_android.auth.AuthState
import com.example.app_android.auth.AuthenticationManager
import com.example.app_android.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    authManager: AuthenticationManager
) {
    val viewModel: RegisterViewModel = remember { RegisterViewModel(authManager) }
    val registerState by viewModel.registerState.collectAsState()
    val activity = LocalContext.current as ComponentActivity

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(registerState) {
        if (registerState is AuthState.Success) {
            navController.navigate("login"){
                popUpTo("register") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))



        Button(
            onClick = {
                if (password == confirmPassword) {
                    viewModel.register(email, password)
                } else {
                    viewModel.clearStates()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = registerState !is AuthState.Loading
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.signInWithGoogle(activity)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = registerState !is AuthState.Loading,
            colors = ButtonDefaults.run {
                buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign up with Google")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ClickableText(
            text = buildAnnotatedString {
                append("Already have an account? ")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("Login")
                }
            },
            onClick = { navController.navigate("login") }
        )
        when (registerState) {
            is AuthState.Error -> {
                Text(
                    (registerState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            is AuthState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
            }
            is AuthState.Success -> {
                navController.navigate("main") {
                    popUpTo("register") { inclusive = true }
                }
            }
            else -> {}
        }
    }
}