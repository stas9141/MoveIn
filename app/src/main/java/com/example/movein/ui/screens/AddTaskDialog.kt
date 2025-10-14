package com.example.movein.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.Priority
import java.util.UUID
import java.util.Calendar
import com.example.movein.utils.getTodayString
import com.example.movein.utils.getTomorrowString
import com.example.movein.utils.getNextWeekString
import com.example.movein.utils.formatPriority
import com.example.movein.utils.formatDateForDisplay
import com.example.movein.ui.components.EnhancedDatePicker
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.example.movein.utils.CategoryUtils

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (ChecklistItem) -> Unit,
    existingTaskNames: List<String> = emptyList(),
    userData: com.example.movein.shared.data.UserData? = null
) {
    var taskName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CategoryUtils.getDefaultCategory(userData)) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var selectedDueDate by remember { mutableStateOf<String?>(null) }
    var showDueDateDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    
    // Focus management
    val focusRequester = remember { FocusRequester() }
    
    // Request focus when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Task",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                        supportingText = {
                            if (nameError != null) {
                                Text(nameError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        readOnly = false,
                        enabled = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { /* Focus next field if needed */ }
                        )
                    )
                }
                
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
                        readOnly = false,
                        enabled = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { /* Dismiss keyboard */ }
                        )
                    )
                }
                
                // Category Selection
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.clickable { showCategoryDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCategory,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Category",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Priority Selection
                Column {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Surface(
                        color = when (selectedPriority) {
                            Priority.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            Priority.MEDIUM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            Priority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.clickable { showPriorityDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = formatPriority(selectedPriority),
                                style = MaterialTheme.typography.bodyMedium,
                                color = when (selectedPriority) {
                                    Priority.LOW -> MaterialTheme.colorScheme.primary
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                                    Priority.HIGH -> MaterialTheme.colorScheme.error
                                }
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Select Priority",
                                tint = when (selectedPriority) {
                                    Priority.LOW -> MaterialTheme.colorScheme.primary
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.primary
                                    Priority.HIGH -> MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
                
                // Due Date Selection
                Column {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Surface(
                        color = if (selectedDueDate != null) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.clickable { showDueDateDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = formatDateForDisplay(selectedDueDate),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedDueDate != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Select Due Date",
                                tint = if (selectedDueDate != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    // Validate task name
                    if (taskName.trim().isEmpty()) {
                        nameError = "Task name is required"
                        return@OutlinedButton
                    }
                    
                    if (existingTaskNames.any { it.equals(taskName.trim(), ignoreCase = true) }) {
                        nameError = "Task name must be unique"
                        return@OutlinedButton
                    }
                    
                    // Create new task
                    val newTask = ChecklistItem(
                        id = UUID.randomUUID().toString(),
                        title = taskName.trim(),
                        description = description.trim(),
                        category = selectedCategory,
                        priority = selectedPriority,
                        dueDate = selectedDueDate,
                        isUserAdded = true
                    )
                    
                    onAddTask(newTask)
                    onDismiss()
                },
                enabled = taskName.trim().isNotEmpty(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Add",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Add Task",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Cancel")
            }
        }
    )
    
    // Priority Selection Dialog
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
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Surface(
                                color = when (priority) {
                                    Priority.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    Priority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                },
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.size(16.dp)
                            ) {}
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                                                            Text(formatPriority(priority))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showPriorityDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Due Date Selection Dialog
    if (showDueDateDialog) {
        EnhancedDatePicker(
            selectedDate = selectedDueDate,
            onDateSelected = { date ->
                selectedDueDate = date
            },
            onDismiss = { showDueDateDialog = false },
            title = "Set Due Date"
        )
    }
    
    // Category Selection Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    val categories = CategoryUtils.getAvailableTaskCategories(userData)
                    
                    categories.forEach { category ->
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
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(category)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showCategoryDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

