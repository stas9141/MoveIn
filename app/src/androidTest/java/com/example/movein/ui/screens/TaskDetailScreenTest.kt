package com.example.movein.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.movein.data.ChecklistItem
import com.example.movein.data.FileAttachment
import com.example.movein.data.Priority
import com.example.movein.data.SubTask
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class TaskDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTask = ChecklistItem(
        id = "test_task",
        title = "Test Task",
        description = "Test Description",
        category = "Test Category",
        notes = "Test Notes",
        priority = Priority.HIGH,
        dueDate = "12/31/2024",
        attachments = listOf(
            FileAttachment(
                id = "attachment1",
                name = "test.jpg",
                type = "image",
                uri = "content://test",
                size = 1024L
            )
        ),
        subTasks = listOf(
            SubTask(
                id = "subtask1",
                title = "Test Sub Task",
                isCompleted = false
            )
        )
    )

    @Test
    fun taskDetailScreen_shouldDisplayCorrectContent() {
        var taskUpdated = false
        var updatedTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = { task ->
                    taskUpdated = true
                    updatedTask = task
                },
                onBackClick = {}
            )
        }

        // Check if main elements are displayed
        composeTestRule.onNodeWithText("Task Details").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Notes").assertIsDisplayed()
        composeTestRule.onNodeWithText("HIGH").assertIsDisplayed()
        composeTestRule.onNodeWithText("Due: 12/31/2024").assertIsDisplayed()
    }

    @Test
    fun taskDetailScreen_shouldHandleBackClick() {
        var backClicked = false
        
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = {},
                onBackClick = { backClicked = true }
            )
        }

        // Click back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // Verify the callback was called
        assertTrue(backClicked)
    }

    @Test
    fun taskDetailScreen_shouldDisplayAttachments() {
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = {},
                onBackClick = {}
            )
        }

        // Check if attachments are displayed
        composeTestRule.onNodeWithText("Attachments:").assertIsDisplayed()
        composeTestRule.onNodeWithText("test.jpg").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 KB").assertIsDisplayed()
    }

    @Test
    fun taskDetailScreen_shouldDisplaySubTasks() {
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = {},
                onBackClick = {}
            )
        }

        // Check if sub tasks are displayed
        composeTestRule.onNodeWithText("Sub Tasks").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Sub Task").assertIsDisplayed()
    }

    @Test
    fun taskDetailScreen_shouldHandleNotesUpdate() {
        var taskUpdated = false
        var updatedTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = { task ->
                    taskUpdated = true
                    updatedTask = task
                },
                onBackClick = {}
            )
        }

        // Find the notes text field and update it
        composeTestRule.onNodeWithText("Test Notes").performClick()
        composeTestRule.onNodeWithText("Test Notes").performTextInput("Updated Notes")
        
        // Verify the callback was called
        assertTrue(taskUpdated)
        assertNotNull(updatedTask)
        assertEquals("Updated Notes", updatedTask!!.notes)
    }

    @Test
    fun taskDetailScreen_shouldHandlePriorityChange() {
        var taskUpdated = false
        var updatedTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = { task ->
                    taskUpdated = true
                    updatedTask = task
                },
                onBackClick = {}
            )
        }

        // Click on priority button
        composeTestRule.onNodeWithText("Priority & Due Date").performClick()
        
        // Select a different priority
        composeTestRule.onNodeWithText("MEDIUM").performClick()
        
        // Verify the callback was called
        assertTrue(taskUpdated)
        assertNotNull(updatedTask)
        assertEquals(Priority.MEDIUM, updatedTask!!.priority)
    }

    @Test
    fun taskDetailScreen_shouldHandleDueDateChange() {
        var taskUpdated = false
        var updatedTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = { task ->
                    taskUpdated = true
                    updatedTask = task
                },
                onBackClick = {}
            )
        }

        // Click on due date button
        composeTestRule.onNodeWithText("Set Due Date").performClick()
        
        // Enter new due date
        composeTestRule.onNodeWithText("Enter due date (MM/DD/YYYY)").performTextInput("01/15/2025")
        composeTestRule.onNodeWithText("Set").performClick()
        
        // Verify the callback was called
        assertTrue(taskUpdated)
        assertNotNull(updatedTask)
        assertEquals("01/15/2025", updatedTask!!.dueDate)
    }

    @Test
    fun taskDetailScreen_shouldHandleSubTaskToggle() {
        var taskUpdated = false
        var updatedTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = { task ->
                    taskUpdated = true
                    updatedTask = task
                },
                onBackClick = {}
            )
        }

        // Click on sub task checkbox
        composeTestRule.onAllNodesWithContentDescription("Sub Task Checkbox")[0].performClick()
        
        // Verify the callback was called
        assertTrue(taskUpdated)
        assertNotNull(updatedTask)
        assertTrue(updatedTask!!.subTasks[0].isCompleted)
    }

    @Test
    fun taskDetailScreen_shouldHandleAddSubTask() {
        var taskUpdated = false
        var updatedTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = { task ->
                    taskUpdated = true
                    updatedTask = task
                },
                onBackClick = {}
            )
        }

        // Add new sub task
        composeTestRule.onNodeWithText("Add Sub Task").performClick()
        composeTestRule.onNodeWithText("Enter sub task title").performTextInput("New Sub Task")
        composeTestRule.onNodeWithText("Add").performClick()
        
        // Verify the callback was called
        assertTrue(taskUpdated)
        assertNotNull(updatedTask)
        assertEquals(2, updatedTask!!.subTasks.size)
        assertEquals("New Sub Task", updatedTask!!.subTasks[1].title)
    }

    @Test
    fun taskDetailScreen_shouldHandleAttachmentDialog() {
        composeTestRule.setContent {
            TaskDetailScreen(
                task = testTask,
                onTaskUpdate = {},
                onBackClick = {}
            )
        }

        // Click on attachment button
        composeTestRule.onNodeWithContentDescription("Attach File").performClick()
        
        // Verify attachment dialog is displayed
        composeTestRule.onNodeWithText("Add Attachment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Image").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add File").assertIsDisplayed()
    }
}
