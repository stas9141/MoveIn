package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movein.reporting.BuildingCompany
import com.example.movein.reporting.BuildingCompanyManager
import com.example.movein.reporting.DefectReportGenerator
import com.example.movein.reporting.EmailService
import com.example.movein.shared.data.Defect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportConfigurationScreen(
    defects: List<Defect>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val buildingCompanyManager = remember(context) { BuildingCompanyManager(context) }
    val reportGenerator = remember(context) { DefectReportGenerator(context) }
    val emailService = remember(context) { EmailService(context) }
    
    var selectedCompany by remember { mutableStateOf<BuildingCompany?>(null) }
    var reportConfig by remember { 
        mutableStateOf(DefectReportGenerator.ReportConfig()) 
    }
    var isGeneratingReport by remember { mutableStateOf(false) }
    var showCompanyDialog by remember { mutableStateOf(false) }
    var newCompany by remember { mutableStateOf(BuildingCompany(name = "", email = "")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    val companies by buildingCompanyManager.companies.collectAsState()
    
    // Set default company if none selected
    LaunchedEffect(companies) {
        if (selectedCompany == null && companies.isNotEmpty()) {
            selectedCompany = buildingCompanyManager.getDefaultCompany() ?: companies.first()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Defect Report") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Report Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Report Summary",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val totalDefects = defects.size
                        val openDefects = defects.count { it.status.name == "OPEN" }
                        val highPriorityDefects = defects.count { it.priority.name == "HIGH" }
                        
                        Text(
                            text = "Total Defects: $totalDefects",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Open Defects: $openDefects",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "High Priority: $highPriorityDefects",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Building Company Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Building Company",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            TextButton(onClick = { showCompanyDialog = true }) {
                                Text("Add New")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Column(
                            modifier = Modifier.selectableGroup()
                        ) {
                            companies.forEach { company ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedCompany?.id == company.id,
                                            onClick = { selectedCompany = company },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedCompany?.id == company.id,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = company.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                        Text(
                                            text = company.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (company.contactPerson.isNotEmpty()) {
                                            Text(
                                                text = "Contact: ${company.contactPerson}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Report Options
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Report Options",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Include Images
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Include Images")
                            Switch(
                                checked = reportConfig.includeImages,
                                onCheckedChange = { 
                                    reportConfig = reportConfig.copy(includeImages = it)
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Include Closed Defects
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Include Closed Defects")
                            Switch(
                                checked = reportConfig.includeClosedDefects,
                                onCheckedChange = { 
                                    reportConfig = reportConfig.copy(includeClosedDefects = it)
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Group by Priority
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Group by Priority")
                            Switch(
                                checked = reportConfig.groupByPriority,
                                onCheckedChange = { 
                                    reportConfig = reportConfig.copy(groupByPriority = it)
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Include Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Include Summary")
                            Switch(
                                checked = reportConfig.includeSummary,
                                onCheckedChange = { 
                                    reportConfig = reportConfig.copy(includeSummary = it)
                                }
                            )
                        }
                    }
                }
            }
            
            // Generate Report Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isGeneratingReport = true
                                errorMessage = null
                                try {
                                    val updatedConfig = reportConfig.copy(
                                        buildingCompanyName = selectedCompany?.name ?: "",
                                        buildingCompanyEmail = selectedCompany?.email ?: ""
                                    )
                                    val reportFile = reportGenerator.generateDefectReport(defects, updatedConfig)
                                    
                                    // Send via email
                                    selectedCompany?.let { company ->
                                        val emailBody = emailService.generateEmailBody(
                                            company.name,
                                            defects.size,
                                            defects.count { it.priority.name == "HIGH" },
                                            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
                                        )
                                        
                                        // Send email on main thread
                                        try {
                                            emailService.sendDefectReport(
                                                EmailService.EmailConfig(
                                                    recipientEmail = company.email,
                                                    recipientName = company.name,
                                                    subject = "Defect Report - ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}",
                                                    body = emailBody,
                                                    attachmentFile = reportFile
                                                )
                                            )
                                            
                                            // Show success message
                                            errorMessage = "Email sent successfully to ${company.name}!\n\nReport generated and email app opened."
                                            showErrorDialog = true
                                        } catch (e: Exception) {
                                            // Handle email sending errors gracefully
                                            errorMessage = "Failed to send email: ${e.message}\n\nPlease check:\n• Email app is installed\n• File permissions\n• Network connection"
                                            showErrorDialog = true
                                            e.printStackTrace()
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to generate report: ${e.message}"
                                    showErrorDialog = true
                                    e.printStackTrace()
                                } finally {
                                    isGeneratingReport = false
                                }
                            }
                        },
                        enabled = selectedCompany != null && !isGeneratingReport,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isGeneratingReport) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(Icons.Default.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate & Email Report")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isGeneratingReport = true
                                errorMessage = null
                                try {
                                    val updatedConfig = reportConfig.copy(
                                        buildingCompanyName = selectedCompany?.name ?: "",
                                        buildingCompanyEmail = selectedCompany?.email ?: ""
                                    )
                                    val reportFile = reportGenerator.generateDefectReport(defects, updatedConfig)
                                    
                                    // Show success message
                                    errorMessage = "PDF report generated successfully!\nLocation: ${reportFile.absolutePath}"
                                    showErrorDialog = true
                                } catch (e: Exception) {
                                    errorMessage = "Failed to generate PDF: ${e.message}\n\nPlease check:\n• Storage permissions\n• Available disk space\n• PDF generation library"
                                    showErrorDialog = true
                                    e.printStackTrace()
                                } finally {
                                    isGeneratingReport = false
                                }
                            }
                        },
                        enabled = !isGeneratingReport,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate PDF Only")
                    }
                }
            }
        }
    }
    
    // Add Company Dialog
    if (showCompanyDialog) {
        AlertDialog(
            onDismissRequest = { showCompanyDialog = false },
            title = { Text("Add Building Company") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newCompany.name,
                        onValueChange = { newCompany = newCompany.copy(name = it) },
                        label = { Text("Company Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCompany.email,
                        onValueChange = { newCompany = newCompany.copy(email = it) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCompany.contactPerson,
                        onValueChange = { newCompany = newCompany.copy(contactPerson = it) },
                        label = { Text("Contact Person") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCompany.phone,
                        onValueChange = { newCompany = newCompany.copy(phone = it) },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        buildingCompanyManager.addCompany(newCompany)
                        showCompanyDialog = false
                        newCompany = BuildingCompany(name = "", email = "")
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompanyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Message Dialog (for both errors and success)
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { 
                Text(
                    if (errorMessage!!.contains("successfully") || errorMessage!!.contains("success")) {
                        "Success"
                    } else {
                        "Error"
                    }
                ) 
            },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
