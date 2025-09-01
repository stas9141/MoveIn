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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
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
import com.example.movein.ui.theme.MoveInTheme
import com.example.movein.data.DefectStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoveInApp()
        }
    }
}

@Composable
fun MoveInApp() {
    val context = LocalContext.current
    val defectStorage = remember(context) { DefectStorage(context) }
    val appState = remember { AppState(defectStorage) }
    
    MoveInTheme(darkTheme = appState.isDarkMode) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (appState.currentScreen) {
            Screen.Welcome -> {
                WelcomeScreen(
                    onGetStartedClick = {
                        appState.navigateTo(Screen.ApartmentDetails)
                    },
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
        }
        }
    }
}