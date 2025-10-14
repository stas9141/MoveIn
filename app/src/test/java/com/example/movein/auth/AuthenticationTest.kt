package com.example.movein.auth

import com.example.movein.utils.ErrorHandler
import com.example.movein.utils.ErrorType
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class AuthenticationTest {
    
    @Before
    fun setup() {
        // Setup test environment
    }
    
    @Test
    fun `test error handler for incorrect password`() {
        // Test that incorrect password errors are handled properly
        val exception = Exception("The password is invalid or the user does not have a password.")
        val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(exception)
        val errorType = ErrorHandler.getErrorType(exception)
        
        assertEquals("Incorrect password. Please try again.", userFriendlyError)
        assertEquals(ErrorType.AUTHENTICATION, errorType)
    }
    
    @Test
    fun `test error handler for user not found`() {
        val exception = Exception("There is no user record corresponding to this identifier.")
        val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(exception)
        val errorType = ErrorHandler.getErrorType(exception)
        
        assertEquals("No account found with this email. Please sign up first.", userFriendlyError)
        assertEquals(ErrorType.AUTHENTICATION, errorType)
    }
    
    @Test
    fun `test error handler for invalid email`() {
        val exception = Exception("The email address is badly formatted.")
        val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(exception)
        val errorType = ErrorHandler.getErrorType(exception)
        
        assertEquals("Please enter a valid email address.", userFriendlyError)
        assertEquals(ErrorType.AUTHENTICATION, errorType)
    }
    
    @Test
    fun `test error handler for user disabled`() {
        val exception = Exception("The user account has been disabled by an administrator.")
        val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(exception)
        val errorType = ErrorHandler.getErrorType(exception)
        
        assertEquals("This account has been disabled. Please contact support.", userFriendlyError)
        assertEquals(ErrorType.AUTHENTICATION, errorType)
    }
    
    @Test
    fun `test error handler for too many attempts`() {
        val exception = Exception("Too many unsuccessful login attempts. Please try again later.")
        val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(exception)
        val errorType = ErrorHandler.getErrorType(exception)
        
        assertEquals("Too many attempts. Please try again later.", userFriendlyError)
        assertEquals(ErrorType.AUTHENTICATION, errorType)
    }
    
    @Test
    fun `test error handler for network error`() {
        val exception = Exception("A network error (such as timeout, interrupted connection or unreachable host) has occurred.")
        val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(exception)
        val errorType = ErrorHandler.getErrorType(exception)
        
        assertEquals("Network error. Please check your internet connection.", userFriendlyError)
        assertEquals(ErrorType.NETWORK, errorType)
    }
    
    @Test
    fun `test error handler for unknown error`() {
        val exception = Exception("Some unknown error occurred.")
        val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(exception)
        val errorType = ErrorHandler.getErrorType(exception)
        
        assertEquals("An unexpected error occurred. Please try again.", userFriendlyError)
        assertEquals(ErrorType.UNKNOWN, errorType)
    }
    
    @Test
    fun `test recovery suggestions for authentication errors`() {
        val recoverySuggestion = ErrorHandler.getRecoverySuggestion(ErrorType.AUTHENTICATION)
        assertTrue(recoverySuggestion?.contains("password") == true || recoverySuggestion?.contains("email") == true)
    }
    
    @Test
    fun `test recovery suggestions for network errors`() {
        val recoverySuggestion = ErrorHandler.getRecoverySuggestion(ErrorType.NETWORK)
        assertTrue(recoverySuggestion?.contains("internet") == true || recoverySuggestion?.contains("connection") == true)
    }
}
