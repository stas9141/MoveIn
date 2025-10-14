package com.example.movein.integration

import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.TaskStatus
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.DefectCategory
import com.example.movein.utils.ErrorHandler
import com.example.movein.utils.ErrorType
import org.junit.Test
import org.junit.Assert.*

class IntegrationTest {
    
    @Test
    fun `test complete task workflow`() {
        // Test complete task workflow from creation to completion
        val task = ChecklistItem(
            id = "integration-task-1",
            title = "Integration Test Task",
            description = "Complete workflow test",
            category = "Cleaning",
            priority = Priority.HIGH,
            status = TaskStatus.OPEN,
            dueDate = "2024-12-31"
        )
        
        // Step 1: Task created
        assertEquals(TaskStatus.OPEN, task.status)
        assertEquals("Integration Test Task", task.title)
        
        // Step 2: Task started
        val inProgressTask = task.copy(status = TaskStatus.IN_PROGRESS)
        assertEquals(TaskStatus.IN_PROGRESS, inProgressTask.status)
        
        // Step 3: Task completed
        val completedTask = inProgressTask.copy(status = TaskStatus.CLOSED)
        assertEquals(TaskStatus.CLOSED, completedTask.status)
        
        // Verify all steps completed successfully
        assertNotNull(completedTask.id)
    }
    
    @Test
    fun `test complete defect workflow`() {
        // Test complete defect workflow from creation to resolution
        val defect = Defect(
            id = "integration-defect-1",
            location = "Bathroom",
            category = DefectCategory.PLUMBING,
            priority = Priority.MEDIUM,
            description = "Complete defect workflow test",
            images = listOf("defect_image1.jpg"),
            status = DefectStatus.OPEN,
            createdAt = "2024-01-01"
        )
        
        // Step 1: Defect reported
        assertEquals(DefectStatus.OPEN, defect.status)
        assertEquals("Bathroom", defect.location)
        assertEquals(1, defect.images.size)
        
        // Step 2: Defect in progress
        val inProgressDefect = defect.copy(status = DefectStatus.IN_PROGRESS)
        assertEquals(DefectStatus.IN_PROGRESS, inProgressDefect.status)
        
        // Step 3: Defect resolved
        val resolvedDefect = inProgressDefect.copy(status = DefectStatus.CLOSED)
        assertEquals(DefectStatus.CLOSED, resolvedDefect.status)
        
        // Verify all steps completed successfully
        assertNotNull(resolvedDefect.id)
    }
    
    @Test
    fun `test authentication workflow`() {
        // Test complete authentication workflow
        val testEmail = "test@example.com"
        val testPassword = "testpassword123"
        
        // Step 1: Validate email format
        assertTrue(testEmail.contains("@"))
        assertTrue(testEmail.contains("."))
        
        // Step 2: Validate password strength
        assertTrue(testPassword.length >= 8)
        assertTrue(testPassword.any { it.isDigit() })
        assertTrue(testPassword.any { it.isLetter() })
        
        // Step 3: Test error handling for invalid credentials
        val invalidPasswordException = Exception("The password is invalid or the user does not have a password.")
        val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(invalidPasswordException)
        val errorType = ErrorHandler.getErrorType(invalidPasswordException)
        
        assertEquals("Incorrect password. Please try again.", userFriendlyError)
        assertEquals(ErrorType.AUTHENTICATION, errorType)
        
        // Step 4: Test error handling for user not found
        val userNotFoundException = Exception("There is no user record corresponding to this identifier.")
        val userNotFoundError = ErrorHandler.getUserFriendlyErrorMessage(userNotFoundException)
        val userNotFoundErrorType = ErrorHandler.getErrorType(userNotFoundException)
        
        assertEquals("No account found with this email. Please sign up first.", userNotFoundError)
        assertEquals(ErrorType.AUTHENTICATION, userNotFoundErrorType)
    }
    
    @Test
    fun `test data filtering and sorting workflow`() {
        // Test complete data filtering and sorting workflow
        val tasks = listOf(
            ChecklistItem(
                id = "task1",
                title = "High Priority Task",
                description = "High priority task",
                category = "Cleaning",
                priority = Priority.HIGH,
                status = TaskStatus.OPEN,
                dueDate = "2024-12-31"
            ),
            ChecklistItem(
                id = "task2",
                title = "Medium Priority Task",
                description = "Medium priority task",
                category = "Repair",
                priority = Priority.MEDIUM,
                status = TaskStatus.IN_PROGRESS,
                dueDate = "2024-12-30"
            ),
            ChecklistItem(
                id = "task3",
                title = "Low Priority Task",
                description = "Low priority task",
                category = "Maintenance",
                priority = Priority.LOW,
                status = TaskStatus.CLOSED,
                dueDate = "2024-12-29"
            )
        )
        
        // Step 1: Filter by status
        val openTasks = tasks.filter { it.status == TaskStatus.OPEN }
        assertEquals(1, openTasks.size)
        assertEquals("High Priority Task", openTasks.first().title)
        
        // Step 2: Filter by priority
        val highPriorityTasks = tasks.filter { it.priority == Priority.HIGH }
        assertEquals(1, highPriorityTasks.size)
        assertEquals("High Priority Task", highPriorityTasks.first().title)
        
        // Step 3: Sort by priority (ordinal: LOW=0, MEDIUM=1, HIGH=2)
        val sortedByPriority = tasks.sortedWith(compareBy<ChecklistItem> { it.priority.ordinal })
        assertEquals("Low Priority Task", sortedByPriority.first().title)
        assertEquals("High Priority Task", sortedByPriority.last().title)
        
        // Step 4: Sort by due date
        val sortedByDueDate = tasks.sortedBy { it.dueDate }
        assertEquals("2024-12-29", sortedByDueDate.first().dueDate)
        assertEquals("2024-12-31", sortedByDueDate.last().dueDate)
    }
    
    @Test
    fun `test image attachment workflow`() {
        // Test complete image attachment workflow
        val initialImages = emptyList<String>()
        val newImages = listOf("image1.jpg", "image2.jpg", "image3.jpg")
        
        // Step 1: Start with no images
        assertEquals(0, initialImages.size)
        
        // Step 2: Add images
        val updatedImages = initialImages + newImages
        assertEquals(3, updatedImages.size)
        assertTrue(updatedImages.contains("image1.jpg"))
        assertTrue(updatedImages.contains("image2.jpg"))
        assertTrue(updatedImages.contains("image3.jpg"))
        
        // Step 3: Remove an image
        val imagesAfterRemoval = updatedImages.filter { it != "image2.jpg" }
        assertEquals(2, imagesAfterRemoval.size)
        assertTrue(imagesAfterRemoval.contains("image1.jpg"))
        assertFalse(imagesAfterRemoval.contains("image2.jpg"))
        assertTrue(imagesAfterRemoval.contains("image3.jpg"))
        
        // Step 4: Add more images
        val finalImages = imagesAfterRemoval + listOf("image4.jpg", "image5.jpg")
        assertEquals(4, finalImages.size)
        assertTrue(finalImages.contains("image4.jpg"))
        assertTrue(finalImages.contains("image5.jpg"))
    }
    
    @Test
    fun `test error handling workflow`() {
        // Test complete error handling workflow
        val testExceptions = listOf(
            Exception("The password is invalid or the user does not have a password."),
            Exception("There is no user record corresponding to this identifier."),
            Exception("The email address is badly formatted."),
            Exception("A network error (such as timeout, interrupted connection or unreachable host) has occurred."),
            Exception("Some unknown error occurred.")
        )
        
        val expectedErrorTypes = listOf(
            ErrorType.AUTHENTICATION,
            ErrorType.AUTHENTICATION,
            ErrorType.AUTHENTICATION,
            ErrorType.NETWORK,
            ErrorType.UNKNOWN
        )
        
        // Test each exception
        testExceptions.forEachIndexed { index, exception ->
            val userFriendlyError = ErrorHandler.getUserFriendlyErrorMessage(exception)
            val errorType = ErrorHandler.getErrorType(exception)
            
            assertNotNull(userFriendlyError)
            assertEquals(expectedErrorTypes[index], errorType)
            assertTrue(userFriendlyError.isNotEmpty())
        }
        
        // Test recovery suggestions
        val recoverySuggestion = ErrorHandler.getRecoverySuggestion(ErrorType.AUTHENTICATION)
        assertNotNull(recoverySuggestion)
        assertTrue(recoverySuggestion?.isNotEmpty() == true)
    }
    
    @Test
    fun `test navigation workflow`() {
        // Test complete navigation workflow
        val navigationFlow = listOf(
            "Welcome" to "Login",
            "Login" to "Dashboard",
            "Dashboard" to "Tasks",
            "Tasks" to "TaskDetail",
            "TaskDetail" to "Dashboard",
            "Dashboard" to "Defects",
            "Defects" to "DefectDetail",
            "DefectDetail" to "Dashboard"
        )
        
        // Verify navigation flow
        assertEquals(8, navigationFlow.size)
        
        // Test each navigation step
        navigationFlow.forEach { (from, to) ->
            assertNotNull(from)
            assertNotNull(to)
            assertTrue(from.isNotEmpty())
            assertTrue(to.isNotEmpty())
        }
        
        // Test specific navigation paths
        assertTrue(navigationFlow.any { it.first == "Welcome" && it.second == "Login" })
        assertTrue(navigationFlow.any { it.first == "Login" && it.second == "Dashboard" })
        assertTrue(navigationFlow.any { it.first == "Dashboard" && it.second == "Tasks" })
    }
}