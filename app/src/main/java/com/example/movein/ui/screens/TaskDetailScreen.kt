package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.SubTask
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.FileAttachment
import com.example.movein.utils.getTodayString
import com.example.movein.utils.getTomorrowString
import com.example.movein.utils.getNextWeekString
import com.example.movein.utils.formatPriority
import com.example.movein.utils.formatDateForDisplay
import com.example.movein.ui.components.SaveCancelButtons
import com.example.movein.utils.formatTaskStatus
import com.example.movein.ui.components.EnhancedDatePicker
import com.example.movein.ui.components.TaskStatusDropdown
import com.example.movein.utils.CategoryUtils
import com.example.movein.utils.PermissionUtils
import com.example.movein.utils.ImageUtils
import com.example.movein.utils.FileReviewUtils
import com.example.movein.ui.components.FileReviewDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.util.*

@Composable
fun TaskDetailScreen(
    task: ChecklistItem,
    onBackClick: () -> Unit,
    onTaskUpdate: (ChecklistItem) -> Unit,
    onTaskDuplicate: (ChecklistItem) -> Unit,
    onTaskDelete: (String) -> Unit,
    userData: com.example.movein.shared.data.UserData? = null,
    modifier: Modifier = Modifier
) {
    var currentTask by remember { mutableStateOf(task) }
    var originalTask by remember { mutableStateOf(task) }
    var isEditing by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }
    var newSubTaskText by remember { mutableStateOf("") }
    var newNoteText by remember { mutableStateOf(task.notes) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var showDueDateDialog by remember { mutableStateOf(false) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var editingSubTaskId by remember { mutableStateOf<String?>(null) }
    var editingSubTaskText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFileReviewDialog by remember { mutableStateOf(false) }
    var selectedAttachment by remember { mutableStateOf<FileAttachment?>(null) }
    
    // Real attachment functionality
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* handled inline */ }
    
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
                currentTask = currentTask.copy(attachments = currentTask.attachments + newAttachment)
                // Immediately save the task with the new attachment
                onTaskUpdate(currentTask)
            }
        }
        pendingCameraUri = null
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        showAttachmentDialog = false
        uris?.forEach { uri ->
            if (uri != null) {
                val newAttachment = FileAttachment(
                    id = UUID.randomUUID().toString(),
                    name = "Gallery_${System.currentTimeMillis()}.jpg",
                    type = "image",
                    uri = uri.toString(),
                    size = 1024 * 1024 // 1MB
                )
                currentTask = currentTask.copy(attachments = currentTask.attachments + newAttachment)
                // Immediately save the task with the new attachment
                onTaskUpdate(currentTask)
            }
        }
    }
    
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        showAttachmentDialog = false
        uris?.forEach { uri ->
            if (uri != null) {
                val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        cursor.getString(nameIndex)
                    } else null
                } ?: "Document_${System.currentTimeMillis()}.pdf"
                
                val newAttachment = FileAttachment(
                    id = UUID.randomUUID().toString(),
                    name = fileName,
                    type = "file",
                    uri = uri.toString(),
                    size = 2048 * 1024 // 2MB
                )
                currentTask = currentTask.copy(attachments = currentTask.attachments + newAttachment)
                // Immediately save the task with the new attachment
                onTaskUpdate(currentTask)
            }
        }
    }
    
    // Helper functions for sub-task editing
    val startEditingSubTask = { subTaskId: String, currentText: String ->
        editingSubTaskId = subTaskId
        editingSubTaskText = currentText
    }
    
    val saveSubTaskChanges = {
        if (editingSubTaskId != null && editingSubTaskText.isNotBlank()) {
            val updatedSubTasks = currentTask.subTasks.map { subTask ->
                if (subTask.id == editingSubTaskId) {
                    subTask.copy(title = editingSubTaskText)
                } else {
                    subTask
                }
            }
            currentTask = currentTask.copy(subTasks = updatedSubTasks)
        }
        editingSubTaskId = null
        editingSubTaskText = ""
    }
    
    val cancelSubTaskEditing = {
        editingSubTaskId = null
        editingSubTaskText = ""
    }
    
    // Save and cancel functions
    val saveChanges = {
        onTaskUpdate(currentTask)
        originalTask = currentTask
        isEditing = false
        hasChanges = false
    }
    
    val cancelChanges = {
        currentTask = originalTask
        newNoteText = originalTask.notes
        isEditing = false
        hasChanges = false
    }
    
    val startEditing = {
        isEditing = true
    }
    
    val duplicateTask = {
        val duplicatedTask = currentTask.copy(
            id = UUID.randomUUID().toString(),
            title = "${currentTask.title} (Copy)",
            status = com.example.movein.shared.data.TaskStatus.OPEN,
            isCompleted = false,
            subTasks = currentTask.subTasks.map { subTask ->
                subTask.copy(id = UUID.randomUUID().toString(), isCompleted = false)
            }
        )
        onTaskDuplicate(duplicatedTask)
    }
    
    val deleteTask = {
        onTaskDelete(currentTask.id)
    }
    
    // Track changes
    LaunchedEffect(currentTask, newNoteText) {
        hasChanges = currentTask != originalTask || newNoteText != originalTask.notes
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
                text = if (isEditing) "Edit Task" else "Task Details",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            
            // Action buttons
            if (!isEditing) {
                Row {
                    // Duplicate button
                    IconButton(onClick = duplicateTask) {
                        Icon(Icons.Default.Add, contentDescription = "Duplicate Task")
                    }
                    
                    // Edit button
                    IconButton(onClick = startEditing) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                    }
                    
                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                    }
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Title and Status
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = currentTask.isCompleted || currentTask.status == com.example.movein.shared.data.TaskStatus.CLOSED,
                            onCheckedChange = { isChecked ->
                                currentTask = currentTask.copy(
                                    isCompleted = isChecked,
                                    status = if (isChecked) com.example.movein.shared.data.TaskStatus.CLOSED else com.example.movein.shared.data.TaskStatus.OPEN
                                )
                            }
                        )
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isEditing) {
                                // Editable title with better styling
                                OutlinedTextField(
                                    value = currentTask.title,
                                    onValueChange = { newTitle ->
                                        currentTask = currentTask.copy(title = newTitle)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Enter task title") },
                                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Category dropdown with better styling
                                CategoryDropdown(
                                    currentCategory = currentTask.category,
                                    onCategoryChange = { newCategory ->
                                        currentTask = currentTask.copy(category = newCategory)
                                    },
                                    userData = userData
                                )
                            } else {
                                Text(
                                    text = currentTask.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Text(
                                    text = currentTask.category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Priority Chip
                                Surface(
                                    color = when (currentTask.priority) {
                                        Priority.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        Priority.MEDIUM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        Priority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    },
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = formatPriority(currentTask.priority),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (currentTask.priority) {
                                            Priority.LOW -> MaterialTheme.colorScheme.primary
                                            Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                                            Priority.HIGH -> MaterialTheme.colorScheme.error
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                
                                // Due Date if available
                                currentTask.dueDate?.let { dueDate ->
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Due: ${formatDateForDisplay(dueDate)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Task Description
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        if (isEditing) {
                            OutlinedTextField(
                                value = currentTask.description,
                                onValueChange = { newDescription ->
                                    currentTask = currentTask.copy(description = newDescription)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter task description") },
                                minLines = 3,
                                maxLines = 6,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                )
                            )
                        } else {
                            Text(
                                text = currentTask.description.ifEmpty { "No description provided" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (currentTask.description.isEmpty()) 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Priority and Due Date Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Status, Priority & Due Date",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Task Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TaskStatusDropdown(
                                    currentStatus = currentTask.status,
                                    onStatusChange = { status ->
                                        currentTask = currentTask.copy(
                                            status = status,
                                            isCompleted = status == com.example.movein.shared.data.TaskStatus.CLOSED
                                        )
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Priority
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Priority",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatPriority(currentTask.priority),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Button(
                                onClick = { showPriorityDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Change")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Due Date",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatDateForDisplay(currentTask.dueDate),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Button(
                                onClick = { showDueDateDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Set")
                            }
                        }
                    }
                }
            }

            // Notes Section
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
                                text = "Notes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            
                            IconButton(onClick = { showAttachmentDialog = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Attach File")
                            }
                        }
                        
                        BasicTextField(
                            value = newNoteText,
                            onValueChange = { 
                                newNoteText = it
                                currentTask = currentTask.copy(notes = it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(12.dp)
                                ) {
                                    if (newNoteText.isEmpty()) {
                                        Text(
                                            text = "Add your notes here...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        
                        // Attachments List
                        if (currentTask.attachments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Attachments:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            currentTask.attachments.forEach { attachment ->
                                AttachmentItem(
                                    attachment = attachment,
                                    onDelete = {
                                        val updatedAttachments = currentTask.attachments.filter { it.id != attachment.id }
                                        currentTask = currentTask.copy(attachments = updatedAttachments)
                                        // Immediately save the task with the updated attachments
                                        onTaskUpdate(currentTask)
                                    },
                                    onReview = {
                                        selectedAttachment = attachment
                                        showFileReviewDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Sub-tasks Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Sub-tasks",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Add new sub-task
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = newSubTaskText,
                                onValueChange = { newSubTaskText = it },
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        if (newSubTaskText.isEmpty()) {
                                            Text(
                                                text = "Add a sub-task...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            
                            IconButton(
                                onClick = {
                                    if (newSubTaskText.isNotBlank()) {
                                        val newSubTaskId = UUID.randomUUID().toString()
                                        val newSubTask = SubTask(
                                            id = newSubTaskId,
                                            title = newSubTaskText
                                        )
                                        currentTask = currentTask.copy(
                                            subTasks = currentTask.subTasks + newSubTask
                                        )
                                        newSubTaskText = ""
                                        
                                        // Auto-open the new sub-task for editing
                                        startEditingSubTask(newSubTaskId, newSubTask.title)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add sub-task")
                            }
                        }
                    }
                }
            }

            // Sub-tasks List
            items(currentTask.subTasks) { subTask ->
                SubTaskItem(
                    subTask = subTask,
                    isEditing = editingSubTaskId == subTask.id,
                    editingText = editingSubTaskText,
                    onToggle = { isChecked ->
                        val updatedSubTasks = currentTask.subTasks.map { 
                            if (it.id == subTask.id) it.copy(isCompleted = isChecked) else it 
                        }
                        currentTask = currentTask.copy(subTasks = updatedSubTasks)
                    },
                    onEditClick = { startEditingSubTask(subTask.id, subTask.title) },
                    onTextChange = { editingSubTaskText = it },
                    onSave = saveSubTaskChanges,
                    onCancel = cancelSubTaskEditing
                )
            }
        }
        
        // Save/Cancel buttons when editing
        if (isEditing) {
            SaveCancelButtons(
                onSave = saveChanges,
                onCancel = cancelChanges
            )
        }
        
        // Delete confirmation dialog
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
                        Text("Delete Task")
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Are you sure you want to delete this task?",
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
                            deleteTask()
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
        
        // Priority Dialog
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
                                    selected = currentTask.priority == priority,
                                    onClick = {
                                        currentTask = currentTask.copy(priority = priority)
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
                    TextButton(onClick = { showPriorityDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Due Date Dialog
        if (showDueDateDialog) {
            EnhancedDatePicker(
                selectedDate = currentTask.dueDate,
                onDateSelected = { date ->
                    currentTask = currentTask.copy(dueDate = date)
                },
                onDismiss = { showDueDateDialog = false },
                title = "Set Due Date"
            )
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
                            showAttachmentDialog = false
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
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
                        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                    val updatedAttachments = currentTask.attachments.filter { it.id != selectedAttachment!!.id }
                    currentTask = currentTask.copy(attachments = updatedAttachments)
                    onTaskUpdate(currentTask)
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

@Composable
fun AttachmentItem(
    attachment: FileAttachment,
    onDelete: () -> Unit,
    onReview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (attachment.type == "image") Icons.Default.Star else Icons.Default.Info,
                contentDescription = "Attachment",
                tint = if (attachment.type == "image") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onReview() }
            ) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = FileReviewUtils.formatFileSize(attachment.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                IconButton(onClick = onReview) {
                    Icon(Icons.Default.Info, contentDescription = "Review")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
        
        // Save/Cancel buttons when editing - needs proper scope
    }
}

@Composable
fun AttachmentDialog(
    onDismiss: () -> Unit,
    onAddImage: () -> Unit,
    onAddFile: () -> Unit,
    onAddGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Attachment") },
        text = {
            Column {
                Text(
                    text = "Choose attachment type:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Camera button
                Button(
                    onClick = onAddImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = null, 
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Take Photo",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Gallery button
                Button(
                    onClick = onAddGallery,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = null, 
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "Choose from Gallery",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // File button
                Button(
                    onClick = onAddFile,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = null, 
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            "Add File",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SubTaskItem(
    subTask: SubTask,
    isEditing: Boolean,
    editingText: String,
    onToggle: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = subTask.isCompleted,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            if (isEditing) {
                // Editing mode
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onSave() }
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(onClick = onSave) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Display mode
                Text(
                    text = subTask.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (subTask.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEditClick() }
                )
                
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // Save/Cancel buttons when editing - added above
    }
}

@Composable
fun CategoryDropdown(
    currentCategory: String,
    onCategoryChange: (String) -> Unit,
    userData: com.example.movein.shared.data.UserData? = null
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Dynamic task categories based on user's apartment details
    val categories = CategoryUtils.getAvailableTaskCategories(userData)
    
    Box {
        OutlinedTextField(
            value = currentCategory,
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
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategoryChange(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
