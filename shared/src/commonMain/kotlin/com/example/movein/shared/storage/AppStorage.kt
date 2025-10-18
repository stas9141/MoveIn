package com.example.movein.shared.storage

import com.example.movein.shared.data.UserData
import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.BuildingCompany

expect class AppStorage {
    fun saveUserData(userData: UserData)
    fun loadUserData(): UserData?
    
    fun saveChecklistData(checklistData: ChecklistData)
    fun loadChecklistData(): ChecklistData?
    
    fun saveDefects(defects: List<Defect>)
    fun loadDefects(): List<Defect>
    
    fun saveBuildingCompanies(companies: List<BuildingCompany>)
    fun loadBuildingCompanies(): List<BuildingCompany>
    
    fun clearAllData()
    fun clearUserData()
}
