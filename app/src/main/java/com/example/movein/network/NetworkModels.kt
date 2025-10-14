package com.example.movein.network

import com.google.gson.annotations.SerializedName

/**
 * Network data models for API communication
 */

// Request models
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class SignupRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null
)

data class GoogleSignInRequest(
    @SerializedName("idToken")
    val idToken: String
)

data class AppleSignInRequest(
    @SerializedName("identityToken")
    val identityToken: String,
    @SerializedName("authorizationCode")
    val authorizationCode: String? = null,
    @SerializedName("user")
    val user: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class LogoutRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)

data class ResetPasswordRequest(
    @SerializedName("token")
    val token: String,
    @SerializedName("newPassword")
    val newPassword: String
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)

data class UpdateProfileRequest(
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null
)

// Response models
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("error")
    val error: ApiError? = null
)

data class ApiError(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("details")
    val details: Map<String, String>? = null
)

data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: AuthData
)

data class AuthData(
    @SerializedName("user")
    val user: User,
    @SerializedName("tokens")
    val tokens: TokenPair
)

data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @SerializedName("profilePictureUrl")
    val profilePictureUrl: String? = null,
    @SerializedName("emailVerified")
    val emailVerified: Boolean = false,
    @SerializedName("phoneVerified")
    val phoneVerified: Boolean = false,
    @SerializedName("isActive")
    val isActive: Boolean = true,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("lastLoginAt")
    val lastLoginAt: String? = null
)

data class TokenPair(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("expiresIn")
    val expiresIn: Long? = null
)

data class UserProfile(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @SerializedName("profilePictureUrl")
    val profilePictureUrl: String? = null,
    @SerializedName("emailVerified")
    val emailVerified: Boolean = false,
    @SerializedName("phoneVerified")
    val phoneVerified: Boolean = false,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("lastLoginAt")
    val lastLoginAt: String? = null
)

data class PasswordResetResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: PasswordResetData? = null
)

data class PasswordResetData(
    @SerializedName("resetToken")
    val resetToken: String,
    @SerializedName("expiresAt")
    val expiresAt: String
)

data class EmailVerificationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: EmailVerificationData? = null
)

data class EmailVerificationData(
    @SerializedName("verificationToken")
    val verificationToken: String,
    @SerializedName("expiresAt")
    val expiresAt: String
)

// Error response models
data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String,
    @SerializedName("error")
    val error: ErrorDetails? = null
)

data class ErrorDetails(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("field")
    val field: String? = null,
    @SerializedName("details")
    val details: Map<String, String>? = null
)

// Validation error response
data class ValidationErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "Validation failed",
    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
)

// Generic response for simple operations
data class SimpleResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

// Pagination models
data class PaginatedResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<T>,
    @SerializedName("pagination")
    val pagination: PaginationInfo
)

data class PaginationInfo(
    @SerializedName("page")
    val page: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("hasNext")
    val hasNext: Boolean,
    @SerializedName("hasPrev")
    val hasPrev: Boolean
)

// Device info for session management
data class DeviceInfo(
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("deviceName")
    val deviceName: String,
    @SerializedName("deviceModel")
    val deviceModel: String,
    @SerializedName("osVersion")
    val osVersion: String,
    @SerializedName("appVersion")
    val appVersion: String,
    @SerializedName("userAgent")
    val userAgent: String
)

// Session management models
data class UserSession(
    @SerializedName("id")
    val id: String,
    @SerializedName("deviceInfo")
    val deviceInfo: DeviceInfo,
    @SerializedName("ipAddress")
    val ipAddress: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("lastActiveAt")
    val lastActiveAt: String,
    @SerializedName("isActive")
    val isActive: Boolean
)

data class SessionListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<UserSession>
)

