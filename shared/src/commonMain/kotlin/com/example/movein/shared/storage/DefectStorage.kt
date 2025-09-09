package com.example.movein.shared.storage

import com.example.movein.shared.data.Defect

expect class DefectStorage {
    fun saveDefects(defects: List<Defect>)
    fun loadDefects(): List<Defect>
}
