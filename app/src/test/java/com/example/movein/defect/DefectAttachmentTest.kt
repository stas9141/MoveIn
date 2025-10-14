package com.example.movein.defect

import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.DefectCategory
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.FileAttachment
import com.example.movein.utils.getTodayString
import org.junit.Test
import org.junit.Assert.*

class DefectAttachmentTest {

    @Test
    fun `defect creation includes attachments`() {
        // Given
        val attachments = listOf(
            FileAttachment(
                id = "att1",
                name = "document.pdf",
                type = "file",
                uri = "content://test/document.pdf",
                size = 1024L
            ),
            FileAttachment(
                id = "att2",
                name = "photo.jpg",
                type = "image",
                uri = "content://test/photo.jpg",
                size = 2048L
            )
        )

        // When
        val defect = Defect(
            id = "defect1",
            location = "Kitchen",
            category = DefectCategory.PLUMBING,
            priority = Priority.HIGH,
            description = "Leaky faucet",
            images = listOf("image1.jpg"),
            attachments = attachments,
            status = DefectStatus.OPEN,
            createdAt = getTodayString()
        )

        // Then
        assertEquals(2, defect.attachments.size)
        assertEquals("document.pdf", defect.attachments[0].name)
        assertEquals("photo.jpg", defect.attachments[1].name)
        assertEquals("file", defect.attachments[0].type)
        assertEquals("image", defect.attachments[1].type)
    }

    @Test
    fun `defect copy preserves attachments`() {
        // Given
        val originalDefect = Defect(
            id = "defect1",
            location = "Kitchen",
            category = DefectCategory.PLUMBING,
            priority = Priority.HIGH,
            description = "Leaky faucet",
            images = listOf("image1.jpg"),
            attachments = listOf(
                FileAttachment(
                    id = "att1",
                    name = "document.pdf",
                    type = "file",
                    uri = "content://test/document.pdf",
                    size = 1024L
                )
            ),
            status = DefectStatus.OPEN,
            createdAt = getTodayString()
        )

        // When
        val updatedDefect = originalDefect.copy(
            status = DefectStatus.IN_PROGRESS,
            attachments = originalDefect.attachments + FileAttachment(
                id = "att2",
                name = "photo.jpg",
                type = "image",
                uri = "content://test/photo.jpg",
                size = 2048L
            )
        )

        // Then
        assertEquals(2, updatedDefect.attachments.size)
        assertEquals(DefectStatus.IN_PROGRESS, updatedDefect.status)
        assertEquals("document.pdf", updatedDefect.attachments[0].name)
        assertEquals("photo.jpg", updatedDefect.attachments[1].name)
    }

    @Test
    fun `defect attachment removal works correctly`() {
        // Given
        val attachments = listOf(
            FileAttachment(
                id = "att1",
                name = "document.pdf",
                type = "file",
                uri = "content://test/document.pdf",
                size = 1024L
            ),
            FileAttachment(
                id = "att2",
                name = "photo.jpg",
                type = "image",
                uri = "content://test/photo.jpg",
                size = 2048L
            )
        )

        val defect = Defect(
            id = "defect1",
            location = "Kitchen",
            category = DefectCategory.PLUMBING,
            priority = Priority.HIGH,
            description = "Leaky faucet",
            images = listOf("image1.jpg"),
            attachments = attachments,
            status = DefectStatus.OPEN,
            createdAt = getTodayString()
        )

        // When - Remove first attachment
        val updatedDefect = defect.copy(
            attachments = defect.attachments.filter { it.id != "att1" }
        )

        // Then
        assertEquals(1, updatedDefect.attachments.size)
        assertEquals("photo.jpg", updatedDefect.attachments[0].name)
        assertEquals("att2", updatedDefect.attachments[0].id)
    }

    @Test
    fun `defect with empty attachments works`() {
        // Given & When
        val defect = Defect(
            id = "defect1",
            location = "Kitchen",
            category = DefectCategory.PLUMBING,
            priority = Priority.HIGH,
            description = "Leaky faucet",
            images = listOf("image1.jpg"),
            attachments = emptyList(),
            status = DefectStatus.OPEN,
            createdAt = getTodayString()
        )

        // Then
        assertTrue(defect.attachments.isEmpty())
        assertEquals(1, defect.images.size)
    }

    @Test
    fun `file attachment properties are correct`() {
        // Given
        val attachment = FileAttachment(
            id = "att1",
            name = "test_document.pdf",
            type = "file",
            uri = "content://test/test_document.pdf",
            size = 5120L
        )

        // Then
        assertEquals("att1", attachment.id)
        assertEquals("test_document.pdf", attachment.name)
        assertEquals("file", attachment.type)
        assertEquals("content://test/test_document.pdf", attachment.uri)
        assertEquals(5120L, attachment.size)
    }

    @Test
    fun `defect duplicate preserves attachments`() {
        // Given
        val originalDefect = Defect(
            id = "defect1",
            location = "Kitchen",
            category = DefectCategory.PLUMBING,
            priority = Priority.HIGH,
            description = "Leaky faucet",
            images = listOf("image1.jpg"),
            attachments = listOf(
                FileAttachment(
                    id = "att1",
                    name = "document.pdf",
                    type = "file",
                    uri = "content://test/document.pdf",
                    size = 1024L
                )
            ),
            status = DefectStatus.OPEN,
            createdAt = getTodayString()
        )

        // When - Duplicate defect (as done in DefectDetailScreen)
        val duplicatedDefect = originalDefect.copy(
            id = "defect2",
            location = "${originalDefect.location} (Copy)",
            status = DefectStatus.OPEN,
            closedAt = null
        )

        // Then
        assertEquals("defect2", duplicatedDefect.id)
        assertEquals("Kitchen (Copy)", duplicatedDefect.location)
        assertEquals(DefectStatus.OPEN, duplicatedDefect.status)
        assertNull(duplicatedDefect.closedAt)
        assertEquals(1, duplicatedDefect.attachments.size)
        assertEquals("document.pdf", duplicatedDefect.attachments[0].name)
    }
}
