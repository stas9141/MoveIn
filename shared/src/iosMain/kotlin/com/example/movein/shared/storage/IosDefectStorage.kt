package com.example.movein.shared.storage

import com.example.movein.shared.data.Defect
import platform.Foundation.NSUserDefaults

actual class DefectStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    actual fun loadDefects(): List<Defect> {
        // For now, return empty list
        // TODO: Implement proper serialization
        return emptyList()
    }
    
    actual fun saveDefects(defects: List<Defect>) {
        // For now, do nothing
        // TODO: Implement proper serialization
        userDefaults.synchronize()
    }
}
