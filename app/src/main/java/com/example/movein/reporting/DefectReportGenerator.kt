package com.example.movein.reporting

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
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
        
        val imagesHtml = if (config.includeImages && defect.images.isNotEmpty()) {
            defect.images.take(3).joinToString("") { imagePath ->
                """
                <img src="file://$imagePath" class="defect-image" alt="Defect Image" />
                """.trimIndent()
            }
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
}
