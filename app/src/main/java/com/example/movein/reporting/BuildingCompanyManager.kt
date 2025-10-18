package com.example.movein.reporting

import android.content.Context
import com.example.movein.shared.storage.AppStorage
import com.example.movein.shared.data.BuildingCompany
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class BuildingCompanyManager(private val context: Context) {
    private val appStorage = AppStorage(context)
    private val _companies = MutableStateFlow<List<BuildingCompany>>(emptyList())
    val companies: StateFlow<List<BuildingCompany>> = _companies.asStateFlow()
    
    init {
        loadCompanies()
    }
    
    private fun loadCompanies() {
        // Try to load saved companies first
        val savedCompanies = appStorage.loadBuildingCompanies()
        
        if (savedCompanies.isNotEmpty()) {
            // Use saved companies
            _companies.value = savedCompanies
        } else {
            // Load default companies if no saved companies exist
            val defaultCompanies = listOf(
                BuildingCompany(
                    id = UUID.randomUUID().toString(),
                    name = "ABC Construction Ltd",
                    email = "defects@abcconstruction.com",
                    phone = "+1-555-0123",
                    address = "123 Construction Ave, City, State 12345",
                    contactPerson = "John Smith",
                    notes = "Primary contractor for building maintenance",
                    isDefault = true
                ),
                BuildingCompany(
                    id = UUID.randomUUID().toString(),
                    name = "XYZ Property Services",
                    email = "reports@xyzproperty.com",
                    phone = "+1-555-0456",
                    address = "456 Property St, City, State 12345",
                    contactPerson = "Sarah Johnson",
                    notes = "Specialized in electrical and plumbing",
                    isDefault = true
                ),
                BuildingCompany(
                    id = UUID.randomUUID().toString(),
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
            // Save default companies for future use
            saveCompanies()
        }
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
        // Save companies to storage
        appStorage.saveBuildingCompanies(_companies.value)
    }
    
    fun getCompanyById(id: String): BuildingCompany? {
        return _companies.value.find { it.id == id }
    }
}
