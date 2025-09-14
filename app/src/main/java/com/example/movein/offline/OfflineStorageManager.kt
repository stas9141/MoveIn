package com.example.movein.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.UserData
import com.example.movein.shared.storage.AppStorage
import com.example.movein.shared.cloud.CloudStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages offline data storage and automatic sync when online
 */
class OfflineStorageManager(
    private val context: Context,
    private val cloudStorage: CloudStorage,
    private val localStorage: AppStorage
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Network state
    private val _isOnline = MutableStateFlow(isNetworkAvailable())
    val isOnline: Flow<Boolean> = _isOnline.asStateFlow()
    
    // Pending operations
    private val pendingOperations = ConcurrentHashMap<String, SyncOperation>()
    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: Flow<Int> = _pendingSyncCount.asStateFlow()
    
    // Sync status
    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: Flow<SyncStatus> = _syncStatus.asStateFlow()
    
    // Sync job
    private var syncJob: Job? = null
    
    init {
        setupNetworkMonitoring()
        startPeriodicSync()
    }
    
    private fun setupNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                _syncStatus.value = _syncStatus.value.copy(isOnline = true, error = null)
                
                // Trigger sync when back online
                scope.launch {
                    syncPendingOperations()
                }
            }
            
            override fun onLost(network: Network) {
                _isOnline.value = false
                _syncStatus.value = _syncStatus.value.copy(isOnline = false)
            }
        })
    }
    
    private fun startPeriodicSync() {
        syncJob = scope.launch {
            while (isActive) {
                delay(30_000) // Check every 30 seconds
                if (_isOnline.value && pendingOperations.isNotEmpty()) {
                    syncPendingOperations()
                }
            }
        }
    }
    
    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    suspend fun saveUserData(userData: UserData): Result<Unit> {
        return try {
            // Always save locally first
            localStorage.saveUserData(userData)
            
            if (_isOnline.value) {
                // Try to sync to cloud
                val result = cloudStorage.saveUserData(userData)
                if (result.isFailure) {
                    // If cloud save fails, queue for later sync
                    queueSyncOperation(SyncOperationType.SAVE_USER_DATA, userData)
                }
                result
            } else {
                // Queue for sync when online
                queueSyncOperation(SyncOperationType.SAVE_USER_DATA, userData)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loadUserData(): Result<UserData?> {
        return try {
            // Load from local storage first
            val localData = localStorage.loadUserData()
            
            // If online, try to sync from cloud
            if (_isOnline.value) {
                val cloudResult = cloudStorage.loadUserData()
                if (cloudResult.isSuccess) {
                    val cloudData = cloudResult.getOrNull()
                    if (cloudData != null) {
                        // Update local storage with cloud data
                        localStorage.saveUserData(cloudData)
                        return Result.success(cloudData)
                    }
                }
            }
            
            Result.success(localData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveChecklistData(checklistData: ChecklistData): Result<Unit> {
        return try {
            // Always save locally first
            localStorage.saveChecklistData(checklistData)
            
            if (_isOnline.value) {
                // Try to sync to cloud
                val result = cloudStorage.saveChecklistData(checklistData)
                if (result.isFailure) {
                    // If cloud save fails, queue for later sync
                    queueSyncOperation(SyncOperationType.SAVE_CHECKLIST_DATA, checklistData)
                }
                result
            } else {
                // Queue for sync when online
                queueSyncOperation(SyncOperationType.SAVE_CHECKLIST_DATA, checklistData)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loadChecklistData(): Result<ChecklistData?> {
        return try {
            // Load from local storage first
            val localData = localStorage.loadChecklistData()
            
            // If online, try to sync from cloud
            if (_isOnline.value) {
                val cloudResult = cloudStorage.loadChecklistData()
                if (cloudResult.isSuccess) {
                    val cloudData = cloudResult.getOrNull()
                    if (cloudData != null) {
                        // Update local storage with cloud data
                        localStorage.saveChecklistData(cloudData)
                        return Result.success(cloudData)
                    }
                }
            }
            
            Result.success(localData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveDefects(defects: List<Defect>): Result<Unit> {
        return try {
            // Always save locally first
            localStorage.saveDefects(defects)
            
            if (_isOnline.value) {
                // Try to sync to cloud
                val result = cloudStorage.saveDefects(defects)
                if (result.isFailure) {
                    // If cloud save fails, queue for later sync
                    queueSyncOperation(SyncOperationType.SAVE_DEFECTS, defects)
                }
                result
            } else {
                // Queue for sync when online
                queueSyncOperation(SyncOperationType.SAVE_DEFECTS, defects)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loadDefects(): Result<List<Defect>> {
        return try {
            // Load from local storage first
            val localData = localStorage.loadDefects()
            
            // If online, try to sync from cloud
            if (_isOnline.value) {
                val cloudResult = cloudStorage.loadDefects()
                if (cloudResult.isSuccess) {
                    val cloudData = cloudResult.getOrNull() ?: emptyList()
                    // Update local storage with cloud data
                    localStorage.saveDefects(cloudData)
                    return Result.success(cloudData)
                }
            }
            
            Result.success(localData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun forceSync(): Result<Unit> {
        return try {
            if (!_isOnline.value) {
                return Result.failure(Exception("No internet connection"))
            }
            
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true, error = null)
            
            val result = syncPendingOperations()
            
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                lastSyncTime = System.currentTimeMillis(),
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
            
            result
        } catch (e: Exception) {
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = false,
                error = e.message
            )
            Result.failure(e)
        }
    }
    
    suspend fun clearOfflineData(): Result<Unit> {
        return try {
            pendingOperations.clear()
            _pendingSyncCount.value = 0
            _syncStatus.value = _syncStatus.value.copy(pendingOperations = 0)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPendingSyncOperations(): List<SyncOperation> {
        return pendingOperations.values.toList()
    }
    
    private fun queueSyncOperation(type: SyncOperationType, data: Any) {
        val operation = SyncOperation(
            id = UUID.randomUUID().toString(),
            type = type,
            data = data,
            timestamp = System.currentTimeMillis()
        )
        
        pendingOperations[operation.id] = operation
        _pendingSyncCount.value = pendingOperations.size
        _syncStatus.value = _syncStatus.value.copy(pendingOperations = pendingOperations.size)
    }
    
    private suspend fun syncPendingOperations(): Result<Unit> {
        if (!_isOnline.value || pendingOperations.isEmpty()) {
            return Result.success(Unit)
        }
        
        val operations = pendingOperations.values.toList()
        val failedOperations = mutableListOf<SyncOperation>()
        
        for (operation in operations) {
            try {
                val result = when (operation.type) {
                    SyncOperationType.SAVE_USER_DATA -> {
                        cloudStorage.saveUserData(operation.data as UserData)
                    }
                    SyncOperationType.SAVE_CHECKLIST_DATA -> {
                        cloudStorage.saveChecklistData(operation.data as ChecklistData)
                    }
                    SyncOperationType.SAVE_DEFECTS -> {
                        cloudStorage.saveDefects(operation.data as List<Defect>)
                    }
                    else -> Result.success(Unit)
                }
                
                if (result.isSuccess) {
                    // Remove successful operation
                    pendingOperations.remove(operation.id)
                } else {
                    // Increment retry count
                    val updatedOperation = operation.copy(retryCount = operation.retryCount + 1)
                    if (updatedOperation.retryCount < 3) {
                        pendingOperations[operation.id] = updatedOperation
                        failedOperations.add(updatedOperation)
                    } else {
                        // Remove after 3 failed attempts
                        pendingOperations.remove(operation.id)
                    }
                }
            } catch (e: Exception) {
                val updatedOperation = operation.copy(retryCount = operation.retryCount + 1)
                if (updatedOperation.retryCount < 3) {
                    pendingOperations[operation.id] = updatedOperation
                    failedOperations.add(updatedOperation)
                } else {
                    pendingOperations.remove(operation.id)
                }
            }
        }
        
        _pendingSyncCount.value = pendingOperations.size
        _syncStatus.value = _syncStatus.value.copy(pendingOperations = pendingOperations.size)
        
        return if (failedOperations.isEmpty()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Some operations failed to sync"))
        }
    }
    
    fun cleanup() {
        syncJob?.cancel()
        scope.cancel()
    }
}

/**
 * Sync status information
 */
data class SyncStatus(
    val isOnline: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val pendingOperations: Int = 0,
    val error: String? = null
)

/**
 * Represents a pending sync operation
 */
data class SyncOperation(
    val id: String,
    val type: SyncOperationType,
    val data: Any,
    val timestamp: Long,
    val retryCount: Int = 0
)

/**
 * Types of sync operations
 */
enum class SyncOperationType {
    SAVE_USER_DATA,
    SAVE_CHECKLIST_DATA,
    SAVE_DEFECTS,
    DELETE_DEFECT,
    UPDATE_DEFECT
}
