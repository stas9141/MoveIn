package com.example.movein.auth

import android.content.Context
import android.util.Log
import com.example.movein.network.ApiClient
import com.example.movein.network.AuthApi
import com.example.movein.network.LoginRequest
import com.example.movein.network.SignupRequest
import com.example.movein.network.RefreshTokenRequest
import com.example.movein.network.LogoutRequest
import com.example.movein.network.ForgotPasswordRequest
import com.example.movein.network.ResetPasswordRequest
import com.example.movein.network.ChangePasswordRequest
import com.example.movein.network.UpdateProfileRequest
import com.example.movein.network.GoogleSignInRequest
import com.example.movein.network.AppleSignInRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Centralized authentication manager for handling all auth operations
 */
class AuthManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AuthManager"
    }
    
    private val apiClient = ApiClient(context)
    private val authApi: AuthApi = apiClient.getAuthApi()
    private val secureStorage = SecureTokenStorage(context)
    
    /**
     * Convert network User to AuthUser
     */
    private fun convertToAuthUser(networkUser: com.example.movein.network.User): AuthUser {
        return AuthUser(
            id = networkUser.id,
            email = networkUser.email,
            firstName = networkUser.firstName,
            lastName = networkUser.lastName,
            phoneNumber = networkUser.phoneNumber,
            profilePictureUrl = networkUser.profilePictureUrl,
            emailVerified = networkUser.emailVerified,
            phoneVerified = networkUser.phoneVerified,
            isActive = networkUser.isActive,
            createdAt = networkUser.createdAt,
            updatedAt = networkUser.updatedAt,
            lastLoginAt = networkUser.lastLoginAt
        )
    }
    
    /**
     * Authentication data class
     */
    data class AuthData(
        val user: AuthUser,
        val accessToken: String,
        val refreshToken: String
    )
    
    /**
     * User data class for authentication
     */
    data class AuthUser(
        val id: String,
        val email: String,
        val firstName: String,
        val lastName: String,
        val phoneNumber: String? = null,
        val profilePictureUrl: String? = null,
        val emailVerified: Boolean = false,
        val phoneVerified: Boolean = false,
        val isActive: Boolean = true,
        val createdAt: String,
        val updatedAt: String,
        val lastLoginAt: String? = null
    )
    
    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<AuthData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting login for email: ${email.take(3)}***")
            
            val request = LoginRequest(email, password)
            val response = authApi.login(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null && authResponse.success) {
                    val authData = AuthData(
                        user = convertToAuthUser(authResponse.data.user),
                        accessToken = authResponse.data.tokens.accessToken,
                        refreshToken = authResponse.data.tokens.refreshToken
                    )
                    
                    Log.d(TAG, "Login successful for user: ${authData.user.id}")
                    Result.success(authData)
                } else {
                    val errorMessage = authResponse?.message ?: "Login failed"
                    Log.w(TAG, "Login failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Login failed: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sign up with user details
     */
    suspend fun signup(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String? = null
    ): Result<AuthData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting signup for email: ${email.take(3)}***")
            
            val request = SignupRequest(email, password, firstName, lastName, phoneNumber)
            val response = authApi.signup(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null && authResponse.success) {
                    val authData = AuthData(
                        user = convertToAuthUser(authResponse.data.user),
                        accessToken = authResponse.data.tokens.accessToken,
                        refreshToken = authResponse.data.tokens.refreshToken
                    )
                    
                    Log.d(TAG, "Signup successful for user: ${authData.user.id}")
                    Result.success(authData)
                } else {
                    val errorMessage = authResponse?.message ?: "Signup failed"
                    Log.w(TAG, "Signup failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Signup failed: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Signup error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Refresh access token
     */
    suspend fun refreshToken(): Result<AuthData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting token refresh")
            
            val refreshToken = secureStorage.getRefreshToken()
            if (refreshToken == null) {
                Log.w(TAG, "No refresh token available")
                return@withContext Result.failure(Exception("No refresh token available"))
            }
            
            val request = RefreshTokenRequest(refreshToken)
            val response = authApi.refreshToken(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null && authResponse.success) {
                    val authData = AuthData(
                        user = convertToAuthUser(authResponse.data.user),
                        accessToken = authResponse.data.tokens.accessToken,
                        refreshToken = authResponse.data.tokens.refreshToken
                    )
                    
                    Log.d(TAG, "Token refresh successful")
                    Result.success(authData)
                } else {
                    val errorMessage = authResponse?.message ?: "Token refresh failed"
                    Log.w(TAG, "Token refresh failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Token refresh failed: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Logout user
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting logout")
            
            val refreshToken = secureStorage.getRefreshToken()
            if (refreshToken != null) {
                val request = LogoutRequest(refreshToken)
                authApi.logout(request)
            }
            
            // Clear local tokens regardless of API response
            secureStorage.clearTokens()
            Log.d(TAG, "Logout successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Logout error", e)
            // Clear local tokens even if API call fails
            secureStorage.clearTokens()
            Result.success(Unit)
        }
    }
    
    /**
     * Forgot password
     */
    suspend fun forgotPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting forgot password for email: ${email.take(3)}***")
            
            val request = ForgotPasswordRequest(email)
            val response = authApi.forgotPassword(request)
            
            if (response.isSuccessful) {
                val resetResponse = response.body()
                if (resetResponse != null && resetResponse.success) {
                    Log.d(TAG, "Forgot password request successful")
                    Result.success(Unit)
                } else {
                    val errorMessage = resetResponse?.message ?: "Forgot password failed"
                    Log.w(TAG, "Forgot password failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Forgot password failed: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Forgot password error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Reset password
     */
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting password reset")
            
            val request = ResetPasswordRequest(token, newPassword)
            val response = authApi.resetPassword(request)
            
            if (response.isSuccessful) {
                val resetResponse = response.body()
                if (resetResponse != null && resetResponse.success) {
                    Log.d(TAG, "Password reset successful")
                    Result.success(Unit)
                } else {
                    val errorMessage = resetResponse?.message ?: "Password reset failed"
                    Log.w(TAG, "Password reset failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Password reset failed: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Password reset error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Change password (authenticated)
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting password change")
            
            val request = ChangePasswordRequest(currentPassword, newPassword)
            val response = authApi.changePassword(request)
            
            if (response.isSuccessful) {
                val changeResponse = response.body()
                if (changeResponse != null && changeResponse.success) {
                    Log.d(TAG, "Password change successful")
                    Result.success(Unit)
                } else {
                    val errorMessage = changeResponse?.message ?: "Password change failed"
                    Log.w(TAG, "Password change failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Password change failed: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Password change error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get current user profile
     */
    suspend fun getCurrentUser(): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching current user profile")
            
            val response = authApi.getProfile()
            
            if (response.isSuccessful) {
                val profileResponse = response.body()
                if (profileResponse != null && profileResponse.success && profileResponse.data != null) {
                    val user = AuthUser(
                        id = profileResponse.data.id,
                        email = profileResponse.data.email,
                        firstName = profileResponse.data.firstName,
                        lastName = profileResponse.data.lastName,
                        phoneNumber = profileResponse.data.phoneNumber,
                        profilePictureUrl = profileResponse.data.profilePictureUrl,
                        emailVerified = profileResponse.data.emailVerified,
                        phoneVerified = profileResponse.data.phoneVerified,
                        isActive = true,
                        createdAt = profileResponse.data.createdAt,
                        updatedAt = profileResponse.data.createdAt, // Use createdAt as fallback
                        lastLoginAt = profileResponse.data.lastLoginAt
                    )
                    
                    Log.d(TAG, "User profile fetched successfully")
                    Result.success(user)
                } else {
                    val errorMessage = profileResponse?.message ?: "Failed to fetch user profile"
                    Log.w(TAG, "Failed to fetch user profile: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Failed to fetch user profile: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get user profile error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null
    ): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating user profile")
            
            val request = UpdateProfileRequest(firstName, lastName, phoneNumber)
            val response = authApi.updateProfile(request)
            
            if (response.isSuccessful) {
                val profileResponse = response.body()
                if (profileResponse != null && profileResponse.success && profileResponse.data != null) {
                    val user = AuthUser(
                        id = profileResponse.data.id,
                        email = profileResponse.data.email,
                        firstName = profileResponse.data.firstName,
                        lastName = profileResponse.data.lastName,
                        phoneNumber = profileResponse.data.phoneNumber,
                        profilePictureUrl = profileResponse.data.profilePictureUrl,
                        emailVerified = profileResponse.data.emailVerified,
                        phoneVerified = profileResponse.data.phoneVerified,
                        isActive = true,
                        createdAt = profileResponse.data.createdAt,
                        updatedAt = profileResponse.data.createdAt,
                        lastLoginAt = profileResponse.data.lastLoginAt
                    )
                    
                    Log.d(TAG, "User profile updated successfully")
                    Result.success(user)
                } else {
                    val errorMessage = profileResponse?.message ?: "Failed to update user profile"
                    Log.w(TAG, "Failed to update user profile: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Failed to update user profile: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update user profile error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean = withContext(Dispatchers.IO) {
        try {
            val isLoggedIn = secureStorage.isUserLoggedIn()
            if (isLoggedIn) {
                // Check if access token is expired
                val isExpired = secureStorage.isAccessTokenExpired()
                if (isExpired) {
                    Log.d(TAG, "Access token expired, attempting refresh")
                    // Try to refresh token
                    val refreshResult = refreshToken()
                    if (refreshResult.isSuccess) {
                        val authData = refreshResult.getOrNull()
                        if (authData != null) {
                            // Store new tokens
                            secureStorage.storeTokens(
                                accessToken = authData.accessToken,
                                refreshToken = authData.refreshToken,
                                userId = authData.user.id,
                                userEmail = authData.user.email
                            )
                            return@withContext true
                        }
                    }
                    return@withContext false
                } else {
                    return@withContext true
                }
            } else {
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Authentication check error", e)
            false
        }
    }
    
    /**
     * Get stored user data
     */
    suspend fun getStoredUserData(): AuthUser? = withContext(Dispatchers.IO) {
        try {
            val userId = secureStorage.getUserId()
            val userEmail = secureStorage.getUserEmail()
            
            if (userId != null && userEmail != null) {
                // Return basic user data from storage
                // In a real app, you might want to fetch full profile from API
                AuthUser(
                    id = userId,
                    email = userEmail,
                    firstName = "", // These would be stored separately or fetched from API
                    lastName = "",
                    createdAt = "",
                    updatedAt = ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get stored user data error", e)
            null
        }
    }
    
    /**
     * Google Sign-In
     */
    suspend fun googleSignIn(idToken: String): Result<AuthData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting Google sign-in")
            
            val request = GoogleSignInRequest(idToken)
            val response = authApi.googleSignIn(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null && authResponse.success) {
                    val authData = AuthData(
                        user = convertToAuthUser(authResponse.data.user),
                        accessToken = authResponse.data.tokens.accessToken,
                        refreshToken = authResponse.data.tokens.refreshToken
                    )
                    secureStorage.storeTokens(
                        authData.accessToken,
                        authData.refreshToken,
                        authData.user.id,
                        authData.user.email
                    )
                    Log.d(TAG, "Google sign-in successful for user: ${authData.user.id}")
                    Result.success(authData)
                } else {
                    val errorMessage = authResponse?.message ?: "Google sign-in failed"
                    Log.w(TAG, "Google sign-in failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Google sign-in failed: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Apple Sign-In
     */
    suspend fun appleSignIn(identityToken: String, authorizationCode: String? = null, user: String? = null): Result<AuthData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting Apple sign-in")
            
            val request = AppleSignInRequest(identityToken, authorizationCode, user)
            val response = authApi.appleSignIn(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null && authResponse.success) {
                    val authData = AuthData(
                        user = convertToAuthUser(authResponse.data.user),
                        accessToken = authResponse.data.tokens.accessToken,
                        refreshToken = authResponse.data.tokens.refreshToken
                    )
                    secureStorage.storeTokens(
                        authData.accessToken,
                        authData.refreshToken,
                        authData.user.id,
                        authData.user.email
                    )
                    Log.d(TAG, "Apple sign-in successful for user: ${authData.user.id}")
                    Result.success(authData)
                } else {
                    val errorMessage = authResponse?.message ?: "Apple sign-in failed"
                    Log.w(TAG, "Apple sign-in failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Apple sign-in failed: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Apple sign-in error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Logout from all devices
     */
    suspend fun logoutAllDevices(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Logging out from all devices")
            
            val response = authApi.logoutAllDevices()
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    // Clear local tokens after successful logout
                    secureStorage.clearTokens()
                    Log.d(TAG, "Successfully logged out from all devices")
                    Result.success(Unit)
                } else {
                    val errorMessage = apiResponse?.message ?: "Failed to logout from all devices"
                    Log.w(TAG, "Logout all devices failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorMessage = "Failed to logout from all devices: ${response.message()}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Logout all devices error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Clear all authentication data
     */
    suspend fun clearAuthData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Clearing all authentication data")
            secureStorage.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Clear auth data error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Enable biometric authentication
     */
    suspend fun enableBiometricAuth(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Enabling biometric authentication")
            secureStorage.setBiometricEnabled(true)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable biometric authentication", e)
            Result.failure(e)
        }
    }
    
    /**
     * Disable biometric authentication
     */
    suspend fun disableBiometricAuth(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Disabling biometric authentication")
            secureStorage.setBiometricEnabled(false)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable biometric authentication", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if biometric authentication is enabled
     */
    suspend fun isBiometricEnabled(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val isEnabled = secureStorage.isBiometricEnabled()
            Log.d(TAG, "Biometric authentication enabled: $isEnabled")
            Result.success(isEnabled)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check biometric status", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if user can use biometric authentication
     */
    suspend fun canUseBiometricAuth(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val hasCredentials = secureStorage.hasStoredCredentials()
            Log.d(TAG, "Can use biometric authentication: $hasCredentials")
            Result.success(hasCredentials)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check biometric availability", e)
            Result.failure(e)
        }
    }
}
