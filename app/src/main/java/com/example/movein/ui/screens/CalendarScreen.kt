package com.example.movein.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movein.shared.data.ChecklistItem
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.DefectCategory
import com.example.movein.shared.data.DefectStatus
import com.example.movein.utils.getTodayString
import com.example.movein.utils.formatDateForDisplay
import com.example.movein.utils.formatTaskStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<ChecklistItem>,
    defects: List<Defect>,
    onBackClick: () -> Unit,
    onTaskClick: (ChecklistItem) -> Unit,
    onDefectClick: (Defect) -> Unit,
    onAddTask: (ChecklistItem) -> Unit = {},
    onAddDefect: (Defect) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showMultipleEventsDialog by remember { mutableStateOf(false) }
    var multipleEventsDate by remember { mutableStateOf<LocalDate?>(null) }
    
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
    
    val openMultipleEventsDialog = { date: LocalDate ->
        multipleEventsDate = date
        showMultipleEventsDialog = true
    }
    
    val dismissMultipleEventsDialog = {
        showMultipleEventsDialog = false
        multipleEventsDate = null
    }
    
    val selectedDateTasks = remember(selectedDate, tasks) {
        tasks.filter { task ->
            task.dueDate?.let { dueDate ->
                parseDate(dueDate) == selectedDate
            } ?: false
        }
    }
    
    val selectedDateDefects = remember(selectedDate, defects) {
        defects.filter { defect ->
            defect.dueDate?.let { dueDate ->
                parseDate(dueDate) == selectedDate
            } ?: false
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button and title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Calendar View",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Month navigation
        MonthNavigation(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Calendar grid
        CalendarGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            tasks = tasks,
            defects = defects,
            onDateClick = { date -> selectedDate = date },
            onMultipleEventsClick = openMultipleEventsDialog
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selected date details
        SelectedDateDetails(
            selectedDate = selectedDate,
            tasks = selectedDateTasks,
            defects = selectedDateDefects,
            onTaskClick = onTaskClick,
            onDefectClick = onDefectClick,
            onAddTask = openAddTaskDialog,
            onAddDefect = onAddDefect
        )
        
        // Add Task Dialog
        if (showAddTaskDialog) {
            val existingTaskNames = tasks.map { it.title }
            
            AddTaskDialog(
                onDismiss = dismissAddTaskDialog,
                onAddTask = addNewTask,
                existingTaskNames = existingTaskNames
            )
        }
        
        // Multiple Events Dialog
        if (showMultipleEventsDialog && multipleEventsDate != null) {
            val eventsDate = multipleEventsDate!!
            val eventsDateTasks = tasks.filter { task ->
                task.dueDate?.let { dueDate ->
                    parseDate(dueDate) == eventsDate
                } ?: false
            }
            val eventsDateDefects = defects.filter { defect ->
                defect.dueDate?.let { dueDate ->
                    parseDate(dueDate) == eventsDate
                } ?: false
            }
            
            MultipleEventsDialog(
                date = eventsDate,
                tasks = eventsDateTasks,
                defects = eventsDateDefects,
                onDismiss = dismissMultipleEventsDialog,
                onTaskClick = { task ->
                    onTaskClick(task)
                    dismissMultipleEventsDialog()
                },
                onDefectClick = { defect ->
                    onDefectClick(defect)
                    dismissMultipleEventsDialog()
                }
            )
        }
    }
}

@Composable
private fun MonthNavigation(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    tasks: List<ChecklistItem>,
    defects: List<Defect>,
    onDateClick: (LocalDate) -> Unit,
    onMultipleEventsClick: (LocalDate) -> Unit = {}
) {
    val firstDayOfMonth = currentMonth.withDayOfMonth(1)
    val lastDayOfMonth = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Convert to 0-6 (Sunday = 0)
    
    Column {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar days
        val weeks = mutableListOf<List<LocalDate>>()
        var currentWeek = mutableListOf<LocalDate>()
        
        // Add empty cells for days before the first day of the month
        repeat(firstDayOfWeek) {
            currentWeek.add(LocalDate.MIN) // Placeholder for empty cells
        }
        
        // Add all days of the month
        for (day in 1..currentMonth.lengthOfMonth()) {
            currentWeek.add(currentMonth.withDayOfMonth(day))
            
            if (currentWeek.size == 7) {
                weeks.add(currentWeek)
                currentWeek = mutableListOf()
            }
        }
        
        // Add remaining empty cells for the last week
        while (currentWeek.size < 7 && currentWeek.isNotEmpty()) {
            currentWeek.add(LocalDate.MIN)
        }
        if (currentWeek.isNotEmpty()) {
            weeks.add(currentWeek)
        }
        
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    val taskCount = tasks.count { task ->
                        task.dueDate?.let { dueDate ->
                            parseDate(dueDate) == date
                        } ?: false
                    }
                    val defectCount = defects.count { defect ->
                        defect.dueDate?.let { dueDate ->
                            parseDate(dueDate) == date
                        } ?: false
                    }
                    val totalEvents = taskCount + defectCount
                    
                    CalendarDay(
                        date = date,
                        isSelected = date == selectedDate,
                        isCurrentMonth = date != LocalDate.MIN,
                        hasTasks = hasTasksOnDate(date, tasks),
                        hasDefects = hasDefectsOnDate(date, defects),
                        taskCount = taskCount,
                        defectCount = defectCount,
                        onClick = { if (date != LocalDate.MIN) onDateClick(date) },
                        onMultipleEventsClick = { 
                            if (date != LocalDate.MIN && totalEvents > 1) {
                                onMultipleEventsClick(date)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    hasTasks: Boolean,
    hasDefects: Boolean,
    taskCount: Int = 0,
    defectCount: Int = 0,
    onClick: () -> Unit,
    onMultipleEventsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        !isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isCurrentMonth) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isCurrentMonth) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                
                // Event indicators
                val totalEvents = taskCount + defectCount
                if (totalEvents > 0) {
                    if (totalEvents == 1) {
                        // Single event - show colored dot
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (hasTasks) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                )
                            }
                            if (hasDefects) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.error,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    } else {
                        // Multiple events - show count badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { onMultipleEventsClick() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = totalEvents.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDateDetails(
    selectedDate: LocalDate,
    tasks: List<ChecklistItem>,
    defects: List<Defect>,
    onTaskClick: (ChecklistItem) -> Unit,
    onDefectClick: (Defect) -> Unit,
    onAddTask: () -> Unit = {},
    onAddDefect: (Defect) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                // Add new event buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddTask,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Add Task",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    OutlinedButton(
                        onClick = {
                            val newDefect = Defect(
                                id = UUID.randomUUID().toString(),
                                location = "",
                                category = com.example.movein.shared.data.DefectCategory.OTHER,
                                priority = Priority.MEDIUM,
                                description = "",
                                images = emptyList(),
                                status = com.example.movein.shared.data.DefectStatus.OPEN,
                                createdAt = com.example.movein.utils.getTodayString(),
                                dueDate = formatDate(selectedDate),
                                notes = "",
                                assignedTo = null
                            )
                            onAddDefect(newDefect)
                        },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Add Defect",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (tasks.isEmpty() && defects.isEmpty()) {
                Text(
                    text = "No tasks or defects due on this date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Tasks section
                if (tasks.isNotEmpty()) {
                    Text(
                        text = "Tasks (${tasks.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    tasks.forEach { task ->
                        TaskItem(
                            task = task,
                            onClick = { onTaskClick(task) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    if (defects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                // Defects section
                if (defects.isNotEmpty()) {
                    Text(
                        text = "Defects (${defects.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    defects.forEach { defect ->
                        DefectItem(
                            defect = defect,
                            onClick = { onDefectClick(defect) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: ChecklistItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Task",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (task.category.isNotEmpty()) {
                        Text(
                            text = "Category: ${task.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Status: ${formatTaskStatus(task.status)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (task.dueDate != null) {
                        Text(
                            text = "Due: ${formatDateForDisplay(task.dueDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            PriorityIndicator(priority = task.priority)
        }
    }
}

@Composable
private fun DefectItem(
    defect: Defect,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Defect",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = defect.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${defect.location} - ${defect.category.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Status: ${defect.status.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (defect.dueDate != null) {
                        Text(
                            text = "Due: ${formatDateForDisplay(defect.dueDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Show closed date if defect is closed
                if (defect.status == DefectStatus.CLOSED && defect.closedAt != null) {
                    Text(
                        text = "Closed: ${formatDateForDisplay(defect.closedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            PriorityIndicator(priority = defect.priority)
        }
    }
}

@Composable
private fun PriorityIndicator(priority: Priority) {
    val color = when (priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.error
        Priority.MEDIUM -> MaterialTheme.colorScheme.tertiary
        Priority.LOW -> MaterialTheme.colorScheme.primary
    }
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color = color, shape = CircleShape)
    )
}

// Helper functions
private fun hasTasksOnDate(date: LocalDate, tasks: List<ChecklistItem>): Boolean {
    return tasks.any { task ->
        task.dueDate?.let { dueDate ->
            parseDate(dueDate) == date
        } ?: false
    }
}

private fun hasDefectsOnDate(date: LocalDate, defects: List<Defect>): Boolean {
    return defects.any { defect ->
        defect.dueDate?.let { dueDate ->
            parseDate(dueDate) == date
        } ?: false
    }
}

private fun parseDate(dateString: String): LocalDate? {
    return try {
        val parts = dateString.split("/")
        if (parts.size == 3) {
            val month = parts[0].toInt()
            val day = parts[1].toInt()
            val year = parts[2].toInt()
            LocalDate.of(year, month, day)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.monthValue}/${date.dayOfMonth}/${date.year}"
}

@Composable
private fun MultipleEventsDialog(
    date: LocalDate,
    tasks: List<ChecklistItem>,
    defects: List<Defect>,
    onDismiss: () -> Unit,
    onTaskClick: (ChecklistItem) -> Unit,
    onDefectClick: (Defect) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Events on ${date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                val totalEvents = tasks.size + defects.size
                Text(
                    text = "Total: $totalEvents events",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tasks section
                    if (tasks.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tasks (${tasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(tasks) { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTaskClick(task) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Task",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = task.category,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Priority indicator
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (task.priority) {
                                                Priority.LOW -> MaterialTheme.colorScheme.primaryContainer
                                                Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                                                Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                            }
                                        ),
                                        modifier = Modifier.padding(0.dp)
                                    ) {
                                        Text(
                                            text = when (task.priority) {
                                                Priority.LOW -> "Low"
                                                Priority.MEDIUM -> "Med"
                                                Priority.HIGH -> "High"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when (task.priority) {
                                                Priority.LOW -> MaterialTheme.colorScheme.onPrimaryContainer
                                                Priority.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                                                Priority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (defects.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    // Defects section
                    if (defects.isNotEmpty()) {
                        item {
                            Text(
                                text = "Defects (${defects.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(defects) { defect ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDefectClick(defect) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Defect",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = defect.location,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = defect.category.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Priority indicator
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (defect.priority) {
                                                Priority.LOW -> MaterialTheme.colorScheme.primaryContainer
                                                Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                                                Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                            }
                                        ),
                                        modifier = Modifier.padding(0.dp)
                                    ) {
                                        Text(
                                            text = when (defect.priority) {
                                                Priority.LOW -> "Low"
                                                Priority.MEDIUM -> "Med"
                                                Priority.HIGH -> "High"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when (defect.priority) {
                                                Priority.LOW -> MaterialTheme.colorScheme.onPrimaryContainer
                                                Priority.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                                                Priority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
