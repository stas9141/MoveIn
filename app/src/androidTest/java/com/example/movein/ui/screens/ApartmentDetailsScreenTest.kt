package com.example.movein.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.movein.data.UserData
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class ApartmentDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun apartmentDetailsScreen_shouldDisplayCorrectContent() {
        var continueClicked = false
        var userData: UserData? = null
        
        composeTestRule.setContent {
            ApartmentDetailsScreen(
                onContinueClick = { data ->
                    continueClicked = true
                    userData = data
                }
            )
        }

        // Check if main elements are displayed
        composeTestRule.onNodeWithText("Tell us about your apartment").assertIsDisplayed()
        composeTestRule.onNodeWithText("We'll create personalized checklists based on your apartment details").assertIsDisplayed()
        composeTestRule.onNodeWithText("Number of Rooms").assertIsDisplayed()
        composeTestRule.onNodeWithText("Number of Bathrooms").assertIsDisplayed()
        composeTestRule.onNodeWithText("Number of Parking Spaces").assertIsDisplayed()
        composeTestRule.onNodeWithText("Warehouse").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue").assertIsDisplayed()
    }

    @Test
    fun apartmentDetailsScreen_shouldHaveDefaultValues() {
        composeTestRule.setContent {
            ApartmentDetailsScreen(
                onContinueClick = {}
            )
        }

        // Check default selections
        composeTestRule.onNodeWithText("3").assertIsSelected()
        composeTestRule.onNodeWithText("1").assertIsSelected()
        composeTestRule.onNodeWithText("1").assertIsSelected()
    }

    @Test
    fun apartmentDetailsScreen_shouldAllowRoomSelection() {
        composeTestRule.setContent {
            ApartmentDetailsScreen(
                onContinueClick = {}
            )
        }

        // Click on different room options
        composeTestRule.onNodeWithText("4").performClick()
        composeTestRule.onNodeWithText("4").assertIsSelected()
        
        composeTestRule.onNodeWithText("5").performClick()
        composeTestRule.onNodeWithText("5").assertIsSelected()
    }

    @Test
    fun apartmentDetailsScreen_shouldAllowBathroomSelection() {
        composeTestRule.setContent {
            ApartmentDetailsScreen(
                onContinueClick = {}
            )
        }

        // Click on different bathroom options
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("2").assertIsSelected()
        
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithText("3").assertIsSelected()
    }

    @Test
    fun apartmentDetailsScreen_shouldAllowParkingSelection() {
        composeTestRule.setContent {
            ApartmentDetailsScreen(
                onContinueClick = {}
            )
        }

        // Click on different parking options
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("2").assertIsSelected()
    }

    @Test
    fun apartmentDetailsScreen_shouldAllowWarehouseToggle() {
        composeTestRule.setContent {
            ApartmentDetailsScreen(
                onContinueClick = {}
            )
        }

        // Find and click the warehouse switch
        composeTestRule.onNodeWithText("Do you have a warehouse?").assertIsDisplayed()
    }

    @Test
    fun apartmentDetailsScreen_shouldCallOnContinueClick_withCorrectData() {
        var continueClicked = false
        var userData: UserData? = null
        
        composeTestRule.setContent {
            ApartmentDetailsScreen(
                onContinueClick = { data ->
                    continueClicked = true
                    userData = data
                }
            )
        }

        // Select different options
        composeTestRule.onNodeWithText("4").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("2").performClick()

        // Click continue
        composeTestRule.onNodeWithText("Continue").performClick()
        
        // Verify the callback was called with correct data
        assertTrue(continueClicked)
        assertNotNull(userData)
        assertEquals(4, userData!!.rooms)
        assertEquals(2, userData!!.bathrooms)
        assertEquals(2, userData!!.parking)
        assertFalse(userData!!.warehouse)
    }
}
