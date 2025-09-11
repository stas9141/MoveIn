package com.example.movein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.movein.ui.screens.ReportConfigurationScreen
import com.example.movein.ui.theme.MoveInTheme
import com.example.movein.ui.components.SimpleTutorialDialog
import com.example.movein.ui.components.rememberSimpleTutorialState
import com.example.movein.ui.components.BottomNavigationBar
import com.example.movein.shared.storage.AppStorage
import com.example.movein.shared.cloud.CloudStorage
import com.example.movein.auth.GoogleSignInHelper
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
            CloudStorage(context)
        } catch (e: Exception) {
            // Return a mock storage if initialization fails
            null
        }
    }
    val appState = remember { 
        val state = AppState(appStorage, cloudStorage, coroutineScope)
        // Initialize cloud sync after AppState is created
        state.initializeCloudSync()
        state
    }
    
    // Tutorial system
    val tutorialState = rememberSimpleTutorialState()
    
    // FAB state for Dashboard
    var showAddTaskDialog by remember { mutableStateOf(false) }
    
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
                        coroutineScope.launch {
                            val result = appState.signIn(email, password)
                            if (result.isSuccess) {
                                appState.navigateTo(Screen.Dashboard)
                            }
                        }
                    },
                    onSignUpClick = {
                        appState.navigateTo(Screen.SignUp)
                    },
                    onGoogleSignInClick = {
                        googleSignInHelper?.signInWithGoogle { result ->
                            coroutineScope.launch {
                                if (result.isSuccess) {
                                    appState.navigateTo(Screen.Dashboard)
                                } else {
                                    // Show error message to user
                                    val errorMessage = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
                                    println("Google Sign-In Error: $errorMessage")
                                    // TODO: Show error dialog to user
                                }
                            }
                        }
                    },
                    isLoading = appState.authState.isLoading,
                    error = appState.authState.error,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.SignUp -> {
                SimpleSignUpScreen(
                    onBackClick = {
                        appState.navigateTo(Screen.Welcome)
                    },
                    onSignUpClick = { email, password ->
                        coroutineScope.launch {
                            val result = appState.signUp(email, password)
                            if (result.isSuccess) {
                                appState.navigateTo(Screen.Dashboard)
                            }
                        }
                    },
                    onSignInClick = {
                        appState.navigateTo(Screen.Login)
                    },
                    onGoogleSignInClick = {
                        googleSignInHelper?.signInWithGoogle { result ->
                            coroutineScope.launch {
                                if (result.isSuccess) {
                                    appState.navigateTo(Screen.Dashboard)
                                } else {
                                    // Show error message to user
                                    val errorMessage = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
                                    println("Google Sign-In Error: $errorMessage")
                                    // TODO: Show error dialog to user
                                }
                            }
                        }
                    },
                    isLoading = appState.authState.isLoading,
                    error = appState.authState.error,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.ApartmentDetails -> {
                ApartmentDetailsScreen(
                    onContinueClick = { userData ->
                        appState.initializeUserData(userData)
                        appState.navigateTo(Screen.Dashboard)
                    },
                    onBackClick = {
                        appState.navigateTo(Screen.Welcome)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.Dashboard -> {
                appState.checklistData?.let { checklistData ->
                    DashboardScreen(
                        checklistData = checklistData,
                        selectedTabIndex = 0,
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            
            is Screen.TaskDetail -> {
                appState.selectedTask?.let { task ->
                    TaskDetailScreen(
                        task = task,
                        onBackClick = {
                            appState.navigateTo(Screen.Dashboard)
                        },
                        onTaskUpdate = { updatedTask ->
                            appState.updateTask(updatedTask)
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
                    onReorganizeTasks = {
                        appState.reorganizeTasksByDueDate()
                    },
                    onClearData = {
                        appState.clearAllData()
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
                    modifier = Modifier.padding(innerPadding)
                )
            }
            
            Screen.DefectList -> {
                DefectListScreen(
                    defects = appState.defects,
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
                    userData = appState.userData,
                    onSave = { defect ->
                        if (appState.selectedDefect != null) {
                            appState.updateDefect(defect)
                            appState.navigateTo(Screen.DefectList)
                        } else {
                            appState.addDefect(defect)
                            appState.selectDefect(defect)
                            appState.navigateTo(Screen.DefectDetail)
                        }
                    },
                    onBack = {
                        appState.navigateTo(Screen.DefectList)
                    },
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
                    },
                    onAddDefect = { newDefect ->
                        appState.selectDefect(newDefect)
                        appState.navigateTo(Screen.AddEditDefect)
                    },
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
        
        // Tutorial Dialog
        SimpleTutorialDialog(
            isVisible = tutorialState.isVisible,
            title = tutorialState.title,
            description = tutorialState.description,
            onClose = { tutorialState.closeTutorial() }
        )
    }
}