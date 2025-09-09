package com.example.movein.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movein.shared.data.TaskStatus
import com.example.movein.utils.formatTaskStatus

@Composable
fun TaskStatusDropdown(
    currentStatus: TaskStatus,
    onStatusChange: (TaskStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            color = when (currentStatus) {
                TaskStatus.OPEN -> MaterialTheme.colorScheme.primaryContainer
                TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                TaskStatus.CLOSED -> MaterialTheme.colorScheme.tertiaryContainer
            },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = formatTaskStatus(currentStatus),
                    style = MaterialTheme.typography.bodySmall,
                    color = when (currentStatus) {
                        TaskStatus.OPEN -> MaterialTheme.colorScheme.onPrimaryContainer
                        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onSecondaryContainer
                        TaskStatus.CLOSED -> MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.size(16.dp),
                    tint = when (currentStatus) {
                        TaskStatus.OPEN -> MaterialTheme.colorScheme.onPrimaryContainer
                        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onSecondaryContainer
                        TaskStatus.CLOSED -> MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TaskStatus.values().forEach { status ->
                DropdownMenuItem(
                    text = { Text(formatTaskStatus(status)) },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    },
                    leadingIcon = {
                        Surface(
                            color = when (status) {
                                TaskStatus.OPEN -> MaterialTheme.colorScheme.primaryContainer
                                TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                                TaskStatus.CLOSED -> MaterialTheme.colorScheme.tertiaryContainer
                            },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.size(16.dp)
                        ) {}
                    }
                )
            }
        }
    }
}
