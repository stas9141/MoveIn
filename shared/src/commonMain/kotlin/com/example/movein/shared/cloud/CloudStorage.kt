package com.example.movein.shared.cloud

import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.UserData
import kotlinx.coroutines.flow.Flow

/**
 * Interface for cloud-based data storage and synchronization
 */
expect class CloudStorage {
    /**
     * Authentication state flow
     */
    val authState: Flow<AuthState>
    
    /**
     * Sync status flow
     */
    val syncStatus: Flow<SyncStatus>
    
    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<Unit>
    
    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String): Result<Unit>
    
    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(): Result<Unit>
    
    /**
     * Sign out
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String?
    
    /**
     * Save user data to cloud
     */
    suspend fun saveUserData(userData: UserData): Result<Unit>
    
    /**
     * Load user data from cloud
     */
    suspend fun loadUserData(): Result<UserData?>
    
    /**
     * Save checklist data to cloud
     */
    suspend fun saveChecklistData(checklistData: ChecklistData): Result<Unit>
    
    /**
     * Load checklist data from cloud
     */
    suspend fun loadChecklistData(): Result<ChecklistData?>
    
    /**
     * Save defects to cloud
     */
    suspend fun saveDefects(defects: List<Defect>): Result<Unit>
    
    /**
     * Load defects from cloud
     */
    suspend fun loadDefects(): Result<List<Defect>>
    
    /**
     * Enable real-time synchronization
     */
    suspend fun enableRealtimeSync(): Result<Unit>
    
    /**
     * Disable real-time synchronization
     */
    suspend fun disableRealtimeSync(): Result<Unit>
    
    /**
     * Force sync all data
     */
    suspend fun forceSync(): Result<Unit>
}

/**
 * Authentication state
 */
data class AuthState(
    val isAuthenticated: Boolean,
    val userId: String? = null,
    val email: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Sync status
 */
data class SyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val error: String? = null,
    val isOnline: Boolean = true
)
