package com.example.movein

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.UserData
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.ChecklistDataGenerator
import com.example.movein.shared.storage.AppStorage
import com.example.movein.shared.cloud.CloudStorage
import com.example.movein.shared.cloud.AuthState
import com.example.movein.shared.cloud.SyncStatus
import com.example.movein.offline.OfflineStorageManager
import com.example.movein.offline.SyncStatus as OfflineSyncStatus
import com.example.movein.navigation.Screen
import com.example.movein.utils.getTodayString
import com.example.movein.auth.SecureTokenStorage
import com.example.movein.utils.FileManager
import com.example.movein.shared.data.FileAttachment
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AppState(
    private val appStorage: AppStorage?,
    private val cloudStorage: CloudStorage?,
    private val offlineStorage: OfflineStorageManager?,
    private val coroutineScope: CoroutineScope,
    private val secureTokenStorage: SecureTokenStorage? = null,
    private val fileManager: FileManager? = null
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
    
    var defects by mutableStateOf<List<Defect>>(emptyList())
        private set
    
    var selectedDefect by mutableStateOf<Defect?>(null)
        private set
    
    var lastAddedTaskTab by mutableStateOf<Int?>(null)
        private set
    
    var pendingDefectDueDate by mutableStateOf<String?>(null)
        private set
    
    var isDarkMode by mutableStateOf(false)
        private set
    
    // Authentication state
    var authState by mutableStateOf(AuthState(false))
    
    // Anonymous user support
    var isAnonymousUser by mutableStateOf(true)
    
    // Registration prompt tracking
    var hasShownRegistrationPrompt by mutableStateOf(false)
    var tasksCreatedCount by mutableStateOf(0)
    var defectsCreatedCount by mutableStateOf(0)
    
    // Pending data for migration when user signs up
    var pendingUserData by mutableStateOf<UserData?>(null)
        private set
    
    // Cloud sync status
    val cloudSyncStatus: SyncStatus
        get() = SyncStatus(
            isSyncing = false,
            lastSyncTime = null,
            error = null,
            isOnline = true
        )
    
    
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
        
        // Load defects and tasks for current user
        coroutineScope.launch {
            loadDefectsForCurrentUser()
            loadTasksForCurrentUser()
        }
    }
    
    fun initializeCloudSync() {
        // Observe cloud storage state
        observeCloudStorage()
    }
    
    private fun observeCloudStorage() {
        if (cloudStorage == null) {
            // If cloud storage is not available, set default states for anonymous users
            authState = AuthState(false) // No error message for anonymous users
            syncStatus = SyncStatus() // Default sync status
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
            
            // Only load user data if there's also checklist data (indicating completed onboarding)
            // This prevents new users from seeing old data from previous app sessions
            val savedChecklistData = if (offlineStorage != null) {
                coroutineScope.launch {
                    offlineStorage.loadChecklistData().getOrNull()
                }
                appStorage?.loadChecklistData() // Fallback to local storage for immediate loading
            } else {
                appStorage?.loadChecklistData()
            }
            
            if (savedUserData != null && savedChecklistData != null) {
                userData = savedUserData
                // Don't set checklistData here - it will be loaded user-specifically
                // This prevents new users from seeing previous test data
            } else {
                // If no checklist data, don't load user data (user hasn't completed onboarding)
                userData = null
            }
        } catch (e: Exception) {
            // If there's an error loading data, initialize with defaults
            userData = null
            checklistData = null
        }
    }
    
    /**
     * Load defects for the current user (authenticated or anonymous)
     * This ensures user-specific data isolation
     */
    private suspend fun loadDefectsForCurrentUser() {
        try {
            if (authState.isAuthenticated) {
                // For authenticated users, load from cloud storage
                val cloudDefectsResult = cloudStorage?.loadDefects()
                if (cloudDefectsResult?.isSuccess == true) {
                    defects = cloudDefectsResult.getOrNull() ?: emptyList()
                } else {
                    // Fallback to local storage for authenticated users
                    defects = appStorage?.loadDefects() ?: emptyList()
                }
            } else {
                // For anonymous users, load from local storage
                // This preserves defects for anonymous users across app restarts
                defects = appStorage?.loadDefects() ?: emptyList()
            }
        } catch (e: Exception) {
            // On error, start with empty list for new users
            defects = emptyList()
        }
    }
    
    /**
     * Load tasks for the current user (authenticated or anonymous)
     * This ensures user-specific data isolation
     */
    private suspend fun loadTasksForCurrentUser() {
        try {
            if (authState.isAuthenticated) {
                // For authenticated users, load from cloud storage
                val cloudChecklistDataResult = cloudStorage?.loadChecklistData()
                if (cloudChecklistDataResult?.isSuccess == true) {
                    checklistData = cloudChecklistDataResult.getOrNull()
                } else {
                    // Fallback to local storage for authenticated users
                    checklistData = appStorage?.loadChecklistData()
                }
            } else {
                // For anonymous users, try to load saved tasks first
                val savedChecklistData = appStorage?.loadChecklistData()
                if (savedChecklistData != null) {
                    // Load saved tasks to preserve user progress
                    checklistData = savedChecklistData
                } else if (userData != null) {
                    // If no saved data, generate fresh checklist based on user data
                    checklistData = com.example.movein.shared.data.ChecklistDataGenerator.generatePersonalizedChecklist(userData!!)
                } else {
                    // If no user data, start with null (will be generated when user data is set)
                    checklistData = null
                }
            }
        } catch (e: Exception) {
            // On error, start with null for new users
            checklistData = null
        }
    }
    
    /**
     * Reload defects after authentication state changes
     * This should be called after successful login/signup
     */
    suspend fun reloadDefectsForCurrentUser() {
        loadDefectsForCurrentUser()
    }
    
    /**
     * Reload tasks after authentication state changes
     * This should be called after successful login/signup
     */
    suspend fun reloadTasksForCurrentUser() {
        loadTasksForCurrentUser()
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
        
        // Reload tasks to ensure user-specific data
        coroutineScope.launch {
            loadTasksForCurrentUser()
        }
    }
    
    fun initializeAnonymousUserData(data: UserData) {
        userData = data
        isAnonymousUser = true
        authState = AuthState(false) // Ensure authentication state is false for anonymous users
        // Generate personalized checklist based on user data
        checklistData = com.example.movein.shared.data.ChecklistDataGenerator.generatePersonalizedChecklist(data)
        
        // Save to local storage only (no cloud sync for anonymous users)
        appStorage?.saveUserData(data)
        appStorage?.saveChecklistData(checklistData!!)
        
        // Reload tasks to ensure user-specific data
        coroutineScope.launch {
            loadTasksForCurrentUser()
        }
    }
    
    /**
     * Migration result data class
     */
    data class MigrationResult(
        val success: Boolean,
        val migratedItems: Int,
        val errors: List<String> = emptyList(),
        val message: String
    )
    
    /**
     * Enhanced data migration with progress tracking and error handling
     */
    suspend fun migrateAnonymousDataToAccount(): Result<MigrationResult> {
        return try {
            println("AppState: Starting migration - isAnonymousUser: $isAnonymousUser, authState.isAuthenticated: ${authState.isAuthenticated}")
            
            // Only migrate if user was anonymous and is now becoming authenticated
            // If user is already authenticated (existing user), no migration needed
            if (!isAnonymousUser || authState.isAuthenticated) {
                println("AppState: User is already authenticated or not anonymous, no migration needed")
                return Result.success(MigrationResult(
                    success = true,
                    migratedItems = 0,
                    message = "User is already authenticated"
                ))
            }
            
            if (userData == null && checklistData == null && defects.isEmpty()) {
                println("AppState: No data to migrate")
                return Result.success(MigrationResult(
                    success = true,
                    migratedItems = 0,
                    message = "No data to migrate"
                ))
            }
            
            // Check if there's actually meaningful data to migrate
            val hasUserData = userData != null
            val hasChecklistData = checklistData != null
            val hasDefects = defects.isNotEmpty()
            
            println("AppState: Data check - userData: $hasUserData, checklistData: $hasChecklistData, defects: $hasDefects")
            
            // If no meaningful data, skip migration
            if (!hasUserData && !hasChecklistData && !hasDefects) {
                println("AppState: No meaningful data to migrate, skipping migration")
                return Result.success(MigrationResult(
                    success = true,
                    migratedItems = 0,
                    message = "No data to migrate"
                ))
            }
            
            println("AppState: Found data to migrate - userData: ${userData != null}, checklistData: ${checklistData != null}, defects: ${defects.size}")
            
            var migratedItems = 0
            val errors = mutableListOf<String>()
            
            // Migrate user data
            if (userData != null) {
                try {
                    offlineStorage?.saveUserData(userData!!)
                    migratedItems++
                } catch (e: Exception) {
                    errors.add("Failed to migrate user data: ${e.message}")
                }
            }
            
            // Migrate checklist data
            if (checklistData != null) {
                try {
                    offlineStorage?.saveChecklistData(checklistData!!)
                    migratedItems++
                } catch (e: Exception) {
                    errors.add("Failed to migrate checklist data: ${e.message}")
                }
            }
            
            // Migrate defects
            if (defects.isNotEmpty()) {
                try {
                    offlineStorage?.saveDefects(defects)
                    migratedItems++
                } catch (e: Exception) {
                    errors.add("Failed to migrate defects: ${e.message}")
                }
            }
            
            val success = errors.isEmpty()
            if (success) {
                isAnonymousUser = false
            }
            
            val message = if (success) {
                "Successfully migrated $migratedItems data items to your account"
            } else {
                "Migration completed with ${errors.size} errors. Some data may not have been synced."
            }
            
            println("AppState: Migration completed - success: $success, migratedItems: $migratedItems, message: $message")
            
            Result.success(MigrationResult(
                success = success,
                migratedItems = migratedItems,
                errors = errors,
                message = message
            ))
            
        } catch (e: Exception) {
            println("AppState: Migration failed with exception: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Legacy migration function for backward compatibility
     */
    fun migrateAnonymousDataToAccountLegacy() {
        coroutineScope.launch {
            migrateAnonymousDataToAccount()
        }
    }
    
    /**
     * Check if registration prompt should be shown
     */
    fun shouldShowRegistrationPrompt(): Boolean {
        if (authState.isAuthenticated || hasShownRegistrationPrompt) {
            return false
        }
        
        // Don't show prompt during onboarding - only after user has completed it
        // and is actively using the app (has created some data)
        if (userData == null || checklistData == null) {
            return false
        }
        
        // Show prompt after user has created some tasks (indicating they're actively using the app)
        if (tasksCreatedCount >= 3) {
            return true
        }
        
        // Show prompt after user has created some defects (indicating they're actively using the app)
        if (defectsCreatedCount >= 2) {
            return true
        }
        
        // Show prompt after user has completed some tasks (indicating engagement)
        val completedTasks = checklistData?.let { data ->
            data.firstWeek.count { it.isCompleted } + 
            data.firstMonth.count { it.isCompleted } + 
            data.firstYear.count { it.isCompleted }
        } ?: 0
        if (completedTasks >= 2) {
            return true
        }
        
        return false
    }
    
    /**
     * Get the appropriate registration prompt type
     */
    fun getRegistrationPromptType(): String {
        return when {
            tasksCreatedCount >= 5 || defectsCreatedCount >= 3 -> 
                "AFTER_MULTIPLE_TASKS"
            tasksCreatedCount >= 1 || defectsCreatedCount >= 1 -> 
                "AFTER_FIRST_TASKS"
            else -> 
                "GENERAL_PROMPT"
        }
    }
    
    /**
     * Mark registration prompt as shown
     */
    fun markRegistrationPromptShown() {
        hasShownRegistrationPrompt = true
    }
    
    /**
     * Track task creation for prompt logic
     */
    fun trackTaskCreated() {
        tasksCreatedCount++
    }
    
    /**
     * Track defect creation for prompt logic
     */
    fun trackDefectCreated() {
        defectsCreatedCount++
    }
    
    /**
     * Set user as anonymous (continue without account)
     * This ensures the authentication state is properly reset
     */
    fun setAnonymousUser() {
        isAnonymousUser = true
        authState = AuthState(false) // Ensure authentication state is false
        // Clear any stored authentication data
        coroutineScope.launch {
            secureTokenStorage?.clearTokens()
        }
    }
    
    /**
     * Check for stored credentials and attempt automatic login
     */
    suspend fun checkForStoredCredentials(): Boolean {
        return secureTokenStorage?.let { storage ->
            try {
                if (storage.hasStoredCredentials() && !storage.isAccessTokenExpired()) {
                    // User has valid stored credentials, log them in automatically
                    val userData = storage.getStoredUserData()
                    val email = userData["email"] as? String
                    val userId = userData["userId"] as? String
                    
                    if (email != null && userId != null) {
                        // Set authentication state
                        authState = AuthState(true, email, userId)
                        isAnonymousUser = false
                        
                        // Load user data from cloud
                        loadUserDataFromCloud()
                        
                        // Navigate to dashboard
                        currentScreen = Screen.Dashboard
                        return true
                    }
                } else if (storage.hasStoredCredentials() && storage.getRefreshToken() != null) {
                    // Access token expired but refresh token exists, try to refresh
                    return attemptTokenRefresh()
                }
            } catch (e: Exception) {
                // Clear invalid stored credentials
                storage.clearTokens()
            }
            false
        } ?: false
    }
    
    /**
     * Attempt to refresh access token using refresh token
     */
    private suspend fun attemptTokenRefresh(): Boolean {
        return secureTokenStorage?.let { storage ->
            try {
                val refreshToken = storage.getRefreshToken()
                if (refreshToken != null) {
                    // For now, just clear tokens and return false
                    // TODO: Implement proper token refresh with backend
                    storage.clearTokens()
                }
            } catch (e: Exception) {
                // Refresh failed, clear tokens
                storage.clearTokens()
            }
            false
        } ?: false
    }
    
    /**
     * Load user data from cloud storage
     */
    private suspend fun loadUserDataFromCloud() {
        try {
            // Load user data
            val cloudUserDataResult = cloudStorage?.loadUserData()
            if (cloudUserDataResult?.isSuccess == true) {
                userData = cloudUserDataResult.getOrNull()
            }
            
            // Load checklist data
            val cloudChecklistDataResult = cloudStorage?.loadChecklistData()
            if (cloudChecklistDataResult?.isSuccess == true) {
                checklistData = cloudChecklistDataResult.getOrNull()
            }
            
            // Load defects
            val cloudDefectsResult = cloudStorage?.loadDefects()
            if (cloudDefectsResult?.isSuccess == true) {
                defects = cloudDefectsResult.getOrNull() ?: emptyList()
            }
        } catch (e: Exception) {
            // Handle error - could fall back to local data
        }
    }
    
    /**
     * Secure logout - clears all tokens and returns to welcome screen
     */
    suspend fun secureLogout() {
        try {
            // Clear tokens from secure storage
            secureTokenStorage?.clearTokens()
            
            // Clear authentication state
            authState = AuthState(false)
            isAnonymousUser = true
            
            // Clear user data
            userData = null
            checklistData = null
            defects = emptyList()
            
            // Navigate to welcome screen
            currentScreen = Screen.Welcome
            clearNavigationStack()
            
        } catch (e: Exception) {
            // Handle error
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
    
    fun clearLastAddedTaskTab() {
        lastAddedTaskTab = null
    }
    
    fun updatePendingDefectDueDate(dueDate: String?) {
        pendingDefectDueDate = dueDate
    }
    
    fun clearPendingDefectDueDate() {
        pendingDefectDueDate = null
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
        // Clear selection if the deleted defect was selected to avoid stale UI state
        if (selectedDefect?.id == defectId) {
            selectedDefect = null
        }
        // Also ensure we navigate to the list if currently on detail (handled by caller)
        
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
    
    fun duplicateDefect(defect: Defect) {
        addDefect(defect)
    }
    
    fun deleteTask(taskId: String) {
        val currentData = checklistData ?: return
        
        // Remove the task from all lists
        val updatedData = currentData.copy(
            firstWeek = currentData.firstWeek.filter { it.id != taskId },
            firstMonth = currentData.firstMonth.filter { it.id != taskId },
            firstYear = currentData.firstYear.filter { it.id != taskId }
        )
        
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
    
    fun duplicateTask(task: ChecklistItem) {
        addTask(task)
    }
    
    fun addTask(newTask: ChecklistItem) {
        // If no checklist data exists, create a new one
        val currentData = checklistData ?: ChecklistData(
            firstWeek = emptyList(),
            firstMonth = emptyList(),
            firstYear = emptyList()
        )
        
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
        
        // Track which tab the task was added to
        lastAddedTaskTab = targetHost
        
        // Automatically select the newly added task
        selectTask(newTask)
        
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
    
    
    // Cloud sync methods
    suspend fun signIn(email: String, password: String): Result<Unit> {
        println("AppState.signIn: CloudStorage available: ${cloudStorage != null}")
        return if (cloudStorage != null) {
            println("AppState.signIn: Calling cloudStorage.signIn")
            cloudStorage.signIn(email, password)
        } else {
            println("AppState.signIn: CloudStorage is null, returning failure")
            Result.failure(Exception("Firebase Authentication is not available. Please check your internet connection and try again."))
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return if (cloudStorage != null) {
            cloudStorage.signUp(email, password)
        } else {
            Result.failure(Exception("Firebase Authentication is not available. Please check your internet connection and try again."))
        }
    }

    suspend fun signInWithGoogle(): Result<Unit> {
        return cloudStorage?.signInWithGoogle() ?: Result.failure(Exception("Cloud storage not available"))
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            // Perform a secure logout that clears tokens, state, and back stack
            secureLogout()
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error signing out: ${e.message}")
            Result.failure(e)
        }
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
    
    
    /**
     * Clear user data while preserving predefined tasks
     */
    suspend fun clearAllData() {
        try {
            // Clear user profile and apartment details
            userData = null
            
            // Clear authentication tokens
            cloudStorage?.signOut()
            
            // Clear all defects (these are user-created)
            defects = emptyList()
            
            // Reset selected items
            selectedTask = null
            selectedDefect = null
            
            // Reset checklist data to remove user modifications but keep predefined tasks
            // We'll reload the predefined tasks from the generator
            checklistData = ChecklistDataGenerator.getDefaultChecklistData()
            
            // Save the reset checklist data
            appStorage?.saveChecklistData(checklistData!!)
            appStorage?.saveDefects(defects)
            
            // Clear user data from storage (selective clearing)
            appStorage?.clearUserData()
            appStorage?.saveUserData(UserData()) // Reset to default
            
            // Navigate to welcome screen
            navigateTo(Screen.Welcome)
            
            println("User data cleared successfully, predefined tasks preserved")
        } catch (e: Exception) {
            println("Error clearing user data: ${e.message}")
        }
    }
    
    // File persistence helper methods
    suspend fun persistFileAttachment(
        uri: Uri,
        originalName: String? = null,
        mimeType: String? = null
    ): FileAttachment? {
        return fileManager?.persistFile(uri, originalName, mimeType)
    }
    
    fun getFileUri(filePath: String): Uri? {
        return fileManager?.getFileUri(filePath)
    }
    
    fun deleteFileAttachment(filePath: String): Boolean {
        return fileManager?.deleteFile(filePath) ?: false
    }
    
    suspend fun cleanupOrphanedFiles() {
        val allFilePaths = mutableSetOf<String>()
        
        // Collect all file paths from defects
        defects.forEach { defect ->
            defect.attachments.forEach { attachment ->
                allFilePaths.add(attachment.uri)
            }
            defect.images.forEach { imagePath ->
                allFilePaths.add(imagePath)
            }
        }
        
        // Collect all file paths from tasks
        checklistData?.let { data ->
            data.firstWeek.forEach { task ->
                task.attachments.forEach { attachment ->
                    allFilePaths.add(attachment.uri)
                }
            }
            data.firstMonth.forEach { task ->
                task.attachments.forEach { attachment ->
                    allFilePaths.add(attachment.uri)
                }
            }
            data.firstYear.forEach { task ->
                task.attachments.forEach { attachment ->
                    allFilePaths.add(attachment.uri)
                }
            }
        }
        
        fileManager?.cleanupOrphanedFiles(allFilePaths)
    }
    
}
