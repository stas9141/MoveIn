package com.example.movein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.movein.shared.storage.AppStorage
import com.example.movein.shared.cloud.CloudStorage
import com.example.movein.auth.GoogleSignInHelper
import com.google.firebase.FirebaseApp

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
    
    // Get the activity for Google Sign-In
    val activity = context as? ComponentActivity
    val googleSignInHelper = remember(activity) { 
        try {
            activity?.let { GoogleSignInHelper(it) }
        } catch (e: Exception) {
            null // Handle Google Sign-In helper creation errors
        }
    }
    
    MoveInTheme(darkTheme = appState.isDarkMode) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (appState.currentScreen) {
            Screen.Welcome -> {
                WelcomeScreen(
                    onGetStartedClick = {
                        appState.navigateTo(Screen.ApartmentDetails)
                    },
                    onSignInClick = {
                        appState.navigateTo(Screen.Login)
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
                        },
                        onSettingsClick = {
                            appState.navigateTo(Screen.Settings)
                        },
                        onDefectListClick = {
                            appState.navigateTo(Screen.DefectList)
                        },
                        onCalendarClick = {
                            appState.navigateTo(Screen.Calendar)
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
    }
}