package com.example.movein.shared.repository

import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChecklistRepository {
    private val _checklistData = MutableStateFlow<ChecklistData?>(null)
    val checklistData: StateFlow<ChecklistData?> = _checklistData.asStateFlow()
    
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()
    
    fun initializeUserData(data: UserData) {
        _userData.value = data
        // Generate personalized checklist based on user data
        _checklistData.value = generatePersonalizedChecklist(data)
    }
    
    fun updateTask(task: ChecklistItem) {
        val currentData = _checklistData.value ?: return
        
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
        _checklistData.value = ChecklistData(
            firstWeek = updatedFirstWeek,
            firstMonth = updatedFirstMonth,
            firstYear = updatedFirstYear
        )
    }
    
    fun addTask(newTask: ChecklistItem) {
        val currentData = _checklistData.value ?: return
        
        // Determine which tab to add the task to based on due date
        val targetTab = when {
            newTask.dueDate == null -> 0 // No due date = First Week
            else -> {
                // For now, just add to first week if there's a due date
                // TODO: Implement proper date parsing and comparison
                0
            }
        }
        
        // Add the task to the appropriate list
        val updatedData = when (targetTab) {
            0 -> currentData.copy(firstWeek = currentData.firstWeek + newTask)
            1 -> currentData.copy(firstMonth = currentData.firstMonth + newTask)
            2 -> currentData.copy(firstYear = currentData.firstYear + newTask)
            else -> currentData.copy(firstWeek = currentData.firstWeek + newTask)
        }
        
        _checklistData.value = updatedData
    }
    
    // Helper function to parse date from MM/dd/yyyy format
    private fun parseDate(dateString: String): String? {
        return try {
            val parts = dateString.split("/")
            if (parts.size == 3) {
                dateString // Return the original string for now
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun generatePersonalizedChecklist(userData: UserData): ChecklistData {
        // This would contain the same logic as ChecklistDataGenerator
        // For now, returning a basic structure
        return ChecklistData(
            firstWeek = emptyList(),
            firstMonth = emptyList(),
            firstYear = emptyList()
        )
    }
}
