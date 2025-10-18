package com.example.movein.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.FileAttachment
import com.example.movein.shared.data.Priority
import com.example.movein.utils.FileManager
import com.example.movein.shared.data.TaskStatus
import com.example.movein.ui.components.AttachmentDialog
import com.example.movein.ui.components.AttachmentItem
import com.example.movein.ui.components.EnhancedDatePicker
import com.example.movein.ui.components.FileReviewDialog
import com.example.movein.utils.CategoryUtils
import com.example.movein.utils.getTodayString
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun AddEditTaskScreen(
    task: ChecklistItem? = null,
    onBackClick: () -> Unit,
    onTaskSave: (ChecklistItem) -> Unit,
    existingTaskNames: List<String> = emptyList(),
    userData: com.example.movein.shared.data.UserData? = null,
    appState: com.example.movein.AppState? = null,
    modifier: Modifier = Modifier
) {
    val isEditing = task != null
    
    var taskName by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(task?.category ?: CategoryUtils.getDefaultCategory(userData)) }
    var selectedPriority by remember { mutableStateOf(task?.priority ?: Priority.MEDIUM) }
    var selectedDueDate by remember { mutableStateOf(task?.dueDate) }
    var selectedAttachments by remember { mutableStateOf(task?.attachments ?: emptyList<FileAttachment>()) }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var showDueDateDialog by remember { mutableStateOf(false) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var showFileReviewDialog by remember { mutableStateOf(false) }
    var selectedAttachment by remember { mutableStateOf<FileAttachment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    // File manager for handling attachments
    val fileManager = remember { FileManager(context) }
    
    // Focus management
    val focusRequester = remember { FocusRequester() }
    
    // Request focus when screen opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        showAttachmentDialog = false
        if (success) {
            pendingCameraUri?.let { uri ->
                coroutineScope.launch {
                    val persistedAttachment = fileManager.persistFile(
                        uri = uri,
                        originalName = "Camera_${System.currentTimeMillis()}.jpg",
                        mimeType = "image/jpeg"
                    )
                    
                    persistedAttachment?.let { attachment ->
                        selectedAttachments = selectedAttachments + attachment
                    }
                }
            }
        }
        pendingCameraUri = null
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        showAttachmentDialog = false
        uris?.forEach { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0 && cursor.moveToFirst()) {
                            cursor.getString(nameIndex)
                        } else {
                            "Gallery_${System.currentTimeMillis()}.jpg"
                        }
                    } ?: "Gallery_${System.currentTimeMillis()}.jpg"
                    
                    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                    
                    val persistedAttachment = fileManager.persistFile(
                        uri = uri,
                        originalName = fileName,
                        mimeType = mimeType
                    )
                    
                    persistedAttachment?.let { attachment ->
                        selectedAttachments = selectedAttachments + attachment
                    }
                }
            }
        }
    }
    
    // File launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        showAttachmentDialog = false
        uris?.forEach { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0 && cursor.moveToFirst()) {
                            cursor.getString(nameIndex)
                        } else {
                            "Document_${System.currentTimeMillis()}.pdf"
                        }
                    } ?: "Document_${System.currentTimeMillis()}.pdf"
                    
                    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                    
                    val persistedAttachment = fileManager.persistFile(
                        uri = uri,
                        originalName = fileName,
                        mimeType = mimeType
                    )
                    
                    persistedAttachment?.let { attachment ->
                        selectedAttachments = selectedAttachments + attachment
                    }
                }
            }
        }
    }
    
    // Save function
    val saveTask = {
        nameError = null
        
        if (taskName.isBlank()) {
            nameError = "Task name is required"
        } else if (isEditing && existingTaskNames.filter { it != task?.title }.contains(taskName)) {
            nameError = "Task name already exists"
        } else if (!isEditing && existingTaskNames.contains(taskName)) {
            nameError = "Task name already exists"
        } else {
        
        isSaving = true
        
        val newTask = if (isEditing) {
            task!!.copy(
                title = taskName,
                description = description,
                category = selectedCategory,
                priority = selectedPriority,
                dueDate = selectedDueDate,
                attachments = selectedAttachments,
                notes = notes
            )
        } else {
            ChecklistItem(
                id = UUID.randomUUID().toString(),
                title = taskName,
                description = description,
                category = selectedCategory,
                isCompleted = false,
                notes = notes,
                attachments = selectedAttachments,
                subTasks = emptyList(),
                priority = selectedPriority,
                dueDate = selectedDueDate,
                isUserAdded = true,
                status = TaskStatus.OPEN
            )
        }
        
            onTaskSave(newTask)
            onBackClick()
        }
    }
    
    // Cancel function
    val cancelTask = {
        onBackClick()
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (isEditing) "Edit Task" else "Add New Task",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
        }
        
        // Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Task Name Field
                Column {
                    Text(
                        text = "Task Name *",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { 
                            taskName = it
                            nameError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("Enter task name") },
                        isError = nameError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                        )
                    )
                    
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            item {
                // Description Field
                Column {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter task description") },
                        minLines = 3,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )
                    )
                }
            }
            
            item {
                // Category Selection
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDialog = true },
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select Category")
                        },
                        placeholder = { Text("Select category") }
                    )
                }
            }
            
            item {
                // Priority Selection
                Column {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = selectedPriority.name,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPriorityDialog = true },
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select Priority")
                        },
                        placeholder = { Text("Select priority") }
                    )
                }
            }
            
            item {
                // Due Date Selection
                Column {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = selectedDueDate ?: "No due date",
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDueDateDialog = true },
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select Due Date")
                        },
                        placeholder = { Text("Select due date") }
                    )
                }
            }
            
            item {
                // Notes Section
                Column {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Add notes...") },
                        minLines = 3,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )
                    )
                }
            }
            
            item {
                // Attachments Section
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
        }
        
        // Action buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save and Cancel buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = cancelTask,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = saveTask,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isEditing) "Save" else "Add Task")
                    }
                }
            }
            
            // Delete button for editing mode (user-friendly)
            if (isEditing) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Task")
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
                    CategoryUtils.getAvailableTaskCategories(userData).forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategory = category
                                    showCategoryDialog = false
                                }
                                .padding(vertical = 8.dp),
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
                                text = category,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
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
                                .clickable {
                                    selectedPriority = priority
                                    showPriorityDialog = false
                                }
                                .padding(vertical = 8.dp),
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
                                text = priority.name,
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
    
    if (showDueDateDialog) {
        EnhancedDatePicker(
            selectedDate = selectedDueDate,
            onDateSelected = { date ->
                selectedDueDate = date
                showDueDateDialog = false
            },
            onDismiss = { showDueDateDialog = false },
            title = if (isEditing) "Set Due Date" else "Select Due Date"
        )
    }
    
    if (showAttachmentDialog) {
        AttachmentDialog(
            onDismiss = { showAttachmentDialog = false },
            onAddImage = {
                // Launch camera
                try {
                    val imageFile = java.io.File.createTempFile("IMG_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
                    val imageUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        imageFile
                    )
                    pendingCameraUri = imageUri
                    cameraLauncher.launch(imageUri)
                } catch (e: Exception) {
                    // Handle error
                }
            },
            onAddFile = {
                fileLauncher.launch("*/*")
            },
            onAddGallery = {
                galleryLauncher.launch("image/*")
            }
        )
    }
    
    if (showFileReviewDialog && selectedAttachment != null) {
        FileReviewDialog(
            attachment = selectedAttachment!!,
            onDismiss = { showFileReviewDialog = false },
            onDelete = {
                selectedAttachments = selectedAttachments.filter { it.id != selectedAttachment!!.id }
                showFileReviewDialog = false
            },
            onShare = {
                // Share functionality
            },
            onOpen = {
                // Open functionality
            }
        )
    }
    
    if (showDeleteDialog && isEditing) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onBackClick()
                        // Note: Delete functionality would need to be passed as a parameter
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
