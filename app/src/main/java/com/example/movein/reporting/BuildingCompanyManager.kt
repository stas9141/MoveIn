package com.example.movein.reporting

import android.content.Context
import com.example.movein.shared.storage.AppStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class BuildingCompany(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String = "",
    val address: String = "",
    val contactPerson: String = "",
    val notes: String = "",
    val isDefault: Boolean = false
)

class BuildingCompanyManager(private val context: Context) {
    private val appStorage = AppStorage(context)
    private val _companies = MutableStateFlow<List<BuildingCompany>>(emptyList())
    val companies: StateFlow<List<BuildingCompany>> = _companies.asStateFlow()
    
    init {
        loadCompanies()
    }
    
    private fun loadCompanies() {
        // Load from storage or use default companies
        val defaultCompanies = listOf(
            BuildingCompany(
                name = "ABC Construction Ltd",
                email = "defects@abcconstruction.com",
                phone = "+1-555-0123",
                address = "123 Construction Ave, City, State 12345",
                contactPerson = "John Smith",
                notes = "Primary contractor for building maintenance",
                isDefault = true
            ),
            BuildingCompany(
                name = "XYZ Property Services",
                email = "reports@xyzproperty.com",
                phone = "+1-555-0456",
                address = "456 Property St, City, State 12345",
                contactPerson = "Sarah Johnson",
                notes = "Specialized in electrical and plumbing",
                isDefault = true
            ),
            BuildingCompany(
                name = "Quick Fix Solutions",
                email = "urgent@quickfix.com",
                phone = "+1-555-0789",
                address = "789 Quick St, City, State 12345",
                contactPerson = "Mike Davis",
                notes = "Emergency repairs and urgent fixes",
                isDefault = true
            )
        )
        
        _companies.value = defaultCompanies
    }
    
    fun addCompany(company: BuildingCompany) {
        val currentCompanies = _companies.value.toMutableList()
        currentCompanies.add(company)
        _companies.value = currentCompanies
        saveCompanies()
    }
    
    fun updateCompany(company: BuildingCompany) {
        val currentCompanies = _companies.value.toMutableList()
        val index = currentCompanies.indexOfFirst { it.id == company.id }
        if (index != -1) {
            currentCompanies[index] = company
            _companies.value = currentCompanies
            saveCompanies()
        }
    }
    
    fun deleteCompany(companyId: String) {
        val currentCompanies = _companies.value.toMutableList()
        currentCompanies.removeAll { it.id == companyId }
        _companies.value = currentCompanies
        saveCompanies()
    }
    
    fun getDefaultCompany(): BuildingCompany? {
        return _companies.value.firstOrNull { it.isDefault }
    }
    
    fun setDefaultCompany(companyId: String) {
        val currentCompanies = _companies.value.toMutableList()
        currentCompanies.forEachIndexed { index, company ->
            currentCompanies[index] = company.copy(isDefault = company.id == companyId)
        }
        _companies.value = currentCompanies
        saveCompanies()
    }
    
    private fun saveCompanies() {
        // In a real implementation, you would save to storage
        // For now, we'll keep them in memory
    }
    
    fun getCompanyById(id: String): BuildingCompany? {
        return _companies.value.find { it.id == id }
    }
}
