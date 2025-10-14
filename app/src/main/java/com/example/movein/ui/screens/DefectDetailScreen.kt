package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.SubTask
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.DefectCategory
import com.example.movein.ui.components.PriorityDropdown
import com.example.movein.ui.components.SaveCancelButtons
import com.example.movein.ui.components.FileReviewDialog
import com.example.movein.utils.FileReviewUtils
import com.example.movein.shared.data.FileAttachment
import com.example.movein.utils.formatCategory
import com.example.movein.utils.getTodayString
import com.example.movein.utils.getTomorrowString
import com.example.movein.utils.getNextWeekString
import com.example.movein.utils.formatDateForDisplay
import com.example.movein.ui.components.EnhancedDatePicker
import com.example.movein.utils.PermissionUtils
import com.example.movein.utils.ImageUtils
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*

private fun getFileExtension(uriString: String): String {
    return try {
        uriString.substringAfterLast('.', "file")
    } catch (e: Exception) {
        "file"
    }
}

@Composable
fun DefectCategoryDropdown(
    currentCategory: DefectCategory,
    onCategoryChange: (DefectCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedTextField(
            value = formatCategory(currentCategory),
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            placeholder = { Text("Select category") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select category"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DefectCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = { Text(formatCategory(category)) },
                    onClick = {
                        onCategoryChange(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DefectDetailScreen(
    defect: Defect,
    onBackClick: () -> Unit,
    onDefectUpdate: (Defect) -> Unit,
    onDefectDuplicate: (Defect) -> Unit,
    onDefectDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDefect by remember { mutableStateOf(defect) }
    var originalDefect by remember { mutableStateOf(defect) }
    var isEditing by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }
    var newSubTaskText by remember { mutableStateOf("") }
    var newNoteText by remember { mutableStateOf(defect.notes) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showDueDateDialog by remember { mutableStateOf(false) }
    var showCompleteAllDialog by remember { mutableStateOf(false) }
    var showRemoveAllDialog by remember { mutableStateOf(false) }
    var editingSubTaskId by remember { mutableStateOf<String?>(null) }
    var editingSubTaskText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFileReviewDialog by remember { mutableStateOf(false) }
    var selectedAttachment by remember { mutableStateOf<FileAttachment?>(null) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    
    // Photo functionality
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        showAttachmentDialog = false
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
                currentDefect = currentDefect.copy(attachments = currentDefect.attachments + newAttachment)
                onDefectUpdate(currentDefect)
            }
        }
        pendingCameraUri = null
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        showAttachmentDialog = false
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
            currentDefect = currentDefect.copy(attachments = currentDefect.attachments + newAttachment)
            onDefectUpdate(currentDefect)
        }
    }
    
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        showAttachmentDialog = false
        if (uri != null) {
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    "File_${System.currentTimeMillis()}"
                }
            } ?: "File_${System.currentTimeMillis()}"
            
            val fileSize = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getLong(sizeIndex)
                } else {
                    0L
                }
            } ?: 0L
            
            val fileType = context.contentResolver.getType(uri) ?: "file"
            
            val newAttachment = FileAttachment(
                id = UUID.randomUUID().toString(),
                name = fileName,
                type = fileType,
                uri = uri.toString(),
                size = fileSize
            )
            currentDefect = currentDefect.copy(attachments = currentDefect.attachments + newAttachment)
            onDefectUpdate(currentDefect)
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
    }
    
    // Save and cancel functions
    val saveChanges = {
        onDefectUpdate(currentDefect)
        originalDefect = currentDefect
        isEditing = false
        hasChanges = false
    }
    
    val cancelChanges = {
        currentDefect = originalDefect
        newNoteText = originalDefect.notes
        isEditing = false
        hasChanges = false
    }
    
    val startEditing = {
        isEditing = true
    }
    
    val duplicateDefect = {
        val duplicatedDefect = currentDefect.copy(
            id = UUID.randomUUID().toString(),
            location = "${currentDefect.location} (Copy)",
            status = DefectStatus.OPEN,
            closedAt = null,
            subTasks = currentDefect.subTasks.map { subTask ->
                subTask.copy(id = UUID.randomUUID().toString(), isCompleted = false)
            }
        )
        onDefectDuplicate(duplicatedDefect)
    }
    
    val deleteDefect = {
        onDefectDelete(currentDefect.id)
    }
    
    // Track changes
    LaunchedEffect(currentDefect, newNoteText) {
        hasChanges = currentDefect != originalDefect || newNoteText != originalDefect.notes
    }
    
    // Helper function to check if defect has open sub-tasks
    fun hasOpenSubTasks(defect: Defect): Boolean {
        return defect.subTasks.any { !it.isCompleted }
    }
    
    // Helper function to start editing a sub-task
    fun startEditingSubTask(subTask: SubTask) {
        editingSubTaskId = subTask.id
        editingSubTaskText = subTask.title
    }
    
    // Helper function to save sub-task changes
    fun saveSubTaskChanges() {
        if (editingSubTaskId != null && editingSubTaskText.isNotBlank()) {
            val updatedSubTasks = currentDefect.subTasks.map {
                if (it.id == editingSubTaskId) {
                    it.copy(title = editingSubTaskText.trim())
                } else {
                    it
                }
            }
            currentDefect = currentDefect.copy(subTasks = updatedSubTasks)
        }
        editingSubTaskId = null
        editingSubTaskText = ""
    }
    
    // Helper function to cancel sub-task editing
    fun cancelSubTaskEditing() {
        editingSubTaskId = null
        editingSubTaskText = ""
    }

    Column(
        modifier = modifier
    ) {
        // Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = if (isEditing) "Edit Defect" else "Defect Details",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            
            // Action buttons
            Row {
                if (!isEditing) {
                    // Duplicate button (only when not editing)
                    IconButton(onClick = duplicateDefect) {
                        Icon(Icons.Default.Add, contentDescription = "Duplicate Defect")
                    }
                    
                    // Edit button (only when not editing)
                    IconButton(onClick = startEditing) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Defect")
                    }
                }
                
                // Delete button (always available) - copied from TaskDetailScreen
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Defect")
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Defect Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEditing) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = if (isEditing) 
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status pill/tag (same as DefectListScreen)
                            Surface(
                                color = when (currentDefect.status) {
                                    DefectStatus.OPEN -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                    DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                },
                                border = when (currentDefect.status) {
                                    DefectStatus.OPEN -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                                    DefectStatus.IN_PROGRESS -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    DefectStatus.CLOSED -> BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                                },
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = when (currentDefect.status) {
                                            DefectStatus.OPEN -> "Open"
                                            DefectStatus.IN_PROGRESS -> "In Progress"
                                            DefectStatus.CLOSED -> "Closed"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = when (currentDefect.status) {
                                            DefectStatus.OPEN -> MaterialTheme.colorScheme.error
                                            DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                                            DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiary
                                        }
                                    )
                                    
                                    when (currentDefect.status) {
                                        DefectStatus.OPEN -> {
                                            // Empty circle for Open status
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                                                shape = CircleShape,
                                                modifier = Modifier.size(12.dp)
                                            ) {}
                                        }
                                        DefectStatus.IN_PROGRESS -> {
                                            // Half-filled circle for In Progress - simplified version
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                                shape = CircleShape,
                                                modifier = Modifier.size(12.dp)
                                            ) {}
                                        }
                                        DefectStatus.CLOSED -> {
                                            // Filled circle for Closed status
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiary,
                                                shape = CircleShape,
                                                modifier = Modifier.size(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Closed",
                                                    tint = MaterialTheme.colorScheme.onTertiary,
                                                    modifier = Modifier.size(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Show closed date if defect is closed
                        if (currentDefect.status == DefectStatus.CLOSED && currentDefect.closedAt != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Closed on ${formatDateForDisplay(currentDefect.closedAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isEditing) {
                            OutlinedTextField(
                                value = currentDefect.location,
                                onValueChange = { newLocation ->
                                    currentDefect = currentDefect.copy(location = newLocation)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter defect location") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { /* Handle next action */ }
                                )
                            )
                        } else {
                            Text(
                                text = currentDefect.location,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Text(
                            text = formatCategory(currentDefect.category),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Priority dropdown (same as tasks)
                            PriorityDropdown(
                                currentPriority = currentDefect.priority,
                                onPriorityChange = { newPriority: Priority ->
                                    currentDefect = currentDefect.copy(priority = newPriority)
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Sub-task indicator in header
                            if (currentDefect.subTasks.isNotEmpty()) {
                                val openSubTasks = currentDefect.subTasks.count { !it.isCompleted }
                                if (openSubTasks > 0) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                                        shape = MaterialTheme.shapes.small,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                                                                                                                                                                                                                                                    Text(
                                        text = "Tasks: $openSubTasks",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                            
                            // Due Date
                            currentDefect.dueDate?.let { dueDate ->
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "Due: $dueDate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Category
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isEditing) {
                            DefectCategoryDropdown(
                                currentCategory = currentDefect.category,
                                onCategoryChange = { newCategory ->
                                    currentDefect = currentDefect.copy(category = newCategory)
                                }
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = formatCategory(currentDefect.category),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Description
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isEditing) {
                            OutlinedTextField(
                                value = currentDefect.description,
                                onValueChange = { newDescription ->
                                    currentDefect = currentDefect.copy(description = newDescription)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter defect description") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { /* Handle done action */ }
                                ),
                                minLines = 3,
                                maxLines = 6
                            )
                        } else {
                            Text(
                                text = currentDefect.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // Status Management
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { showStatusDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = when (currentDefect.status) {
                                    DefectStatus.OPEN -> Icons.Default.Info
                                    DefectStatus.IN_PROGRESS -> Icons.Default.Info
                                    DefectStatus.CLOSED -> Icons.Default.CheckCircle
                                },
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Status: ${currentDefect.status.name.replace("_", " ")}")
                        }
                        
                                                    // Gentle reminder about open sub-tasks
                            if (hasOpenSubTasks(currentDefect)) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "You have ${currentDefect.subTasks.count { !it.isCompleted }} open sub-tasks - consider completing them before closing the defect",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                    }
                }
            }
            
            // Due Date Management
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Due Date",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { showDueDateDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formatDateForDisplay(currentDefect.dueDate)
                            )
                        }
                    }
                }
            }
            
            // Notes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = newNoteText,
                            onValueChange = { 
                                newNoteText = it
                                currentDefect = currentDefect.copy(notes = it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Add notes about this defect...") },
                            minLines = 3
                        )
                    }
                }
            }
            
            // Sub-tasks
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sub-tasks",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Sub-task summary indicator
                            if (currentDefect.subTasks.isNotEmpty()) {
                                val totalSubTasks = currentDefect.subTasks.size
                                val completedSubTasks = currentDefect.subTasks.count { it.isCompleted }
                                val openSubTasks = totalSubTasks - completedSubTasks
                                
                                if (openSubTasks > 0) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                                        shape = MaterialTheme.shapes.small,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Open Sub-tasks",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "$openSubTasks/$totalSubTasks open",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                } else {
                                    // All sub-tasks completed
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
                                        shape = MaterialTheme.shapes.small,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "All Sub-tasks Completed",
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "All completed",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Gentle action buttons for open sub-tasks
                        if (hasOpenSubTasks(currentDefect)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showCompleteAllDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Mark All Complete",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Mark All Complete",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                OutlinedButton(
                                    onClick = { showRemoveAllDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Clear All Sub-tasks",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Clear All Sub-tasks",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // Helpful hint
                                                    Text(
                            text = "Quick actions to help manage your sub-tasks efficiently.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Add new sub-task
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSubTaskText,
                                onValueChange = { newSubTaskText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Add a sub-task...") },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newSubTaskText.isNotBlank()) {
                                            val newSubTask = SubTask(
                                                id = UUID.randomUUID().toString(),
                                                title = newSubTaskText
                                            )
                                            currentDefect = currentDefect.copy(
                                                subTasks = currentDefect.subTasks + newSubTask
                                            )
                                            newSubTaskText = ""
                                            
                                            // Automatically start editing the newly created sub-task
                                            startEditingSubTask(newSubTask)
                                        }
                                    }
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            IconButton(
                                onClick = {
                                    if (newSubTaskText.isNotBlank()) {
                                        val newSubTask = SubTask(
                                            id = UUID.randomUUID().toString(),
                                            title = newSubTaskText
                                        )
                                        currentDefect = currentDefect.copy(
                                            subTasks = currentDefect.subTasks + newSubTask
                                        )
                                        newSubTaskText = ""
                                        
                                        // Automatically start editing the newly created sub-task
                                        startEditingSubTask(newSubTask)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add sub-task")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Sub-tasks list
                        currentDefect.subTasks.forEach { subTask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = subTask.isCompleted,
                                    onCheckedChange = { isChecked ->
                                        val updatedSubTasks = currentDefect.subTasks.map {
                                            if (it.id == subTask.id) {
                                                it.copy(isCompleted = isChecked)
                                            } else {
                                                it
                                            }
                                        }
                                        currentDefect = currentDefect.copy(subTasks = updatedSubTasks)
                                    }
                                )
                                
                                // Sub-task title - editable when editing
                                if (editingSubTaskId == subTask.id) {
                                    OutlinedTextField(
                                        value = editingSubTaskText,
                                        onValueChange = { editingSubTaskText = it },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        ),
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { saveSubTaskChanges() }
                                        )
                                    )
                                } else {
                                    Text(
                                        text = subTask.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        color = if (subTask.isCompleted) {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                                
                                // Action buttons
                                if (editingSubTaskId == subTask.id) {
                                    // Save and Cancel buttons when editing
                                    IconButton(
                                        onClick = { saveSubTaskChanges() }
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Save sub-task")
                                    }
                                    
                                    IconButton(
                                        onClick = { cancelSubTaskEditing() }
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "Cancel editing")
                                    }
                                } else {
                                    // Edit and Delete buttons when not editing
                                    IconButton(
                                        onClick = { startEditingSubTask(subTask) }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit sub-task")
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            val updatedSubTasks = currentDefect.subTasks.filter { it.id != subTask.id }
                                            currentDefect = currentDefect.copy(subTasks = updatedSubTasks)
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete sub-task")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Attachments section (files)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attachments",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            if (isEditing) {
                                IconButton(onClick = { showAttachmentDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Attachment")
                                }
                            }
                        }
                        
                        // Show attachments
                        if (currentDefect.attachments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            currentDefect.attachments.forEach { attachment ->
                                AttachmentItem(
                                    attachment = attachment,
                                    onDelete = {
                                        val updatedAttachments = currentDefect.attachments.filter { it.id != attachment.id }
                                        currentDefect = currentDefect.copy(attachments = updatedAttachments)
                                        onDefectUpdate(currentDefect)
                                    },
                                    onReview = {
                                        println("DEFECT: Attachment review clicked for: ${attachment.name}")
                                        selectedAttachment = attachment
                                        showFileReviewDialog = true
                                        println("DEFECT: showFileReviewDialog set to: $showFileReviewDialog")
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Created date
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Created",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = currentDefect.createdAt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        // Save/Cancel buttons when editing
        if (isEditing) {
            SaveCancelButtons(
                onSave = saveChanges,
                onCancel = cancelChanges
            )
        }
    }
    
    // Status Dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Change Status") },
            text = {
                Column {
                    // Helpful message for open sub-tasks
                    if (hasOpenSubTasks(currentDefect)) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "You have ${currentDefect.subTasks.count { !it.isCompleted }} open sub-tasks",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Consider completing all sub-tasks before closing the defect to ensure nothing is missed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    DefectStatus.values().forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentDefect.status == status,
                                onClick = {
                                    // Only allow status change if not trying to close with open sub-tasks
                                    if (status != DefectStatus.CLOSED || !hasOpenSubTasks(currentDefect)) {
                                        currentDefect = currentDefect.copy(status = status)
                                        showStatusDialog = false
                                    }
                                },
                                enabled = !(status == DefectStatus.CLOSED && hasOpenSubTasks(currentDefect))
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Status indicator (same as DefectListScreen)
                            Surface(
                                color = when (status) {
                                    DefectStatus.OPEN -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                    DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                },
                                border = when (status) {
                                    DefectStatus.OPEN -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                                    DefectStatus.IN_PROGRESS -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    DefectStatus.CLOSED -> BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                                },
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (status) {
                                        DefectStatus.OPEN -> {
                                            // Empty circle for Open status
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                                                shape = CircleShape,
                                                modifier = Modifier.size(12.dp)
                                            ) {}
                                        }
                                        DefectStatus.IN_PROGRESS -> {
                                            // Half-filled circle for In Progress - simplified version
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                                shape = CircleShape,
                                                modifier = Modifier.size(12.dp)
                                            ) {}
                                        }
                                        DefectStatus.CLOSED -> {
                                            // Filled circle for Closed status
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiary,
                                                shape = CircleShape,
                                                modifier = Modifier.size(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Closed",
                                                    tint = MaterialTheme.colorScheme.onTertiary,
                                                    modifier = Modifier.size(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = when (status) {
                                        DefectStatus.OPEN -> "Open"
                                        DefectStatus.IN_PROGRESS -> "In Progress"
                                        DefectStatus.CLOSED -> "Closed"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (currentDefect.status == status) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (currentDefect.status == status) {
                                        when (status) {
                                            DefectStatus.OPEN -> MaterialTheme.colorScheme.outline
                                            DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                                            DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiary
                                        }
                                    } else if (status == DefectStatus.CLOSED && hasOpenSubTasks(currentDefect)) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                
                                // Show helpful hint for Closed status with open sub-tasks
                                if (status == DefectStatus.CLOSED && hasOpenSubTasks(currentDefect)) {
                                    Text(
                                        text = "Has open sub-tasks",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Due Date Dialog
    if (showDueDateDialog) {
        EnhancedDatePicker(
            selectedDate = currentDefect.dueDate,
            onDateSelected = { date ->
                currentDefect = currentDefect.copy(dueDate = date)
            },
            onDismiss = { showDueDateDialog = false },
            title = "Set Due Date"
        )
    }
    
    // Complete All Sub-tasks Confirmation Dialog
    if (showCompleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteAllDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark All Sub-tasks Complete?")
                }
            },
            text = {
                Column {
                    Text(
                        text = "This will mark all ${currentDefect.subTasks.count { !it.isCompleted }} open sub-tasks as completed.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This is useful when you've finished all the related work. Would you like to proceed?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedSubTasks = currentDefect.subTasks.map { it.copy(isCompleted = true) }
                        currentDefect = currentDefect.copy(subTasks = updatedSubTasks)
                        showCompleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Complete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Remove All Sub-tasks Confirmation Dialog
    if (showRemoveAllDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveAllDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Organize Sub-tasks")
                }
            },
            text = {
                Column {
                                            Text(
                            text = "This will help you start fresh with your sub-tasks.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All current sub-tasks will be removed, but this can help you reorganize your work more effectively. Would you like to continue?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentDefect = currentDefect.copy(subTasks = emptyList())
                        showRemoveAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Start Fresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
        
    }
    
    // Delete confirmation dialog - positioned outside main Column to avoid UI conflicts
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Defect")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this defect?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteDefect()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Attachment Dialog - positioned outside main Column to avoid UI conflicts
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
    
    // File Review Dialog - positioned outside main Column to avoid UI conflicts
    if (showFileReviewDialog && selectedAttachment != null) {
        println("DEFECT: Rendering FileReviewDialog for: ${selectedAttachment!!.name}")
        FileReviewDialog(
            attachment = selectedAttachment!!,
            onDismiss = {
                showFileReviewDialog = false
                selectedAttachment = null
            },
            onDelete = {
                // Delete from attachments if present, otherwise from images fallback
                val att = selectedAttachment!!
                val updatedAttachments = currentDefect.attachments.filter { it.id != att.id }
                val updatedImages = currentDefect.images.filter { it != att.uri }
                currentDefect = currentDefect.copy(
                    attachments = updatedAttachments,
                    images = updatedImages
                )
                onDefectUpdate(currentDefect)
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
