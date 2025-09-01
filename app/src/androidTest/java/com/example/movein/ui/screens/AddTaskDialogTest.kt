package com.example.movein.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.movein.data.ChecklistItem
import com.example.movein.data.Priority
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class AddTaskDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addTaskDialog_shouldDisplayCorrectContent() {
        var taskAdded = false
        var addedTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            AddTaskDialog(
                onDismiss = {},
                onAddTask = { task ->
                    taskAdded = true
                    addedTask = task
                },
                existingTaskNames = emptyList()
            )
        }

        // Check if main elements are displayed
        composeTestRule.onNodeWithText("Add New Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task Name *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Priority").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun addTaskDialog_shouldValidateRequiredTaskName() {
        composeTestRule.setContent {
            AddTaskDialog(
                onDismiss = {},
                onAddTask = {},
                existingTaskNames = emptyList()
            )
        }

        // Try to add task without name
        composeTestRule.onNodeWithText("Add Task").performClick()
        
        // Verify error message is shown
        composeTestRule.onNodeWithText("Task name is required").assertIsDisplayed()
    }

    @Test
    fun addTaskDialog_shouldValidateUniqueTaskName() {
        composeTestRule.setContent {
            AddTaskDialog(
                onDismiss = {},
                onAddTask = {},
                existingTaskNames = listOf("Existing Task")
            )
        }

        // Enter existing task name
        composeTestRule.onNodeWithText("Enter task name").performTextInput("Existing Task")
        
        // Try to add task
        composeTestRule.onNodeWithText("Add Task").performClick()
        
        // Verify error message is shown
        composeTestRule.onNodeWithText("Task name must be unique").assertIsDisplayed()
    }

    @Test
    fun addTaskDialog_shouldCreateTaskWithValidInput() {
        var taskAdded = false
        var addedTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            AddTaskDialog(
                onDismiss = {},
                onAddTask = { task ->
                    taskAdded = true
                    addedTask = task
                },
                existingTaskNames = emptyList()
            )
        }

        // Enter task name
        composeTestRule.onNodeWithText("Enter task name").performTextInput("New Test Task")
        
        // Enter description
        composeTestRule.onNodeWithText("Enter task description").performTextInput("This is a test task description")
        
        // Add task
        composeTestRule.onNodeWithText("Add Task").performClick()
        
        // Verify task was created
        assertTrue(taskAdded)
        assertNotNull(addedTask)
        assertEquals("New Test Task", addedTask!!.title)
        assertEquals("This is a test task description", addedTask!!.description)
        assertEquals(Priority.MEDIUM, addedTask!!.priority)
        assertEquals("Custom", addedTask!!.category)
        assertTrue(addedTask!!.isUserAdded)
    }

    @Test
    fun addTaskDialog_shouldHandlePrioritySelection() {
        composeTestRule.setContent {
            AddTaskDialog(
                onDismiss = {},
                onAddTask = {},
                existingTaskNames = emptyList()
            )
        }

        // Click on priority to open selection dialog
        composeTestRule.onNodeWithText("MEDIUM").performClick()
        
        // Verify priority selection dialog is shown
        composeTestRule.onNodeWithText("Select Priority").assertIsDisplayed()
        composeTestRule.onNodeWithText("LOW").assertIsDisplayed()
        composeTestRule.onNodeWithText("HIGH").assertIsDisplayed()
        
        // Select HIGH priority
        composeTestRule.onNodeWithText("HIGH").performClick()
        
        // Verify HIGH is now selected
        composeTestRule.onNodeWithText("HIGH").assertIsDisplayed()
    }

    @Test
    fun addTaskDialog_shouldHandleDismiss() {
        var dismissed = false
        
        composeTestRule.setContent {
            AddTaskDialog(
                onDismiss = { dismissed = true },
                onAddTask = {},
                existingTaskNames = emptyList()
            )
        }

        // Click cancel button
        composeTestRule.onNodeWithText("Cancel").performClick()
        
        // Verify dismiss callback was called
        assertTrue(dismissed)
    }

    @Test
    fun addTaskDialog_shouldEnableAddButtonWithValidName() {
        composeTestRule.setContent {
            AddTaskDialog(
                onDismiss = {},
                onAddTask = {},
                existingTaskNames = emptyList()
            )
        }

        // Initially, Add Task button should be disabled
        composeTestRule.onNodeWithText("Add Task").assertIsNotEnabled()
        
        // Enter task name
        composeTestRule.onNodeWithText("Enter task name").performTextInput("Valid Task Name")
        
        // Now Add Task button should be enabled
        composeTestRule.onNodeWithText("Add Task").assertIsEnabled()
    }
}
