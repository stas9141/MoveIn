package com.example.movein.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movein.offline.SyncStatus
import kotlinx.coroutines.delay

@Composable
fun OfflineIndicator(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // Show indicator when offline or when there are pending operations
    val shouldShow = !syncStatus.isOnline || syncStatus.pendingOperations > 0
    
    LaunchedEffect(shouldShow) {
        if (shouldShow) {
            isVisible = true
        } else {
            delay(2000) // Keep showing for 2 seconds after sync completes
            isVisible = false
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    !syncStatus.isOnline -> MaterialTheme.colorScheme.errorContainer
                    syncStatus.isSyncing -> MaterialTheme.colorScheme.primaryContainer
                    syncStatus.pendingOperations > 0 -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon with animation
                val icon = when {
                    !syncStatus.isOnline -> Icons.Default.Warning
                    syncStatus.isSyncing -> Icons.Default.Add
                    syncStatus.pendingOperations > 0 -> Icons.Default.Info
                    else -> Icons.Default.Email
                }
                
                val iconColor = when {
                    !syncStatus.isOnline -> MaterialTheme.colorScheme.onErrorContainer
                    syncStatus.isSyncing -> MaterialTheme.colorScheme.onPrimaryContainer
                    syncStatus.pendingOperations > 0 -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
                
                if (syncStatus.isSyncing) {
                    // Animated rotating sync icon
                    val infiniteTransition = rememberInfiniteTransition(label = "sync")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = "Syncing",
                        tint = iconColor,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Status",
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Status text
                Text(
                    text = when {
                        !syncStatus.isOnline -> "Offline - Changes saved locally"
                        syncStatus.isSyncing -> "Syncing data..."
                        syncStatus.pendingOperations > 0 -> "Syncing ${syncStatus.pendingOperations} changes..."
                        else -> "All data synced"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = iconColor,
                    modifier = Modifier.weight(1f)
                )
                
                // Pending count badge
                if (syncStatus.pendingOperations > 0 && !syncStatus.isSyncing) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = syncStatus.pendingOperations.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactOfflineIndicator(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    val shouldShow = !syncStatus.isOnline || syncStatus.pendingOperations > 0
    
    if (shouldShow) {
        Surface(
            color = when {
                !syncStatus.isOnline -> MaterialTheme.colorScheme.error
                syncStatus.isSyncing -> MaterialTheme.colorScheme.primary
                syncStatus.pendingOperations > 0 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.surface
            },
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val icon = when {
                    !syncStatus.isOnline -> Icons.Default.Warning
                    syncStatus.isSyncing -> Icons.Default.Add
                    syncStatus.pendingOperations > 0 -> Icons.Default.Info
                    else -> Icons.Default.Email
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = "Status",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                
                if (syncStatus.pendingOperations > 0) {
                    Text(
                        text = syncStatus.pendingOperations.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
