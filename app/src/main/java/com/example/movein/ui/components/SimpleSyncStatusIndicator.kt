package com.example.movein.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movein.shared.cloud.AuthState
import com.example.movein.shared.cloud.SyncStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SimpleSyncStatusIndicator(
    authState: AuthState,
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icon based on sync status
        val (icon, color) = when {
            !authState.isAuthenticated -> Pair(Icons.Default.Warning, MaterialTheme.colorScheme.onSurfaceVariant)
            syncStatus.isSyncing -> Pair(Icons.Default.Refresh, MaterialTheme.colorScheme.primary)
            syncStatus.error != null -> Pair(Icons.Default.Warning, MaterialTheme.colorScheme.error)
            syncStatus.lastSyncTime != null -> Pair(Icons.Default.Check, MaterialTheme.colorScheme.primary)
            else -> Pair(Icons.Default.Warning, MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Icon(
            imageVector = icon,
            contentDescription = "Sync Status",
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        
        // Status text
        val statusText = when {
            !authState.isAuthenticated -> "Offline"
            syncStatus.isSyncing -> "Syncing..."
            syncStatus.error != null -> "Sync Error"
            syncStatus.lastSyncTime != null -> {
                val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                val lastSyncTime = syncStatus.lastSyncTime
                "Synced ${formatter.format(java.util.Date(lastSyncTime ?: 0L))}"
            }
            else -> "Not Synced"
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = if (syncStatus.isSyncing) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun SimpleSyncStatusCard(
    authState: AuthState,
    syncStatus: SyncStatus,
    onForceSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !authState.isAuthenticated -> MaterialTheme.colorScheme.surfaceVariant
                syncStatus.error != null -> MaterialTheme.colorScheme.errorContainer
                syncStatus.isSyncing -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleSyncStatusIndicator(
                    authState = authState,
                    syncStatus = syncStatus
                )
                
                if (authState.isAuthenticated && !syncStatus.isSyncing) {
                    TextButton(
                        onClick = onForceSync,
                        enabled = !syncStatus.isSyncing
                    ) {
                        Text("Sync Now")
                    }
                }
            }
            
            // Error message
            syncStatus.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
