package com.example.movein.data

import org.junit.Test
import org.junit.Assert.*
import com.example.movein.data.ChecklistDataGenerator

class ChecklistDataGeneratorTest {

    @Test
    fun `generatePersonalizedChecklist should create checklist with base tasks`() {
        val userData = UserData()
        val checklistData = ChecklistDataGenerator.generatePersonalizedChecklist(userData)
        
        assertNotNull(checklistData)
        assertTrue(checklistData.firstWeek.isNotEmpty())
        assertTrue(checklistData.firstMonth.isNotEmpty())
        assertTrue(checklistData.firstYear.isNotEmpty())
    }

    @Test
    fun `generatePersonalizedChecklist should add bathroom tasks based on bathroom count`() {
        val userData = UserData(bathrooms = 2)
        val checklistData = ChecklistDataGenerator.generatePersonalizedChecklist(userData)
        
        val bathroomTasks = checklistData.firstWeek.filter { 
            it.title.contains("bathroom", ignoreCase = true) 
        }
        
        assertEquals(2, bathroomTasks.size)
        assertTrue(bathroomTasks.any { it.title.contains("bathroom 1") })
        assertTrue(bathroomTasks.any { it.title.contains("bathroom 2") })
    }

    @Test
    fun `generatePersonalizedChecklist should add warehouse task when warehouse is true`() {
        val userData = UserData(warehouse = true)
        val checklistData = ChecklistDataGenerator.generatePersonalizedChecklist(userData)
        
        val warehouseTasks = checklistData.firstWeek.filter { 
            it.title.contains("warehouse", ignoreCase = true) 
        }
        
        assertEquals(1, warehouseTasks.size)
        assertTrue(warehouseTasks[0].title.contains("warehouse", ignoreCase = true))
    }

    @Test
    fun `generatePersonalizedChecklist should not add warehouse task when warehouse is false`() {
        val userData = UserData(warehouse = false)
        val checklistData = ChecklistDataGenerator.generatePersonalizedChecklist(userData)
        
        val warehouseTasks = checklistData.firstWeek.filter { 
            it.title.contains("warehouse", ignoreCase = true) 
        }
        
        assertEquals(0, warehouseTasks.size)
    }

    @Test
    fun `generatePersonalizedChecklist should add parking tasks based on parking count`() {
        val userData = UserData(parking = 2)
        val checklistData = ChecklistDataGenerator.generatePersonalizedChecklist(userData)
        
        val parkingTasks = checklistData.firstWeek.filter { 
            it.title.contains("parking", ignoreCase = true) 
        }
        
        assertEquals(2, parkingTasks.size)
        assertTrue(parkingTasks.any { it.title.contains("parking space 1") })
        assertTrue(parkingTasks.any { it.title.contains("parking space 2") })
    }

    @Test
    fun `generatePersonalizedChecklist should combine all personalized tasks`() {
        val userData = UserData(
            bathrooms = 2,
            parking = 2,
            warehouse = true
        )
        val checklistData = ChecklistDataGenerator.generatePersonalizedChecklist(userData)
        
        val personalizedTasks = checklistData.firstWeek.filter { task ->
            task.title.contains("bathroom", ignoreCase = true) ||
            task.title.contains("parking", ignoreCase = true) ||
            task.title.contains("warehouse", ignoreCase = true)
        }
        
        // 2 bathroom tasks + 2 parking tasks + 1 warehouse task = 5 personalized tasks
        assertEquals(5, personalizedTasks.size)
    }

    @Test
    fun `getDefaultChecklistData should return non-empty checklists`() {
        val checklistData = ChecklistDataGenerator.getDefaultChecklistData()
        
        assertNotNull(checklistData)
        assertTrue(checklistData.firstWeek.isNotEmpty())
        assertTrue(checklistData.firstMonth.isNotEmpty())
        assertTrue(checklistData.firstYear.isNotEmpty())
    }

    @Test
    fun `first week tasks should contain essential move-in tasks`() {
        val checklistData = ChecklistDataGenerator.getDefaultChecklistData()
        val firstWeekTitles = checklistData.firstWeek.map { it.title.lowercase() }
        
        assertTrue(firstWeekTitles.any { it.contains("lock") })
        assertTrue(firstWeekTitles.any { it.contains("smoke") })
        assertTrue(firstWeekTitles.any { it.contains("meter") })
        assertTrue(firstWeekTitles.any { it.contains("clean") })
    }

    @Test
    fun `first month tasks should contain administrative tasks`() {
        val checklistData = ChecklistDataGenerator.getDefaultChecklistData()
        val firstMonthTitles = checklistData.firstMonth.map { it.title.lowercase() }
        
        assertTrue(firstMonthTitles.any { it.contains("address") })
        assertTrue(firstMonthTitles.any { it.contains("neighbor") })
        assertTrue(firstMonthTitles.any { it.contains("emergency") })
    }

    @Test
    fun `first year tasks should contain long-term planning tasks`() {
        val checklistData = ChecklistDataGenerator.getDefaultChecklistData()
        val firstYearTitles = checklistData.firstYear.map { it.title.lowercase() }
        
        assertTrue(firstYearTitles.any { it.contains("lease") })
        assertTrue(firstYearTitles.any { it.contains("cleaning") })
        assertTrue(firstYearTitles.any { it.contains("insurance") })
    }
}
