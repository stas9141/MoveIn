package com.example.movein.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class WelcomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun welcomeScreen_shouldDisplayCorrectContent() {
        var getStartedClicked = false
        
        composeTestRule.setContent {
            WelcomeScreen(
                onGetStartedClick = { getStartedClicked = true }
            )
        }

        // Check if main elements are displayed
        composeTestRule.onNodeWithText("Your New Home Companion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Make your move-in experience smooth and organized with personalized checklists tailored to your new apartment.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started").assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_shouldCallOnGetStartedClick_whenButtonIsClicked() {
        var getStartedClicked = false
        
        composeTestRule.setContent {
            WelcomeScreen(
                onGetStartedClick = { getStartedClicked = true }
            )
        }

        // Click the Get Started button
        composeTestRule.onNodeWithText("Get Started").performClick()
        
        // Verify the callback was called
        assertTrue(getStartedClicked)
    }

    @Test
    fun welcomeScreen_shouldDisplayHomeIcon() {
        composeTestRule.setContent {
            WelcomeScreen(
                onGetStartedClick = {}
            )
        }

        // Check if the home icon is displayed
        composeTestRule.onNodeWithContentDescription("App Logo").assertIsDisplayed()
    }

    @Test
    fun welcomeScreen_shouldHaveCorrectButtonText() {
        composeTestRule.setContent {
            WelcomeScreen(
                onGetStartedClick = {}
            )
        }

        // Verify button text
        composeTestRule.onNodeWithText("Get Started").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started").assertIsEnabled()
    }
}
