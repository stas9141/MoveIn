package com.example.movein

import android.os.Bundle
import android.util.Log
import android.view.WindowManager

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.movein.navigation.Screen
import com.example.movein.ui.screens.ApartmentDetailsScreen
import com.example.movein.ui.screens.DashboardScreen
import com.example.movein.ui.screens.SettingsScreen
import com.example.movein.ui.screens.TaskDetailScreen
import com.example.movein.ui.screens.WelcomeScreen
import com.example.movein.ui.screens.DefectListScreen
import com.example.movein.ui.screens.AddEditDefectScreen
import com.example.movein.ui.screens.DefectDetailScreen
import com.example.movein.ui.screens.CalendarScreen
import com.example.movein.ui.screens.SimpleLoginScreen
import com.example.movein.ui.screens.SimpleSignUpScreen
import com.example.movein.ui.screens.MyApartmentScreen
import com.example.movein.ui.screens.ForgotPasswordScreen
import com.example.movein.ui.screens.ResetPasswordScreen
import com.example.movein.ui.screens.ReportConfigurationScreen
import com.example.movein.ui.theme.MoveInTheme
import com.example.movein.ui.components.SimpleTutorialDialog
import com.example.movein.utils.FileManager
import com.example.movein.ui.components.rememberSimpleTutorialState
import com.example.movein.ui.components.BottomNavigationBar
import com.example.movein.ui.components.OfflineIndicator
import com.example.movein.shared.storage.AppStorage
import com.example.movein.shared.cloud.CloudStorage
import com.example.movein.offline.OfflineStorageManager
import com.example.movein.auth.GoogleSignInHelper
import com.example.movein.auth.AuthManager
import com.example.movein.auth.BiometricAuthManager
import com.example.movein.utils.ErrorHandler
import com.example.movein.auth.SecureTokenStorage
import com.google.firebase.FirebaseApp

private fun shouldShowBottomNavigation(currentScreen: Screen): Boolean {
    return when (currentScreen) {
        Screen.Dashboard,
        Screen.Calendar,
        Screen.DefectList,
        Screen.Settings,
        is Screen.TaskDetail,
        is Screen.DefectDetail,
        is Screen.AddEditDefect,
        Screen.ReportConfiguration -> true
        else -> false
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Configure keyboard behavior to adjust the layout when keyboard appears
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        
        enableEdgeToEdge()
        setContent {
            MoveInApp()
        }
    }
}

@Composable
fun MoveInApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appStorage = remember(context) { 
        try {
            AppStorage(context)
        } catch (e: Exception) {
            // Return a mock storage if initialization fails
            null
        }
    }
    val cloudStorage = remember(context) { 
        try {
            val storage = CloudStorage(context)
            Log.d("MainActivity", "CloudStorage initialized successfully")
            storage
        } catch (e: Exception) {
            Log.e("MainActivity", "CloudStorage initialization failed", e)
            // Return a mock storage if initialization fails
            null
        }
    }
    val appState = remember { 
        // Initialize offline storage
        val offlineStorage = try {
            if (appStorage != null && cloudStorage != null) {
                OfflineStorageManager(context, cloudStorage, appStorage)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
        
        val fileManager = FileManager(context)
        val state = AppState(appStorage, cloudStorage, offlineStorage, coroutineScope, fileManager = fileManager)
        // Initialize cloud sync after AppState is created
        state.initializeCloudSync()
        state
    }
    
    // Tutorial system
    val tutorialState = rememberSimpleTutorialState()
    
    // FAB state for Dashboard
    var showAddTaskDialog by remember { mutableStateOf(false) }
    
    // Google Sign-In error state
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    
    // Biometric authentication state
    var biometricError by remember { mutableStateOf<String?>(null) }
    var isBiometricAvailable by remember { mutableStateOf(false) }
    
    // Password reset state
    var passwordResetLoading by remember { mutableStateOf(false) }
    var passwordResetError by remember { mutableStateOf<String?>(null) }
    var passwordResetSuccess by remember { mutableStateOf<String?>(null) }
    
    // Reset password state
    var resetPasswordLoading by remember { mutableStateOf(false) }
    var resetPasswordError by remember { mutableStateOf<String?>(null) }
    var resetPasswordSuccess by remember { mutableStateOf<String?>(null) }
    
    // Error clearing functions
    val clearAuthError = {
        appState.clearAuthError()
    }
    
    val clearGoogleSignInError = {
        googleSignInError = null
    }
    
    val clearBiometricError = {
        biometricError = null
    }
    
    val clearPasswordResetError = {
        passwordResetError = null
    }
    
    val clearPasswordResetSuccess = {
        passwordResetSuccess = null
    }
    
    val clearResetPasswordError = {
        resetPasswordError = null
    }
    
    val clearResetPasswordSuccess = {
        resetPasswordSuccess = null
    }
    
    // Check biometric availability when app starts
    LaunchedEffect(Unit) {
        try {
            val biometricManager = BiometricAuthManager(context)
            val authManager = AuthManager(context)
            
            // Check if biometric hardware is available
            val hasHardware = biometricManager.hasBiometricHardware()
            val hasEnrolled = biometricManager.hasEnrolledBiometrics()
            val canUseBiometric = authManager.canUseBiometricAuth().getOrNull() ?: false
            
            isBiometricAvailable = hasHardware && hasEnrolled && canUseBiometric
            
            Log.d("MainActivity", "Biometric availability: hardware=$hasHardware, enrolled=$hasEnrolled, canUse=$canUseBiometric")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking biometric availability", e)
            isBiometricAvailable = false
        }
    }
    
    // Get the activity for Google Sign-In
    val activity = context as? ComponentActivity
    val googleSignInHelper = remember(activity) { 
        try {
            activity?.let { GoogleSignInHelper(it) }
        } catch (e: Exception) {
            null // Handle Google Sign-In helper creation errors
        }
    }
    
    // Handle system back button
    BackHandler(enabled = appState.canNavigateBack()) {
        appState.navigateBack()
    }
    
    MoveInTheme(darkTheme = appState.isDarkMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                // Show bottom navigation for main screens only
                if (shouldShowBottomNavigation(appState.currentScreen)) {
                    BottomNavigationBar(
                        currentScreen = appState.currentScreen,
                        onNavigateTo = { screen -> appState.navigateTo(screen) }
                    )
                }
            },
            floatingActionButton = {
                // Show FAB for Dashboard screen
                if (appState.currentScreen == Screen.Dashboard) {
                    FloatingActionButton(
                        onClick = {
                            showAddTaskDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            }
        ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            // Offline indicator
            OfflineIndicator(
                syncStatus = appState.offlineSyncStatus,
                modifier = Modifier.fillMaxWidth()
            )
            
    // Handle Android back button
    BackHandler(enabled = appState.canNavigateBack()) {
        appState.navigateBack()
    }
    
    // Main content with proper padding
    Box(modifier = Modifier.fillMaxSize()) {
        when (appState.currentScreen) {
            Screen.Welcome -> {
                WelcomeScreen(
                    onGetStartedClick = {
                        appState.navigateTo(Screen.ApartmentDetails)
                    },
                    onSignInClick = {
                        appState.navigateTo(Screen.Login)
                    },
                    onTutorialClick = {
                        tutorialState.showTutorial(
                            "Welcome to MoveIn!", 
                            "Your comprehensive home inspection and move-in companion. Navigate through the app to track tasks, defects, and manage your move-in process."
                        )
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.Login -> {
                SimpleLoginScreen(
                    onBackClick = {
                        appState.navigateTo(Screen.Welcome)
                    },
                    onSignInClick = { email, password ->
                        Log.d("MainActivity", "Sign-in button clicked with email: ${email.take(3)}***")
                        Log.d("MainActivity", "CloudStorage available: ${cloudStorage != null}")
                        
                        // Clear other errors but keep auth error for display
                        clearGoogleSignInError()
                        clearBiometricError()
                        coroutineScope.launch {
                            Log.d("MainActivity", "Starting sign-in process...")
                            val result = appState.signIn(email, password)
                            Log.d("MainActivity", "Sign-in result: ${result.isSuccess}")
                            
                            if (result.isSuccess) {
                                Log.d("MainActivity", "Sign-in successful!")
                                // Clear auth error on successful login
                                clearAuthError()
                                // Migrate anonymous data to the signed-in account
                                appState.migrateAnonymousDataToAccount()
                                // Enable biometric authentication after successful login
                                try {
                                    val authManager = AuthManager(context)
                                    authManager.enableBiometricAuth()
                                    Log.d("MainActivity", "Biometric authentication enabled after login")
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Failed to enable biometric authentication", e)
                                }
                                appState.navigateTo(Screen.Dashboard)
                            } else {
                                // Handle login failure
                                val error = result.exceptionOrNull()
                                Log.e("MainActivity", "Sign-In failed: ${error?.message}")
                                Log.e("MainActivity", "Sign-In error type: ${error?.javaClass?.simpleName}")
                                
                                // Force set the error in authState if it's not already set
                                if (appState.authState.error == null) {
                                    Log.d("MainActivity", "AuthState error is null, setting error manually")
                                    appState.authState = appState.authState.copy(error = error?.message ?: "Sign-in failed")
                                }
                                
                                // Ensure error is visible by logging it
                                Log.d("MainActivity", "AuthState error after handling: ${appState.authState.error}")
                                Log.d("MainActivity", "AuthState isLoading: ${appState.authState.isLoading}")
                                Log.d("MainActivity", "AuthState isAuthenticated: ${appState.authState.isAuthenticated}")
                            }
                        }
                    },
                    onSignUpClick = {
                        appState.navigateTo(Screen.SignUp)
                    },
                    onForgotPasswordClick = {
                        appState.navigateTo(Screen.ForgotPassword)
                    },
                    onGoogleSignInClick = {
                        Log.d("MainActivity", "Google Sign-In button clicked (Welcome)")
                        // Clear any existing errors before attempting Google sign in
                        clearAuthError()
                        clearGoogleSignInError()
                        clearBiometricError()
                        googleSignInHelper?.signInWithGoogle { result ->
                            Log.d("MainActivity", "Google Sign-In result: ${result.isSuccess}")
                            coroutineScope.launch {
                                if (result.isSuccess) {
                                    // Migrate anonymous data to the Google account
                                    appState.migrateAnonymousDataToAccount()
                                    // Enable biometric authentication after successful Google login
                                    try {
                                        val authManager = AuthManager(context)
                                        authManager.enableBiometricAuth()
                                        Log.d("MainActivity", "Biometric authentication enabled after Google login")
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Failed to enable biometric authentication", e)
                                    }
                                    Log.d("MainActivity", "Navigating to Dashboard")
                                    appState.navigateTo(Screen.Dashboard)
                                } else {
                                    Log.e("MainActivity", "Google Sign-In failed: ${result.exceptionOrNull()?.message}")
                                    // Show user-friendly error message
                                    val error = result.exceptionOrNull()
                                    val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(error)
                                    googleSignInError = userFriendlyError
                                    println("Google Sign-In Error: ${error?.message}")
                                }
                            }
                        }
                    },
                    onBiometricSignInClick = {
                        // Clear any existing errors before attempting biometric sign in
                        clearAuthError()
                        clearGoogleSignInError()
                        clearBiometricError()
                        coroutineScope.launch {
                            try {
                                val biometricManager = BiometricAuthManager(context)
                                val result = biometricManager.authenticateForLogin(activity as androidx.fragment.app.FragmentActivity)
                                
                                if (result.isSuccess) {
                                    // Biometric authentication successful, check if we have stored credentials
                                    val authManager = AuthManager(context)
                                    val hasCredentials = authManager.canUseBiometricAuth().getOrNull() ?: false
                                    
                                    if (hasCredentials) {
                                        // User is authenticated, navigate to dashboard
                                        appState.navigateTo(Screen.Dashboard)
                                        Log.d("MainActivity", "Biometric authentication successful")
                                    } else {
                                        biometricError = "No stored credentials found. Please sign in with your password first."
                                    }
                                } else {
                                    val error = result.exceptionOrNull()
                                    Log.e("MainActivity", "Biometric authentication failed: ${error?.message}")
                                    biometricError = error?.message ?: "Biometric authentication failed"
                                }
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Biometric authentication error: ${e.message}")
                                biometricError = e.message ?: "Biometric authentication failed"
                            }
                        }
                    },
                    isLoading = appState.authState.isLoading,
                    error = appState.authState.error,
                    googleSignInError = googleSignInError,
                    biometricError = biometricError,
                    onDismissError = clearAuthError,
                    onDismissGoogleError = clearGoogleSignInError,
                    onDismissBiometricError = clearBiometricError,
                    isBiometricAvailable = isBiometricAvailable,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.SignUp -> {
                SimpleSignUpScreen(
                    onBackClick = {
                        appState.navigateTo(Screen.Welcome)
                    },
                    onSignUpClick = { email, password ->
                        // Clear any existing errors before attempting sign up
                        clearAuthError()
                        clearGoogleSignInError()
                        coroutineScope.launch {
                            val result = appState.signUp(email, password)
                            if (result.isSuccess) {
                                // Migrate anonymous data to the new account
                                appState.migrateAnonymousDataToAccount()
                                appState.navigateTo(Screen.Dashboard)
                            }
                        }
                    },
                    onSignInClick = {
                        appState.navigateTo(Screen.Login)
                    },
                    onGoogleSignInClick = {
                        Log.d("MainActivity", "Google Sign-In button clicked (Welcome)")
                        // Clear any existing errors before attempting Google sign in
                        clearAuthError()
                        clearGoogleSignInError()
                        googleSignInHelper?.signInWithGoogle { result ->
                            Log.d("MainActivity", "Google Sign-In result: ${result.isSuccess}")
                            coroutineScope.launch {
                                if (result.isSuccess) {
                                    // Migrate anonymous data to the Google account
                                    appState.migrateAnonymousDataToAccount()
                                    Log.d("MainActivity", "Navigating to Dashboard")
                                    appState.navigateTo(Screen.Dashboard)
                                } else {
                                    Log.e("MainActivity", "Google Sign-In failed: ${result.exceptionOrNull()?.message}")
                                    // Show user-friendly error message
                                    val error = result.exceptionOrNull()
                                    val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(error)
                                    googleSignInError = userFriendlyError
                                    println("Google Sign-In Error: ${error?.message}")
                                }
                            }
                        }
                    },
                    isLoading = appState.authState.isLoading,
                    error = appState.authState.error,
                    googleSignInError = googleSignInError,
                    onDismissError = clearAuthError,
                    onDismissGoogleError = clearGoogleSignInError,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.ForgotPassword -> {
                ForgotPasswordScreen(
                    onBackClick = {
                        // Clear password reset state when going back
                        clearPasswordResetError()
                        clearPasswordResetSuccess()
                        appState.navigateTo(Screen.Login)
                    },
                    onSendResetEmail = { email ->
                        coroutineScope.launch {
                            try {
                                // Clear previous states
                                clearPasswordResetError()
                                clearPasswordResetSuccess()
                                passwordResetLoading = true
                                
                                val authManager = AuthManager(context)
                                val result = authManager.forgotPassword(email)
                                
                                if (result.isSuccess) {
                                    passwordResetSuccess = "We've sent a password reset link to $email. Please check your email and follow the instructions to reset your password."
                                } else {
                                    val error = result.exceptionOrNull()
                                    passwordResetError = error?.message ?: "Failed to send reset email. Please try again."
                                }
                            } catch (e: Exception) {
                                passwordResetError = "Error sending reset email: ${e.message}"
                            } finally {
                                passwordResetLoading = false
                            }
                        }
                    },
                    isLoading = passwordResetLoading,
                    error = passwordResetError,
                    successMessage = passwordResetSuccess,
                    onDismissError = clearPasswordResetError,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            is Screen.ResetPassword -> {
                // Extract token from route
                val token = appState.currentScreen.route.split("/").lastOrNull() ?: ""
                ResetPasswordScreen(
                    resetToken = token,
                    onBackClick = {
                        // Clear reset password state when going back
                        clearResetPasswordError()
                        clearResetPasswordSuccess()
                        appState.navigateTo(Screen.Login)
                    },
                    onResetPassword = { resetToken, newPassword ->
                        coroutineScope.launch {
                            try {
                                // Clear previous states
                                clearResetPasswordError()
                                clearResetPasswordSuccess()
                                resetPasswordLoading = true
                                
                                val authManager = AuthManager(context)
                                val result = authManager.resetPassword(resetToken, newPassword)
                                
                                if (result.isSuccess) {
                                    resetPasswordSuccess = "Your password has been reset successfully. You can now log in with your new password."
                                    // Navigate to login after a short delay to show success message
                                    kotlinx.coroutines.delay(2000)
                                    appState.navigateTo(Screen.Login)
                                } else {
                                    val error = result.exceptionOrNull()
                                    resetPasswordError = error?.message ?: "Failed to reset password. Please try again."
                                }
                            } catch (e: Exception) {
                                resetPasswordError = "Error resetting password: ${e.message}"
                            } finally {
                                resetPasswordLoading = false
                            }
                        }
                    },
                    isLoading = resetPasswordLoading,
                    error = resetPasswordError,
                    successMessage = resetPasswordSuccess,
                    onDismissError = clearResetPasswordError,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.ApartmentDetails -> {
                ApartmentDetailsScreen(
                    onContinueClick = { userData ->
                        appState.initializeAnonymousUserData(userData)
                        appState.navigateTo(Screen.Dashboard)
                    },
                    onBackClick = {
                        appState.navigateTo(Screen.Welcome)
                    },
                    onSignUpClick = {
                        appState.navigateTo(Screen.SignUp)
                    },
                    onSignInClick = {
                        appState.navigateTo(Screen.Login)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.Dashboard -> {
                appState.checklistData?.let { checklistData ->
                    DashboardScreen(
                        checklistData = checklistData,
                        selectedTabIndex = appState.lastAddedTaskTab ?: 0,
                        selectedTaskId = appState.selectedTask?.id,
                        onTaskClick = { task ->
                            appState.selectTask(task)
                            appState.navigateTo(Screen.TaskDetail)
                        },
                        onTaskToggle = { task ->
                            appState.updateTask(task)
                        },
                        onAddTask = { newTask ->
                            appState.addTask(newTask)
                            showAddTaskDialog = false
                            // Stay on Dashboard; newly added task is selected via addTask()
                        },
                        onDefectListClick = {
                            appState.navigateTo(Screen.DefectList)
                        },
                        showAddTaskDialog = showAddTaskDialog,
                        onDismissAddTaskDialog = { showAddTaskDialog = false },
                        onTutorialClick = {
                            tutorialState.showTutorial(
                                "Dashboard Overview", 
                                "Your main hub for tracking tasks and defects. View your progress across different timeframes: First Week, First Month, and First Year. Use the floating action button to add new tasks."
                            )
                        },
                        defects = appState.defects,
                        userEmail = if (appState.authState.isAuthenticated) appState.authState.email else null,
                        onProfileClick = {
                            appState.navigateTo(Screen.Settings)
                        },
                        onSignInClick = {
                            appState.navigateTo(Screen.Login)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                } ?: run {
                    // Show loading or redirect to apartment details if no checklist data
                    LaunchedEffect(Unit) {
                        appState.navigateTo(Screen.ApartmentDetails)
                    }
                }
            }
            
            is Screen.TaskDetail -> {
                appState.selectedTask?.let { task ->
                    TaskDetailScreen(
                        task = task,
                        onBackClick = {
                            appState.clearLastAddedTaskTab()
                            appState.navigateTo(Screen.Dashboard)
                        },
                        onTaskUpdate = { updatedTask ->
                            appState.updateTask(updatedTask)
                        },
                        onTaskDuplicate = { duplicatedTask ->
                            appState.duplicateTask(duplicatedTask)
                            appState.navigateTo(Screen.Dashboard)
                        },
                        onTaskDelete = { taskId ->
                            appState.deleteTask(taskId)
                            appState.navigateTo(Screen.Dashboard)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            
            Screen.Settings -> {
                SettingsScreen(
                    isDarkMode = appState.isDarkMode,
                    onDarkModeToggle = { isDark ->
                        appState.toggleDarkMode()
                    },
                    onBackClick = {
                        appState.navigateTo(Screen.Dashboard)
                    },
                    onMyApartmentClick = {
                        appState.navigateTo(Screen.MyApartment)
                    },
                    onCreateAccountClick = {
                        appState.navigateTo(Screen.SignUp)
                    },
                    onSignInClick = {
                        appState.navigateTo(Screen.Login)
                    },
                    onReorganizeTasks = {
                        appState.reorganizeTasksByDueDate()
                    },
                    onClearData = {
                        coroutineScope.launch {
                            appState.clearAllData()
                        }
                    },
                    onGenerateReport = {
                        appState.navigateTo(Screen.ReportConfiguration)
                    },
                    onTutorialClick = {
                        tutorialState.showTutorial(
                            "Settings & Features", 
                            "Manage your preferences, sync data across devices, generate defect reports, and customize your MoveIn experience. Access cloud sync and report generation from here."
                        )
                    },
                    authState = appState.authState,
                    syncStatus = appState.syncStatus,
                    onForceSync = {
                        coroutineScope.launch {
                            appState.forceSync()
                        }
                    },
                    onSignOut = {
                        coroutineScope.launch {
                            appState.signOut()
                        }
                    },
                    onLogoutAllDevices = {
                        coroutineScope.launch {
                            try {
                                val authManager = AuthManager(context)
                                val result = authManager.logoutAllDevices()
                                if (result.isSuccess) {
                                    // Show success message
                                    println("Successfully logged out from all devices")
                                    // Also sign out locally
                                    appState.signOut()
                                } else {
                                    // Show error message
                                    val error = result.exceptionOrNull()
                                    println("Failed to logout from all devices: ${error?.message}")
                                }
                            } catch (e: Exception) {
                                println("Error logging out from all devices: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.DefectList -> {
                DefectListScreen(
                    defects = appState.defects,
                    selectedDefectId = appState.selectedDefect?.id,
                    onDefectClick = { defect ->
                        appState.selectDefect(defect)
                        appState.navigateTo(Screen.DefectDetail)
                    },
                    onAddDefect = {
                        appState.clearSelectedDefect()
                        appState.navigateTo(Screen.AddEditDefect)
                    },
                    onBackClick = {
                        appState.navigateTo(Screen.Dashboard)
                    },
                    onDefectUpdate = { updatedDefect ->
                        appState.updateDefect(updatedDefect)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.AddEditDefect -> {
                AddEditDefectScreen(
                    defect = appState.selectedDefect,
                    initialDueDate = appState.pendingDefectDueDate,
                    userData = appState.userData,
                    onSave = { defect ->
                        if (appState.selectedDefect != null) {
                            appState.updateDefect(defect)
                            appState.navigateTo(Screen.DefectList)
                        } else {
                            appState.addDefect(defect)
                            appState.selectDefect(defect)
                            // Close add window and return to list with new defect selected
                            appState.navigateTo(Screen.DefectList)
                        }
                        appState.clearPendingDefectDueDate()
                    },
                    onBack = {
                        appState.clearPendingDefectDueDate()
                        appState.navigateTo(Screen.DefectList)
                    },
                    appState = appState,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            is Screen.DefectDetail -> {
                appState.selectedDefect?.let { defect ->
                    DefectDetailScreen(
                        defect = defect,
                        onBackClick = {
                            appState.navigateTo(Screen.DefectList)
                        },
                        onDefectUpdate = { updatedDefect ->
                            appState.updateDefect(updatedDefect)
                        },
                        onDefectDuplicate = { duplicatedDefect ->
                            appState.duplicateDefect(duplicatedDefect)
                            appState.navigateTo(Screen.DefectList)
                        },
                        onDefectDelete = { defectId ->
                            appState.deleteDefect(defectId)
                            appState.navigateTo(Screen.DefectList)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            
            Screen.Calendar -> {
                val allTasks = appState.checklistData?.let { data ->
                    data.firstWeek + data.firstMonth + data.firstYear
                } ?: emptyList()
                
                CalendarScreen(
                    tasks = allTasks,
                    defects = appState.defects,
                    onBackClick = {
                        appState.navigateTo(Screen.Dashboard)
                    },
                    onTaskClick = { task ->
                        appState.selectTask(task)
                        appState.navigateTo(Screen.TaskDetail)
                    },
                    onDefectClick = { defect ->
                        appState.selectDefect(defect)
                        appState.navigateTo(Screen.DefectDetail)
                    },
                    onAddTask = { newTask ->
                        appState.addTask(newTask)
                        // Navigate to Dashboard to show the newly added task on the relevant tab
                        appState.navigateTo(Screen.Dashboard)
                    },
                    onAddDefect = { newDefect ->
                        appState.clearSelectedDefect()
                        appState.updatePendingDefectDueDate(newDefect.dueDate)
                        appState.navigateTo(Screen.AddEditDefect)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.MyApartment -> {
                MyApartmentScreen(
                    userData = appState.userData,
                    hasCompletedOnboarding = appState.userData != null && appState.checklistData != null,
                    onBackClick = {
                        appState.navigateTo(Screen.Settings)
                    },
                    onEditClick = { /* Handle edit */ },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.ReportConfiguration -> {
                ReportConfigurationScreen(
                    defects = appState.defects,
                    onBackClick = {
                        appState.navigateTo(Screen.Settings)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        }
        }
        }
        
        // Tutorial Dialog
        SimpleTutorialDialog(
            isVisible = tutorialState.isVisible,
            title = tutorialState.title,
            description = tutorialState.description,
            onClose = { tutorialState.closeTutorial() }
        )
    }
}