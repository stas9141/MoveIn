package com.example.movein.auth

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Secure token storage using Android Keystore and EncryptedSharedPreferences
 * Provides secure storage for authentication tokens and user data
 */
class SecureTokenStorage(private val context: Context) {
    
    companion object {
        private const val TAG = "SecureTokenStorage"
        private const val KEY_ALIAS = "MoveInAuthKey"
        private const val PREFS_NAME = "secure_auth_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_ID_KEY = "user_id"
        private const val USER_EMAIL_KEY = "user_email"
        private const val REMEMBER_ME_KEY = "remember_me"
        private const val TOKEN_EXPIRY_KEY = "token_expiry"
        private const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
        private const val LAST_LOGIN_KEY = "last_login"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Store authentication tokens securely
     */
    suspend fun storeTokens(
        accessToken: String,
        refreshToken: String,
        userId: String,
        userEmail: String,
        rememberMe: Boolean = false,
        expiryTime: Long? = null
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Storing tokens securely for user: $userEmail")
            
            val expiry = expiryTime ?: System.currentTimeMillis() + 15 * 60 * 1000 // 15 minutes default
            
            sharedPreferences.edit()
                .putString(ACCESS_TOKEN_KEY, accessToken)
                .putString(REFRESH_TOKEN_KEY, refreshToken)
                .putString(USER_ID_KEY, userId)
                .putString(USER_EMAIL_KEY, userEmail)
                .putBoolean(REMEMBER_ME_KEY, rememberMe)
                .putLong(TOKEN_EXPIRY_KEY, expiry)
                .putLong(LAST_LOGIN_KEY, System.currentTimeMillis())
                .apply()
                
            Log.d(TAG, "Tokens stored successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store tokens securely", e)
            throw SecurityException("Failed to store tokens securely", e)
        }
    }
    
    /**
     * Retrieve access token
     */
    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val token = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
            Log.d(TAG, "Retrieved access token: ${if (token != null) "***" else "null"}")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve access token", e)
            null
        }
    }
    
    /**
     * Retrieve refresh token
     */
    suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        try {
            val token = sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
            Log.d(TAG, "Retrieved refresh token: ${if (token != null) "***" else "null"}")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve refresh token", e)
            null
        }
    }
    
    /**
     * Retrieve user ID
     */
    suspend fun getUserId(): String? = withContext(Dispatchers.IO) {
        try {
            val userId = sharedPreferences.getString(USER_ID_KEY, null)
            Log.d(TAG, "Retrieved user ID: $userId")
            userId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve user ID", e)
            null
        }
    }
    
    /**
     * Retrieve user email
     */
    suspend fun getUserEmail(): String? = withContext(Dispatchers.IO) {
        try {
            val email = sharedPreferences.getString(USER_EMAIL_KEY, null)
            Log.d(TAG, "Retrieved user email: $email")
            email
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve user email", e)
            null
        }
    }
    
    /**
     * Check if remember me is enabled
     */
    suspend fun isRememberMeEnabled(): Boolean = withContext(Dispatchers.IO) {
        try {
            val rememberMe = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false)
            Log.d(TAG, "Remember me enabled: $rememberMe")
            rememberMe
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check remember me status", e)
            false
        }
    }
    
    /**
     * Check if access token is expired
     */
    suspend fun isAccessTokenExpired(): Boolean = withContext(Dispatchers.IO) {
        try {
            val expiryTime = sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
            val isExpired = System.currentTimeMillis() >= expiryTime
            Log.d(TAG, "Access token expired: $isExpired")
            isExpired
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check token expiry", e)
            true
        }
    }
    
    /**
     * Get token expiry time
     */
    suspend fun getTokenExpiryTime(): Long = withContext(Dispatchers.IO) {
        try {
            val expiryTime = sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
            Log.d(TAG, "Token expiry time: $expiryTime")
            expiryTime
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get token expiry time", e)
            0L
        }
    }
    
    /**
     * Get time until token expires in milliseconds
     */
    suspend fun getTimeUntilExpiry(): Long = withContext(Dispatchers.IO) {
        try {
            val expiryTime = getTokenExpiryTime()
            val timeUntilExpiry = expiryTime - System.currentTimeMillis()
            Log.d(TAG, "Time until expiry: ${timeUntilExpiry}ms")
            maxOf(0, timeUntilExpiry)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate time until expiry", e)
            0L
        }
    }
    
    /**
     * Set biometric authentication preference
     */
    suspend fun setBiometricEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Setting biometric enabled: $enabled")
            sharedPreferences.edit()
                .putBoolean(BIOMETRIC_ENABLED_KEY, enabled)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set biometric preference", e)
            throw SecurityException("Failed to set biometric preference", e)
        }
    }
    
    /**
     * Check if biometric authentication is enabled
     */
    suspend fun isBiometricEnabled(): Boolean = withContext(Dispatchers.IO) {
        try {
            val enabled = sharedPreferences.getBoolean(BIOMETRIC_ENABLED_KEY, false)
            Log.d(TAG, "Biometric enabled: $enabled")
            enabled
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check biometric preference", e)
            false
        }
    }
    
    /**
     * Get last login time
     */
    suspend fun getLastLoginTime(): Long = withContext(Dispatchers.IO) {
        try {
            val lastLogin = sharedPreferences.getLong(LAST_LOGIN_KEY, 0)
            Log.d(TAG, "Last login time: $lastLogin")
            lastLogin
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last login time", e)
            0L
        }
    }
    
    /**
     * Clear all stored tokens and user data
     */
    suspend fun clearTokens() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Clearing all stored tokens and user data")
            sharedPreferences.edit().clear().apply()
            Log.d(TAG, "Tokens cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear tokens", e)
            throw SecurityException("Failed to clear tokens", e)
        }
    }
    
    /**
     * Check if user is logged in
     */
    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            val refreshToken = getRefreshToken()
            val userId = getUserId()
            val userEmail = getUserEmail()
            
            val isLoggedIn = accessToken != null && refreshToken != null && userId != null && userEmail != null
            Log.d(TAG, "User logged in: $isLoggedIn")
            isLoggedIn
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check login status", e)
            false
        }
    }
    
    /**
     * Update access token only (for token refresh)
     */
    suspend fun updateAccessToken(accessToken: String, expiryTime: Long? = null) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating access token")
            val expiry = expiryTime ?: System.currentTimeMillis() + 15 * 60 * 1000 // 15 minutes default
            
            sharedPreferences.edit()
                .putString(ACCESS_TOKEN_KEY, accessToken)
                .putLong(TOKEN_EXPIRY_KEY, expiry)
                .apply()
                
            Log.d(TAG, "Access token updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update access token", e)
            throw SecurityException("Failed to update access token", e)
        }
    }
    
    /**
     * Update refresh token only
     */
    suspend fun updateRefreshToken(refreshToken: String) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating refresh token")
            sharedPreferences.edit()
                .putString(REFRESH_TOKEN_KEY, refreshToken)
                .apply()
                
            Log.d(TAG, "Refresh token updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update refresh token", e)
            throw SecurityException("Failed to update refresh token", e)
        }
    }
    
    /**
     * Get all stored user data for debugging (without sensitive tokens)
     */
    suspend fun getStoredUserData(): Map<String, Any?> = withContext(Dispatchers.IO) {
        try {
            mapOf(
                "userId" to getUserId(),
                "userEmail" to getUserEmail(),
                "rememberMe" to isRememberMeEnabled(),
                "biometricEnabled" to isBiometricEnabled(),
                "lastLogin" to getLastLoginTime(),
                "tokenExpiry" to getTokenExpiryTime(),
                "hasAccessToken" to (getAccessToken() != null),
                "hasRefreshToken" to (getRefreshToken() != null)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get stored user data", e)
            emptyMap()
        }
    }

    /**
     * Store tokens with additional parameters
     */
    suspend fun storeTokens(
        accessToken: String,
        refreshToken: String,
        userId: String,
        userEmail: String,
        rememberMe: Boolean = false
    ) = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit()
                .putString(ACCESS_TOKEN_KEY, accessToken)
                .putString(REFRESH_TOKEN_KEY, refreshToken)
                .putString(USER_ID_KEY, userId)
                .putString(USER_EMAIL_KEY, userEmail)
                .putBoolean("remember_me", rememberMe)
                .apply()
            Log.d(TAG, "Tokens stored securely with remember me: $rememberMe")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing tokens: ${e.message}", e)
        }
    }
    
    /**
     * Check if user has valid stored credentials for biometric login
     */
    suspend fun hasStoredCredentials(): Boolean = withContext(Dispatchers.IO) {
        try {
            val hasTokens = getAccessToken() != null && getRefreshToken() != null
            val hasUserData = getUserId() != null && getUserEmail() != null
            val biometricEnabled = isBiometricEnabled()
            
            val hasCredentials = hasTokens && hasUserData && biometricEnabled
            Log.d(TAG, "Has stored credentials for biometric login: $hasCredentials")
            hasCredentials
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check stored credentials", e)
            false
        }
    }

}
