package com.example.movein.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.DefectCategory
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.SubTask
import com.example.movein.ui.components.PriorityDropdown
import com.example.movein.utils.formatCategory
import com.example.movein.utils.formatPriority
import java.util.UUID

@Composable
fun DefectListScreen(
    defects: List<Defect>,
    onDefectClick: (Defect) -> Unit,
    onAddDefect: () -> Unit,
    onBackClick: () -> Unit,
    onDefectUpdate: (Defect) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<DefectCategory?>(null) }
    var selectedStatus by remember { mutableStateOf<DefectStatus?>(null) }
    var selectedPriority by remember { mutableStateOf<Priority?>(null) }
    
    // Filter defects based on search and filters
    val filteredDefects = defects.filter { defect ->
        val matchesSearch = searchQuery.isEmpty() || 
            defect.location.contains(searchQuery, ignoreCase = true) ||
            defect.description.contains(searchQuery, ignoreCase = true)
        
        val matchesCategory = selectedCategory == null || defect.category == selectedCategory
        val matchesStatus = selectedStatus == null || defect.status == selectedStatus
        val matchesPriority = selectedPriority == null || defect.priority == selectedPriority
        
        matchesSearch && matchesCategory && matchesStatus && matchesPriority
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with back button, search and filter
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to Dashboard")
                        }
                        
                        Text(
                            text = "Defect Management",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search defects...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Filter button with active indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Found ${filteredDefects.size} defects",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Box {
                            IconButton(
                                onClick = { showFilterDialog = true }
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Filter")
                            }
                            
                            // Show indicator if filters are active
                            if (selectedCategory != null || selectedStatus != null || selectedPriority != null) {
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
                }
            }
            
            // Filter Summary (only show if filters are active)
            if (selectedCategory != null || selectedStatus != null || selectedPriority != null) {
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
                                    label = { Text(formatCategory(category)) }
                                )
                            }
                            
                            selectedStatus?.let { status ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(status.name.replace("_", " ")) }
                                )
                            }
                            
                            selectedPriority?.let { priority ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(formatPriority(priority)) }
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                selectedCategory = null
                                selectedStatus = null
                                selectedPriority = null
                            }
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
            
            // Defects list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredDefects.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "No Defects",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No defects found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Try adjusting your search or filters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredDefects) { defect ->
                        DefectCard(
                            defect = defect,
                            onClick = { onDefectClick(defect) },
                            onDefectUpdate = onDefectUpdate
                        )
                    }
                }
            }
        }
        
        // Add defect button
        FloatingActionButton(
            onClick = onAddDefect,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Defect")
        }
        
        // Filter dialog
        if (showFilterDialog) {
            DefectFilterDialog(
                onDismiss = { showFilterDialog = false },
                onApplyFilters = { category, status, priority ->
                    selectedCategory = category
                    selectedStatus = status
                    selectedPriority = priority
                    showFilterDialog = false
                },
                currentCategory = selectedCategory,
                currentStatus = selectedStatus,
                currentPriority = selectedPriority
            )
        }
    }
}

@Composable
fun DefectCard(
    defect: Defect,
    onClick: () -> Unit,
    onDefectUpdate: (Defect) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (defect.status == DefectStatus.CLOSED) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Main content
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checklist icon for tasks with sub-tasks
                        if (defect.subTasks.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Has sub-tasks",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Text(
                            text = defect.location,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (defect.status == DefectStatus.CLOSED) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Chevron for expandable tasks
                        if (defect.subTasks.isNotEmpty()) {
                            IconButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier.size(32.dp)
                            ) {
                                                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.graphicsLayer(rotationZ = if (isExpanded) 180f else 0f)
                                    )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = defect.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (defect.status == DefectStatus.CLOSED) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2
                    )
                }
                
                // Status text and icon in top right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                ) {
                    Surface(
                        color = when (defect.status) {
                            DefectStatus.OPEN -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        },
                        border = when (defect.status) {
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
                                text = when (defect.status) {
                                    DefectStatus.OPEN -> "Open"
                                    DefectStatus.IN_PROGRESS -> "In Progress"
                                    DefectStatus.CLOSED -> "Closed"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = when (defect.status) {
                                    DefectStatus.OPEN -> MaterialTheme.colorScheme.error
                                    DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                                    DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiary
                                }
                            )
                            
                            Box(
                                modifier = Modifier.size(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (defect.status) {
                                    DefectStatus.OPEN -> {
                                        // Empty circle for Open status
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                                            shape = CircleShape,
                                            modifier = Modifier.fillMaxSize()
                                        ) {}
                                    }
                                    DefectStatus.IN_PROGRESS -> {
                                        // Half-filled circle for In Progress - simplified version
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                            shape = CircleShape,
                                            modifier = Modifier.fillMaxSize()
                                        ) {}
                                    }
                                    DefectStatus.CLOSED -> {
                                        // Filled circle for Closed status
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiary,
                                            shape = CircleShape,
                                            modifier = Modifier.fillMaxSize()
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
                }
            }
            
            // Expandable sub-tasks view
            if (defect.subTasks.isNotEmpty() && isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Progress indicator
                val completedSubTasks = defect.subTasks.count { it.isCompleted }
                val totalSubTasks = defect.subTasks.size
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sub-tasks: $completedSubTasks/$totalSubTasks complete",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Progress percentage
                            Text(
                                text = "${if (totalSubTasks > 0) (completedSubTasks * 100 / totalSubTasks) else 0}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Progress bar
                        LinearProgressIndicator(
                            progress = if (totalSubTasks > 0) completedSubTasks.toFloat() / totalSubTasks else 0f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Sub-tasks list
                        defect.subTasks.forEach { subTask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Indentation for sub-tasks
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Sub-task status indicator
                                Surface(
                                    color = if (subTask.isCompleted) {
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (subTask.isCompleted) {
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                                        } else {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        }
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    if (subTask.isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = subTask.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (subTask.isCompleted) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category and priority
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(formatCategory(defect.category)) }
                    )
                    
                    // Priority dropdown (same as tasks)
                    PriorityDropdown(
                        currentPriority = defect.priority,
                        onPriorityChange = { newPriority: Priority ->
                            val updatedDefect = defect.copy(priority = newPriority)
                            onDefectUpdate(updatedDefect)
                        }
                    )
                    
                    // Sub-task progress indicator
                    if (defect.subTasks.isNotEmpty()) {
                        val completedSubTasks = defect.subTasks.count { it.isCompleted }
                        val totalSubTasks = defect.subTasks.size
                        
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$completedSubTasks/$totalSubTasks",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                // Due date if available
                defect.dueDate?.let { dueDate ->
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

@Composable
fun DefectFilterDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (DefectCategory?, DefectStatus?, Priority?) -> Unit,
    currentCategory: DefectCategory?,
    currentStatus: DefectStatus?,
    currentPriority: Priority?
) {
    var selectedCategory by remember { mutableStateOf(currentCategory) }
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var selectedPriority by remember { mutableStateOf(currentPriority) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Defects") },
        text = {
            Column {
                // Category filter
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                DefectCategory.values().forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedCategory = if (selectedCategory == category) null else category },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = if (selectedCategory == category) null else category },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = formatCategory(category),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedCategory == category) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (selectedCategory == category) {
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
                
                DefectStatus.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedStatus = if (selectedStatus == status) null else status },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = if (selectedStatus == status) null else status },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = when (status) {
                                    DefectStatus.OPEN -> MaterialTheme.colorScheme.error
                                    DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                                    DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiary
                                },
                                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Status indicator
                        Surface(
                            color = when (status) {
                                DefectStatus.OPEN -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
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
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    DefectStatus.IN_PROGRESS -> {
                                        // Half-filled circle for In Progress - simplified version
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                            shape = CircleShape,
                                            modifier = Modifier.size(18.dp)
                                        ) {}
                                    }
                                    DefectStatus.CLOSED -> {
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
                                fontWeight = if (selectedStatus == status) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (selectedStatus == status) {
                                when (status) {
                                    DefectStatus.OPEN -> MaterialTheme.colorScheme.error
                                    DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                                    DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiary
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
                            .clickable { selectedPriority = if (selectedPriority == priority) null else priority },
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
                                unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                                    fontWeight = if (selectedPriority == priority) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (selectedPriority == priority) {
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
                onClick = { onApplyFilters(selectedCategory, selectedStatus, selectedPriority) }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { 
                        selectedCategory = null
                        selectedStatus = null
                        selectedPriority = null
                        onApplyFilters(null, null, null)
                    }
                ) {
                    Text("Clear All")
                }
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}


