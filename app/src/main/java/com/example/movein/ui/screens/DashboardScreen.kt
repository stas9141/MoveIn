package com.example.movein.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movein.shared.data.ChecklistData
import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.Defect
import com.example.movein.ui.components.PriorityDropdown
import com.example.movein.ui.components.TaskStatusDropdown
import com.example.movein.utils.formatPriority
import com.example.movein.utils.formatTaskStatus

@Composable
fun DashboardScreen(
    checklistData: ChecklistData,
    selectedTabIndex: Int = 0,
    onTaskClick: (ChecklistItem) -> Unit,
    onTaskToggle: (ChecklistItem) -> Unit,
    onAddTask: (ChecklistItem) -> Unit,
    onSettingsClick: () -> Unit,
    onDefectListClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    defects: List<Defect> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(selectedTabIndex) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedPriority by remember { mutableStateOf<Priority?>(null) }
    
    // Callback functions for state updates
    val clearFilters = {
        selectedCategory = null
        selectedPriority = null
    }
    
    val applyFilters = { category: String?, priority: Priority? ->
        selectedCategory = category
        selectedPriority = priority
        showFilterDialog = false
    }
    
    val dismissFilterDialog = {
        showFilterDialog = false
    }
    
    val dismissAddTaskDialog = {
        showAddTaskDialog = false
    }
    
    val addNewTask = { newTask: ChecklistItem ->
        onAddTask(newTask)
        showAddTaskDialog = false
    }
    
    val openAddTaskDialog = {
        showAddTaskDialog = true
    }
    

    
    val tabs = listOf("First Week", "First Month", "First Year")
    val tabLabels = listOf("Week", "Month", "Year")
    
    val currentTasks = when (selectedTab) {
        0 -> checklistData.firstWeek
        1 -> checklistData.firstMonth
        2 -> checklistData.firstYear
        else -> checklistData.firstWeek
    }
    
    // Apply filters
    val filteredTasks = currentTasks.filter { task ->
        (selectedCategory == null || task.category == selectedCategory) &&
        (selectedPriority == null || task.priority == selectedPriority)
    }
    
    // Separate active and completed tasks
    val activeTasks = filteredTasks.filter { !it.isCompleted }
    val completedTasks = filteredTasks.filter { it.isCompleted }
    val totalTasks = filteredTasks.size
    
    // Calculate additional summary statistics
    val today = java.time.LocalDate.now()
    val overdueTasks = filteredTasks.count { task ->
        val dueDate = task.dueDate
        dueDate != null && !task.isCompleted && 
        parseDate(dueDate)?.isBefore(today) == true
    }
    val dueThisWeek = filteredTasks.count { task ->
        val dueDate = task.dueDate
        dueDate != null && !task.isCompleted && 
        parseDate(dueDate)?.isBefore(today.plusDays(7)) == true
    }
    val highPriorityTasks = filteredTasks.count { task ->
        !task.isCompleted && task.priority == Priority.HIGH
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome to your new home!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Main summary
                    Text(
                        text = "You have ${completedTasks.size} of $totalTasks tasks completed in ${tabs[selectedTab].lowercase()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Additional summary details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Overdue tasks
                        if (overdueTasks > 0) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$overdueTasks",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Overdue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                        
                        // Due this week
                        if (dueThisWeek > 0) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$dueThisWeek",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Due This Week",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        
                        // High priority tasks
                        if (highPriorityTasks > 0) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$highPriorityTasks",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "High Priority",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tab Row and Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom Tab Row
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start
                ) {
                    tabLabels.forEachIndexed { index, title ->
                        Surface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { 
                                    selectedTab = index
                                    onTabSelected(index)
                                },
                            color = if (selectedTab == index) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = title,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (selectedTab == index) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
                
                // Filter Button with indicator
                Box {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Filter")
                    }
                    
                    // Show indicator if filters are active
                    if (selectedCategory != null || selectedPriority != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                        ) {}
                    }
                }
                
                // Calendar Button
                IconButton(
                    onClick = onCalendarClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                }
                
                // Settings Button
                IconButton(
                    onClick = onSettingsClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                }
            }

            // Filter Summary (only show if filters are active)
            if (selectedCategory != null || selectedPriority != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Active Filters",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Active Filters:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            selectedCategory?.let { category ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(category) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.List,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        labelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            
                            selectedPriority?.let { priority ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(formatPriority(priority)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = when (priority) {
                                            Priority.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            Priority.MEDIUM -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            Priority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                        },
                                        labelColor = when (priority) {
                                            Priority.HIGH -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                )
                            }
                            
                            // Clear Filters Button
                            TextButton(
                                onClick = clearFilters,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Clear", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            
            // Defect Management Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onDefectListClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Defect Management",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Defect Management",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Text(
                            text = "Report and track apartment issues",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${defects.size} defect(s) reported",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Go to Defect Management",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Task Lists
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Tasks Section
                if (activeTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active Tasks (${activeTasks.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(activeTasks) { task ->
                        ChecklistItemCard(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onToggle = { onTaskToggle(task) },
                            onPriorityChange = { newPriority ->
                                val updatedTask = task.copy(priority = newPriority)
                                onTaskToggle(updatedTask)
                            },
                            onStatusChange = { newStatus ->
                                val updatedTask = task.copy(
                                    status = newStatus,
                                    isCompleted = newStatus == com.example.movein.shared.data.TaskStatus.CLOSED
                                )
                                onTaskToggle(updatedTask)
                            }
                        )
                    }
                }
                
                // Completed Tasks Section
                if (completedTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Completed Tasks (${completedTasks.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                completedTasks.forEach { task ->
                                    CompletedTaskItem(
                                        task = task,
                                        onClick = { onTaskClick(task) }
                                    )
                                    if (task != completedTasks.last()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Empty State
                if (activeTasks.isEmpty() && completedTasks.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "No Tasks",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No tasks found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Try adjusting your filters or add a new task",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Add Task Button - Floating over the content
        FloatingActionButton(
            onClick = openAddTaskDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
        
        // Filter Dialog
        if (showFilterDialog) {
            FilterDialog(
                onDismiss = dismissFilterDialog,
                onApplyFilters = applyFilters,
                currentCategory = selectedCategory,
                currentPriority = selectedPriority
            )
        }
        
        // Add Task Dialog
        if (showAddTaskDialog) {
            val existingTaskNames = checklistData.firstWeek.map { it.title } +
                    checklistData.firstMonth.map { it.title } +
                    checklistData.firstYear.map { it.title }
            
            AddTaskDialog(
                onDismiss = dismissAddTaskDialog,
                onAddTask = addNewTask,
                existingTaskNames = existingTaskNames
            )
        }
    }
}

@Composable
fun ChecklistItemCard(
    task: ChecklistItem,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onStatusChange: (com.example.movein.shared.data.TaskStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Checkbox(
                    checked = task.isCompleted || task.status == com.example.movein.shared.data.TaskStatus.CLOSED,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            // If checking, set status to CLOSED and isCompleted to true
                            onStatusChange(com.example.movein.shared.data.TaskStatus.CLOSED)
                        } else {
                            // If unchecking, set status to OPEN and isCompleted to false
                            onStatusChange(com.example.movein.shared.data.TaskStatus.OPEN)
                        }
                        onToggle()
                    },
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                // Task Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Task Status dropdown
                        TaskStatusDropdown(
                            currentStatus = task.status,
                            onStatusChange = onStatusChange
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Priority dropdown
                        PriorityDropdown(
                            currentPriority = task.priority,
                            onPriorityChange = onPriorityChange
                        )
                        
                        // Due date if available
                        task.dueDate?.let { dueDate ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Due: $dueDate",
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



@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (String?, Priority?) -> Unit,
    currentCategory: String?,
    currentPriority: Priority?
) {
    var selectedCategory by remember { mutableStateOf(currentCategory) }
    var selectedPriority by remember { mutableStateOf(currentPriority) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Tasks") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (selectedCategory != null) {
                        TextButton(
                            onClick = { selectedCategory = null },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Clear", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                val categories = listOf("Security", "Safety", "Utilities", "Cleaning", "Maintenance", "Comfort", "Administrative", "Services", "Community", "Technology", "Organization", "Parking", "Custom")
                
                categories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = if (selectedCategory == category) null else category }
                        )
                        Text(
                            text = category,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (selectedPriority != null) {
                        TextButton(
                            onClick = { selectedPriority = null },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Clear", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Priority.values().forEach { priority ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = if (selectedPriority == priority) null else priority },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = when (priority) {
                                    Priority.LOW -> MaterialTheme.colorScheme.primary
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
                                    Priority.HIGH -> MaterialTheme.colorScheme.error
                                },
                                unselectedColor = when (priority) {
                                    Priority.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    Priority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                }
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Priority indicator
                        Surface(
                            color = when (priority) {
                                Priority.LOW -> MaterialTheme.colorScheme.primary
                                Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
                                Priority.HIGH -> MaterialTheme.colorScheme.error
                            },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(20.dp)
                        ) {}
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = formatPriority(priority),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedPriority == priority) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selectedPriority == priority) {
                                when (priority) {
                                    Priority.LOW -> MaterialTheme.colorScheme.primary
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
                                    Priority.HIGH -> MaterialTheme.colorScheme.error
                                }
                            } else {
                                when (priority) {
                                    Priority.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                    Priority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApplyFilters(selectedCategory, selectedPriority) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { 
                        selectedCategory = null
                        selectedPriority = null
                        onApplyFilters(null, null)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All")
                }
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun CompletedTaskItem(
    task: ChecklistItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Completed",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = task.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                if (task.dueDate != null) {
                    Text(
                        text = "• Due: ${task.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// Helper function to parse date from MM/dd/yyyy format
private fun parseDate(dateString: String): java.time.LocalDate? {
    return try {
        val parts = dateString.split("/")
        if (parts.size == 3) {
            val month = parts[0].toInt()
            val day = parts[1].toInt()
            val year = parts[2].toInt()
            java.time.LocalDate.of(year, month, day)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}