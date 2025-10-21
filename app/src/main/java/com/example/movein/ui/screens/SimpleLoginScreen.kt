package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    onForgotPasswordClick: () -> Unit,
    onBiometricSignInClick: () -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null,
    googleSignInError: String? = null,
    biometricError: String? = null,
    onDismissError: (() -> Unit)? = null,
    onDismissGoogleError: (() -> Unit)? = null,
    onDismissBiometricError: (() -> Unit)? = null,
    isBiometricAvailable: Boolean = false,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Focus requesters for keyboard management
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    
    // Auto-focus email field when screen loads
    LaunchedEffect(Unit) {
        emailFocusRequester.requestFocus()
    }
    
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sign Up Link - Moved to top for better UX
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Error Messages
        error?.let { errorMessage ->
            // Debug logging
            println("SimpleLoginScreen: RAW ERROR MESSAGE: '$errorMessage'")
            println("SimpleLoginScreen: Error type: ${errorMessage::class.simpleName}")
            println("SimpleLoginScreen: Error length: ${errorMessage.length}")
            println("SimpleLoginScreen: Error contains 'wrong-password': ${errorMessage.contains("wrong-password", ignoreCase = true)}")
            println("SimpleLoginScreen: Error contains 'user-not-found': ${errorMessage.contains("user-not-found", ignoreCase = true)}")
            println("SimpleLoginScreen: Error contains 'invalid-email': ${errorMessage.contains("invalid-email", ignoreCase = true)}")
            
            val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(Exception(errorMessage))
            val errorType = ErrorHandler.getErrorType(Exception(errorMessage))
            
            println("SimpleLoginScreen: User-friendly error: $userFriendlyError")
            println("SimpleLoginScreen: Error type: $errorType")
            
            // TEMPORARY DEBUG: Show what ErrorHandler is returning
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "DEBUG - ErrorHandler Result:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Raw: '$errorMessage'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Processed: '$userFriendlyError'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Test pattern matching directly
                    val patternMatch = errorMessage.contains("The supplied auth credentials is incorrect, malformed or has expired", ignoreCase = true)
                    Text(
                        text = "Pattern match: $patternMatch",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    // Test with a simpler pattern
                    val simpleMatch = errorMessage.contains("supplied auth credentials", ignoreCase = true)
                    Text(
                        text = "Simple pattern match: $simpleMatch",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Show exact characters and length
                    Text(
                        text = "Length: ${errorMessage.length}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Test individual words
                    val hasSupplied = errorMessage.contains("supplied", ignoreCase = true)
                    val hasAuth = errorMessage.contains("auth", ignoreCase = true)
                    val hasCredentials = errorMessage.contains("credentials", ignoreCase = true)
                    Text(
                        text = "Has 'supplied': $hasSupplied, 'auth': $hasAuth, 'credentials': $hasCredentials",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ErrorDisplay(
                error = userFriendlyError,
                errorType = errorType,
                onDismiss = onDismissError,
                showRecoverySuggestion = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        } ?: run {
            // Debug: Log when no error is present
            println("SimpleLoginScreen: No error to display")
        }
        
        // Always show current auth state for debugging
        println("SimpleLoginScreen: Current auth state - isLoading: $isLoading, error: $error")
        println("SimpleLoginScreen: Error is null: ${error == null}")
        println("SimpleLoginScreen: Error is empty: ${error?.isEmpty()}")
        println("SimpleLoginScreen: Error length: ${error?.length}")
        
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
        
        // Biometric Error Message
        biometricError?.let { errorMessage ->
            val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(Exception(errorMessage))
            val errorType = ErrorHandler.getErrorType(Exception(errorMessage))
            
            ErrorDisplay(
                error = userFriendlyError,
                errorType = errorType,
                onDismiss = onDismissBiometricError,
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(emailFocusRequester),
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        scope.launch {
                            onSignInClick(email, password)
                        }
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign In Button
        Button(
            onClick = {
                // Don't clear errors here - let the sign-in process handle error display
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
        
        // Forgot Password Link
        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Google Sign In Button
        OutlinedButton(
            onClick = {
                // Don't clear errors here - let the sign-in process handle error display
                scope.launch {
                    onGoogleSignInClick()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Sign in with Google")
        }
        
        // Biometric Sign In Button (only show if biometric is available)
        if (isBiometricAvailable) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = {
                    // Don't clear errors here - let the sign-in process handle error display
                    scope.launch {
                        onBiometricSignInClick()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Biometric",
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Sign in with Biometric")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
