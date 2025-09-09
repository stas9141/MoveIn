package com.example.movein.shared.data

object ChecklistDataGenerator {
    fun getDefaultChecklistData(): ChecklistData {
        return ChecklistData(
            firstWeek = getFirstWeekTasks(),
            firstMonth = getFirstMonthTasks(),
            firstYear = getFirstYearTasks()
        )
    }

    private fun getFirstWeekTasks(): List<ChecklistItem> {
        return listOf(
            ChecklistItem(
                id = "week_1",
                title = "Change all locks",
                description = "Replace or rekey all exterior door locks for security",
                category = "Security"
            ),
            ChecklistItem(
                id = "week_2",
                title = "Test all smoke detectors",
                description = "Check that all smoke detectors are working properly and replace batteries if needed",
                category = "Safety"
            ),
            ChecklistItem(
                id = "week_3",
                title = "Take initial meter readings",
                description = "Document electricity, gas, and water meter readings",
                category = "Utilities"
            ),
            ChecklistItem(
                id = "week_4",
                title = "Locate circuit breaker and water shut-off",
                description = "Find and test the main circuit breaker and water shut-off valve",
                category = "Utilities"
            ),
            ChecklistItem(
                id = "week_5",
                title = "Clean all appliances",
                description = "Deep clean refrigerator, oven, dishwasher, and other appliances",
                category = "Cleaning"
            ),
            ChecklistItem(
                id = "week_6",
                title = "Check for leaks",
                description = "Inspect all faucets, pipes, and under sinks for any leaks",
                category = "Maintenance"
            ),
            ChecklistItem(
                id = "week_7",
                title = "Test heating and cooling systems",
                description = "Verify that HVAC systems are working properly",
                category = "Comfort"
            )
        )
    }

    private fun getFirstMonthTasks(): List<ChecklistItem> {
        return listOf(
            ChecklistItem(
                id = "month_1",
                title = "Update address with important services",
                description = "Notify banks, insurance, government agencies, and subscriptions of your new address",
                category = "Administrative"
            ),
            ChecklistItem(
                id = "month_2",
                title = "Register with local services",
                description = "Sign up for garbage collection, recycling, and other local services",
                category = "Services"
            ),
            ChecklistItem(
                id = "month_3",
                title = "Meet your neighbors",
                description = "Introduce yourself to neighbors and exchange contact information",
                category = "Community"
            ),
            ChecklistItem(
                id = "month_4",
                title = "Explore the neighborhood",
                description = "Find nearby grocery stores, pharmacies, restaurants, and other amenities",
                category = "Community"
            ),
            ChecklistItem(
                id = "month_5",
                title = "Check internet and phone reception",
                description = "Test internet speeds and phone signal strength in different rooms",
                category = "Technology"
            ),
            ChecklistItem(
                id = "month_6",
                title = "Document any issues",
                description = "Take photos and document any problems that need landlord attention",
                category = "Maintenance"
            ),
            ChecklistItem(
                id = "month_7",
                title = "Plan emergency contacts",
                description = "Save important numbers: landlord, maintenance, emergency services",
                category = "Safety"
            )
        )
    }

    private fun getFirstYearTasks(): List<ChecklistItem> {
        return listOf(
            ChecklistItem(
                id = "year_1",
                title = "Review lease renewal",
                description = "Consider if you want to renew your lease and negotiate terms if needed",
                category = "Administrative"
            ),
            ChecklistItem(
                id = "year_2",
                title = "Annual deep cleaning",
                description = "Schedule a comprehensive cleaning including carpets, windows, and appliances",
                category = "Cleaning"
            ),
            ChecklistItem(
                id = "year_3",
                title = "Check for maintenance needs",
                description = "Inspect for any wear and tear that needs attention",
                category = "Maintenance"
            ),
            ChecklistItem(
                id = "year_4",
                title = "Update emergency kit",
                description = "Refresh first aid supplies, flashlights, and emergency items",
                category = "Safety"
            ),
            ChecklistItem(
                id = "year_5",
                title = "Review insurance coverage",
                description = "Ensure your renter's insurance still meets your needs",
                category = "Administrative"
            ),
            ChecklistItem(
                id = "year_6",
                title = "Plan for next move",
                description = "If considering moving, start planning and saving",
                category = "Planning"
            )
        )
    }

    fun generatePersonalizedChecklist(userData: UserData): ChecklistData {
        val baseData = getDefaultChecklistData()
        
        // Add bathroom-specific tasks
        val bathroomTasks = mutableListOf<ChecklistItem>()
        for (i in 1..userData.bathrooms) {
            bathroomTasks.add(
                ChecklistItem(
                    id = "bathroom_$i",
                    title = "Inspect bathroom $i thoroughly",
                    description = "Check for mold, leaks, water pressure, and ventilation in bathroom $i",
                    category = "Maintenance"
                )
            )
        }

        // Add warehouse task if applicable
        val warehouseTasks = if (userData.warehouse) {
            listOf(
                ChecklistItem(
                    id = "warehouse",
                    title = "Organize warehouse",
                    description = "Set up shelving and organize items in your warehouse",
                    category = "Organization"
                )
            )
        } else {
            emptyList()
        }

        // Add parking-specific tasks
        val parkingTasks = mutableListOf<ChecklistItem>()
        for (i in 1..userData.parking) {
            parkingTasks.add(
                ChecklistItem(
                    id = "parking_$i",
                    title = "Test parking space $i",
                    description = "Ensure parking space $i is accessible and properly marked",
                    category = "Parking"
                )
            )
        }

        return ChecklistData(
            firstWeek = baseData.firstWeek + bathroomTasks + warehouseTasks + parkingTasks,
            firstMonth = baseData.firstMonth,
            firstYear = baseData.firstYear
        )
    }
}
