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
import com.example.movein.shared.data.DefectStatus

@Composable
fun DefectStatusDropdown(
    currentStatus: DefectStatus,
    onStatusChange: (DefectStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            color = when (currentStatus) {
                DefectStatus.OPEN -> MaterialTheme.colorScheme.errorContainer
                DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiaryContainer
            },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (currentStatus) {
                        DefectStatus.OPEN -> "Open"
                        DefectStatus.IN_PROGRESS -> "In Progress"
                        DefectStatus.CLOSED -> "Closed"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (currentStatus) {
                        DefectStatus.OPEN -> MaterialTheme.colorScheme.onErrorContainer
                        DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
                        DefectStatus.CLOSED -> MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.size(16.dp),
                    tint = when (currentStatus) {
                        DefectStatus.OPEN -> MaterialTheme.colorScheme.onErrorContainer
                        DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
                        DefectStatus.CLOSED -> MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DefectStatus.values().forEach { status ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            when (status) {
                                DefectStatus.OPEN -> "Open"
                                DefectStatus.IN_PROGRESS -> "In Progress"
                                DefectStatus.CLOSED -> "Closed"
                            }
                        )
                    },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    },
                    leadingIcon = {
                        Surface(
                            color = when (status) {
                                DefectStatus.OPEN -> MaterialTheme.colorScheme.errorContainer
                                DefectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                                DefectStatus.CLOSED -> MaterialTheme.colorScheme.tertiaryContainer
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
