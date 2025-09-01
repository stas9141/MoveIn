package com.example.movein.utils

import com.example.movein.data.DefectCategory
import com.example.movein.data.Priority

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
