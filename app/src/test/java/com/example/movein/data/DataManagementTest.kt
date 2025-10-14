package com.example.movein.data

import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.TaskStatus
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.DefectCategory
import org.junit.Test
import org.junit.Assert.*

class DataManagementTest {
    
    @Test
    fun `test task creation and validation`() {
        val task = ChecklistItem(
            id = "test-task-1",
            title = "Test Task",
            description = "This is a test task",
            category = "Cleaning",
            priority = Priority.HIGH,
            status = TaskStatus.OPEN,
            dueDate = "2024-12-31"
        )
        
        assertEquals("Test Task", task.title)
        assertEquals("Cleaning", task.category)
        assertEquals(Priority.HIGH, task.priority)
        assertEquals(TaskStatus.OPEN, task.status)
        assertNotNull(task.id)
    }
    
    @Test
    fun `test defect creation and validation`() {
        val defect = Defect(
            id = "test-defect-1",
            location = "Bathroom",
            category = DefectCategory.PLUMBING,
            priority = Priority.MEDIUM,
            description = "This is a test defect",
            images = listOf("image1.jpg", "image2.jpg"),
            status = DefectStatus.OPEN,
            createdAt = "2024-01-01"
        )
        
        assertEquals("Bathroom", defect.location)
        assertEquals(DefectCategory.PLUMBING, defect.category)
        assertEquals(Priority.MEDIUM, defect.priority)
        assertEquals(DefectStatus.OPEN, defect.status)
        assertEquals(2, defect.images.size)
        assertTrue(defect.images.contains("image1.jpg"))
    }
    
    @Test
    fun `test task status transitions`() {
        val task = ChecklistItem(
            id = "test-task-2",
            title = "Status Test Task",
            description = "Testing status transitions",
            category = "Cleaning",
            priority = Priority.LOW,
            status = TaskStatus.OPEN,
            dueDate = "2024-12-31"
        )
        
        // Test status transitions
        val inProgressTask = task.copy(status = TaskStatus.IN_PROGRESS)
        assertEquals(TaskStatus.IN_PROGRESS, inProgressTask.status)
        
        val completedTask = inProgressTask.copy(status = TaskStatus.CLOSED)
        assertEquals(TaskStatus.CLOSED, completedTask.status)
    }
    
    @Test
    fun `test defect status transitions`() {
        val defect = Defect(
            id = "test-defect-2",
            location = "Kitchen",
            category = DefectCategory.ELECTRICITY,
            priority = Priority.HIGH,
            description = "Testing defect status transitions",
            images = emptyList(),
            status = DefectStatus.OPEN,
            createdAt = "2024-01-01"
        )
        
        // Test status transitions
        val inProgressDefect = defect.copy(status = DefectStatus.IN_PROGRESS)
        assertEquals(DefectStatus.IN_PROGRESS, inProgressDefect.status)
        
        val closedDefect = inProgressDefect.copy(status = DefectStatus.CLOSED)
        assertEquals(DefectStatus.CLOSED, closedDefect.status)
    }
    
    @Test
    fun `test priority levels`() {
        val highPriorityTask = ChecklistItem(
            id = "high-priority",
            title = "High Priority Task",
            description = "This is high priority",
            category = "Cleaning",
            priority = Priority.HIGH,
            status = TaskStatus.OPEN,
            dueDate = "2024-12-31"
        )
        
        val mediumPriorityTask = highPriorityTask.copy(priority = Priority.MEDIUM)
        val lowPriorityTask = highPriorityTask.copy(priority = Priority.LOW)
        
        assertEquals(Priority.HIGH, highPriorityTask.priority)
        assertEquals(Priority.MEDIUM, mediumPriorityTask.priority)
        assertEquals(Priority.LOW, lowPriorityTask.priority)
    }
    
    @Test
    fun `test category types`() {
        val categories = listOf(
            DefectCategory.ELECTRICITY,
            DefectCategory.PLUMBING,
            DefectCategory.INSTALLATIONS,
            DefectCategory.WINDOWS,
            DefectCategory.WALLS_FLOORS,
            DefectCategory.OTHER
        )
        
        assertEquals(6, categories.size)
        assertTrue(categories.contains(DefectCategory.ELECTRICITY))
        assertTrue(categories.contains(DefectCategory.PLUMBING))
    }
    
    @Test
    fun `test task with attachments`() {
        val taskWithAttachments = ChecklistItem(
            id = "task-with-attachments",
            title = "Task with Attachments",
            description = "This task has attachments",
            category = "Inspection",
            priority = Priority.MEDIUM,
            status = TaskStatus.OPEN,
            dueDate = "2024-12-31",
            attachments = listOf(
                com.example.movein.shared.data.FileAttachment(
                    id = "att1",
                    name = "task_image1.jpg",
                    type = "image",
                    uri = "file://task_image1.jpg"
                ),
                com.example.movein.shared.data.FileAttachment(
                    id = "att2",
                    name = "task_image2.jpg",
                    type = "image",
                    uri = "file://task_image2.jpg"
                )
            )
        )
        
        assertEquals(2, taskWithAttachments.attachments.size)
        assertTrue(taskWithAttachments.attachments.any { it.name == "task_image1.jpg" })
        assertTrue(taskWithAttachments.attachments.any { it.name == "task_image2.jpg" })
    }
    
    @Test
    fun `test defect with multiple images`() {
        val defectWithImages = Defect(
            id = "defect-with-images",
            location = "Kitchen",
            category = DefectCategory.ELECTRICITY,
            priority = Priority.HIGH,
            description = "This defect has multiple images",
            images = listOf("defect1.jpg", "defect2.jpg", "defect3.jpg", "defect4.jpg"),
            status = DefectStatus.OPEN,
            createdAt = "2024-01-01"
        )
        
        assertEquals(4, defectWithImages.images.size)
        assertTrue(defectWithImages.images.contains("defect1.jpg"))
        assertTrue(defectWithImages.images.contains("defect4.jpg"))
    }
}