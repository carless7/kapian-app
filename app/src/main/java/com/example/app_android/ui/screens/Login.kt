// LoginScreen.kt
/*TODO:
->Intergrar Login funcional
	-> Registrar App Firebase
	-> Adaptar codi per register/login
	-> Aplicar Codi Google
	-> Testejar
 */
package com.example.app_android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_android.R
import com.example.app_android.ui.components.KapianButton

@Composable
fun LoginScreen(navController: NavController) {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        LoginContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            user = user,
            onUserChange = { user = it },
            password = password,
            onPasswordChange = { password = it },
            errorMessage = errorMessage,
            onLogin = {
                if (user.isNotEmpty() && password.isNotEmpty()) {
                    navController.navigate("main")
                } else {
                    errorMessage = "Enter your credentials"
                }
            },
            onRegister = { navController.navigate("register") }
        )
    }
}

@Composable
private fun LoginContent(
    modifier: Modifier = Modifier,
    user: String,
    onUserChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String,
    onLogin: () -> Unit,
    onRegister: () -> Unit
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
            user = user,
            onUserChange = onUserChange,
            password = password,
            onPasswordChange = onPasswordChange,
            errorMessage = errorMessage
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginButtons(
            onLogin = onLogin,
            onRegister = onRegister
        )
    }
}

@Composable
private fun LoginForm(
    user: String,
    onUserChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String
) {
    OutlinedTextField(
        value = user,
        onValueChange = onUserChange,
        label = { Text(stringResource(R.string.user)) },
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
    onRegister: () -> Unit
) {
    KapianButton(
        text = stringResource(R.string.login),
        onClick = onLogin,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    KapianButton(
        text = stringResource(R.string.register),
        onClick = onRegister,
        modifier = Modifier.fillMaxWidth()
    )
}