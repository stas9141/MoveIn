package com.example.movein.shared.storage

import com.example.movein.shared.data.UserData
import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.Defect
import platform.Foundation.NSUserDefaults

actual class AppStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual fun saveUserData(userData: UserData) {
        // For now, just store basic data
        userDefaults.setInteger(userData.rooms.toLong(), "rooms")
        userDefaults.setInteger(userData.bathrooms.toLong(), "bathrooms")
        userDefaults.setInteger(userData.parking.toLong(), "parking")
        userDefaults.setBool(userData.warehouse, "warehouse")
        userDefaults.setInteger(userData.balconies.toLong(), "balconies")
        userDefaults.synchronize()
    }
    
    actual fun loadUserData(): UserData? {
        // For now, return default data
        return UserData()
    }
    
    actual fun saveChecklistData(checklistData: ChecklistData) {
        // For now, do nothing - iOS implementation would need proper serialization
        userDefaults.synchronize()
    }
    
    actual fun loadChecklistData(): ChecklistData? {
        // For now, return null - iOS implementation would need proper deserialization
        return null
    }
    
    actual fun saveDefects(defects: List<Defect>) {
        // For now, do nothing - iOS implementation would need proper serialization
        userDefaults.synchronize()
    }
    
    actual fun loadDefects(): List<Defect> {
        // For now, return empty list - iOS implementation would need proper deserialization
        return emptyList()
    }
}
