package com.example.movein.shared.repository

import com.example.movein.shared.data.Defect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefectRepository {
    private val _defects = MutableStateFlow<List<Defect>>(emptyList())
    val defects: StateFlow<List<Defect>> = _defects.asStateFlow()
    
    fun addDefect(defect: Defect) {
        val currentDefects = _defects.value
        _defects.value = currentDefects + defect
    }
    
    fun updateDefect(updatedDefect: Defect) {
        val currentDefects = _defects.value
        _defects.value = currentDefects.map { 
            if (it.id == updatedDefect.id) updatedDefect else it 
        }
    }
    
    fun deleteDefect(defectId: String) {
        val currentDefects = _defects.value
        _defects.value = currentDefects.filter { it.id != defectId }
    }
    
    fun getDefectById(id: String): Defect? {
        return _defects.value.find { it.id == id }
    }
    
    fun setDefects(defects: List<Defect>) {
        _defects.value = defects
    }
}

