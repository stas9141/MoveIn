package com.example.movein.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
// RoundedCornerShape already imported above; removing duplicate import to avoid ambiguity
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.DefectCategory
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.UserData
import java.util.*
import java.util.Calendar
import com.example.movein.utils.getTodayString
import com.example.movein.utils.getTomorrowString
import com.example.movein.utils.getNextWeekString
import com.example.movein.utils.formatCategory
import com.example.movein.utils.formatPriority
import com.example.movein.utils.formatDateForDisplay
import com.example.movein.utils.PermissionUtils
import com.example.movein.utils.rememberPermissionState
import com.example.movein.utils.ImageUtils
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.movein.ui.components.EnhancedDatePicker
import com.example.movein.ui.components.ImageAttachButton
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDefectScreen(
    defect: Defect? = null,
    initialDueDate: String? = null,
    userData: UserData? = null,
    onSave: (Defect) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var location by remember { mutableStateOf(defect?.location ?: "") }
    var selectedCategory by remember { mutableStateOf(defect?.category ?: DefectCategory.OTHER) }
    var selectedPriority by remember { mutableStateOf(defect?.priority ?: Priority.MEDIUM) }
    var description by remember { mutableStateOf(defect?.description ?: "") }
    var selectedDueDate by remember { mutableStateOf(defect?.dueDate ?: initialDueDate) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var showDueDateDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    var selectedImages by remember { mutableStateOf(defect?.images ?: emptyList<String>()) }
    var isSaving by remember { mutableStateOf(false) }
    
    val isEditing = defect != null
    
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    
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
    
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
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
            },
            bottomBar = {
                // Bottom buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onBack()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        
                        // Save Button
                        Button(
                            onClick = {
                                // Hide keyboard first
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                
                                // Validate inputs
                                if (location.trim().isEmpty()) {
                                    locationError = "Location is required"
                                    return@Button
                                }
                                if (description.trim().isEmpty()) {
                                    descriptionError = "Description is required"
                                    return@Button
                                }
                                
                                // Show loading state
                                isSaving = true
                                
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
                                
                                // Reset loading state after a short delay
                                // Note: In a real app, you'd handle this in the parent component
                                // For now, we'll just reset it immediately after the save
                                isSaving = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSaving
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Check,
                                        contentDescription = if (isEditing) "Update" else "Save",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                
                                Text(
                                    text = when {
                                        isSaving && isEditing -> "Updating..."
                                        isSaving -> "Saving..."
                                        isEditing -> "Update"
                                        else -> "Save"
                                    },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .clickable { 
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
            ) {
        
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
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLocationDialog = true }) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                }
                    
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) { detectTapGestures(onTap = { showCategoryDialog = true }) },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) { detectTapGestures(onTap = { showPriorityDialog = true }) },
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
                    value = formatDateForDisplay(selectedDueDate),
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) { detectTapGestures(onTap = { showDueDateDialog = true }) },
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
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
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
                        
                        // Attach Button
                        ImageAttachButton(
                            onImageSelected = { imagePath ->
                                selectedImages = selectedImages + imagePath
                            },
                            modifier = Modifier.fillMaxWidth(),
                            currentCount = selectedImages.size
                        )
                        
                        // Show selected images if any
                        if (selectedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            var previewUri by remember { mutableStateOf<String?>(null) }
                            var showClearAllDialog by remember { mutableStateOf(false) }

                            // Header with count
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${selectedImages.size} photo(s)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap to preview • Tap × to remove",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = { showClearAllDialog = true }) {
                                    Text("Clear all")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Thumbnails row
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(selectedImages) { imagePath ->
                                    val context = LocalContext.current
                                    var bitmapState by remember(imagePath) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

                                    LaunchedEffect(imagePath) {
                                        bitmapState = withContext(Dispatchers.IO) {
                                            try {
                                                val uri = Uri.parse(imagePath)
                                                context.contentResolver.openInputStream(uri)?.use { input ->
                                                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = false; inSampleSize = 4 }
                                                    BitmapFactory.decodeStream(input, null, options)?.asImageBitmap()
                                                }
                                            } catch (_: Exception) { null }
                                        }
                                    }

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(96.dp)
                                                .clickable { previewUri = imagePath },
                                            contentAlignment = Alignment.TopEnd
                                        ) {
                                            if (bitmapState != null) {
                                                Image(
                                                    bitmap = bitmapState!!,
                                                    contentDescription = "Photo",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                                }
                                            }

                                            IconButton(
                                                onClick = { selectedImages = selectedImages.filter { it != imagePath } },
                                                modifier = Modifier
                                                    .padding(4.dp)
                                                    .size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove photo",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Preview dialog
                            if (previewUri != null) {
                                AlertDialog(
                                    onDismissRequest = { previewUri = null },
                                    text = {
                                        val context = LocalContext.current
                                        var fullBitmap by remember(previewUri) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
                                        LaunchedEffect(previewUri) {
                                            fullBitmap = withContext(Dispatchers.IO) {
                                                try {
                                                    val uri = Uri.parse(previewUri)
                                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                                        BitmapFactory.decodeStream(input)?.asImageBitmap()
                                                    }
                                                } catch (_: Exception) { null }
                                            }
                                        }
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            if (fullBitmap != null) {
                                                Image(bitmap = fullBitmap!!, contentDescription = "Preview")
                                            } else {
                                                CircularProgressIndicator()
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { previewUri = null }) { Text("Close") }
                                    }
                                )
                            }

                            // Clear all confirmation dialog
                            if (showClearAllDialog) {
                                AlertDialog(
                                    onDismissRequest = { showClearAllDialog = false },
                                    title = { Text("Remove all images?") },
                                    text = { Text("This will remove all attached images from this defect.") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            selectedImages = emptyList()
                                            showClearAllDialog = false
                                        }) { Text("Remove") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showClearAllDialog = false }) { Text("Cancel") }
                                    }
                                )
                            }
                        }
                    }
                }
            }
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
            EnhancedDatePicker(
                selectedDate = selectedDueDate,
                onDateSelected = { date ->
                    selectedDueDate = date
                },
                onDismiss = { showDueDateDialog = false },
                title = "Select Due Date"
            )
        }
            }
        }
    }


