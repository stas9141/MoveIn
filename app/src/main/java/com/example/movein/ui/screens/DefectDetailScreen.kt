package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movein.data.Defect
import com.example.movein.data.SubTask
import com.example.movein.data.Priority
import com.example.movein.data.DefectStatus
import com.example.movein.data.DefectCategory
import com.example.movein.ui.components.PriorityDropdown
import com.example.movein.utils.formatCategory
import com.example.movein.utils.getTodayString
import com.example.movein.utils.getTomorrowString
import com.example.movein.utils.getNextWeekString
import java.util.*

@Composable
fun DefectDetailScreen(
    defect: Defect,
    onBackClick: () -> Unit,
    onDefectUpdate: (Defect) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDefect by remember { mutableStateOf(defect) }
    var newSubTaskText by remember { mutableStateOf("") }
    var newNoteText by remember { mutableStateOf(defect.notes) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showDueDateDialog by remember { mutableStateOf(false) }

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
                text = "Defect Details",
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
            // Defect Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
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
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = currentDefect.location,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
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
                                    onDefectUpdate(currentDefect)
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
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
                        
                        Text(
                            text = currentDefect.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                                    DefectStatus.IN_PROGRESS -> Icons.Default.Star
                                    DefectStatus.CLOSED -> Icons.Default.CheckCircle
                                },
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Status: ${currentDefect.status.name.replace("_", " ")}")
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
                                text = currentDefect.dueDate ?: "Set Due Date"
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
                                onDefectUpdate(currentDefect)
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
                        Text(
                            text = "Sub-tasks",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Add new sub-task
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSubTaskText,
                                onValueChange = { newSubTaskText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Add a sub-task...") }
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
                                        onDefectUpdate(currentDefect)
                                        newSubTaskText = ""
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
                                        onDefectUpdate(currentDefect)
                                    }
                                )
                                
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
                                
                                IconButton(
                                    onClick = {
                                        val updatedSubTasks = currentDefect.subTasks.filter { it.id != subTask.id }
                                        currentDefect = currentDefect.copy(subTasks = updatedSubTasks)
                                        onDefectUpdate(currentDefect)
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete sub-task")
                                }
                            }
                        }
                    }
                }
            }
            
            // Images (placeholder)
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
                            text = "Images",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (currentDefect.images.isEmpty()) {
                            Text(
                                text = "No images attached",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                text = "${currentDefect.images.size} image(s) attached",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
    }
    
    // Status Dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Change Status") },
            text = {
                Column {
                    DefectStatus.values().forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentDefect = currentDefect.copy(status = status)
                                    onDefectUpdate(currentDefect)
                                    showStatusDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentDefect.status == status,
                                onClick = {
                                    currentDefect = currentDefect.copy(status = status)
                                    onDefectUpdate(currentDefect)
                                    showStatusDialog = false
                                }
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
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
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
        AlertDialog(
            onDismissRequest = { showDueDateDialog = false },
            title = { Text("Set Due Date") },
            text = {
                Column {
                    Button(
                        onClick = {
                            currentDefect = currentDefect.copy(dueDate = getTodayString())
                            onDefectUpdate(currentDefect)
                            showDueDateDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Today")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            currentDefect = currentDefect.copy(dueDate = getTomorrowString())
                            onDefectUpdate(currentDefect)
                            showDueDateDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tomorrow")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            currentDefect = currentDefect.copy(dueDate = getNextWeekString())
                            onDefectUpdate(currentDefect)
                            showDueDateDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next Week")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            currentDefect = currentDefect.copy(dueDate = null)
                            onDefectUpdate(currentDefect)
                            showDueDateDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove Due Date")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDueDateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


