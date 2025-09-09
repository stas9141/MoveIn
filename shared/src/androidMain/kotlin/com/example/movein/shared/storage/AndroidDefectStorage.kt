package com.example.movein.shared.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.movein.shared.data.Defect

actual class DefectStorage(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("defects", Context.MODE_PRIVATE)
    
    actual fun loadDefects(): List<Defect> {
        // For now, return empty list
        // TODO: Implement proper serialization
        return emptyList()
    }
    
    actual fun saveDefects(defects: List<Defect>) {
        // For now, do nothing
        // TODO: Implement proper serialization
    }
}
