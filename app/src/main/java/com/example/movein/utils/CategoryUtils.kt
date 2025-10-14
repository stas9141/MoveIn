package com.example.movein.utils

import com.example.movein.shared.data.UserData

object CategoryUtils {
    
    /**
     * Generate dynamic task categories based on user's apartment details
     */
    fun getAvailableTaskCategories(userData: UserData?): List<String> {
        val baseCategories = mutableListOf(
            "General",
            "Kitchen", 
            "Bathroom",
            "Living Room",
            "Bedroom",
            "Cleaning",
            "Maintenance"
        )
        
        // Add categories based on apartment features
        userData?.let { data ->
            // Add parking category if user has parking spaces
            if (data.parking > 0) {
                baseCategories.add("Parking")
            }
            
            // Add warehouse/storage category if user has warehouse
            if (data.warehouse) {
                baseCategories.add("Storage")
            }
            
            // Add balcony category if user has balconies
            if (data.balconies > 0) {
                baseCategories.add("Balcony")
            }
            
            // Add garden category if user has a garden
            if (data.garden) {
                baseCategories.add("Garden")
            }
            
            // Add utility category if user has multiple bathrooms or other utilities
            if (data.bathrooms > 1) {
                baseCategories.add("Utility")
            }
        }
        
        return baseCategories
    }
    
    /**
     * Get default category for new tasks
     */
    fun getDefaultCategory(userData: UserData?): String {
        return "General"
    }
}
