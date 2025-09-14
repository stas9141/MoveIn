package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.movein.ui.components.ErrorDisplay
import com.example.movein.utils.ErrorHandler

@Composable
fun SimpleLoginScreen(
    onBackClick: () -> Unit,
    onSignInClick: (email: String, password: String) -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    googleSignInError: String? = null,
    onDismissError: (() -> Unit)? = null,
    onDismissGoogleError: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Welcome Text
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Sign in to sync your data across devices",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Error Messages
        error?.let { errorMessage ->
            val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(Exception(errorMessage))
            val errorType = ErrorHandler.getErrorType(Exception(errorMessage))
            
            ErrorDisplay(
                error = userFriendlyError,
                errorType = errorType,
                onDismiss = onDismissError,
                showRecoverySuggestion = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Google Sign-In Error Message
        googleSignInError?.let { errorMessage ->
            val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(Exception(errorMessage))
            val errorType = ErrorHandler.getErrorType(Exception(errorMessage))
            
            ErrorDisplay(
                error = userFriendlyError,
                errorType = errorType,
                onDismiss = onDismissGoogleError,
                showRecoverySuggestion = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password")
            },
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign In Button
        Button(
            onClick = {
                // Clear any existing errors when attempting to sign in
                onDismissError?.invoke()
                onDismissGoogleError?.invoke()
                scope.launch {
                    onSignInClick(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Sign In")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Google Sign In Button
        OutlinedButton(
            onClick = {
                // Clear any existing errors when attempting to sign in
                onDismissError?.invoke()
                onDismissGoogleError?.invoke()
                scope.launch {
                    onGoogleSignInClick()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Sign in with Google")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Sign Up Link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onSignUpClick) {
                Text("Sign Up")
            }
        }
    }
}
