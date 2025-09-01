package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movein.data.Defect
import com.example.movein.data.DefectCategory
import com.example.movein.data.DefectStatus
import com.example.movein.data.Priority
import com.example.movein.data.UserData
import java.util.*
import java.util.Calendar
import com.example.movein.utils.getTodayString
import com.example.movein.utils.getTomorrowString
import com.example.movein.utils.getNextWeekString
import com.example.movein.utils.formatCategory
import com.example.movein.utils.formatPriority
import com.example.movein.utils.PermissionUtils
import com.example.movein.utils.rememberPermissionState
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDefectScreen(
    defect: Defect? = null,
    userData: UserData? = null,
    onSave: (Defect) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var location by remember { mutableStateOf(defect?.location ?: "") }
    var selectedCategory by remember { mutableStateOf(defect?.category ?: DefectCategory.OTHER) }
    var selectedPriority by remember { mutableStateOf(defect?.priority ?: Priority.MEDIUM) }
    var description by remember { mutableStateOf(defect?.description ?: "") }
    var selectedDueDate by remember { mutableStateOf(defect?.dueDate) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var showDueDateDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    var selectedImages by remember { mutableStateOf(defect?.images ?: emptyList<String>()) }
    
    val isEditing = defect != null
    
    // Generate location options based on user data
    val locationOptions = remember(userData) {
        val options = mutableListOf<String>()
        
        userData?.let { data ->
            // Add selected room names (Mamad)
            options.addAll(data.selectedRoomNames.filter { 
                it != "Bedroom 1" && it != "Bedroom 2" && it != "Bedroom 3" && it != "Bedroom 4" &&
                it != "Salon" && it != "Kitchen" && it != "Salon + Kitchen" // Filter out individual rooms and combined option
            })
            
            // Add Salon and Kitchen as separate options if they were selected during onboarding
            if (data.selectedRoomNames.contains("Salon + Kitchen")) {
                options.add("Salon")
                options.add("Kitchen")
            }
            
            // Add bedrooms based on count
            val bedroomCount = data.selectedRoomNames.count { it.startsWith("Bedroom ") }
            for (i in 1..bedroomCount) {
                options.add("Bedroom $i")
            }
            
            // Add bathrooms
            for (i in 1..data.bathrooms) {
                options.add("Bathroom $i")
            }
            
            // Add parking spaces
            for (i in 1..data.parking) {
                options.add("Parking Space $i")
            }
            
            // Add Machsan if available
            if (data.warehouse) {
                options.add("Machsan")
            }
            
            // Add balconies
            for (i in 1..data.balconies) {
                options.add("Balcony $i")
            }
            
            // Add common areas (only if not already in selected rooms)
            val commonAreas = listOf(
                "Hallway",
                "Entrance"
            )
            
            commonAreas.forEach { area ->
                if (!data.selectedRoomNames.contains(area)) {
                    options.add(area)
                }
            }
        } ?: run {
            // Default options if no user data
            options.addAll(listOf(
                "Salon",
                "Kitchen",
                "Mamad",
                "Bedroom 1",
                "Bedroom 2",
                "Bedroom 3",
                "Bedroom 4",
                "Bathroom 1",
                "Hallway",
                "Entrance",
                "Balcony 1",
                "Balcony 2",
                "Balcony 3"
            ))
        }
        
        // Filter out empty strings and duplicates
        options.distinct().filter { it.isNotBlank() }
    }
    
        Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Custom top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                
                Text(
                    text = if (isEditing) "Edit Defect" else "Add New Defect",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location field
            Column {
                Text(
                    text = "Location *",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = showLocationDialog,
                    onExpandedChange = { showLocationDialog = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { },
                        placeholder = { Text("Select location...") },
                        readOnly = true,
                        isError = locationError != null,
                        supportingText = {
                            if (locationError != null) {
                                Text(locationError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLocationDialog) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showLocationDialog,
                        onDismissRequest = { showLocationDialog = false }
                    ) {
                        locationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    location = option
                                    locationError = null
                                    showLocationDialog = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Category field
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = formatCategory(selectedCategory),
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCategoryDialog = true }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select Category")
                        }
                    }
                )
            }
            
            // Priority field
            Column {
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = formatPriority(selectedPriority),
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showPriorityDialog = true }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select Priority")
                        }
                    }
                )
            }
            
            // Due date field
            Column {
                Text(
                    text = "Due Date",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = selectedDueDate ?: "No due date set",
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDueDateDialog = true }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select Due Date")
                        }
                    }
                )
            }
            
            // Description field
            Column {
                Text(
                    text = "Description *",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        description = it
                        descriptionError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Describe the defect in detail...") },
                    isError = descriptionError != null,
                    supportingText = {
                        if (descriptionError != null) {
                            Text(descriptionError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
            
            // Image upload section
            Column {
                Text(
                    text = "Images",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Images",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Add photos of the defect",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // Simulate camera photo capture
                                    val timestamp = System.currentTimeMillis()
                                    selectedImages = selectedImages + "IMG_${timestamp}_camera.jpg"
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Take Photo")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    // Simulate gallery photo selection
                                    val timestamp = System.currentTimeMillis()
                                    selectedImages = selectedImages + "IMG_${timestamp}_gallery.jpg"
                                }
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Select from Gallery")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery")
                            }
                        }
                        
                        // Show selected images if any
                        if (selectedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Show photo count
                            Text(
                                text = "${selectedImages.size} photo(s) selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Show selected photos as chips
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedImages) { imagePath ->
                                    AssistChip(
                                        onClick = { },
                                        label = { 
                                            Text(
                                                text = imagePath.substringAfterLast("/").substringBeforeLast("."),
                                                maxLines = 1
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    selectedImages = selectedImages.filter { it != imagePath }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Remove photo",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Save Button
        Button(
            onClick = {
                // Validate inputs
                if (location.trim().isEmpty()) {
                    locationError = "Location is required"
                    return@Button
                }
                if (description.trim().isEmpty()) {
                    descriptionError = "Description is required"
                    return@Button
                }
                
                val newDefect = Defect(
                    id = defect?.id ?: UUID.randomUUID().toString(),
                    location = location.trim(),
                    category = selectedCategory,
                    priority = selectedPriority,
                    description = description.trim(),
                    images = selectedImages,
                    status = defect?.status ?: DefectStatus.OPEN,
                    createdAt = defect?.createdAt ?: getTodayString(),
                    dueDate = selectedDueDate,
                    subTasks = defect?.subTasks ?: emptyList(),
                    notes = defect?.notes ?: "",
                    assignedTo = defect?.assignedTo
                )
                
                onSave(newDefect)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (isEditing) "Update Defect" else "Save Defect",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // Dialogs
        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                title = { Text("Select Category") },
                text = {
                    Column {
                        DefectCategory.values().forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCategory == category,
                                    onClick = { 
                                        selectedCategory = category
                                        showCategoryDialog = false
                                    }
                                )
                                Text(
                                    text = formatCategory(category),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showCategoryDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showPriorityDialog) {
            AlertDialog(
                onDismissRequest = { showPriorityDialog = false },
                title = { Text("Select Priority") },
                text = {
                    Column {
                        Priority.values().forEach { priority ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPriority == priority,
                                    onClick = { 
                                        selectedPriority = priority
                                        showPriorityDialog = false
                                    }
                                )
                                Text(
                                    text = formatPriority(priority),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showPriorityDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showDueDateDialog) {
            AlertDialog(
                onDismissRequest = { showDueDateDialog = false },
                title = { Text("Select Due Date") },
                text = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = { 
                                    selectedDueDate = getTodayString()
                                    showDueDateDialog = false
                                }
                            ) {
                                Text("Today")
                            }
                            
                            OutlinedButton(
                                onClick = { 
                                    selectedDueDate = getTomorrowString()
                                    showDueDateDialog = false
                                }
                            ) {
                                Text("Tomorrow")
                            }
                            
                            OutlinedButton(
                                onClick = { 
                                    selectedDueDate = getNextWeekString()
                                    showDueDateDialog = false
                                }
                            ) {
                                Text("Next Week")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = { 
                                selectedDueDate = null
                                showDueDateDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("No Due Date")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDueDateDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        

        

    }
}


