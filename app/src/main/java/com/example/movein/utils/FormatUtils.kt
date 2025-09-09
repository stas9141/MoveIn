package com.example.movein.utils

import com.example.movein.shared.data.DefectCategory
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.TaskStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Helper functions to format enum values for display
fun formatCategory(category: DefectCategory): String {
    return when (category) {
        DefectCategory.ELECTRICITY -> "Electricity"
        DefectCategory.PLUMBING -> "Plumbing"
        DefectCategory.INSTALLATIONS -> "Installations"
        DefectCategory.WINDOWS -> "Windows"
        DefectCategory.WALLS_FLOORS -> "Walls & Floors"
        DefectCategory.OTHER -> "Other"
    }
}

fun formatPriority(priority: Priority): String {
    return when (priority) {
        Priority.HIGH -> "High"
        Priority.MEDIUM -> "Medium"
        Priority.LOW -> "Low"
    }
}

fun formatTaskStatus(status: TaskStatus): String {
    return when (status) {
        TaskStatus.OPEN -> "Open"
        TaskStatus.IN_PROGRESS -> "In Progress"
        TaskStatus.CLOSED -> "Closed"
    }
}

// Date formatting utilities
fun formatDateForDisplay(dateString: String?): String {
    if (dateString == null) return "No due date"
    
    return try {
        val parts = dateString.split("/")
        if (parts.size == 3) {
            val month = parts[0].toInt()
            val day = parts[1].toInt()
            val year = parts[2].toInt()
            val date = LocalDate.of(year, month, day)
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            
            when {
                date == today -> "Today"
                date == tomorrow -> "Tomorrow"
                else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
            }
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

fun parseDate(dateString: String): LocalDate? {
    return try {
        val parts = dateString.split("/")
        if (parts.size == 3) {
            val month = parts[0].toInt()
            val day = parts[1].toInt()
            val year = parts[2].toInt()
            LocalDate.of(year, month, day)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
