package com.example.movein.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
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
import com.example.movein.ui.components.FileReviewDialog
import com.example.movein.utils.FileReviewUtils
import com.example.movein.shared.data.FileAttachment
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun getFileExtension(uriString: String): String {
    return try {
        uriString.substringAfterLast('.', "file")
    } catch (e: Exception) {
        "file"
    }
}

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

    var selectedAttachments by remember { mutableStateOf(defect?.attachments ?: emptyList<FileAttachment>()) }
    var isSaving by remember { mutableStateOf(false) }
    var showFileReviewDialog by remember { mutableStateOf(false) }
    var selectedAttachment by remember { mutableStateOf<FileAttachment?>(null) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    
    val isEditing = defect != null
    
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    // Camera launcher
    var pendingCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                // Get the actual file size from the camera image
                val fileSize = try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.available().toLong()
                    } ?: 0L
                } catch (e: Exception) {
                    0L
                }
                
                val newAttachment = FileAttachment(
                    id = UUID.randomUUID().toString(),
                    name = "Camera_${System.currentTimeMillis()}.jpg",
                    type = "image/jpeg",
                    uri = uri.toString(),
                    size = fileSize
                )
                selectedAttachments = selectedAttachments + newAttachment
            }
        }
        pendingCameraUri = null
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    "Gallery_${System.currentTimeMillis()}.jpg"
                }
            } ?: "Gallery_${System.currentTimeMillis()}.jpg"
            
            val fileSize = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getLong(sizeIndex)
                } else {
                    0L
                }
            } ?: 0L
            
            val fileType = context.contentResolver.getType(uri) ?: "image/jpeg"
            
            val newAttachment = FileAttachment(
                id = UUID.randomUUID().toString(),
                name = fileName,
                type = fileType,
                uri = uri.toString(),
                size = fileSize
            )
            selectedAttachments = selectedAttachments + newAttachment
        }
    }
    
    // File launcher for attachments
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val timestamp = System.currentTimeMillis()
            val fileName = "File_${timestamp}.${getFileExtension(uri.toString())}"
            val newAttachment = FileAttachment(
                id = UUID.randomUUID().toString(),
                name = fileName,
                type = "file",
                uri = uri.toString(),
                size = 0L // We don't have access to file size here
            )
            selectedAttachments = selectedAttachments + newAttachment
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
    }
    
    
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
                                    images = emptyList(), // Images are now handled as attachments
                                    attachments = selectedAttachments,
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
            
            // Attachments section (files) - moved to be with other form fields
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Attachments",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (selectedAttachments.isNotEmpty()) {
                            Text(
                                text = "${selectedAttachments.size} file${if (selectedAttachments.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedAttachments.isEmpty()) {
                    // Compact empty state
                    OutlinedButton(
                        onClick = { showAttachmentDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Attachments",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Attachments")
                    }
                } else {
                    // Compact attachments list
                    Column {
                        selectedAttachments.forEach { attachment ->
                            AttachmentItem(
                                attachment = attachment,
                                onDelete = {
                                    selectedAttachments = selectedAttachments.filter { it.id != attachment.id }
                                },
                                onReview = {
                                    selectedAttachment = attachment
                                    showFileReviewDialog = true
                                }
                            )
                            
                            if (attachment != selectedAttachments.last()) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                        
                        // Compact Add More button
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showAttachmentDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add More Files",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add More Files")
                        }
                    }
                }
            }
        }
        
        // Attachment Dialog
        if (showAttachmentDialog) {
            AttachmentDialog(
                onDismiss = { showAttachmentDialog = false },
                onAddImage = {
                    // Launch camera
                    if (PermissionUtils.hasCameraPermission(context)) {
                        try {
                            val imageFile = ImageUtils.createImageFile(context)
                            val imageUri = ImageUtils.getImageUri(context, imageFile)
                            pendingCameraUri = imageUri
                            cameraLauncher.launch(imageUri)
                        } catch (e: Exception) {
                            // Handle error
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
                    }
                },
                onAddFile = {
                    // Launch file picker
                    fileLauncher.launch("*/*")
                },
                onAddGallery = {
                    // Launch gallery
                    if (PermissionUtils.hasStoragePermission(context)) {
                        galleryLauncher.launch("image/*")
                    } else {
                        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        permissionLauncher.launch(permissions)
                    }
                }
            )
        }
        
        // File Review Dialog
        if (showFileReviewDialog && selectedAttachment != null) {
            FileReviewDialog(
                attachment = selectedAttachment!!,
                onDismiss = {
                    showFileReviewDialog = false
                    selectedAttachment = null
                },
                onDelete = {
                    selectedAttachments = selectedAttachments.filter { it.id != selectedAttachment!!.id }
                    showFileReviewDialog = false
                    selectedAttachment = null
                },
                onShare = {
                    FileReviewUtils.shareFile(context, selectedAttachment!!.uri, selectedAttachment!!.name)
                },
                onOpen = {
                    FileReviewUtils.openFileWithExternalApp(context, selectedAttachment!!.uri)
                }
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
            EnhancedDatePicker(
                selectedDate = selectedDueDate,
                onDateSelected = { date ->
                    selectedDueDate = date
                    showDueDateDialog = false // Automatically close the dialog when a date is selected
                },
                onDismiss = { showDueDateDialog = false },
                title = "Select Due Date"
            )
        }
        
        // File Review Dialog
        if (showFileReviewDialog && selectedAttachment != null) {
            FileReviewDialog(
                attachment = selectedAttachment!!,
                onDismiss = {
                    showFileReviewDialog = false
                    selectedAttachment = null
                },
                onDelete = {
                    selectedAttachments = selectedAttachments.filter { it.id != selectedAttachment!!.id }
                    showFileReviewDialog = false
                    selectedAttachment = null
                },
                onShare = {
                    FileReviewUtils.shareFile(context, selectedAttachment!!.uri, selectedAttachment!!.name)
                },
                onOpen = {
                    FileReviewUtils.openFileWithExternalApp(context, selectedAttachment!!.uri)
                }
            )
        }
            }
        }
    }
