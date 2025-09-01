package com.example.movein

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.movein.data.ChecklistData
import com.example.movein.data.ChecklistItem
import com.example.movein.data.UserData
import com.example.movein.data.Defect
import com.example.movein.data.DefectStorage
import com.example.movein.navigation.Screen

class AppState(private val defectStorage: DefectStorage) {
    var currentScreen by mutableStateOf<Screen>(Screen.Welcome)
        private set
    
    var userData by mutableStateOf<UserData?>(null)
        private set
    
    var checklistData by mutableStateOf<ChecklistData?>(null)
        private set
    
    var selectedTask by mutableStateOf<ChecklistItem?>(null)
        private set
    
    var defects by mutableStateOf<List<Defect>>(defectStorage.loadDefects())
        private set
    
    var selectedDefect by mutableStateOf<Defect?>(null)
        private set
    
    var isDarkMode by mutableStateOf(false)
        private set

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    fun initializeUserData(data: UserData) {
        userData = data
        // Generate personalized checklist based on user data
        checklistData = com.example.movein.data.ChecklistDataGenerator.generatePersonalizedChecklist(data)
    }

    fun updateTask(task: ChecklistItem) {
        val currentUserData = userData ?: return
        val currentData = checklistData ?: return
        
        // Create updated lists with the modified task
        val updatedFirstWeek = currentData.firstWeek.map { 
            if (it.id == task.id) task else it 
        }
        val updatedFirstMonth = currentData.firstMonth.map { 
            if (it.id == task.id) task else it 
        }
        val updatedFirstYear = currentData.firstYear.map { 
            if (it.id == task.id) task else it 
        }
        
        // Update the checklist data
        checklistData = ChecklistData(
            firstWeek = updatedFirstWeek,
            firstMonth = updatedFirstMonth,
            firstYear = updatedFirstYear
        )
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
        defectStorage.saveDefects(defects)
    }
    
    fun updateDefect(updatedDefect: Defect) {
        defects = defects.map { if (it.id == updatedDefect.id) updatedDefect else it }
        defectStorage.saveDefects(defects)
    }
    
    fun deleteDefect(defectId: String) {
        defects = defects.filter { it.id != defectId }
        defectStorage.saveDefects(defects)
    }
    
    fun addTask(newTask: ChecklistItem) {
        val currentData = checklistData ?: return
        
        // Determine which tab to add the task to based on due date
        val targetTab = when {
            newTask.dueDate == null -> 0 // No due date = First Week
            else -> {
                val taskDate = parseDate(newTask.dueDate)
                val today = java.time.LocalDate.now()
                
                when {
                    taskDate == null -> 0 // Invalid date = First Week
                    taskDate.isBefore(today.plusDays(7)) -> 0 // Within a week = First Week
                    taskDate.isBefore(today.plusMonths(1)) -> 1 // Within a month = First Month
                    else -> 2 // Beyond a month = First Year
                }
            }
        }
        
        // Add the task to the appropriate list
        val updatedData = when (targetTab) {
            0 -> currentData.copy(firstWeek = currentData.firstWeek + newTask)
            1 -> currentData.copy(firstMonth = currentData.firstMonth + newTask)
            2 -> currentData.copy(firstYear = currentData.firstYear + newTask)
            else -> currentData.copy(firstWeek = currentData.firstWeek + newTask)
        }
        
        checklistData = updatedData
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
}
