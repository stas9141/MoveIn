package com.example.movein.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.movein.shared.data.FileAttachment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Manages file persistence for attachments
 * Copies files from temporary URIs to app's internal storage for permanent access
 */
class FileManager(private val context: Context) {
    
    companion object {
        private const val TAG = "FileManager"
        private const val ATTACHMENTS_DIR = "attachments"
    }
    
    private val attachmentsDir: File by lazy {
        val dir = File(context.filesDir, ATTACHMENTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
    
    /**
     * Persist a file from URI to internal storage and return a FileAttachment
     */
    suspend fun persistFile(
        uri: Uri,
        originalName: String? = null,
        mimeType: String? = null
    ): FileAttachment? {
        return try {
            val fileName = originalName ?: generateFileName(uri, mimeType)
            val fileId = UUID.randomUUID().toString()
            val fileExtension = getFileExtension(fileName)
            val persistentFileName = "${fileId}${if (fileExtension.isNotEmpty()) ".$fileExtension" else ""}"
            val persistentFile = File(attachmentsDir, persistentFileName)
            
            // Copy file from URI to internal storage
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(persistentFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            val fileSize = persistentFile.length()
            
            FileAttachment(
                id = fileId,
                name = fileName,
                type = mimeType ?: getMimeTypeFromExtension(fileExtension),
                uri = persistentFile.absolutePath, // Store absolute path instead of content URI
                size = fileSize
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting file: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get a URI for a persisted file
     */
    fun getFileUri(filePath: String): Uri? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                Uri.fromFile(file)
            } else {
                Log.w(TAG, "File not found: $filePath")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file URI: ${e.message}", e)
            null
        }
    }
    
    /**
     * Delete a persisted file
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                true // File doesn't exist, consider it deleted
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${e.message}", e)
            false
        }
    }
    
    /**
     * Clean up orphaned files (files that are no longer referenced by any task/defect)
     */
    suspend fun cleanupOrphanedFiles(referencedFilePaths: Set<String>) {
        try {
            val allFiles = attachmentsDir.listFiles() ?: return
            val referencedFiles = referencedFilePaths.map { File(it).name }.toSet()
            
            allFiles.forEach { file ->
                if (!referencedFiles.contains(file.name)) {
                    file.delete()
                    Log.d(TAG, "Deleted orphaned file: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up orphaned files: ${e.message}", e)
        }
    }
    
    /**
     * Get total size of all attachments
     */
    fun getTotalAttachmentsSize(): Long {
        return try {
            attachmentsDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating attachments size: ${e.message}", e)
            0L
        }
    }
    
    private fun generateFileName(uri: Uri, mimeType: String?): String {
        val timestamp = System.currentTimeMillis()
        return when {
            mimeType?.startsWith("image/") == true -> "image_$timestamp.jpg"
            mimeType?.startsWith("video/") == true -> "video_$timestamp.mp4"
            mimeType?.contains("pdf") == true -> "document_$timestamp.pdf"
            mimeType?.contains("text/") == true -> "text_$timestamp.txt"
            else -> "file_$timestamp"
        }
    }
    
    private fun getFileExtension(fileName: String): String {
        return if (fileName.contains(".")) {
            fileName.substringAfterLast(".", "").lowercase()
        } else {
            ""
        }
    }
    
    private fun getMimeTypeFromExtension(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            else -> "application/octet-stream"
        }
    }
}

