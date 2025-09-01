package com.example.movein.data

import org.junit.Test
import org.junit.Assert.*

class ModelsTest {

    @Test
    fun `UserData should have correct default values`() {
        val userData = UserData()
        
        assertEquals(4, userData.rooms)
        assertEquals(listOf("Salon", "Kitchen", "Master Bedroom", "Mamad"), userData.selectedRoomNames)
        assertEquals(1, userData.bathrooms)
        assertEquals(1, userData.parking)
        assertFalse(userData.warehouse)
    }

    @Test
    fun `UserData should accept custom values`() {
        val userData = UserData(
            rooms = 4,
            bathrooms = 2,
            parking = 2,
            warehouse = true
        )
        
        assertEquals(4, userData.rooms)
        assertEquals(2, userData.bathrooms)
        assertEquals(2, userData.parking)
        assertTrue(userData.warehouse)
    }

    @Test
    fun `ChecklistItem should have correct default values`() {
        val checklistItem = ChecklistItem(
            id = "test_id",
            title = "Test Task",
            description = "Test Description",
            category = "Test Category"
        )
        
        assertEquals("test_id", checklistItem.id)
        assertEquals("Test Task", checklistItem.title)
        assertEquals("Test Description", checklistItem.description)
        assertEquals("Test Category", checklistItem.category)
        assertFalse(checklistItem.isCompleted)
        assertEquals("", checklistItem.notes)
        assertTrue(checklistItem.attachments.isEmpty())
        assertTrue(checklistItem.subTasks.isEmpty())
        assertEquals(Priority.MEDIUM, checklistItem.priority)
        assertNull(checklistItem.dueDate)
        assertFalse(checklistItem.isUserAdded)
    }

    @Test
    fun `ChecklistItem should accept custom values`() {
        val subTask = SubTask(
            id = "subtask_id",
            title = "Sub Task",
            isCompleted = true
        )
        
        val attachment = FileAttachment(
            id = "attachment_id",
            name = "test.jpg",
            type = "image",
            uri = "content://test",
            size = 1024L
        )
        
        val checklistItem = ChecklistItem(
            id = "test_id",
            title = "Test Task",
            description = "Test Description",
            category = "Test Category",
            isCompleted = true,
            notes = "Test Notes",
            attachments = listOf(attachment),
            subTasks = listOf(subTask),
            priority = Priority.HIGH,
            dueDate = "12/31/2024",
            isUserAdded = true
        )
        
        assertEquals("test_id", checklistItem.id)
        assertEquals("Test Task", checklistItem.title)
        assertTrue(checklistItem.isCompleted)
        assertEquals("Test Notes", checklistItem.notes)
        assertEquals(1, checklistItem.attachments.size)
        assertEquals(1, checklistItem.subTasks.size)
        assertEquals(Priority.HIGH, checklistItem.priority)
        assertEquals("12/31/2024", checklistItem.dueDate)
        assertTrue(checklistItem.isUserAdded)
    }

    @Test
    fun `SubTask should have correct values`() {
        val subTask = SubTask(
            id = "subtask_id",
            title = "Sub Task",
            isCompleted = true
        )
        
        assertEquals("subtask_id", subTask.id)
        assertEquals("Sub Task", subTask.title)
        assertTrue(subTask.isCompleted)
    }

    @Test
    fun `FileAttachment should have correct values`() {
        val attachment = FileAttachment(
            id = "attachment_id",
            name = "test.jpg",
            type = "image",
            uri = "content://test",
            size = 1024L
        )
        
        assertEquals("attachment_id", attachment.id)
        assertEquals("test.jpg", attachment.name)
        assertEquals("image", attachment.type)
        assertEquals("content://test", attachment.uri)
        assertEquals(1024L, attachment.size)
    }

    @Test
    fun `Priority enum should have correct values`() {
        assertEquals(3, Priority.values().size)
        assertTrue(Priority.values().contains(Priority.LOW))
        assertTrue(Priority.values().contains(Priority.MEDIUM))
        assertTrue(Priority.values().contains(Priority.HIGH))
    }

    @Test
    fun `ChecklistData should have correct structure`() {
        val task1 = ChecklistItem(
            id = "task1",
            title = "Task 1",
            description = "Description 1",
            category = "Category 1"
        )
        
        val task2 = ChecklistItem(
            id = "task2",
            title = "Task 2",
            description = "Description 2",
            category = "Category 2"
        )
        
        val checklistData = ChecklistData(
            firstWeek = listOf(task1),
            firstMonth = listOf(task2),
            firstYear = emptyList()
        )
        
        assertEquals(1, checklistData.firstWeek.size)
        assertEquals(1, checklistData.firstMonth.size)
        assertEquals(0, checklistData.firstYear.size)
        assertEquals("Task 1", checklistData.firstWeek[0].title)
        assertEquals("Task 2", checklistData.firstMonth[0].title)
    }
}
