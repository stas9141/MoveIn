package com.example.movein.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuthException

object ErrorHandler {
    
    /**
     * Converts technical error messages to user-friendly messages
     */
    fun getUserFriendlyErrorMessage(error: Throwable?): String {
        if (error == null) return "An unexpected error occurred"
        
        val errorMessage = error.message ?: "An unexpected error occurred"
        
        // DEBUG: Log the error message and pattern matching
        println("ErrorHandler: Processing error message: '$errorMessage'")
        println("ErrorHandler: Checking pattern 'The supplied auth credentials is incorrect, malformed or has expired'")
        println("ErrorHandler: Pattern match result: ${errorMessage.contains("The supplied auth credentials is incorrect, malformed or has expired", ignoreCase = true)}")
        
        return when {
            // Canonical Firebase client messages (exact phrases from tests/SDK)
            errorMessage.contains("The password is invalid", ignoreCase = true) ||
            errorMessage.contains("does not have a password", ignoreCase = true) ||
            errorMessage.contains("The supplied auth credentials is incorrect, malformed or has expired", ignoreCase = true) ||
            errorMessage.contains("supplied auth", ignoreCase = true) ->
                "Incorrect password. Please try again."

            errorMessage.contains("There is no user record corresponding to this identifier", ignoreCase = true) ->
                "No account found with this email. Please sign up first."

            errorMessage.contains("The email address is badly formatted", ignoreCase = true) ->
                "Please enter a valid email address."

            errorMessage.contains("The user account has been disabled", ignoreCase = true) ->
                "This account has been disabled. Please contact support."

            errorMessage.contains("Too many unsuccessful login attempts", ignoreCase = true) ->
                "Too many attempts. Please try again later."

            errorMessage.contains("A network error", ignoreCase = true) ->
                "Network error. Please check your internet connection."

            // Firebase Authentication Errors - More specific handling
            errorMessage.contains("API key not valid", ignoreCase = true) -> 
                "Unable to connect to authentication service. Please check your internet connection and try again."
            
            errorMessage.contains("network", ignoreCase = true) -> 
                "Network error. Please check your internet connection."
            
            errorMessage.contains("timeout", ignoreCase = true) -> 
                "Request timed out. Please check your connection and try again."
            
            errorMessage.contains("invalid-email", ignoreCase = true) -> 
                "Please enter a valid email address."
            
            errorMessage.contains("user-not-found", ignoreCase = true) -> 
                "No account found with this email address. Please check your email or sign up."
            
            errorMessage.contains("wrong-password", ignoreCase = true) -> 
                "Incorrect password. Please try again."
            
            errorMessage.contains("email-already-in-use", ignoreCase = true) -> 
                "An account with this email already exists. Please sign in instead."
            
            errorMessage.contains("weak-password", ignoreCase = true) -> 
                "Password is too weak. Please choose a stronger password with at least 6 characters."
            
            errorMessage.contains("too-many-requests", ignoreCase = true) -> 
                "Too many failed attempts. Please wait a moment before trying again."
            
            errorMessage.contains("user-disabled", ignoreCase = true) -> 
                "This account has been disabled. Please contact support."
            
            errorMessage.contains("operation-not-allowed", ignoreCase = true) -> 
                "This sign-in method is not enabled. Please try a different method."
            
            // Firebase specific errors
            errorMessage.contains("FirebaseAuthInvalidCredentialsException", ignoreCase = true) -> 
                "Invalid email or password. Please check your credentials and try again."
            
            errorMessage.contains("FirebaseAuthUserCollisionException", ignoreCase = true) -> 
                "An account with this email already exists. Please sign in instead."
            
            errorMessage.contains("FirebaseAuthWeakPasswordException", ignoreCase = true) -> 
                "Password is too weak. Please choose a stronger password with at least 6 characters."
            
            errorMessage.contains("FirebaseAuthInvalidUserException", ignoreCase = true) -> 
                "Invalid user account. Please check your email or create a new account."
            
            errorMessage.contains("FirebaseAuthEmailException", ignoreCase = true) -> 
                "Invalid email address. Please enter a valid email."
            
            errorMessage.contains("FirebaseAuthTooManyRequestsException", ignoreCase = true) -> 
                "Too many failed attempts. Please wait a moment before trying again."
            
            errorMessage.contains("FirebaseAuthOperationNotAllowedException", ignoreCase = true) -> 
                "This sign-in method is not enabled. Please try a different method."
            
            // Google Sign-In Errors
            errorMessage.contains("google", ignoreCase = true) && errorMessage.contains("sign", ignoreCase = true) -> 
                "Google Sign-In is temporarily unavailable. Please try signing in with email and password."
            
            // Cloud Storage Errors
            errorMessage.contains("cloud storage not available", ignoreCase = true) -> 
                "Cloud sync is temporarily unavailable. Your data is saved locally."
            
            errorMessage.contains("permission", ignoreCase = true) -> 
                "You don't have permission to perform this action."
            
            errorMessage.contains("quota", ignoreCase = true) -> 
                "Storage quota exceeded. Please contact support."
            
            // General Network Errors
            errorMessage.contains("connection", ignoreCase = true) -> 
                "Unable to connect to the server. Please check your internet connection."
            
            errorMessage.contains("server", ignoreCase = true) -> 
                "Server is temporarily unavailable. Please try again later."
            
            // File/Storage Errors
            errorMessage.contains("file", ignoreCase = true) -> 
                "Unable to access files. Please try again."
            
            errorMessage.contains("storage", ignoreCase = true) -> 
                "Storage error occurred. Your data is safe."
            
            // Default case - return a generic user-friendly message matching tests
            else -> "An unexpected error occurred. Please try again."
        }
    }
    
    /**
     * Gets a user-friendly error message for specific error types
     */
    fun getErrorMessageForType(errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.NETWORK -> "Please check your internet connection and try again."
            ErrorType.AUTHENTICATION -> "Authentication failed. Please check your credentials."
            ErrorType.PERMISSION -> "You don't have permission to perform this action."
            ErrorType.STORAGE -> "Unable to save data. Please try again."
            ErrorType.SYNC -> "Unable to sync data. Your local data is safe."
            ErrorType.UNKNOWN -> "Something went wrong. Please try again."
        }
    }
    
    /**
     * Determines the error type from an exception
     */
    fun getErrorType(error: Throwable?): ErrorType {
        if (error == null) return ErrorType.UNKNOWN
        
        val errorMessage = error.message ?: ""
        
        return when {
            // Explicit Firebase phrases used in tests
            errorMessage.contains("There is no user record corresponding to this identifier", ignoreCase = true) -> ErrorType.AUTHENTICATION
            errorMessage.contains("The user account has been disabled", ignoreCase = true) -> ErrorType.AUTHENTICATION
            errorMessage.contains("Too many unsuccessful login attempts", ignoreCase = true) -> ErrorType.AUTHENTICATION
            errorMessage.contains("The password is invalid", ignoreCase = true) -> ErrorType.AUTHENTICATION
            errorMessage.contains("The email address is badly formatted", ignoreCase = true) -> ErrorType.AUTHENTICATION
            errorMessage.contains("The supplied auth credentials is incorrect, malformed or has expired", ignoreCase = true) -> ErrorType.AUTHENTICATION
            errorMessage.contains("supplied auth", ignoreCase = true) -> ErrorType.AUTHENTICATION
            
            errorMessage.contains("network", ignoreCase = true) || 
            errorMessage.contains("connection", ignoreCase = true) ||
            errorMessage.contains("timeout", ignoreCase = true) ||
            errorMessage.contains("API key not valid", ignoreCase = true) -> ErrorType.NETWORK
            
            errorMessage.contains("auth", ignoreCase = true) ||
            errorMessage.contains("sign", ignoreCase = true) ||
            errorMessage.contains("password", ignoreCase = true) ||
            errorMessage.contains("email", ignoreCase = true) ||
            errorMessage.contains("FirebaseAuth", ignoreCase = true) ||
            errorMessage.contains("invalid-email", ignoreCase = true) ||
            errorMessage.contains("user-not-found", ignoreCase = true) ||
            errorMessage.contains("wrong-password", ignoreCase = true) ||
            errorMessage.contains("email-already-in-use", ignoreCase = true) ||
            errorMessage.contains("weak-password", ignoreCase = true) ||
            errorMessage.contains("too-many-requests", ignoreCase = true) ||
            errorMessage.contains("user-disabled", ignoreCase = true) ||
            errorMessage.contains("operation-not-allowed", ignoreCase = true) -> ErrorType.AUTHENTICATION
            
            errorMessage.contains("permission", ignoreCase = true) -> ErrorType.PERMISSION
            
            errorMessage.contains("storage", ignoreCase = true) ||
            errorMessage.contains("file", ignoreCase = true) -> ErrorType.STORAGE
            
            errorMessage.contains("sync", ignoreCase = true) ||
            errorMessage.contains("cloud", ignoreCase = true) -> ErrorType.SYNC
            
            else -> ErrorType.UNKNOWN
        }
    }
    
    /**
     * Provides recovery suggestions for different error types
     */
    fun getRecoverySuggestion(errorType: ErrorType): String? {
        return when (errorType) {
            ErrorType.NETWORK -> "Check your internet connection and try again."
            ErrorType.AUTHENTICATION -> "Check your email format and password strength, or try signing in if you already have an account."
            ErrorType.PERMISSION -> "Contact support if you believe this is an error."
            ErrorType.STORAGE -> "Try restarting the app or clearing some space."
            ErrorType.SYNC -> "Your data is saved locally. Sync will resume when connection is restored."
            ErrorType.UNKNOWN -> "Try restarting the app or contact support if the problem persists."
        }
    }
    
    /**
     * Gets a user-friendly error message with context awareness
     */
    fun getUserFriendlyErrorMessage(error: Throwable?, isSignUp: Boolean = false): String {
        if (error == null) return "An unexpected error occurred"
        
        val errorMessage = error.message ?: "An unexpected error occurred"
        
        return when {
            // Canonical Firebase client messages (exact phrases from tests/SDK)
            errorMessage.contains("The password is invalid", ignoreCase = true) ||
            errorMessage.contains("does not have a password", ignoreCase = true) ||
            errorMessage.contains("The supplied auth credentials is incorrect, malformed or has expired", ignoreCase = true) ||
            errorMessage.contains("supplied auth", ignoreCase = true) ->
                "Incorrect password. Please try again."

            errorMessage.contains("There is no user record corresponding to this identifier", ignoreCase = true) ->
                "No account found with this email. Please sign up first."

            errorMessage.contains("The email address is badly formatted", ignoreCase = true) ->
                "Please enter a valid email address."

            errorMessage.contains("The user account has been disabled", ignoreCase = true) ->
                "This account has been disabled. Please contact support."

            errorMessage.contains("Too many unsuccessful login attempts", ignoreCase = true) ->
                "Too many attempts. Please try again later."

            errorMessage.contains("A network error", ignoreCase = true) ->
                "Network error. Please check your internet connection."

            // Firebase Authentication Errors - Context aware
            errorMessage.contains("API key not valid", ignoreCase = true) -> 
                if (isSignUp) {
                    "Unable to create account. Please check your internet connection and try again."
                } else {
                    "Unable to connect to authentication service. Please check your internet connection and try again."
                }
            
            errorMessage.contains("network", ignoreCase = true) -> 
                "Network error. Please check your internet connection."
            
            errorMessage.contains("timeout", ignoreCase = true) -> 
                "Request timed out. Please check your connection and try again."
            
            errorMessage.contains("invalid-email", ignoreCase = true) -> 
                "Please enter a valid email address."
            
            errorMessage.contains("user-not-found", ignoreCase = true) -> 
                "No account found with this email address. Please check your email or sign up."
            
            errorMessage.contains("wrong-password", ignoreCase = true) -> 
                "Incorrect password. Please try again."
            
            errorMessage.contains("email-already-in-use", ignoreCase = true) -> 
                if (isSignUp) {
                    "An account with this email already exists. Please sign in instead."
                } else {
                    "An account with this email already exists. Please sign in instead."
                }
            
            errorMessage.contains("weak-password", ignoreCase = true) -> 
                if (isSignUp) {
                    "Password is too weak. Please choose a stronger password with at least 6 characters."
                } else {
                    "Password is too weak. Please choose a stronger password with at least 6 characters."
                }
            
            errorMessage.contains("too-many-requests", ignoreCase = true) -> 
                "Too many failed attempts. Please wait a moment before trying again."
            
            errorMessage.contains("user-disabled", ignoreCase = true) -> 
                "This account has been disabled. Please contact support."
            
            errorMessage.contains("operation-not-allowed", ignoreCase = true) -> 
                "This sign-in method is not enabled. Please try a different method."
            
            // Firebase specific errors
            errorMessage.contains("FirebaseAuthInvalidCredentialsException", ignoreCase = true) -> 
                "Invalid email or password. Please check your credentials and try again."
            
            errorMessage.contains("FirebaseAuthUserCollisionException", ignoreCase = true) -> 
                if (isSignUp) {
                    "An account with this email already exists. Please sign in instead."
                } else {
                    "An account with this email already exists. Please sign in instead."
                }
            
            errorMessage.contains("FirebaseAuthWeakPasswordException", ignoreCase = true) -> 
                "Password is too weak. Please choose a stronger password with at least 6 characters."
            
            errorMessage.contains("FirebaseAuthInvalidUserException", ignoreCase = true) -> 
                "Invalid user account. Please check your email or create a new account."
            
            errorMessage.contains("FirebaseAuthEmailException", ignoreCase = true) -> 
                "Invalid email address. Please enter a valid email."
            
            errorMessage.contains("FirebaseAuthTooManyRequestsException", ignoreCase = true) -> 
                "Too many failed attempts. Please wait a moment before trying again."
            
            errorMessage.contains("FirebaseAuthOperationNotAllowedException", ignoreCase = true) -> 
                "This sign-in method is not enabled. Please try a different method."
            
            // Google Sign-In Errors
            errorMessage.contains("google", ignoreCase = true) && errorMessage.contains("sign", ignoreCase = true) -> 
                "Google Sign-In is temporarily unavailable. Please try signing in with email and password."
            
            // Cloud Storage Errors
            errorMessage.contains("cloud storage not available", ignoreCase = true) -> 
                "Cloud sync is temporarily unavailable. Your data is saved locally."
            
            errorMessage.contains("permission", ignoreCase = true) -> 
                "You don't have permission to perform this action."
            
            errorMessage.contains("quota", ignoreCase = true) -> 
                "Storage quota exceeded. Please contact support."
            
            // General Network Errors
            errorMessage.contains("connection", ignoreCase = true) -> 
                "Unable to connect to the server. Please check your internet connection."
            
            errorMessage.contains("server", ignoreCase = true) -> 
                "Server is temporarily unavailable. Please try again later."
            
            // File/Storage Errors
            errorMessage.contains("file", ignoreCase = true) -> 
                "Unable to access files. Please try again."
            
            errorMessage.contains("storage", ignoreCase = true) -> 
                "Storage error occurred. Your data is safe."
            
            // Default case - return a generic user-friendly message matching tests
            else -> if (isSignUp) {
                "Unable to create account. Please try again."
            } else {
                "An unexpected error occurred. Please try again."
            }
        }
    }
}

enum class ErrorType {
    NETWORK,
    AUTHENTICATION,
    PERMISSION,
    STORAGE,
    SYNC,
    UNKNOWN
}
