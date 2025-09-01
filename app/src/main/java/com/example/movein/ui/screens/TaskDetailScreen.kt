package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movein.data.ChecklistItem
import com.example.movein.data.SubTask
import com.example.movein.data.Priority
import com.example.movein.data.FileAttachment
import com.example.movein.utils.getTodayString
import com.example.movein.utils.getTomorrowString
import com.example.movein.utils.getNextWeekString
import com.example.movein.utils.formatPriority
import java.util.*

@Composable
fun TaskDetailScreen(
    task: ChecklistItem,
    onBackClick: () -> Unit,
    onTaskUpdate: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTask by remember { mutableStateOf(task) }
    var newSubTaskText by remember { mutableStateOf("") }
    var newNoteText by remember { mutableStateOf(task.notes) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var showDueDateDialog by remember { mutableStateOf(false) }
    var showAttachmentDialog by remember { mutableStateOf(false) }

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
                text = "Task Details",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Title and Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = currentTask.isCompleted,
                            onCheckedChange = { isChecked ->
                                currentTask = currentTask.copy(isCompleted = isChecked)
                                onTaskUpdate(currentTask)
                            }
                        )
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
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
                                        text = "Due: $dueDate",
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
                        
                        Text(
                            text = currentTask.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
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
                            text = "Priority & Due Date",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
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
                                    text = currentTask.dueDate ?: "Not set",
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
                                onTaskUpdate(currentTask)
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
                                        onTaskUpdate(currentTask)
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
                                        val newSubTask = SubTask(
                                            id = UUID.randomUUID().toString(),
                                            title = newSubTaskText
                                        )
                                        currentTask = currentTask.copy(
                                            subTasks = currentTask.subTasks + newSubTask
                                        )
                                        onTaskUpdate(currentTask)
                                        newSubTaskText = ""
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
                    onToggle = { isChecked ->
                        val updatedSubTasks = currentTask.subTasks.map { 
                            if (it.id == subTask.id) it.copy(isCompleted = isChecked) else it 
                        }
                        currentTask = currentTask.copy(subTasks = updatedSubTasks)
                        onTaskUpdate(currentTask)
                    }
                )
            }
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
                                        onTaskUpdate(currentTask)
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
            var selectedDate by remember { mutableStateOf(currentTask.dueDate ?: "") }
            var currentMonth by remember { mutableStateOf(0) } // 0 = current month
            
            AlertDialog(
                onDismissRequest = { showDueDateDialog = false },
                title = { Text("Set Due Date") },
                text = {
                    Column {
                        // Calendar Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { currentMonth-- }
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                            }
                            
                            Text(
                                text = getMonthYearString(currentMonth),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            IconButton(
                                onClick = { currentMonth++ }
                            ) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Calendar Grid
                        CalendarGrid(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            onDateSelected = { date ->
                                selectedDate = date
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = { selectedDate = getTodayString() }
                            ) {
                                Text("Today")
                            }
                            TextButton(
                                onClick = { selectedDate = getTomorrowString() }
                            ) {
                                Text("Tomorrow")
                            }
                            TextButton(
                                onClick = { selectedDate = getNextWeekString() }
                            ) {
                                Text("Next Week")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            currentTask = currentTask.copy(dueDate = if (selectedDate.isNotBlank()) selectedDate else null)
                            onTaskUpdate(currentTask)
                            showDueDateDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Set")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDueDateDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Attachment Dialog
        if (showAttachmentDialog) {
            AttachmentDialog(
                onDismiss = { showAttachmentDialog = false },
                onAddImage = {
                    // Simulate adding an image attachment
                    val newAttachment = FileAttachment(
                        id = UUID.randomUUID().toString(),
                        name = "Image_${System.currentTimeMillis()}.jpg",
                        type = "image",
                        uri = "content://image/${System.currentTimeMillis()}",
                        size = 1024 * 1024 // 1MB
                    )
                    currentTask = currentTask.copy(attachments = currentTask.attachments + newAttachment)
                    onTaskUpdate(currentTask)
                    showAttachmentDialog = false
                },
                onAddFile = {
                    // Simulate adding a file attachment
                    val newAttachment = FileAttachment(
                        id = UUID.randomUUID().toString(),
                        name = "Document_${System.currentTimeMillis()}.pdf",
                        type = "file",
                        uri = "content://file/${System.currentTimeMillis()}",
                        size = 2048 * 1024 // 2MB
                    )
                    currentTask = currentTask.copy(attachments = currentTask.attachments + newAttachment)
                    onTaskUpdate(currentTask)
                    showAttachmentDialog = false
                }
            )
        }
    }
}

@Composable
fun AttachmentItem(
    attachment: FileAttachment,
    onDelete: () -> Unit
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
                imageVector = if (attachment.type == "image") Icons.Default.Check else Icons.Default.MoreVert,
                contentDescription = "Attachment",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${attachment.size / 1024} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun AttachmentDialog(
    onDismiss: () -> Unit,
    onAddImage: () -> Unit,
    onAddFile: () -> Unit
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
                
                Button(
                    onClick = onAddImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Add Image")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onAddFile,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Add File")
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
    onToggle: (Boolean) -> Unit
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
            
            Text(
                text = subTask.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (subTask.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
