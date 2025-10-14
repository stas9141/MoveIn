package com.example.movein.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object FileReviewUtils {
    
    /**
     * Get MIME type from ContentResolver (more reliable for content URIs)
     */
    fun getMimeTypeFromContentResolver(context: Context, uri: String): String {
        return try {
            val parsedUri = Uri.parse(uri)
            context.contentResolver.getType(parsedUri) ?: getMimeType(uri)
        } catch (e: Exception) {
            getMimeType(uri)
        }
    }
    
    /**
     * Get MIME type from file extension
     */
    fun getMimeType(fileName: String): String {
        // Extract file extension from filename
        val extension = if (fileName.contains(".")) {
            fileName.substringAfterLast(".", "").lowercase()
        } else {
            ""
        }
        
        // Get MIME type from extension
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        
        // Fallback for common file types
        return mimeType ?: when (extension) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "txt" -> "text/plain"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Check if file type is supported for preview
     */
    fun isPreviewable(fileName: String): Boolean {
        val mimeType = getMimeType(fileName)
        return when {
            mimeType.startsWith("image/") -> true
            mimeType == "application/pdf" -> true
            mimeType.startsWith("text/") -> true
            else -> false
        }
    }
    
    /**
     * Get file type category for UI display
     */
    fun getFileTypeCategory(fileName: String): FileTypeCategory {
        val mimeType = getMimeType(fileName)
        return when {
            mimeType.startsWith("image/") -> FileTypeCategory.IMAGE
            mimeType == "application/pdf" -> FileTypeCategory.PDF
            mimeType.startsWith("text/") -> FileTypeCategory.TEXT
            mimeType.startsWith("video/") -> FileTypeCategory.VIDEO
            mimeType.startsWith("audio/") -> FileTypeCategory.AUDIO
            mimeType.contains("word") || mimeType.contains("document") -> FileTypeCategory.DOCUMENT
            mimeType.contains("spreadsheet") || mimeType.contains("excel") -> FileTypeCategory.SPREADSHEET
            mimeType.contains("presentation") || mimeType.contains("powerpoint") -> FileTypeCategory.PRESENTATION
            mimeType.contains("zip") || mimeType.contains("archive") -> FileTypeCategory.ARCHIVE
            else -> FileTypeCategory.OTHER
        }
    }
    
    /**
     * Format file size for display
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * Open file with external app
     */
    fun openFileWithExternalApp(context: Context, uri: String) {
        try {
            val parsedUri = Uri.parse(uri)
            val mimeType = getMimeTypeFromContentResolver(context, uri)
            
            // Try different approaches based on URI type
            when (parsedUri.scheme) {
                "content" -> {
                    // Content URI (from file picker)
                    
                    // Try multiple approaches in order of preference
                    val approaches = listOf(
                        // Approach 0: Special handling for images
                        if (mimeType.startsWith("image/")) {
                            {
                                val imageIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(parsedUri, mimeType)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                }
                                
                                val packageManager = context.packageManager
                                val activities = packageManager.queryIntentActivities(imageIntent, 0)
                                
                                if (activities.isNotEmpty()) {
                                    context.startActivity(Intent.createChooser(imageIntent, "Open image with"))
                                    "Image-specific intent"
                                } else {
                                    throw Exception("No image viewers found")
                                }
                            }
                        } else null,
                        // Approach 1: Special handling for PDF files
                        if (mimeType == "application/pdf" || uri.lowercase().endsWith(".pdf")) {
                            {
                                val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(parsedUri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                
                                val packageManager = context.packageManager
                                val activities = packageManager.queryIntentActivities(pdfIntent, 0)
                                
                                if (activities.isNotEmpty()) {
                                    context.startActivity(Intent.createChooser(pdfIntent, "Open PDF with"))
                                    "PDF-specific intent"
                                } else {
                                    throw Exception("No PDF readers found")
                                }
                            }
                        } else null,
                        // Approach 1: Simple view intent
                        {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setData(parsedUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            
                            val packageManager = context.packageManager
                            val activities = packageManager.queryIntentActivities(intent, 0)
                            
                            if (activities.isNotEmpty()) {
                                context.startActivity(Intent.createChooser(intent, "Open with"))
                                "Simple view intent"
                            } else {
                                throw Exception("No apps found for simple view")
                            }
                        },
                        // Approach 2: View with MIME type and enhanced permissions
                        {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(parsedUri, mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                            }
                            
                            val packageManager = context.packageManager
                            val activities = packageManager.queryIntentActivities(intent, 0)
                            
                            if (activities.isNotEmpty()) {
                                context.startActivity(Intent.createChooser(intent, "Open with"))
                                "View with MIME type"
                            } else {
                                throw Exception("No apps found for MIME type: $mimeType")
                            }
                        },
                        // Approach 3: Send action (like sharing)
                        {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = mimeType
                                putExtra(Intent.EXTRA_STREAM, parsedUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Open with"))
                            "Send action"
                        }
                    )
                    
                    for ((index, approach) in approaches.withIndex()) {
                        if (approach != null) {
                        try {
                            approach()
                            return
                        } catch (e: Exception) {
                            continue
                        }
                        }
                    }
                    
                    // All approaches failed - silently return
                }
                "file" -> {
                    // File URI (from camera)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(parsedUri, mimeType)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open with"))
                }
                else -> {
                    // Unknown scheme, try generic approach
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(parsedUri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open with"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Share file with other apps
     */
    fun shareFile(context: Context, uri: String, fileName: String) {
        try {
            val parsedUri = Uri.parse(uri)
            val mimeType = getMimeType(fileName)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, parsedUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Share file"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

enum class FileTypeCategory {
    IMAGE, PDF, TEXT, VIDEO, AUDIO, DOCUMENT, SPREADSHEET, PRESENTATION, ARCHIVE, OTHER
}

@Composable
fun rememberImageBitmap(uri: String?): ImageBitmap? {
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(uri) { mutableStateOf(true) }
    val context = LocalContext.current
    
    LaunchedEffect(uri) {
        if (uri != null) {
            isLoading = true
            bitmap = withContext(Dispatchers.IO) {
                try {
                    val parsedUri = Uri.parse(uri)
                    
                    // For content URIs, try to get the actual file path first
                    val inputStream = if (parsedUri.scheme == "content") {
                        // For content URIs, we need to use ContentResolver
                        context.contentResolver.openInputStream(parsedUri)
                    } else {
                        // For file URIs, try direct file access
                        java.io.FileInputStream(parsedUri.path ?: "")
                    }
                    
                    inputStream?.use { input ->
                        val options = android.graphics.BitmapFactory.Options().apply {
                            inJustDecodeBounds = false
                            inSampleSize = 2 // Reduce memory usage for large images
                        }
                        android.graphics.BitmapFactory.decodeStream(input, null, options)?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }
    
    return bitmap
}
