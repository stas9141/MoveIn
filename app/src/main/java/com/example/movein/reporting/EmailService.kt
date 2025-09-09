package com.example.movein.reporting

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class EmailService(private val context: Context) {
    
    data class EmailConfig(
        val recipientEmail: String,
        val recipientName: String = "",
        val subject: String = "Defect Report",
        val body: String = "",
        val attachmentFile: File? = null
    )
    
    fun sendDefectReport(config: EmailConfig) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(config.recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, config.subject)
            putExtra(Intent.EXTRA_TEXT, config.body)
            
            // Add attachment if provided
            config.attachmentFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        
        // Check if there's an email app available
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, "Send Defect Report"))
        } else {
            // Fallback: open email app with pre-filled data
            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${config.recipientEmail}")
                putExtra(Intent.EXTRA_SUBJECT, config.subject)
                putExtra(Intent.EXTRA_TEXT, config.body)
            }
            context.startActivity(fallbackIntent)
        }
    }
    
    fun generateEmailBody(
        buildingCompanyName: String,
        totalDefects: Int,
        highPriorityDefects: Int,
        reportDate: String
    ): String {
        return """
        Dear $buildingCompanyName,
        
        Please find attached the defect report for your property. This report contains details of all outstanding defects that require attention.
        
        Report Summary:
        • Total Defects: $totalDefects
        • High Priority Defects: $highPriorityDefects
        • Report Date: $reportDate
        
        Please review the attached PDF report and let us know if you have any questions or need additional information.
        
        We appreciate your prompt attention to these matters.
        
        Best regards,
        Property Management Team
        MoveIn App
        
        ---
        This email was generated automatically by the MoveIn Property Management App.
        """.trimIndent()
    }
}
