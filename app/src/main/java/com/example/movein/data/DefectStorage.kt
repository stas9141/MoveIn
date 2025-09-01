package com.example.movein.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DefectStorage(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("defects_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val defectListType = object : TypeToken<List<Defect>>() {}.type

    fun saveDefects(defects: List<Defect>) {
        val json = gson.toJson(defects, defectListType)
        sharedPreferences.edit().putString("defects", json).apply()
    }

    fun loadDefects(): List<Defect> {
        val json = sharedPreferences.getString("defects", null)
        return if (json != null) {
            try {
                gson.fromJson(json, defectListType) ?: getDefaultDefects()
            } catch (e: Exception) {
                getDefaultDefects()
            }
        } else {
            getDefaultDefects()
        }
    }

    private fun getDefaultDefects(): List<Defect> {
        return listOf(
            Defect(
                id = "1",
                location = "Main Bathroom",
                category = DefectCategory.PLUMBING,
                priority = Priority.HIGH,
                description = "Leaking faucet in the sink. Water is dripping constantly and needs immediate attention.",
                status = DefectStatus.OPEN,
                createdAt = "12/15/2024",
                dueDate = "12/20/2024"
            ),
            Defect(
                id = "2",
                location = "Living Room",
                category = DefectCategory.ELECTRICITY,
                priority = Priority.MEDIUM,
                description = "Light switch not working properly. Sometimes it takes multiple attempts to turn on the lights.",
                status = DefectStatus.IN_PROGRESS,
                createdAt = "12/10/2024",
                dueDate = "12/25/2024"
            ),
            Defect(
                id = "3",
                location = "Kitchen",
                category = DefectCategory.INSTALLATIONS,
                priority = Priority.LOW,
                description = "Cabinet door is slightly misaligned. Minor adjustment needed.",
                status = DefectStatus.CLOSED,
                createdAt = "12/05/2024",
                dueDate = "12/15/2024"
            )
        )
    }
}
