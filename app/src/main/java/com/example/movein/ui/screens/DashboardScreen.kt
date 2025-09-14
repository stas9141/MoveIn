package com.example.movein.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    checklistData: ChecklistData,
    selectedTabIndex: Int = 0,
    onTaskClick: (ChecklistItem) -> Unit,
    onTaskToggle: (ChecklistItem) -> Unit,
    onAddTask: (ChecklistItem) -> Unit,
    onDefectListClick: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    onTutorialClick: (() -> Unit)? = null,
    defects: List<Defect> = emptyList(),
    showAddTaskDialog: Boolean = false,
    onDismissAddTaskDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(selectedTabIndex) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedStatuses by remember { mutableStateOf<List<com.example.movein.shared.data.TaskStatus>>(emptyList()) }
    var selectedPriorities by remember { mutableStateOf<List<Priority>>(emptyList()) }
    
    // Callback functions for state updates
    val clearFilters = {
        selectedCategories = emptyList()
        selectedStatuses = emptyList()
        selectedPriorities = emptyList()
    }
    
    val applyFilters = { categories: List<String>, statuses: List<com.example.movein.shared.data.TaskStatus>, priorities: List<Priority> ->
        selectedCategories = categories
        selectedStatuses = statuses
        selectedPriorities = priorities
        showFilterDialog = false
    }
    
    val dismissFilterDialog = {
        showFilterDialog = false
    }
    
    val dismissAddTaskDialog = {
        onDismissAddTaskDialog()
    }
    
    val addNewTask = { newTask: ChecklistItem ->
        onAddTask(newTask)
        onDismissAddTaskDialog()
    }
    
    val openAddTaskDialog = {
        // This will be handled by the FAB in MainActivity
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
        (selectedCategories.isEmpty() || selectedCategories.contains(task.category)) &&
        (selectedStatuses.isEmpty() || selectedStatuses.contains(task.status)) &&
        (selectedPriorities.isEmpty() || selectedPriorities.contains(task.priority))
    }
    
    // Get today's date for sorting
    val today = java.time.LocalDate.now()
    
    // Sort tasks by status first, then priority, then due date
    val sortedTasks = filteredTasks.sortedWith(compareBy<ChecklistItem> { task ->
        // First priority: Status (Open → In Progress → Closed)
        when (task.status) {
            com.example.movein.shared.data.TaskStatus.OPEN -> 0
            com.example.movein.shared.data.TaskStatus.IN_PROGRESS -> 1
            com.example.movein.shared.data.TaskStatus.CLOSED -> 2
        }
    }.thenBy { task ->
        // Second priority: Priority within each status (High → Medium → Low)
        when (task.priority) {
            Priority.HIGH -> 0
            Priority.MEDIUM -> 1
            Priority.LOW -> 2
        }
    }.thenBy { task ->
        // Third priority: Due date (overdue tasks first)
        val dueDate = task.dueDate
        if (dueDate != null) {
            val parsedDate = parseDate(dueDate)
            if (parsedDate != null && parsedDate.isBefore(today) && !task.isCompleted) {
                -1 // Overdue tasks
            } else {
                0 // Normal tasks
            }
        } else {
            1 // Tasks without due date
        }
    })
    
    val activeTasks = sortedTasks.filter { !it.isCompleted }
    val completedTasks = sortedTasks.filter { it.isCompleted }
    val totalTasks = filteredTasks.size
    
    // Calculate additional summary statistics
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
                    // Header with title and tutorial button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Welcome to your new home!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Tutorial button
                        onTutorialClick?.let { onTutorial ->
                            IconButton(
                                onClick = onTutorial,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Start Tutorial",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
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
                
                // Filter Button with indicator (keep only filter since it's not in bottom nav)
                Box {
                    val tooltipState = rememberTooltipState()
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text("Filter tasks by category, status, and priority")
                            }
                        },
                        state = tooltipState
                    ) {
                        IconButton(
                            onClick = { showFilterDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Filter")
                        }
                    }
                    
                    // Show indicator if filters are active
                    if (selectedCategories.isNotEmpty() || selectedStatuses.isNotEmpty() || selectedPriorities.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                        ) {}
                    }
                }
            }

            // Filter Summary (only show if filters are active)
            if (selectedCategories.isNotEmpty() || selectedStatuses.isNotEmpty() || selectedPriorities.isNotEmpty()) {
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
                                imageVector = Icons.Default.KeyboardArrowDown,
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
                            // Show count of selected filters
                            Text(
                                text = "Filters: ${selectedCategories.size + selectedStatuses.size + selectedPriorities.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            
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
        
        // FAB is now handled by the main Scaffold in MainActivity
        
        // Filter Dialog
        if (showFilterDialog) {
            FilterDialog(
                onDismiss = dismissFilterDialog,
                onApplyFilters = applyFilters,
                currentCategories = selectedCategories,
                currentStatuses = selectedStatuses,
                currentPriorities = selectedPriorities
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
            // Top row with title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Task title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // Status in top-right corner
                TaskStatusDropdown(
                    currentStatus = task.status,
                    onStatusChange = onStatusChange
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Task details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        text = task.category,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Priority
                PriorityDropdown(
                    currentPriority = task.priority,
                    onPriorityChange = onPriorityChange
                )
                
                // Due date if available
                task.dueDate?.let { dueDate ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Due date",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = dueDate,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
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
    onApplyFilters: (List<String>, List<com.example.movein.shared.data.TaskStatus>, List<Priority>) -> Unit,
    currentCategories: List<String>,
    currentStatuses: List<com.example.movein.shared.data.TaskStatus>,
    currentPriorities: List<Priority>
) {
    var selectedCategories by remember { mutableStateOf(currentCategories) }
    var selectedStatuses by remember { mutableStateOf(currentStatuses) }
    var selectedPriorities by remember { mutableStateOf(currentPriorities) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Tasks") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Category filter
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val categories = listOf("Security", "Safety", "Utilities", "Cleaning", "Maintenance", "Comfort", "Administrative", "Services", "Community", "Technology", "Organization", "Parking", "Custom")
                
                categories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { 
                                selectedCategories = if (selectedCategories.contains(category)) {
                                    selectedCategories.filter { it != category }
                                } else {
                                    selectedCategories + category
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedCategories.contains(category),
                            onCheckedChange = { isChecked ->
                                selectedCategories = if (isChecked) {
                                    selectedCategories + category
                                } else {
                                    selectedCategories.filter { it != category }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedCategories.contains(category)) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (selectedCategories.contains(category)) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Status filter
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                com.example.movein.shared.data.TaskStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { 
                                selectedStatuses = if (selectedStatuses.contains(status)) {
                                    selectedStatuses.filter { it != status }
                                } else {
                                    selectedStatuses + status
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedStatuses.contains(status),
                            onCheckedChange = { isChecked ->
                                selectedStatuses = if (isChecked) {
                                    selectedStatuses + status
                                } else {
                                    selectedStatuses.filter { it != status }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = when (status) {
                                    com.example.movein.shared.data.TaskStatus.OPEN -> MaterialTheme.colorScheme.error
                                    com.example.movein.shared.data.TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                                    com.example.movein.shared.data.TaskStatus.CLOSED -> MaterialTheme.colorScheme.tertiary
                                },
                                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Status indicator
                        Surface(
                            color = when (status) {
                                com.example.movein.shared.data.TaskStatus.OPEN -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                com.example.movein.shared.data.TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                com.example.movein.shared.data.TaskStatus.CLOSED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                when (status) {
                                    com.example.movein.shared.data.TaskStatus.OPEN -> {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    com.example.movein.shared.data.TaskStatus.IN_PROGRESS -> {
                                        // Half-filled circle for In Progress - simplified version
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                            shape = CircleShape,
                                            modifier = Modifier.size(18.dp)
                                        ) {}
                                    }
                                    com.example.movein.shared.data.TaskStatus.CLOSED -> {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = status.name.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedStatuses.contains(status)) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (selectedStatuses.contains(status)) {
                                when (status) {
                                    com.example.movein.shared.data.TaskStatus.OPEN -> MaterialTheme.colorScheme.error
                                    com.example.movein.shared.data.TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                                    com.example.movein.shared.data.TaskStatus.CLOSED -> MaterialTheme.colorScheme.tertiary
                                }
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority filter
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Priority.values().forEach { priority ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { 
                                selectedPriorities = if (selectedPriorities.contains(priority)) {
                                    selectedPriorities.filter { it != priority }
                                } else {
                                    selectedPriorities + priority
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedPriorities.contains(priority),
                            onCheckedChange = { isChecked ->
                                selectedPriorities = if (isChecked) {
                                    selectedPriorities + priority
                                } else {
                                    selectedPriorities.filter { it != priority }
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = when (priority) {
                                    Priority.LOW -> MaterialTheme.colorScheme.primary
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
                                    Priority.HIGH -> MaterialTheme.colorScheme.error
                                },
                                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Priority indicator
                        Surface(
                            color = when (priority) {
                                Priority.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                Priority.MEDIUM -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                Priority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (priority) {
                                        Priority.LOW -> "L"
                                        Priority.MEDIUM -> "M"
                                        Priority.HIGH -> "H"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = when (priority) {
                                        Priority.LOW -> MaterialTheme.colorScheme.primary
                                        Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
                                        Priority.HIGH -> MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = formatPriority(priority),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedPriorities.contains(priority)) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (selectedPriorities.contains(priority)) {
                                    when (priority) {
                                        Priority.LOW -> MaterialTheme.colorScheme.primary
                                        Priority.MEDIUM -> MaterialTheme.colorScheme.secondary
                                        Priority.HIGH -> MaterialTheme.colorScheme.error
                                    }
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = when (priority) {
                                    Priority.LOW -> "Low urgency"
                                    Priority.MEDIUM -> "Medium urgency"
                                    Priority.HIGH -> "High urgency"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApplyFilters(selectedCategories, selectedStatuses, selectedPriorities) },
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
                        selectedCategories = emptyList()
                        selectedStatuses = emptyList()
                        selectedPriorities = emptyList()
                        onApplyFilters(emptyList(), emptyList(), emptyList())
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