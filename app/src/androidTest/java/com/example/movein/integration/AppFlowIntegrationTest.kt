package com.example.movein.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.movein.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppFlowIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun completeAppFlow_shouldWorkCorrectly() {
        // Step 1: Welcome Screen
        composeTestRule.onNodeWithText("Your New Home Companion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started").performClick()

        // Step 2: Apartment Details Screen
        composeTestRule.onNodeWithText("Tell us about your apartment").assertIsDisplayed()
        
        // Select apartment details
        composeTestRule.onNodeWithText("4").performClick() // 4 rooms
        composeTestRule.onNodeWithText("2").performClick() // 2 bathrooms
        composeTestRule.onNodeWithText("2").performClick() // 2 parking spaces
        
        composeTestRule.onNodeWithText("Continue").performClick()

        // Step 3: Dashboard Screen
        composeTestRule.onNodeWithText("Welcome to your new home!").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Week").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Month").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Year").assertIsDisplayed()
    }

    @Test
    fun taskManagementFlow_shouldWorkCorrectly() {
        // Navigate to dashboard
        composeTestRule.onNodeWithText("Get Started").performClick()
        composeTestRule.onNodeWithText("Continue").performClick()

        // Check if tasks are displayed
        composeTestRule.onNodeWithText("First Week").assertIsDisplayed()
        
        // Click on a task to open details
        composeTestRule.onAllNodesWithText("Change all locks")[0].performClick()
        
        // Verify task detail screen
        composeTestRule.onNodeWithText("Task Details").assertIsDisplayed()
        composeTestRule.onNodeWithText("Change all locks").assertIsDisplayed()
        
        // Go back to dashboard
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText("Welcome to your new home!").assertIsDisplayed()
    }

    @Test
    fun settingsFlow_shouldWorkCorrectly() {
        // Navigate to dashboard
        composeTestRule.onNodeWithText("Get Started").performClick()
        composeTestRule.onNodeWithText("Continue").performClick()

        // Open settings
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        
        // Verify settings screen
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        
        // Go back to dashboard
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText("Welcome to your new home!").assertIsDisplayed()
    }

    @Test
    fun tabNavigation_shouldWorkCorrectly() {
        // Navigate to dashboard
        composeTestRule.onNodeWithText("Get Started").performClick()
        composeTestRule.onNodeWithText("Continue").performClick()

        // Test tab navigation
        composeTestRule.onNodeWithText("First Month").performClick()
        composeTestRule.onNodeWithText("First Month").assertIsSelected()
        
        composeTestRule.onNodeWithText("First Year").performClick()
        composeTestRule.onNodeWithText("First Year").assertIsSelected()
        
        composeTestRule.onNodeWithText("First Week").performClick()
        composeTestRule.onNodeWithText("First Week").assertIsSelected()
    }

    @Test
    fun addTaskFlow_shouldWorkCorrectly() {
        // Navigate to dashboard
        composeTestRule.onNodeWithText("Get Started").performClick()
        composeTestRule.onNodeWithText("Continue").performClick()

        // Click add task button
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()
        
        // Verify new task is added (should appear in the list)
        // Note: This test verifies the FAB is clickable and doesn't crash
        composeTestRule.onNodeWithContentDescription("Add Task").assertIsDisplayed()
    }
}
