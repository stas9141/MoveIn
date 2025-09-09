package com.example.movein

import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.UserData
import com.example.movein.shared.data.FileAttachment
import com.example.movein.shared.data.SubTask
import com.example.movein.navigation.Screen
import com.example.movein.shared.storage.DefectStorage
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import io.mockk.mockk
import io.mockk.every

class AppStateTest {

    private lateinit var appState: AppState

    @Before
    fun setUp() {
        val mockDefectStorage = mockk<DefectStorage>()
        every { mockDefectStorage.loadDefects() } returns emptyList()
        every { mockDefectStorage.saveDefects(any()) } returns Unit
        appState = AppState(mockDefectStorage)
    }

    @Test
    fun `AppState should initialize with welcome screen`() {
        assertEquals(Screen.Welcome, appState.currentScreen)
    }

    @Test
    fun `AppState should initialize with null user data`() {
        assertNull(appState.userData)
    }

    @Test
    fun `AppState should initialize with null checklist data`() {
        assertNull(appState.checklistData)
    }

    @Test
    fun `AppState should initialize with null selected task`() {
        assertNull(appState.selectedTask)
    }

    @Test
    fun `AppState should initialize with light mode`() {
        assertFalse(appState.isDarkMode)
    }

    @Test
    fun `navigateTo should change current screen`() {
        appState.navigateTo(Screen.Dashboard)
        assertEquals(Screen.Dashboard, appState.currentScreen)
    }

    @Test
    fun `initializeUserData should set user data and generate checklist`() {
        val userData = UserData(rooms = 4, bathrooms = 2, parking = 2, warehouse = true)
        
        appState.initializeUserData(userData)
        
        assertEquals(userData, appState.userData)
        assertNotNull(appState.checklistData)
        assertTrue(appState.checklistData!!.firstWeek.isNotEmpty())
    }

    @Test
    fun `updateTask should update task in checklist`() {
        // First initialize with user data
        val userData = UserData()
        appState.initializeUserData(userData)
        
        val originalTask = appState.checklistData!!.firstWeek[0]
        val updatedTask = originalTask.copy(isCompleted = true, notes = "Updated notes")
        
        appState.updateTask(updatedTask)
        
        val updatedTaskInList = appState.checklistData!!.firstWeek.find { it.id == originalTask.id }
        assertNotNull(updatedTaskInList)
        assertTrue(updatedTaskInList!!.isCompleted)
        assertEquals("Updated notes", updatedTaskInList.notes)
    }

    @Test
    fun `selectTask should set selected task`() {
        val task = ChecklistItem(
            id = "test_id",
            title = "Test Task",
            description = "Test Description",
            category = "Test Category"
        )
        
        appState.selectTask(task)
        assertEquals(task, appState.selectedTask)
    }

    @Test
    fun `addTask should add new task to first week`() {
        // First initialize with user data
        val userData = UserData()
        appState.initializeUserData(userData)
        
        val originalCount = appState.checklistData!!.firstWeek.size
        
        val newTask = ChecklistItem(
            id = "new_task_id",
            title = "New Task",
            description = "New Description",
            category = "New Category",
            isUserAdded = true
        )
        
        appState.addTask(newTask)
        
        val updatedCount = appState.checklistData!!.firstWeek.size
        assertEquals(originalCount + 1, updatedCount)
        
        val addedTask = appState.checklistData!!.firstWeek.find { it.id == "new_task_id" }
        assertNotNull(addedTask)
        assertEquals("New Task", addedTask!!.title)
        assertTrue(addedTask.isUserAdded)
    }

    @Test
    fun `toggleDarkMode should switch dark mode state`() {
        assertFalse(appState.isDarkMode)
        
        appState.toggleDarkMode()
        assertTrue(appState.isDarkMode)
        
        appState.toggleDarkMode()
        assertFalse(appState.isDarkMode)
    }

    @Test
    fun `updateTask should handle task with attachments`() {
        // First initialize with user data
        val userData = UserData()
        appState.initializeUserData(userData)
        
        val originalTask = appState.checklistData!!.firstWeek[0]
        val updatedTask = originalTask.copy(
            attachments = listOf(
                FileAttachment(
                    id = "attachment_id",
                    name = "test.jpg",
                    type = "image",
                    uri = "content://test",
                    size = 1024L
                )
            )
        )
        
        appState.updateTask(updatedTask)
        
        val updatedTaskInList = appState.checklistData!!.firstWeek.find { it.id == originalTask.id }
        assertNotNull(updatedTaskInList)
        assertEquals(1, updatedTaskInList!!.attachments.size)
        assertEquals("test.jpg", updatedTaskInList.attachments[0].name)
    }

    @Test
    fun `updateTask should handle task with sub tasks`() {
        // First initialize with user data
        val userData = UserData()
        appState.initializeUserData(userData)
        
        val originalTask = appState.checklistData!!.firstWeek[0]
        val updatedTask = originalTask.copy(
            subTasks = listOf(
                SubTask(
                    id = "subtask_id",
                    title = "Sub Task",
                    isCompleted = true
                )
            )
        )
        
        appState.updateTask(updatedTask)
        
        val updatedTaskInList = appState.checklistData!!.firstWeek.find { it.id == originalTask.id }
        assertNotNull(updatedTaskInList)
        assertEquals(1, updatedTaskInList!!.subTasks.size)
        assertEquals("Sub Task", updatedTaskInList.subTasks[0].title)
        assertTrue(updatedTaskInList.subTasks[0].isCompleted)
    }

    @Test
    fun `updateTask should handle priority changes`() {
        // First initialize with user data
        val userData = UserData()
        appState.initializeUserData(userData)
        
        val originalTask = appState.checklistData!!.firstWeek[0]
        val updatedTask = originalTask.copy(priority = Priority.HIGH)
        
        appState.updateTask(updatedTask)
        
        val updatedTaskInList = appState.checklistData!!.firstWeek.find { it.id == originalTask.id }
        assertNotNull(updatedTaskInList)
        assertEquals(Priority.HIGH, updatedTaskInList!!.priority)
    }

    @Test
    fun `updateTask should handle due date changes`() {
        // First initialize with user data
        val userData = UserData()
        appState.initializeUserData(userData)
        
        val originalTask = appState.checklistData!!.firstWeek[0]
        val updatedTask = originalTask.copy(dueDate = "12/31/2024")
        
        appState.updateTask(updatedTask)
        
        val updatedTaskInList = appState.checklistData!!.firstWeek.find { it.id == originalTask.id }
        assertNotNull(updatedTaskInList)
        assertEquals("12/31/2024", updatedTaskInList!!.dueDate)
    }
}
