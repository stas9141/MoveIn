package com.example.movein.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.movein.data.ChecklistData
import com.example.movein.data.ChecklistItem
import com.example.movein.data.Priority
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testChecklistData = ChecklistData(
        firstWeek = listOf(
            ChecklistItem(
                id = "task1",
                title = "Test Task 1",
                description = "Test Description 1",
                category = "Security",
                priority = Priority.HIGH
            ),
            ChecklistItem(
                id = "task2",
                title = "Test Task 2",
                description = "Test Description 2",
                category = "Cleaning",
                priority = Priority.MEDIUM
            )
        ),
        firstMonth = emptyList(),
        firstYear = emptyList()
    )

    @Test
    fun dashboardScreen_shouldDisplayCorrectContent() {
        var taskClicked = false
        var taskToggled = false
        var addTaskClicked = false
        var settingsClicked = false
        
        composeTestRule.setContent {
            DashboardScreen(
                checklistData = testChecklistData,
                selectedTabIndex = 0,
                onTabSelected = {},
                onTaskClick = { taskClicked = true },
                onTaskToggle = { taskToggled = true },
                onAddTask = { _ -> addTaskClicked = true },
                onSettingsClick = { settingsClicked = true }
            )
        }

        // Check if main elements are displayed
        composeTestRule.onNodeWithText("Welcome to your new home!").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Week").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Month").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Year").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Task 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Task 2").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_shouldHandleTaskClick() {
        var taskClicked = false
        var clickedTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            DashboardScreen(
                checklistData = testChecklistData,
                selectedTabIndex = 0,
                onTabSelected = {},
                onTaskClick = { task ->
                    taskClicked = true
                    clickedTask = task
                },
                onTaskToggle = {},
                onAddTask = { _ -> },
                onSettingsClick = {}
            )
        }

        // Click on a task
        composeTestRule.onNodeWithText("Test Task 1").performClick()
        
        // Verify the callback was called
        assertTrue(taskClicked)
        assertNotNull(clickedTask)
        assertEquals("Test Task 1", clickedTask!!.title)
    }

    @Test
    fun dashboardScreen_shouldHandleTaskToggle() {
        var taskToggled = false
        var toggledTask: ChecklistItem? = null
        
        composeTestRule.setContent {
            DashboardScreen(
                checklistData = testChecklistData,
                selectedTabIndex = 0,
                onTabSelected = {},
                onTaskClick = {},
                onTaskToggle = { task ->
                    taskToggled = true
                    toggledTask = task
                },
                onAddTask = { _ -> },
                onSettingsClick = {}
            )
        }

        // Find and click the checkbox
        composeTestRule.onAllNodesWithContentDescription("Checkbox")[0].performClick()
        
        // Verify the callback was called
        assertTrue(taskToggled)
        assertNotNull(toggledTask)
        assertEquals("Test Task 1", toggledTask!!.title)
    }

    @Test
    fun dashboardScreen_shouldShowAddTaskDialog() {
        composeTestRule.setContent {
            DashboardScreen(
                checklistData = testChecklistData,
                selectedTabIndex = 0,
                onTabSelected = {},
                onTaskClick = {},
                onTaskToggle = {},
                onAddTask = { _ -> },
                onSettingsClick = {}
            )
        }

        // Click the add task button
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        
        // Verify the dialog is shown
        composeTestRule.onNodeWithText("Add New Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task Name *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Priority").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_shouldHandleSettingsClick() {
        var settingsClicked = false
        
        composeTestRule.setContent {
            DashboardScreen(
                checklistData = testChecklistData,
                selectedTabIndex = 0,
                onTabSelected = {},
                onTaskClick = {},
                onTaskToggle = {},
                onAddTask = { _ -> },
                onSettingsClick = { settingsClicked = true }
            )
        }

        // Click the settings button
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        
        // Verify the callback was called
        assertTrue(settingsClicked)
    }

    @Test
    fun dashboardScreen_shouldHandleTabSelection() {
        var tabSelected = false
        var selectedTabIndex = -1
        
        composeTestRule.setContent {
            DashboardScreen(
                checklistData = testChecklistData,
                selectedTabIndex = 0,
                onTabSelected = { index ->
                    tabSelected = true
                    selectedTabIndex = index
                },
                onTaskClick = {},
                onTaskToggle = {},
                onAddTask = { _ -> },
                onSettingsClick = {}
            )
        }

        // Click on First Month tab
        composeTestRule.onNodeWithText("First Month").performClick()
        
        // Verify the callback was called
        assertTrue(tabSelected)
        assertEquals(1, selectedTabIndex)
    }

    @Test
    fun dashboardScreen_shouldDisplayPriorityDropdown() {
        composeTestRule.setContent {
            DashboardScreen(
                checklistData = testChecklistData,
                selectedTabIndex = 0,
                onTabSelected = {},
                onTaskClick = {},
                onTaskToggle = {},
                onAddTask = { _ -> },
                onSettingsClick = {}
            )
        }

        // Check if priority dropdown is displayed
        composeTestRule.onNodeWithText("HIGH").assertIsDisplayed()
        composeTestRule.onNodeWithText("MEDIUM").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_shouldHandlePriorityChange() {
        var priorityChanged = false
        var newPriority: Priority? = null
        
        composeTestRule.setContent {
            DashboardScreen(
                checklistData = testChecklistData,
                selectedTabIndex = 0,
                onTabSelected = {},
                onTaskClick = {},
                onTaskToggle = { task ->
                    priorityChanged = true
                    newPriority = task.priority
                },
                onAddTask = { _ -> },
                onSettingsClick = {}
            )
        }

        // Click on priority dropdown
        composeTestRule.onNodeWithText("HIGH").performClick()
        
        // Select a different priority
        composeTestRule.onNodeWithText("LOW").performClick()
        
        // Verify the callback was called
        assertTrue(priorityChanged)
        assertEquals(Priority.LOW, newPriority)
    }
}
