package com.example.movein.network

import retrofit2.Response
import retrofit2.http.*

/**
 * API interface for authentication endpoints
 */
interface AuthApi {
    
    /**
     * User login endpoint
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    /**
     * User signup endpoint
     */
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>
    
    /**
     * Google Sign-In endpoint
     */
    @POST("auth/google-signin")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): Response<AuthResponse>
    
    /**
     * Apple Sign-In endpoint
     */
    @POST("auth/apple-signin")
    suspend fun appleSignIn(@Body request: AppleSignInRequest): Response<AuthResponse>
    
    /**
     * Refresh access token endpoint
     */
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
    
    /**
     * User logout endpoint
     */
    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<SimpleResponse>
    
    /**
     * Logout from all devices endpoint
     */
    @POST("auth/logout-all-devices")
    suspend fun logoutAllDevices(): Response<SimpleResponse>
    
    /**
     * Forgot password endpoint
     */
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<PasswordResetResponse>
    
    /**
     * Reset password endpoint
     */
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<SimpleResponse>
    
    /**
     * Change password endpoint (authenticated)
     */
    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<SimpleResponse>
    
    /**
     * Verify email endpoint
     */
    @POST("auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): Response<SimpleResponse>
    
    /**
     * Resend email verification endpoint
     */
    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ForgotPasswordRequest): Response<EmailVerificationResponse>
    
    /**
     * Get current user profile (authenticated)
     */
    @GET("auth/profile")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>
    
    /**
     * Update user profile (authenticated)
     */
    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserProfile>>
    
    /**
     * Get user sessions (authenticated)
     */
    @GET("auth/sessions")
    suspend fun getSessions(): Response<SessionListResponse>
    
    /**
     * Revoke a specific session (authenticated)
     */
    @DELETE("auth/sessions/{sessionId}")
    suspend fun revokeSession(@Path("sessionId") sessionId: String): Response<SimpleResponse>
    
    /**
     * Revoke all other sessions (authenticated)
     */
    @DELETE("auth/sessions/others")
    suspend fun revokeOtherSessions(): Response<SimpleResponse>
    
    /**
     * Delete user account (authenticated)
     */
    @DELETE("auth/account")
    suspend fun deleteAccount(@Body request: ChangePasswordRequest): Response<SimpleResponse>
}

