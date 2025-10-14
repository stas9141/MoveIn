package com.example.movein.auth

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Session manager for monitoring user sessions and handling timeouts
 */
class SessionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SessionManager"
        private const val SESSION_CHECK_INTERVAL = 60 * 1000L // 1 minute
        private const val SESSION_WARNING_TIME = 5 * 60 * 1000L // 5 minutes before expiry
        private const val DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000L // 30 minutes
    }
    
    private val secureStorage = SecureTokenStorage(context)
    private val authManager = AuthManager(context)
    
    private val _sessionState = MutableStateFlow(SessionState.ACTIVE)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    private val _timeUntilExpiry = MutableStateFlow(0L)
    val timeUntilExpiry: StateFlow<Long> = _timeUntilExpiry.asStateFlow()
    
    private var sessionJob: Job? = null
    private var sessionTimeout = DEFAULT_SESSION_TIMEOUT
    
    /**
     * Session state enumeration
     */
    enum class SessionState {
        ACTIVE,
        WARNING,
        EXPIRED,
        INACTIVE
    }
    
    /**
     * Start session monitoring
     */
    fun startSessionMonitoring() {
        Log.d(TAG, "Starting session monitoring")
        
        if (sessionJob?.isActive == true) {
            Log.d(TAG, "Session monitoring already active")
            return
        }
        
        sessionJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    checkSessionStatus()
                    delay(SESSION_CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in session monitoring", e)
                    delay(SESSION_CHECK_INTERVAL)
                }
            }
        }
    }
    
    /**
     * Stop session monitoring
     */
    fun stopSessionMonitoring() {
        Log.d(TAG, "Stopping session monitoring")
        sessionJob?.cancel()
        sessionJob = null
        _sessionState.value = SessionState.INACTIVE
    }
    
    /**
     * Check current session status
     */
    private suspend fun checkSessionStatus() {
        try {
            val isLoggedIn = secureStorage.isUserLoggedIn()
            if (!isLoggedIn) {
                Log.d(TAG, "User not logged in, setting session to inactive")
                _sessionState.value = SessionState.INACTIVE
                _timeUntilExpiry.value = 0L
                return
            }
            
            val isExpired = secureStorage.isAccessTokenExpired()
            if (isExpired) {
                Log.d(TAG, "Access token expired, attempting refresh")
                _sessionState.value = SessionState.EXPIRED
                
                // Try to refresh token
                val refreshResult = authManager.refreshToken()
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
                        
                        Log.d(TAG, "Token refreshed successfully")
                        _sessionState.value = SessionState.ACTIVE
                        updateTimeUntilExpiry()
                    } else {
                        Log.w(TAG, "Token refresh failed: no data returned")
                        _sessionState.value = SessionState.EXPIRED
                    }
                } else {
                    Log.w(TAG, "Token refresh failed: ${refreshResult.exceptionOrNull()?.message}")
                    _sessionState.value = SessionState.EXPIRED
                }
            } else {
                // Token is still valid, check if we're close to expiry
                val timeUntilExpiry = secureStorage.getTimeUntilExpiry()
                _timeUntilExpiry.value = timeUntilExpiry
                
                if (timeUntilExpiry <= SESSION_WARNING_TIME) {
                    Log.d(TAG, "Session warning: ${timeUntilExpiry / 1000} seconds until expiry")
                    _sessionState.value = SessionState.WARNING
                } else {
                    _sessionState.value = SessionState.ACTIVE
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking session status", e)
            _sessionState.value = SessionState.EXPIRED
        }
    }
    
    /**
     * Update time until expiry
     */
    private suspend fun updateTimeUntilExpiry() {
        try {
            val timeUntilExpiry = secureStorage.getTimeUntilExpiry()
            _timeUntilExpiry.value = timeUntilExpiry
        } catch (e: Exception) {
            Log.e(TAG, "Error updating time until expiry", e)
            _timeUntilExpiry.value = 0L
        }
    }
    
    /**
     * Extend session by refreshing token
     */
    suspend fun extendSession(): Result<Unit> {
        return try {
            Log.d(TAG, "Attempting to extend session")
            
            val result = authManager.refreshToken()
            if (result.isSuccess) {
                val authData = result.getOrNull()
                if (authData != null) {
                    // Store new tokens
                    secureStorage.storeTokens(
                        accessToken = authData.accessToken,
                        refreshToken = authData.refreshToken,
                        userId = authData.user.id,
                        userEmail = authData.user.email
                    )
                    
                    _sessionState.value = SessionState.ACTIVE
                    updateTimeUntilExpiry()
                    
                    Log.d(TAG, "Session extended successfully")
                    Result.success(Unit)
                } else {
                    Log.w(TAG, "Session extension failed: no data returned")
                    Result.failure(Exception("Failed to extend session"))
                }
            } else {
                Log.w(TAG, "Session extension failed: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to extend session"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Session extension error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Force session expiry
     */
    suspend fun expireSession() {
        Log.d(TAG, "Forcing session expiry")
        try {
            secureStorage.clearTokens()
            _sessionState.value = SessionState.EXPIRED
            _timeUntilExpiry.value = 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error expiring session", e)
        }
    }
    
    /**
     * Get formatted time until expiry
     */
    fun getFormattedTimeUntilExpiry(): String {
        val timeUntilExpiry = _timeUntilExpiry.value
        if (timeUntilExpiry <= 0) return "Expired"
        
        val minutes = timeUntilExpiry / (60 * 1000)
        val seconds = (timeUntilExpiry % (60 * 1000)) / 1000
        
        return when {
            minutes > 0 -> "${minutes}m ${seconds}s"
            seconds > 0 -> "${seconds}s"
            else -> "Expired"
        }
    }
    
    /**
     * Check if session is active
     */
    fun isSessionActive(): Boolean {
        return _sessionState.value == SessionState.ACTIVE
    }
    
    /**
     * Check if session is in warning state
     */
    fun isSessionInWarning(): Boolean {
        return _sessionState.value == SessionState.WARNING
    }
    
    /**
     * Check if session is expired
     */
    fun isSessionExpired(): Boolean {
        return _sessionState.value == SessionState.EXPIRED
    }
    
    /**
     * Get current session state
     */
    fun getCurrentSessionState(): SessionState {
        return _sessionState.value
    }
    
    /**
     * Set custom session timeout
     */
    fun setSessionTimeout(timeoutMs: Long) {
        Log.d(TAG, "Setting session timeout to ${timeoutMs}ms")
        sessionTimeout = timeoutMs
    }
    
    /**
     * Get session timeout
     */
    fun getSessionTimeout(): Long {
        return sessionTimeout
    }
    
    /**
     * Reset session state to active
     */
    fun resetSessionState() {
        Log.d(TAG, "Resetting session state to active")
        _sessionState.value = SessionState.ACTIVE
    }
    
    /**
     * Get session statistics
     */
    suspend fun getSessionStats(): SessionStats {
        return try {
            val isLoggedIn = secureStorage.isUserLoggedIn()
            val lastLoginTime = secureStorage.getLastLoginTime()
            val timeUntilExpiry = secureStorage.getTimeUntilExpiry()
            val isExpired = secureStorage.isAccessTokenExpired()
            
            SessionStats(
                isLoggedIn = isLoggedIn,
                lastLoginTime = lastLoginTime,
                timeUntilExpiry = timeUntilExpiry,
                isExpired = isExpired,
                currentState = _sessionState.value,
                sessionTimeout = sessionTimeout
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting session stats", e)
            SessionStats(
                isLoggedIn = false,
                lastLoginTime = 0L,
                timeUntilExpiry = 0L,
                isExpired = true,
                currentState = SessionState.INACTIVE,
                sessionTimeout = sessionTimeout
            )
        }
    }
}

/**
 * Session statistics data class
 */
data class SessionStats(
    val isLoggedIn: Boolean,
    val lastLoginTime: Long,
    val timeUntilExpiry: Long,
    val isExpired: Boolean,
    val currentState: SessionManager.SessionState,
    val sessionTimeout: Long
)

