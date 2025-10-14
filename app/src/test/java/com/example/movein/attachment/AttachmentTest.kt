package com.example.movein.attachment

import com.example.movein.shared.data.FileAttachment
import org.junit.Test
import org.junit.Assert.*

class AttachmentTest {

    @Test
    fun `FileAttachment should have correct properties`() {
        val attachment = FileAttachment(
            id = "test_id",
            name = "test_image.jpg",
            type = "image",
            uri = "content://test_uri",
            size = 1024 * 1024
        )
        
        assertEquals("test_id", attachment.id)
        assertEquals("test_image.jpg", attachment.name)
        assertEquals("image", attachment.type)
        assertEquals("content://test_uri", attachment.uri)
        assertEquals(1024 * 1024, attachment.size)
    }

    @Test
    fun `FileAttachment should handle file type correctly`() {
        val attachment = FileAttachment(
            id = "file_id",
            name = "document.pdf",
            type = "file",
            uri = "content://file_uri",
            size = 2048 * 1024
        )
        
        assertEquals("file", attachment.type)
        assertEquals("document.pdf", attachment.name)
        assertTrue(attachment.size > 0)
    }
}
