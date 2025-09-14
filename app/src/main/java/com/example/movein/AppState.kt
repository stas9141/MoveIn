package com.example.movein

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.UserData
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.storage.AppStorage
import com.example.movein.shared.cloud.CloudStorage
import com.example.movein.shared.cloud.AuthState
import com.example.movein.shared.cloud.SyncStatus
import com.example.movein.offline.OfflineStorageManager
import com.example.movein.offline.SyncStatus as OfflineSyncStatus
import com.example.movein.navigation.Screen
import com.example.movein.utils.getTodayString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppState(
    private val appStorage: AppStorage?,
    private val cloudStorage: CloudStorage?,
    private val offlineStorage: OfflineStorageManager?,
    private val coroutineScope: CoroutineScope
) {
    var currentScreen by mutableStateOf<Screen>(Screen.Welcome)
        private set
    
    private val navigationStack = mutableListOf<Screen>()
    
    var userData by mutableStateOf<UserData?>(null)
        private set
    
    var checklistData by mutableStateOf<ChecklistData?>(null)
        private set
    
    var selectedTask by mutableStateOf<ChecklistItem?>(null)
        private set
    
    var defects by mutableStateOf<List<Defect>>(appStorage?.loadDefects() ?: emptyList())
        private set
    
    var selectedDefect by mutableStateOf<Defect?>(null)
        private set
    
    var isDarkMode by mutableStateOf(false)
        private set
    
    // Cloud sync state
    var authState by mutableStateOf(AuthState(false))
        private set
    
    var syncStatus by mutableStateOf(SyncStatus())
        private set
    
    // Offline sync state
    var offlineSyncStatus by mutableStateOf(OfflineSyncStatus())
        private set
    
    var isCloudSyncEnabled by mutableStateOf(false)
        private set

    init {
        // Load saved data when AppState is created
        loadSavedData()
        
        // Initialize offline sync if available
        offlineStorage?.let { offline ->
            coroutineScope.launch {
                offline.syncStatus.collect { syncStatus ->
                    this@AppState.offlineSyncStatus = syncStatus
                }
            }
        }
    }
    
    fun initializeCloudSync() {
        // Observe cloud storage state
        observeCloudStorage()
    }
    
    private fun observeCloudStorage() {
        if (cloudStorage == null) {
            // If cloud storage is not available, set default states
            authState = AuthState(false, error = "Cloud storage not available")
            syncStatus = SyncStatus(error = "Cloud storage not available")
            return
        }
        
        coroutineScope.launch {
            try {
                cloudStorage.authState.collect { authState ->
                    this@AppState.authState = authState
                    if (authState.isAuthenticated && !isCloudSyncEnabled) {
                        enableCloudSync()
                    } else if (!authState.isAuthenticated && isCloudSyncEnabled) {
                        disableCloudSync()
                    }
                }
            } catch (e: Exception) {
                // Handle cloud storage errors gracefully
                this@AppState.authState = AuthState(false, error = e.message)
            }
        }
        
        coroutineScope.launch {
            try {
                cloudStorage.syncStatus.collect { syncStatus ->
                    this@AppState.syncStatus = syncStatus
                }
            } catch (e: Exception) {
                // Handle sync status errors gracefully
                this@AppState.syncStatus = SyncStatus(error = e.message)
            }
        }
    }

    private fun loadSavedData() {
        try {
            // Load user data using offline storage if available, otherwise fallback to local storage
            val savedUserData = if (offlineStorage != null) {
                coroutineScope.launch {
                    offlineStorage.loadUserData().getOrNull()
                }
                appStorage?.loadUserData() // Fallback to local storage for immediate loading
            } else {
                appStorage?.loadUserData()
            }
            
            if (savedUserData != null) {
                userData = savedUserData
                // Load checklist data
                val savedChecklistData = if (offlineStorage != null) {
                    coroutineScope.launch {
                        offlineStorage.loadChecklistData().getOrNull()
                    }
                    appStorage?.loadChecklistData() // Fallback to local storage for immediate loading
                } else {
                    appStorage?.loadChecklistData()
                }
                
                if (savedChecklistData != null) {
                    checklistData = savedChecklistData
                } else {
                    // Generate fresh checklist if none saved
                    checklistData = com.example.movein.shared.data.ChecklistDataGenerator.generatePersonalizedChecklist(savedUserData)
                }
            }
        } catch (e: Exception) {
            // If there's an error loading data, initialize with defaults
            userData = null
            checklistData = null
        }
    }

    fun navigateTo(screen: Screen) {
        // Add current screen to navigation stack if it's not already there
        if (navigationStack.isEmpty() || navigationStack.last() != currentScreen) {
            navigationStack.add(currentScreen)
        }
        currentScreen = screen
    }
    
    fun navigateBack(): Boolean {
        return if (navigationStack.isNotEmpty()) {
            currentScreen = navigationStack.removeAt(navigationStack.size - 1)
            true
        } else {
            false
        }
    }
    
    fun canNavigateBack(): Boolean {
        return navigationStack.isNotEmpty()
    }
    
    fun clearNavigationStack() {
        navigationStack.clear()
    }

    fun initializeUserData(data: UserData) {
        userData = data
        // Generate personalized checklist based on user data
        checklistData = com.example.movein.shared.data.ChecklistDataGenerator.generatePersonalizedChecklist(data)
        
        // Save to offline storage if available, otherwise fallback to local storage
        if (offlineStorage != null) {
            coroutineScope.launch {
                offlineStorage.saveUserData(data)
                offlineStorage.saveChecklistData(checklistData!!)
            }
        } else {
            // Fallback to local storage
            appStorage?.saveUserData(data)
            appStorage?.saveChecklistData(checklistData!!)
        }
    }

    fun updateTask(task: ChecklistItem) {
        val currentUserData = userData ?: return
        val currentData = checklistData ?: return
        
        // Remove the task from all lists first
        val firstWeekWithoutTask = currentData.firstWeek.filter { it.id != task.id }
        val firstMonthWithoutTask = currentData.firstMonth.filter { it.id != task.id }
        val firstYearWithoutTask = currentData.firstYear.filter { it.id != task.id }
        
        // Determine which host the updated task should go to
        val targetHost = getTaskHost(task)
        
        // Add the task to the correct host
        val updatedData = when (targetHost) {
            0 -> ChecklistData(
                firstWeek = firstWeekWithoutTask + task,
                firstMonth = firstMonthWithoutTask,
                firstYear = firstYearWithoutTask
            )
            1 -> ChecklistData(
                firstWeek = firstWeekWithoutTask,
                firstMonth = firstMonthWithoutTask + task,
                firstYear = firstYearWithoutTask
            )
            2 -> ChecklistData(
                firstWeek = firstWeekWithoutTask,
                firstMonth = firstMonthWithoutTask,
                firstYear = firstYearWithoutTask + task
            )
            else -> ChecklistData(
                firstWeek = firstWeekWithoutTask + task,
                firstMonth = firstMonthWithoutTask,
                firstYear = firstYearWithoutTask
            )
        }
        
        // Update the checklist data
        checklistData = updatedData
        
        // Save to offline storage if available, otherwise fallback to local storage
        if (offlineStorage != null) {
            coroutineScope.launch {
                offlineStorage.saveChecklistData(checklistData!!)
            }
        } else {
            // Fallback to local storage
            appStorage?.saveChecklistData(checklistData!!)
        }
    }

    fun selectTask(task: ChecklistItem) {
        selectedTask = task
    }
    
    fun selectDefect(defect: Defect) {
        selectedDefect = defect
    }
    
    fun clearSelectedDefect() {
        selectedDefect = null
    }
    
    fun addDefect(defect: Defect) {
        defects = defects + defect
        
        // Save to offline storage if available, otherwise fallback to local storage
        if (offlineStorage != null) {
            coroutineScope.launch {
                offlineStorage.saveDefects(defects)
            }
        } else {
            // Fallback to local storage
            appStorage?.saveDefects(defects)
        }
    }
    
    fun updateDefect(updatedDefect: Defect) {
        // Check if status is changing to CLOSED and set closedAt date
        val originalDefect = defects.find { it.id == updatedDefect.id }
        val finalDefect = if (originalDefect?.status != DefectStatus.CLOSED && 
                             updatedDefect.status == DefectStatus.CLOSED) {
            updatedDefect.copy(closedAt = getTodayString())
        } else {
            updatedDefect
        }
        
        defects = defects.map { if (it.id == finalDefect.id) finalDefect else it }
        
        // Save to offline storage if available, otherwise fallback to local storage
        if (offlineStorage != null) {
            coroutineScope.launch {
                offlineStorage.saveDefects(defects)
            }
        } else {
            // Fallback to local storage
            appStorage?.saveDefects(defects)
        }
    }
    
    fun deleteDefect(defectId: String) {
        defects = defects.filter { it.id != defectId }
        
        // Save to offline storage if available, otherwise fallback to local storage
        if (offlineStorage != null) {
            coroutineScope.launch {
                offlineStorage.saveDefects(defects)
            }
        } else {
            // Fallback to local storage
            appStorage?.saveDefects(defects)
        }
    }
    
    fun addTask(newTask: ChecklistItem) {
        val currentData = checklistData ?: return
        
        // Determine which host to add the task to based on due date
        val targetHost = getTaskHost(newTask)
        
        // Add the task to the appropriate list
        val updatedData = when (targetHost) {
            0 -> currentData.copy(firstWeek = currentData.firstWeek + newTask)
            1 -> currentData.copy(firstMonth = currentData.firstMonth + newTask)
            2 -> currentData.copy(firstYear = currentData.firstYear + newTask)
            else -> currentData.copy(firstWeek = currentData.firstWeek + newTask)
        }
        
        checklistData = updatedData
        // Save to storage
        appStorage?.saveChecklistData(checklistData!!)
        
        // Sync to cloud if authenticated
        if (authState.isAuthenticated && cloudStorage != null) {
            coroutineScope.launch {
                cloudStorage.saveChecklistData(checklistData!!)
            }
        }
    }
    
    // Helper function to determine which host a task should belong to based on due date
    private fun getTaskHost(task: ChecklistItem): Int {
        return when {
            task.dueDate == null -> 0 // No due date = First Week
            else -> {
                val dueDate = task.dueDate
                val taskDate = if (dueDate != null) parseDate(dueDate) else null
                val today = java.time.LocalDate.now()
                
                when {
                    taskDate == null -> 0 // Invalid date = First Week
                    taskDate.isBefore(today.plusDays(7)) -> 0 // Within a week = First Week
                    taskDate.isBefore(today.plusMonths(1)) -> 1 // Within a month = First Month
                    else -> 2 // Beyond a month = First Year
                }
            }
        }
    }
    
    // Helper function to parse date from MM/dd/yyyy format
    private fun parseDate(dateString: String): java.time.LocalDate? {
        return try {
            val parts = dateString.split("/")
            if (parts.size == 3) {
                val month = parts[0].toInt()
                val day = parts[1].toInt()
                val year = parts[2].toInt()
                java.time.LocalDate.of(year, month, day)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }
    
    fun reorganizeTasksByDueDate() {
        val currentData = checklistData ?: return
        
        // Collect all tasks from all hosts
        val allTasks = currentData.firstWeek + currentData.firstMonth + currentData.firstYear
        
        // Clear all hosts
        val firstWeek = mutableListOf<ChecklistItem>()
        val firstMonth = mutableListOf<ChecklistItem>()
        val firstYear = mutableListOf<ChecklistItem>()
        
        // Reorganize tasks based on their due dates
        allTasks.forEach { task ->
            val targetHost = getTaskHost(task)
            when (targetHost) {
                0 -> firstWeek.add(task)
                1 -> firstMonth.add(task)
                2 -> firstYear.add(task)
                else -> firstWeek.add(task)
            }
        }
        
        // Update checklist data
        checklistData = ChecklistData(
            firstWeek = firstWeek,
            firstMonth = firstMonth,
            firstYear = firstYear
        )
        
        // Save to storage
        appStorage?.saveChecklistData(checklistData!!)
        
        // Sync to cloud if authenticated
        if (authState.isAuthenticated && cloudStorage != null) {
            coroutineScope.launch {
                cloudStorage.saveChecklistData(checklistData!!)
            }
        }
    }
    
    fun clearAllData() {
        userData = null
        checklistData = null
        defects = emptyList()
        selectedTask = null
        selectedDefect = null
        // Clear from storage
        appStorage?.saveUserData(UserData()) // Save default empty data
        appStorage?.saveChecklistData(ChecklistData(emptyList(), emptyList(), emptyList()))
        appStorage?.saveDefects(emptyList())
        
        // Clear from cloud if authenticated
        if (authState.isAuthenticated && cloudStorage != null) {
            coroutineScope.launch {
                cloudStorage.saveUserData(UserData())
                cloudStorage.saveChecklistData(ChecklistData(emptyList(), emptyList(), emptyList()))
                cloudStorage.saveDefects(emptyList())
            }
        }
    }
    
    // Cloud sync methods
    suspend fun signIn(email: String, password: String): Result<Unit> {
        return cloudStorage?.signIn(email, password) ?: Result.failure(Exception("Cloud storage not available"))
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return cloudStorage?.signUp(email, password) ?: Result.failure(Exception("Cloud storage not available"))
    }

    suspend fun signInWithGoogle(): Result<Unit> {
        return cloudStorage?.signInWithGoogle() ?: Result.failure(Exception("Cloud storage not available"))
    }

    suspend fun signOut(): Result<Unit> {
        return cloudStorage?.signOut() ?: Result.failure(Exception("Cloud storage not available"))
    }
    
    fun clearAuthError() {
        authState = authState.copy(error = null)
    }
    
    private suspend fun enableCloudSync() {
        isCloudSyncEnabled = true
        cloudStorage?.enableRealtimeSync()
        
        // Sync local data to cloud
        syncToCloud()
    }
    
    private suspend fun disableCloudSync() {
        isCloudSyncEnabled = false
        cloudStorage?.disableRealtimeSync()
    }
    
    suspend fun syncToCloud() {
        if (!authState.isAuthenticated || cloudStorage == null) return
        
        try {
            // Sync user data
            userData?.let { cloudStorage.saveUserData(it) }
            
            // Sync checklist data
            checklistData?.let { cloudStorage.saveChecklistData(it) }
            
            // Sync defects
            cloudStorage.saveDefects(defects)
        } catch (e: Exception) {
            // Handle sync error
            println("Sync to cloud failed: ${e.message}")
        }
    }
    
    suspend fun syncFromCloud() {
        if (!authState.isAuthenticated) return
        
        try {
            // Load user data from cloud
            val cloudUserData = cloudStorage?.loadUserData()?.getOrNull()
            if (cloudUserData != null) {
                userData = cloudUserData
                appStorage?.saveUserData(cloudUserData)
            }
            
            // Load checklist data from cloud
            val cloudChecklistData = cloudStorage?.loadChecklistData()?.getOrNull()
            if (cloudChecklistData != null) {
                checklistData = cloudChecklistData
                appStorage?.saveChecklistData(cloudChecklistData)
            }
            
            // Load defects from cloud
            val cloudDefects = cloudStorage?.loadDefects()?.getOrNull()
            if (cloudDefects != null) {
                defects = cloudDefects
                appStorage?.saveDefects(cloudDefects)
            }
        } catch (e: Exception) {
            // Handle sync error
            println("Sync from cloud failed: ${e.message}")
        }
    }
    
    suspend fun forceSync(): Result<Unit> {
        return cloudStorage?.forceSync() ?: Result.failure(Exception("Cloud storage not available"))
    }
}
