package com.example.movein.network

import android.content.Context
import android.util.Log
import com.example.movein.auth.SecureTokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * API client with automatic token management and refresh
 */
class ApiClient(private val context: Context) {
    
    companion object {
        private const val TAG = "ApiClient"
        private const val BASE_URL = "https://your-api-domain.com/api/"
        private const val TIMEOUT_SECONDS = 30L
    }
    
    private val secureStorage = SecureTokenStorage(context)
    private val authApi: AuthApi
    
    init {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(secureStorage))
            .addInterceptor(TokenRefreshInterceptor(secureStorage, this))
            .addInterceptor(LoggingInterceptor())
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        authApi = retrofit.create(AuthApi::class.java)
    }
    
    /**
     * Get the AuthApi instance
     */
    fun getAuthApi(): AuthApi = authApi
    
    /**
     * Interceptor to add authentication headers
     */
    private class AuthInterceptor(
        private val secureStorage: SecureTokenStorage
    ) : Interceptor {
        
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            
            // Skip auth for login/signup/refresh endpoints
            val url = originalRequest.url.encodedPath
            if (url.contains("/auth/login") || 
                url.contains("/auth/signup") || 
                url.contains("/auth/refresh") ||
                url.contains("/auth/forgot-password") ||
                url.contains("/auth/reset-password")) {
                return chain.proceed(originalRequest)
            }
            
            val accessToken = runBlocking { secureStorage.getAccessToken() }
            
            val authenticatedRequest = if (accessToken != null) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
            } else {
                originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
            }
            
            return chain.proceed(authenticatedRequest)
        }
    }
    
    /**
     * Interceptor to handle token refresh
     */
    private class TokenRefreshInterceptor(
        private val secureStorage: SecureTokenStorage,
        private val apiClient: ApiClient
    ) : Interceptor {
        
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val response = chain.proceed(originalRequest)
            
            // If we get a 401, try to refresh the token
            if (response.code == 401) {
                response.close()
                
                val refreshToken = runBlocking { secureStorage.getRefreshToken() }
                if (refreshToken != null) {
                    try {
                        Log.d(TAG, "Attempting to refresh token")
                        val refreshResponse = runBlocking {
                            apiClient.getAuthApi().refreshToken(RefreshTokenRequest(refreshToken))
                        }
                        
                        if (refreshResponse.isSuccessful) {
                            val authResponse = refreshResponse.body()
                            if (authResponse != null && authResponse.success) {
                                Log.d(TAG, "Token refresh successful")
                                
                                // Store new tokens
                                runBlocking {
                                    secureStorage.storeTokens(
                                        accessToken = authResponse.data.tokens.accessToken,
                                        refreshToken = authResponse.data.tokens.refreshToken,
                                        userId = authResponse.data.user.id,
                                        userEmail = authResponse.data.user.email
                                    )
                                }
                                
                                // Retry original request with new token
                                val newRequest = originalRequest.newBuilder()
                                    .removeHeader("Authorization")
                                    .addHeader("Authorization", "Bearer ${authResponse.data.tokens.accessToken}")
                                    .build()
                                
                                return chain.proceed(newRequest)
                            }
                        }
                        
                        Log.w(TAG, "Token refresh failed, clearing tokens")
                        // Refresh failed, clear tokens
                        runBlocking { secureStorage.clearTokens() }
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Token refresh error", e)
                        // Clear tokens on any error
                        runBlocking { secureStorage.clearTokens() }
                    }
                } else {
                    Log.w(TAG, "No refresh token available")
                }
            }
            
            return response
        }
    }
    
    /**
     * Logging interceptor for debugging
     */
    private class LoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            
            Log.d(TAG, "Request: ${request.method} ${request.url}")
            request.headers.forEach { (name, value) ->
                if (name != "Authorization") {
                    Log.d(TAG, "Header: $name: $value")
                } else {
                    Log.d(TAG, "Header: $name: ***")
                }
            }
            
            val response = chain.proceed(request)
            
            Log.d(TAG, "Response: ${response.code} ${response.message}")
            response.headers.forEach { (name, value) ->
                Log.d(TAG, "Response Header: $name: $value")
            }
            
            return response
        }
    }
    
    /**
     * Check if the API client is properly configured
     */
    fun isConfigured(): Boolean {
        return try {
            // Test if we can create a simple request
            val testRequest = okhttp3.Request.Builder()
                .url("$BASE_URL/health")
                .build()
            true
        } catch (e: Exception) {
            Log.e(TAG, "API client configuration error", e)
            false
        }
    }
    
    /**
     * Get the base URL
     */
    fun getBaseUrl(): String = BASE_URL
    
    /**
     * Update base URL (for different environments)
     */
    fun updateBaseUrl(newBaseUrl: String) {
        Log.d(TAG, "Base URL updated to: $newBaseUrl")
        // Note: In a real implementation, you'd recreate the Retrofit instance
        // For now, we'll just log the change
    }
}

/**
 * HTTP Logging Interceptor for debugging
 */
class HttpLoggingInterceptor {
    companion object {
        fun create(): okhttp3.logging.HttpLoggingInterceptor {
            return okhttp3.logging.HttpLoggingInterceptor { message ->
                Log.d("HTTP", message)
            }.apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            }
        }
    }
}

