package com.example.movein.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ErrorDisplay(
    error: String?,
    errorType: com.example.movein.utils.ErrorType = com.example.movein.utils.ErrorType.UNKNOWN,
    onDismiss: (() -> Unit)? = null,
    showRecoverySuggestion: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (error == null) {
        println("ErrorDisplay: No error to display")
        return
    }
    
    println("ErrorDisplay: Displaying error: $error, type: $errorType")
    
    val recoverySuggestion = if (showRecoverySuggestion) {
        com.example.movein.utils.ErrorHandler.getRecoverySuggestion(errorType)
    } else null
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (errorType) {
                com.example.movein.utils.ErrorType.NETWORK -> MaterialTheme.colorScheme.primaryContainer
                com.example.movein.utils.ErrorType.AUTHENTICATION -> MaterialTheme.colorScheme.errorContainer
                com.example.movein.utils.ErrorType.PERMISSION -> MaterialTheme.colorScheme.errorContainer
                com.example.movein.utils.ErrorType.STORAGE -> MaterialTheme.colorScheme.secondaryContainer
                com.example.movein.utils.ErrorType.SYNC -> MaterialTheme.colorScheme.tertiaryContainer
                com.example.movein.utils.ErrorType.UNKNOWN -> MaterialTheme.colorScheme.errorContainer
            }
        ),
        border = when (errorType) {
            com.example.movein.utils.ErrorType.NETWORK -> null
            com.example.movein.utils.ErrorType.AUTHENTICATION -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            com.example.movein.utils.ErrorType.PERMISSION -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            com.example.movein.utils.ErrorType.STORAGE -> null
            com.example.movein.utils.ErrorType.SYNC -> null
            com.example.movein.utils.ErrorType.UNKNOWN -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (errorType) {
                        com.example.movein.utils.ErrorType.NETWORK -> Icons.Default.Warning
                        com.example.movein.utils.ErrorType.AUTHENTICATION -> Icons.Default.Warning
                        com.example.movein.utils.ErrorType.PERMISSION -> Icons.Default.Warning
                        com.example.movein.utils.ErrorType.STORAGE -> Icons.Default.Warning
                        com.example.movein.utils.ErrorType.SYNC -> Icons.Default.Info
                        com.example.movein.utils.ErrorType.UNKNOWN -> Icons.Default.Warning
                    },
                    contentDescription = "Error",
                    tint = when (errorType) {
                        com.example.movein.utils.ErrorType.NETWORK -> MaterialTheme.colorScheme.onPrimaryContainer
                        com.example.movein.utils.ErrorType.AUTHENTICATION -> MaterialTheme.colorScheme.onErrorContainer
                        com.example.movein.utils.ErrorType.PERMISSION -> MaterialTheme.colorScheme.onErrorContainer
                        com.example.movein.utils.ErrorType.STORAGE -> MaterialTheme.colorScheme.onSecondaryContainer
                        com.example.movein.utils.ErrorType.SYNC -> MaterialTheme.colorScheme.onTertiaryContainer
                        com.example.movein.utils.ErrorType.UNKNOWN -> MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = when (errorType) {
                        com.example.movein.utils.ErrorType.NETWORK -> "Connection Issue"
                        com.example.movein.utils.ErrorType.AUTHENTICATION -> "Sign-In Error"
                        com.example.movein.utils.ErrorType.PERMISSION -> "Access Denied"
                        com.example.movein.utils.ErrorType.STORAGE -> "Storage Issue"
                        com.example.movein.utils.ErrorType.SYNC -> "Sync Issue"
                        com.example.movein.utils.ErrorType.UNKNOWN -> "Error"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = when (errorType) {
                        com.example.movein.utils.ErrorType.NETWORK -> MaterialTheme.colorScheme.onPrimaryContainer
                        com.example.movein.utils.ErrorType.AUTHENTICATION -> MaterialTheme.colorScheme.onErrorContainer
                        com.example.movein.utils.ErrorType.PERMISSION -> MaterialTheme.colorScheme.onErrorContainer
                        com.example.movein.utils.ErrorType.STORAGE -> MaterialTheme.colorScheme.onSecondaryContainer
                        com.example.movein.utils.ErrorType.SYNC -> MaterialTheme.colorScheme.onTertiaryContainer
                        com.example.movein.utils.ErrorType.UNKNOWN -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                
                if (onDismiss != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = when (errorType) {
                                com.example.movein.utils.ErrorType.NETWORK -> MaterialTheme.colorScheme.onPrimaryContainer
                                com.example.movein.utils.ErrorType.AUTHENTICATION -> MaterialTheme.colorScheme.onErrorContainer
                                com.example.movein.utils.ErrorType.PERMISSION -> MaterialTheme.colorScheme.onErrorContainer
                                com.example.movein.utils.ErrorType.STORAGE -> MaterialTheme.colorScheme.onSecondaryContainer
                                com.example.movein.utils.ErrorType.SYNC -> MaterialTheme.colorScheme.onTertiaryContainer
                                com.example.movein.utils.ErrorType.UNKNOWN -> MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    ) {
                        Text("Dismiss")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = when (errorType) {
                    com.example.movein.utils.ErrorType.NETWORK -> MaterialTheme.colorScheme.onPrimaryContainer
                    com.example.movein.utils.ErrorType.AUTHENTICATION -> MaterialTheme.colorScheme.onErrorContainer
                    com.example.movein.utils.ErrorType.PERMISSION -> MaterialTheme.colorScheme.onErrorContainer
                    com.example.movein.utils.ErrorType.STORAGE -> MaterialTheme.colorScheme.onSecondaryContainer
                    com.example.movein.utils.ErrorType.SYNC -> MaterialTheme.colorScheme.onTertiaryContainer
                    com.example.movein.utils.ErrorType.UNKNOWN -> MaterialTheme.colorScheme.onErrorContainer
                },
                textAlign = TextAlign.Start
            )
            
            if (recoverySuggestion != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 $recoverySuggestion",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (errorType) {
                        com.example.movein.utils.ErrorType.NETWORK -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        com.example.movein.utils.ErrorType.AUTHENTICATION -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        com.example.movein.utils.ErrorType.PERMISSION -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        com.example.movein.utils.ErrorType.STORAGE -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        com.example.movein.utils.ErrorType.SYNC -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        com.example.movein.utils.ErrorType.UNKNOWN -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    },
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
fun SimpleErrorDisplay(
    error: String?,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (error == null) return
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            if (onDismiss != null) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}
