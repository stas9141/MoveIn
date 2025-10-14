package com.example.movein.performance

import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.TaskStatus
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.DefectCategory
import org.junit.Test
import org.junit.Assert.*

class PerformanceTest {
    
    @Test
    fun `test large dataset filtering performance`() {
        // Test performance with large dataset
        val largeTaskList = generateLargeTaskList(1000)
        val largeDefectList = generateLargeDefectList(1000)
        
        // Test filtering performance
        val startTime = System.currentTimeMillis()
        
        val openTasks = largeTaskList.filter { it.status == TaskStatus.OPEN }
        val highPriorityTasks = largeTaskList.filter { it.priority == Priority.HIGH }
        val cleaningTasks = largeTaskList.filter { it.category == "Cleaning" }
        
        val openDefects = largeDefectList.filter { it.status == DefectStatus.OPEN }
        val repairDefects = largeDefectList.filter { it.category == DefectCategory.PLUMBING }
        
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        // Verify results
        assertTrue(openTasks.isNotEmpty())
        assertTrue(highPriorityTasks.isNotEmpty())
        assertTrue(cleaningTasks.isNotEmpty())
        assertTrue(openDefects.isNotEmpty())
        assertTrue(repairDefects.isNotEmpty())
        
        // Performance should be under 100ms for 1000 items
        assertTrue("Filtering took too long: ${executionTime}ms", executionTime < 100)
    }
    
    @Test
    fun `test large dataset sorting performance`() {
        // Test sorting performance with large dataset
        val largeTaskList = generateLargeTaskList(1000)
        val largeDefectList = generateLargeDefectList(1000)
        
        val startTime = System.currentTimeMillis()
        
        // Sort tasks by priority
        val tasksSortedByPriority = largeTaskList.sortedWith(compareBy<ChecklistItem> { it.priority.ordinal })
        
        // Sort tasks by due date
        val tasksSortedByDueDate = largeTaskList.sortedBy { it.dueDate }
        
        // Sort defects by priority
        val defectsSortedByPriority = largeDefectList.sortedWith(compareBy<Defect> { it.priority.ordinal })
        
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        // Verify results
        assertEquals(largeTaskList.size, tasksSortedByPriority.size)
        assertEquals(largeTaskList.size, tasksSortedByDueDate.size)
        assertEquals(largeDefectList.size, defectsSortedByPriority.size)
        
        // Performance should be under 100ms for 1000 items
        assertTrue("Sorting took too long: ${executionTime}ms", executionTime < 100)
    }
    
    @Test
    fun `test memory usage with large datasets`() {
        // Test memory usage with large datasets
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        val largeTaskList = generateLargeTaskList(5000)
        val largeDefectList = generateLargeDefectList(5000)
        
        val afterCreationMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryUsed = afterCreationMemory - initialMemory
        
        // Verify data was created
        assertEquals(5000, largeTaskList.size)
        assertEquals(5000, largeDefectList.size)
        
        // Memory usage should be reasonable (less than 50MB for 10,000 items)
        assertTrue("Memory usage too high: ${memoryUsed / 1024 / 1024}MB", memoryUsed < 50 * 1024 * 1024)
    }
    
    @Test
    fun `test concurrent operations performance`() {
        // Test concurrent operations performance
        val taskList = generateLargeTaskList(1000)
        val defectList = generateLargeDefectList(1000)
        
        val startTime = System.currentTimeMillis()
        
        // Simulate concurrent operations
        val openTasks = taskList.filter { it.status == TaskStatus.OPEN }
        val inProgressTasks = taskList.filter { it.status == TaskStatus.IN_PROGRESS }
        val closedTasks = taskList.filter { it.status == TaskStatus.CLOSED }
        
        val highPriorityTasks = taskList.filter { it.priority == Priority.HIGH }
        val mediumPriorityTasks = taskList.filter { it.priority == Priority.MEDIUM }
        val lowPriorityTasks = taskList.filter { it.priority == Priority.LOW }
        
        val openDefects = defectList.filter { it.status == DefectStatus.OPEN }
        val inProgressDefects = defectList.filter { it.status == DefectStatus.IN_PROGRESS }
        val closedDefects = defectList.filter { it.status == DefectStatus.CLOSED }
        
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        // Verify all operations completed
        assertTrue(openTasks.isNotEmpty())
        assertTrue(inProgressTasks.isNotEmpty())
        assertTrue(closedTasks.isNotEmpty())
        assertTrue(highPriorityTasks.isNotEmpty())
        assertTrue(mediumPriorityTasks.isNotEmpty())
        assertTrue(lowPriorityTasks.isNotEmpty())
        assertTrue(openDefects.isNotEmpty())
        assertTrue(inProgressDefects.isNotEmpty())
        assertTrue(closedDefects.isNotEmpty())
        
        // Performance should be under 200ms for concurrent operations
        assertTrue("Concurrent operations took too long: ${executionTime}ms", executionTime < 200)
    }
    
    @Test
    fun `test image handling performance`() {
        // Test image handling performance
        val imageList = generateImageList(100)
        
        val startTime = System.currentTimeMillis()
        
        // Simulate image operations
        val filteredImages = imageList.filter { it.contains("image") }
        val sortedImages = imageList.sorted()
        val uniqueImages = imageList.distinct()
        
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        // Verify results
        assertEquals(100, filteredImages.size)
        assertEquals(100, sortedImages.size)
        assertEquals(100, uniqueImages.size)
        
        // Performance should be under 50ms for 100 images
        assertTrue("Image operations took too long: ${executionTime}ms", executionTime < 50)
    }
    
    private fun generateLargeTaskList(size: Int): List<ChecklistItem> {
        return (1..size).map { index ->
            ChecklistItem(
                id = "task-$index",
                title = "Task $index",
                description = "Description for task $index",
                category = when (index % 5) {
                    0 -> "Cleaning"
                    1 -> "Repair"
                    2 -> "Maintenance"
                    3 -> "Inspection"
                    else -> "Other"
                },
                priority = Priority.values()[index % Priority.values().size],
                status = TaskStatus.values()[index % TaskStatus.values().size],
                dueDate = "2024-12-${(index % 30) + 1}"
            )
        }
    }
    
    private fun generateLargeDefectList(size: Int): List<Defect> {
        return (1..size).map { index ->
            Defect(
                id = "defect-$index",
                location = "Location $index",
                category = DefectCategory.values()[index % DefectCategory.values().size],
                priority = Priority.values()[index % Priority.values().size],
                description = "Description for defect $index",
                images = listOf("image${index}.jpg", "image${index + 1}.jpg"),
                status = DefectStatus.values()[index % DefectStatus.values().size],
                createdAt = "2024-01-01"
            )
        }
    }
    
    private fun generateImageList(size: Int): List<String> {
        return (1..size).map { "image$it.jpg" }
    }
}