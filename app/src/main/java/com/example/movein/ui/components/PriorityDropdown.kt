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
import com.example.movein.shared.data.Priority
import com.example.movein.utils.formatPriority

@Composable
fun PriorityDropdown(
    currentPriority: Priority,
    onPriorityChange: (Priority) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            color = when (currentPriority) {
                Priority.LOW -> MaterialTheme.colorScheme.primaryContainer
                Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
            },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = formatPriority(currentPriority),
                    style = MaterialTheme.typography.bodySmall,
                    color = when (currentPriority) {
                        Priority.LOW -> MaterialTheme.colorScheme.onPrimaryContainer
                        Priority.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                        Priority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.size(16.dp),
                    tint = when (currentPriority) {
                        Priority.LOW -> MaterialTheme.colorScheme.onPrimaryContainer
                        Priority.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                        Priority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Priority.values().forEach { priority ->
                DropdownMenuItem(
                                                    text = { Text(formatPriority(priority)) },
                    onClick = {
                        onPriorityChange(priority)
                        expanded = false
                    },
                    leadingIcon = {
                        Surface(
                            color = when (priority) {
                                Priority.LOW -> MaterialTheme.colorScheme.primaryContainer
                                Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                                Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
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

