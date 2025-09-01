package com.example.movein.data

data class UserData(
    val rooms: Int = 4,
    val selectedRoomNames: List<String> = listOf("Salon", "Kitchen", "Master Bedroom", "Mamad"),
    val bathrooms: Int = 1,
    val parking: Int = 1,
    val warehouse: Boolean = false,
    val balconies: Int = 0
)

enum class Priority {
    LOW, MEDIUM, HIGH
}

data class FileAttachment(
    val id: String,
    val name: String,
    val type: String, // "image" or "file"
    val uri: String,
    val size: Long = 0L
)

data class ChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val attachments: List<FileAttachment> = emptyList(),
    val subTasks: List<SubTask> = emptyList(),
    val priority: Priority = Priority.MEDIUM,
    val dueDate: String? = null,
    val isUserAdded: Boolean = false
)

data class SubTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

data class ChecklistData(
    val firstWeek: List<ChecklistItem>,
    val firstMonth: List<ChecklistItem>,
    val firstYear: List<ChecklistItem>
)

enum class DefectStatus {
    OPEN, IN_PROGRESS, CLOSED
}

enum class DefectCategory {
    ELECTRICITY, PLUMBING, INSTALLATIONS, WINDOWS, WALLS_FLOORS, OTHER
}

data class Defect(
    val id: String,
    val location: String,
    val category: DefectCategory,
    val priority: Priority,
    val description: String,
    val images: List<String> = emptyList(),
    val status: DefectStatus = DefectStatus.OPEN,
    val createdAt: String,
    val dueDate: String? = null,
    val subTasks: List<SubTask> = emptyList(),
    val notes: String = "",
    val assignedTo: String? = null
)
