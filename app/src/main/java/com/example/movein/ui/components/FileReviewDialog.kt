package com.example.movein.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.movein.shared.data.FileAttachment
import com.example.movein.utils.FileReviewUtils
import com.example.movein.utils.FileTypeCategory
import com.example.movein.utils.rememberImageBitmap
import android.widget.Toast

@Composable
fun FileReviewDialog(
    attachment: FileAttachment,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit
) {
    val context = LocalContext.current
    val fileTypeCategory = FileReviewUtils.getFileTypeCategory(attachment.name)
    val isPreviewable = FileReviewUtils.isPreviewable(attachment.name)
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = getFileTypeColor(fileTypeCategory).copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getFileTypeIcon(fileTypeCategory),
                                    contentDescription = null,
                                    tint = getFileTypeColor(fileTypeCategory),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "File Preview",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Preview and manage your file",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Close",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // File Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = attachment.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = FileReviewUtils.getMimeType(attachment.name),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = FileReviewUtils.formatFileSize(attachment.size),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Preview Section
                if (isPreviewable) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    when (fileTypeCategory) {
                        FileTypeCategory.IMAGE -> {
                            ImagePreview(attachment.uri)
                        }
                        FileTypeCategory.PDF -> {
                            PdfPreview(attachment.name)
                        }
                        FileTypeCategory.TEXT -> {
                            TextPreview(attachment.uri)
                        }
                        else -> {
                            UnsupportedPreview(fileTypeCategory)
                        }
                    }
                } else {
                    UnsupportedPreview(fileTypeCategory)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Primary Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Open Button - Primary action
        Button(
            onClick = {
                onOpen()
            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = null, 
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Open File",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Share Button - Secondary action
                        OutlinedButton(
                            onClick = {
                                onShare()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            border = BorderStroke(
                                1.5.dp, 
                                MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Share, 
                                contentDescription = null, 
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Share",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    // Delete Button - Destructive action (separate row)
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(
                            1.5.dp, 
                            MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = null, 
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Delete File",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete File")
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Are you sure you want to delete this file?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = attachment.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This action cannot be undone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDelete()
                            showDeleteConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmation = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ImagePreview(uri: String) {
    val bitmap = rememberImageBitmap(uri)
    var isLoading by remember(uri) { mutableStateOf(true) }
    
    LaunchedEffect(uri) {
        isLoading = true
        // Simulate loading time to show loading indicator
        kotlinx.coroutines.delay(100)
        isLoading = false
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Image preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gallery image preview not available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Open' to view with gallery app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfPreview(fileName: String) {
    val context = LocalContext.current
    var pdfInfo by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(fileName) {
        pdfInfo = try {
            // Try to get basic PDF info
            "PDF Document • ${fileName.substringAfterLast('.', "pdf").uppercase()}"
        } catch (e: Exception) {
            "PDF Document"
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // PDF Icon with better styling
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "PDF Document",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (pdfInfo != null) {
                Text(
                    text = pdfInfo!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap 'Open' to view with PDF reader",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TextPreview(uri: String) {
    var textContent by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    
    LaunchedEffect(uri) {
        textContent = try {
            val parsedUri = android.net.Uri.parse(uri)
            context.contentResolver.openInputStream(parsedUri)?.use { input ->
                input.bufferedReader().readText()
            }
        } catch (e: Exception) {
            "Unable to load text content"
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Text Content",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (textContent != null) {
                Text(
                    text = textContent!!.take(500) + if (textContent!!.length > 500) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun UnsupportedPreview(fileTypeCategory: FileTypeCategory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                getFileTypeIcon(fileTypeCategory),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = getFileTypeColor(fileTypeCategory)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Preview not available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tap 'Open' to view with external app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getFileTypeIcon(category: FileTypeCategory): ImageVector {
    return when (category) {
        FileTypeCategory.IMAGE -> Icons.Default.Star
        FileTypeCategory.PDF -> Icons.Default.Info
        FileTypeCategory.TEXT -> Icons.Default.Info
        FileTypeCategory.VIDEO -> Icons.Default.PlayArrow
        FileTypeCategory.AUDIO -> Icons.Default.Info
        FileTypeCategory.DOCUMENT -> Icons.Default.Info
        FileTypeCategory.SPREADSHEET -> Icons.Default.Info
        FileTypeCategory.PRESENTATION -> Icons.Default.Info
        FileTypeCategory.ARCHIVE -> Icons.Default.Info
        FileTypeCategory.OTHER -> Icons.Default.Info
    }
}

@Composable
private fun getFileTypeColor(category: FileTypeCategory): Color {
    return when (category) {
        FileTypeCategory.IMAGE -> MaterialTheme.colorScheme.primary
        FileTypeCategory.PDF -> MaterialTheme.colorScheme.error
        FileTypeCategory.TEXT -> MaterialTheme.colorScheme.secondary
        FileTypeCategory.VIDEO -> MaterialTheme.colorScheme.tertiary
        FileTypeCategory.AUDIO -> MaterialTheme.colorScheme.primary
        FileTypeCategory.DOCUMENT -> MaterialTheme.colorScheme.secondary
        FileTypeCategory.SPREADSHEET -> MaterialTheme.colorScheme.tertiary
        FileTypeCategory.PRESENTATION -> MaterialTheme.colorScheme.primary
        FileTypeCategory.ARCHIVE -> MaterialTheme.colorScheme.onSurfaceVariant
        FileTypeCategory.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
