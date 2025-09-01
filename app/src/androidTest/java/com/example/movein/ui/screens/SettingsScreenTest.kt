package com.example.movein.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_shouldDisplayCorrectContent() {
        var backClicked = false
        var darkModeToggled = false
        
        composeTestRule.setContent {
            SettingsScreen(
                isDarkMode = false,
                onDarkModeToggle = { darkModeToggled = true },
                onBackClick = { backClicked = true }
            )
        }

        // Check if main elements are displayed
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        composeTestRule.onNodeWithText("Light Mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task Reminders").assertIsDisplayed()
        composeTestRule.onNodeWithText("MoveIn - Your New Home Companion").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldHandleBackClick() {
        var backClicked = false
        
        composeTestRule.setContent {
            SettingsScreen(
                isDarkMode = false,
                onDarkModeToggle = {},
                onBackClick = { backClicked = true }
            )
        }

        // Click back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // Verify the callback was called
        assertTrue(backClicked)
    }

    @Test
    fun settingsScreen_shouldHandleDarkModeToggle() {
        var darkModeToggled = false
        var newDarkModeValue = false
        
        composeTestRule.setContent {
            SettingsScreen(
                isDarkMode = false,
                onDarkModeToggle = { isDark ->
                    darkModeToggled = true
                    newDarkModeValue = isDark
                },
                onBackClick = {}
            )
        }

        // Click dark mode switch
        composeTestRule.onNodeWithText("Light Mode").performClick()
        
        // Verify the callback was called
        assertTrue(darkModeToggled)
        assertTrue(newDarkModeValue)
    }

    @Test
    fun settingsScreen_shouldDisplayDarkMode_whenEnabled() {
        composeTestRule.setContent {
            SettingsScreen(
                isDarkMode = true,
                onDarkModeToggle = {},
                onBackClick = {}
            )
        }

        // Check if dark mode is displayed correctly
        composeTestRule.onNodeWithText("Dark Mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark theme enabled").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldDisplayLightMode_whenDisabled() {
        composeTestRule.setContent {
            SettingsScreen(
                isDarkMode = false,
                onDarkModeToggle = {},
                onBackClick = {}
            )
        }

        // Check if light mode is displayed correctly
        composeTestRule.onNodeWithText("Light Mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Light theme enabled").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldDisplayNotificationSettings() {
        composeTestRule.setContent {
            SettingsScreen(
                isDarkMode = false,
                onDarkModeToggle = {},
                onBackClick = {}
            )
        }

        // Check notification settings
        composeTestRule.onNodeWithText("Task Reminders").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get notified about upcoming tasks").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldDisplayAboutSection() {
        composeTestRule.setContent {
            SettingsScreen(
                isDarkMode = false,
                onDarkModeToggle = {},
                onBackClick = {}
            )
        }

        // Check about section
        composeTestRule.onNodeWithText("MoveIn - Your New Home Companion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Version 1.0.0").assertIsDisplayed()
        composeTestRule.onNodeWithText("A comprehensive app to help you organize your apartment move-in process with personalized checklists.").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldHaveCorrectIcons() {
        composeTestRule.setContent {
            SettingsScreen(
                isDarkMode = false,
                onDarkModeToggle = {},
                onBackClick = {}
            )
        }

        // Check if icons are displayed
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Theme Icon").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Notifications").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_shouldHandleMultipleDarkModeToggles() {
        var toggleCount = 0
        var currentDarkMode = false
        
        composeTestRule.setContent {
            SettingsScreen(
                isDarkMode = currentDarkMode,
                onDarkModeToggle = { isDark ->
                    toggleCount++
                    currentDarkMode = isDark
                },
                onBackClick = {}
            )
        }

        // Toggle dark mode multiple times
        composeTestRule.onNodeWithText("Light Mode").performClick()
        composeTestRule.onNodeWithText("Dark Mode").performClick()
        composeTestRule.onNodeWithText("Light Mode").performClick()
        
        // Verify toggles were called
        assertEquals(3, toggleCount)
    }
}
