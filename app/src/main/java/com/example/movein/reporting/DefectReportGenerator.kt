package com.example.movein.reporting

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.DefectStatus
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.io.image.ImageDataFactory
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import java.io.File
import android.util.Base64
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DefectReportGenerator(private val context: Context) {
    
    data class ReportConfig(
        val includeImages: Boolean = true,
        val includeClosedDefects: Boolean = false,
        val groupByPriority: Boolean = true,
        val includeSummary: Boolean = true,
        val buildingCompanyName: String = "",
        val buildingCompanyEmail: String = "",
        val reportTitle: String = "Defect Report"
    )
    
    suspend fun generateDefectReport(
        defects: List<Defect>,
        config: ReportConfig = ReportConfig()
    ): File = withContext(Dispatchers.IO) {
        try {
            val fileName = "defect_report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val externalDir = context.getExternalFilesDir(null)
            val file = if (externalDir != null) {
                File(externalDir, fileName)
            } else {
                // Fallback to internal storage
                File(context.filesDir, fileName)
            }
            
            val filteredDefects = if (config.includeClosedDefects) {
                defects
            } else {
                defects.filter { it.status != DefectStatus.CLOSED }
            }
            
            val htmlContent = generateHtmlReport(filteredDefects, config)
            
            // Create PDF using HtmlConverter
            val pdfWriter = PdfWriter(FileOutputStream(file))
            
            try {
                // Convert HTML to PDF
                HtmlConverter.convertToPdf(htmlContent, pdfWriter)
            } finally {
                pdfWriter.close()
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Generate PDF report in memory and return as ByteArray for sharing
     */
    suspend fun generatePdfReportForSharing(
        defects: List<Defect>,
        config: ReportConfig = ReportConfig()
    ): ByteArray = withContext(Dispatchers.IO) {
        try {
            val filteredDefects = if (config.includeClosedDefects) {
                defects
            } else {
                defects.filter { it.status != DefectStatus.CLOSED }
            }
            
            val htmlContent = generateHtmlReport(filteredDefects, config)
            
            // Create PDF in memory using ByteArrayOutputStream
            val outputStream = ByteArrayOutputStream()
            val pdfWriter = PdfWriter(outputStream)
            
            try {
                // Convert HTML to PDF
                HtmlConverter.convertToPdf(htmlContent, pdfWriter)
            } finally {
                pdfWriter.close()
            }
            
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Create a share intent for the PDF report
     */
    fun createShareIntent(
        pdfData: ByteArray,
        fileName: String = "defect_report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
    ): Intent {
        // Create a temporary file for sharing
        val tempFile = File(context.cacheDir, fileName)
        tempFile.writeBytes(pdfData)
        
        // Create URI using FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
        
        // Create share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Defect Report")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the defect report.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        return Intent.createChooser(shareIntent, "Share Defect Report")
    }
    
    private fun generateHtmlReport(defects: List<Defect>, config: ReportConfig): String {
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val currentDate = dateFormatter.format(Date())
        
        val groupedDefects = if (config.groupByPriority) {
            defects.groupBy { it.priority }
        } else {
            mapOf(Priority.MEDIUM to defects) // Default grouping
        }
        
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: Arial, sans-serif;
                    margin: 20px;
                    color: #333;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #2196F3;
                    padding-bottom: 20px;
                    margin-bottom: 30px;
                }
                .title {
                    font-size: 24px;
                    font-weight: bold;
                    color: #2196F3;
                    margin-bottom: 10px;
                }
                .subtitle {
                    font-size: 16px;
                    color: #666;
                }
                .summary {
                    background-color: #f5f5f5;
                    padding: 15px;
                    border-radius: 5px;
                    margin-bottom: 30px;
                }
                .summary h3 {
                    margin-top: 0;
                    color: #2196F3;
                }
                .priority-section {
                    margin-bottom: 30px;
                }
                .priority-title {
                    font-size: 18px;
                    font-weight: bold;
                    color: #2196F3;
                    margin-bottom: 15px;
                    padding: 10px;
                    background-color: #e3f2fd;
                    border-left: 4px solid #2196F3;
                }
                .defect-item {
                    border: 1px solid #ddd;
                    border-radius: 5px;
                    margin-bottom: 15px;
                    padding: 15px;
                    background-color: #fff;
                }
                .defect-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 10px;
                }
                .defect-title {
                    font-size: 16px;
                    font-weight: bold;
                    color: #333;
                }
                .defect-status {
                    padding: 4px 8px;
                    border-radius: 3px;
                    font-size: 12px;
                    font-weight: bold;
                }
                .status-open { background-color: #ffebee; color: #c62828; }
                .status-in-progress { background-color: #fff3e0; color: #ef6c00; }
                .status-closed { background-color: #e8f5e8; color: #2e7d32; }
                .defect-details {
                    margin: 10px 0;
                }
                .defect-detail {
                    margin: 5px 0;
                }
                .detail-label {
                    font-weight: bold;
                    color: #666;
                }
                .defect-description {
                    margin: 10px 0;
                    padding: 10px;
                    background-color: #f9f9f9;
                    border-radius: 3px;
                }
                .defect-image {
                    max-width: 200px;
                    max-height: 150px;
                    margin: 10px 0;
                    border-radius: 3px;
                }
                .footer {
                    margin-top: 40px;
                    text-align: center;
                    color: #666;
                    font-size: 12px;
                    border-top: 1px solid #ddd;
                    padding-top: 20px;
                }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="title">${config.reportTitle}</div>
                <div class="subtitle">Generated on $currentDate</div>
                ${if (config.buildingCompanyName.isNotEmpty()) "<div class='subtitle'>For: ${config.buildingCompanyName}</div>" else ""}
            </div>
            
            ${if (config.includeSummary) generateSummarySection(defects) else ""}
            
            ${generateDefectsSections(groupedDefects, config)}
            
            <div class="footer">
                <p>This report was generated by MoveIn - Property Management App</p>
                <p>For questions or support, please contact your property manager</p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
    
    private fun generateSummarySection(defects: List<Defect>): String {
        val totalDefects = defects.size
        val openDefects = defects.count { it.status == DefectStatus.OPEN }
        val inProgressDefects = defects.count { it.status == DefectStatus.IN_PROGRESS }
        val closedDefects = defects.count { it.status == DefectStatus.CLOSED }
        
        val highPriorityDefects = defects.count { it.priority == Priority.HIGH }
        val mediumPriorityDefects = defects.count { it.priority == Priority.MEDIUM }
        val lowPriorityDefects = defects.count { it.priority == Priority.LOW }
        
        return """
        <div class="summary">
            <h3>Report Summary</h3>
            <div class="defect-detail">
                <span class="detail-label">Total Defects:</span> $totalDefects
            </div>
            <div class="defect-detail">
                <span class="detail-label">Open:</span> $openDefects | 
                <span class="detail-label">In Progress:</span> $inProgressDefects | 
                <span class="detail-label">Closed:</span> $closedDefects
            </div>
            <div class="defect-detail">
                <span class="detail-label">High Priority:</span> $highPriorityDefects | 
                <span class="detail-label">Medium Priority:</span> $mediumPriorityDefects | 
                <span class="detail-label">Low Priority:</span> $lowPriorityDefects
            </div>
        </div>
        """.trimIndent()
    }
    
    private fun generateDefectsSections(groupedDefects: Map<Priority, List<Defect>>, config: ReportConfig): String {
        return groupedDefects.entries.joinToString("") { (priority, defects) ->
            """
            <div class="priority-section">
                <div class="priority-title">${priority.displayName} Priority Defects (${defects.size})</div>
                ${defects.joinToString("") { defect -> generateDefectItem(defect, config) }}
            </div>
            """.trimIndent()
        }
    }
    
    private fun generateDefectItem(defect: Defect, config: ReportConfig): String {
        val createdDate = defect.createdAt
        
        val statusClass = when (defect.status) {
            DefectStatus.OPEN -> "status-open"
            DefectStatus.IN_PROGRESS -> "status-in-progress"
            DefectStatus.CLOSED -> "status-closed"
        }
        
        val imagesHtml = if (config.includeImages) {
            val allImages = mutableListOf<String>()
            
            // Add images from defect.images
            allImages.addAll(defect.images)
            
            // Add images from defect.attachments
            val attachmentImages = defect.attachments.filter { 
                it.type == "image" || it.type.startsWith("image/")
            }.map { it.uri }
            allImages.addAll(attachmentImages)
            
            if (allImages.isNotEmpty()) {
                println("PDF Report: Processing ${allImages.size} images for defect: ${defect.location}")
                allImages.take(5).forEachIndexed { index, imagePath ->
                    println("PDF Report: Processing image $index: $imagePath")
                }
                allImages.take(5).joinToString("<br>") { imagePath ->
                    try {
                        // Try to convert image to base64 for PDF embedding
                        val base64Image = convertImageToBase64(imagePath)
                        if (base64Image != null) {
                            println("PDF Report: Successfully converted image: $imagePath")
                            """
                            <img src="data:image/jpeg;base64,$base64Image" class="defect-image" alt="Defect Image" />
                            """.trimIndent()
                        } else {
                            println("PDF Report: Failed to convert image: $imagePath")
                            """
                            <div class="defect-image-placeholder">Image: ${imagePath.substringAfterLast("/")}</div>
                            """.trimIndent()
                        }
                    } catch (e: Exception) {
                        println("PDF Report: Exception converting image: $imagePath - ${e.message}")
                        """
                        <div class="defect-image-placeholder">Image: ${imagePath.substringAfterLast("/")}</div>
                        """.trimIndent()
                    }
                }
            } else ""
        } else ""
        
        return """
        <div class="defect-item">
            <div class="defect-header">
                <div class="defect-title">${defect.location}</div>
                <div class="defect-status $statusClass">${defect.status.name}</div>
            </div>
            
            <div class="defect-details">
                <div class="defect-detail">
                    <span class="detail-label">Category:</span> ${defect.category.name}
                </div>
                <div class="defect-detail">
                    <span class="detail-label">Priority:</span> ${defect.priority.name}
                </div>
                <div class="defect-detail">
                    <span class="detail-label">Created:</span> $createdDate
                </div>
                ${if (defect.location.isNotEmpty()) """
                <div class="defect-detail">
                    <span class="detail-label">Location:</span> ${defect.location}
                </div>
                """ else ""}
            </div>
            
            ${if (defect.description.isNotEmpty()) """
            <div class="defect-description">
                <strong>Description:</strong><br>
                ${defect.description.replace("\n", "<br>")}
            </div>
            """ else ""}
            
            ${if (defect.notes.isNotEmpty()) """
            <div class="defect-description">
                <strong>Notes:</strong><br>
                ${defect.notes.replace("\n", "<br>")}
            </div>
            """ else ""}
            
            ${if (defect.attachments.isNotEmpty()) """
            <div class="defect-description">
                <strong>Attachments:</strong><br>
                ${defect.attachments.joinToString("<br>") { attachment ->
                    "${attachment.name} (${attachment.type})"
                }}
            </div>
            """ else ""}
            
            $imagesHtml
        </div>
        """.trimIndent()
    }
    
    private val Priority.displayName: String
        get() = when (this) {
            Priority.HIGH -> "High"
            Priority.MEDIUM -> "Medium"
            Priority.LOW -> "Low"
        }
    
    private val DefectStatus.displayName: String
        get() = when (this) {
            DefectStatus.OPEN -> "Open"
            DefectStatus.IN_PROGRESS -> "In Progress"
            DefectStatus.CLOSED -> "Closed"
        }
    
    private fun convertImageToBase64(imagePath: String): String? {
        return try {
            println("PDF Report: Converting image to base64: $imagePath")
            val bitmap = if (imagePath.startsWith("content://")) {
                // Handle content URI (from gallery/camera)
                println("PDF Report: Processing content URI")
                val uri = Uri.parse(imagePath)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else if (imagePath.startsWith("file://")) {
                // Handle file URI
                println("PDF Report: Processing file URI")
                val file = File(imagePath.substring(7)) // Remove "file://" prefix
                if (!file.exists()) {
                    println("PDF Report: File does not exist: ${file.absolutePath}")
                    return null
                }
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                // Handle direct file path
                println("PDF Report: Processing direct file path")
                val file = File(imagePath)
                if (!file.exists()) {
                    println("PDF Report: File does not exist: ${file.absolutePath}")
                    return null
                }
                BitmapFactory.decodeFile(imagePath)
            }
            
            if (bitmap == null) {
                println("PDF Report: Failed to decode bitmap from: $imagePath")
                return null
            }
            
            println("PDF Report: Successfully decoded bitmap: ${bitmap.width}x${bitmap.height}")
            
            // Resize bitmap to reduce size for PDF
            val maxWidth = 400
            val maxHeight = 300
            val scaledBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                val scale = minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }
            
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            println("PDF Report: Successfully converted to base64: ${byteArray.size} bytes")
            base64String
        } catch (e: Exception) {
            println("PDF Report: Exception in convertImageToBase64: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
