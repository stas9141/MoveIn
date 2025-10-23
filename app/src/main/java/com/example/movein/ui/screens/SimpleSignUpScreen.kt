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
fun SimpleSignUpScreen(
    onBackClick: () -> Unit,
    onSignUpClick: (email: String, password: String) -> Unit,
    onSignInClick: () -> Unit,
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
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Focus requesters for keyboard management
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    
    // Auto-focus email field when screen loads
    LaunchedEffect(Unit) {
        emailFocusRequester.requestFocus()
    }
    
    // Enhanced password validation
    val isPasswordValid = password.length >= 6 && password.any { it.isLetter() } && password.any { it.isDigit() }
    val isPasswordMatch = password == confirmPassword
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isFormValid = isEmailValid && isPasswordValid && isPasswordMatch
    
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
                text = "Sign Up",
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
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Sign up to sync your data across devices",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sign In Link - Moved to top for better UX
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onSignInClick) {
                Text("Sign In")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error Messages
        error?.let { errorMessage ->
            val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(Exception(errorMessage), isSignUp = true)
            val errorType = ErrorHandler.getErrorType(Exception(errorMessage))
            
            ErrorDisplay(
                error = userFriendlyError,
                errorType = errorType,
                onDismiss = onDismissError,
                showRecoverySuggestion = true
            )
            
            // Add "Sign In Instead" button for account already exists error
            if (errorMessage.contains("email-already-in-use", ignoreCase = true) || 
                userFriendlyError.contains("already exists", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign In Instead")
                }
            }
            
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
            singleLine = true,
            isError = email.isNotBlank() && !isEmailValid,
            supportingText = if (email.isNotBlank() && !isEmailValid) {
                { Text("Please enter a valid email address") }
            } else null
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
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { confirmPasswordFocusRequester.requestFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            singleLine = true,
            isError = password.isNotBlank() && !isPasswordValid,
            supportingText = if (password.isNotBlank() && !isPasswordValid) {
                { Text("Password must be at least 6 characters with letters and numbers") }
            } else null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
            },
            trailingIcon = {
                TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Text(if (confirmPasswordVisible) "Hide" else "Show")
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isFormValid) {
                        scope.launch {
                            onSignUpClick(email, password)
                        }
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmPasswordFocusRequester),
            singleLine = true,
            isError = confirmPassword.isNotBlank() && !isPasswordMatch,
            supportingText = if (confirmPassword.isNotBlank() && !isPasswordMatch) {
                { Text("Passwords do not match") }
            } else null
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign Up Button
        Button(
            onClick = {
                // Clear any existing errors when attempting to sign up
                onDismissError?.invoke()
                onDismissGoogleError?.invoke()
                scope.launch {
                    onSignUpClick(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && isFormValid
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Sign Up")
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
            Text("Sign up with Google")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
