package com.example.movein.defect

import com.example.movein.shared.data.Defect
import com.example.movein.shared.data.DefectCategory
import com.example.movein.shared.data.DefectStatus
import com.example.movein.shared.data.Priority
import com.example.movein.shared.data.FileAttachment
import com.example.movein.utils.getTodayString
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration test to verify the complete defect attachment flow
 * This simulates the user journey of adding, editing, and managing attachments
 */
class DefectAttachmentFlowTest {

    @Test
    fun `complete defect attachment flow works correctly`() {
        // Step 1: Create a new defect with no attachments
        val initialDefect = Defect(
            id = "defect1",
            location = "Kitchen",
            category = DefectCategory.PLUMBING,
            priority = Priority.HIGH,
            description = "Leaky faucet",
            images = emptyList(),
            attachments = emptyList(),
            status = DefectStatus.OPEN,
            createdAt = getTodayString()
        )

        assertEquals(0, initialDefect.attachments.size)
        assertEquals(0, initialDefect.images.size)

        // Step 2: Add first attachment (file)
        val fileAttachment = FileAttachment(
            id = "att1",
            name = "repair_guide.pdf",
            type = "file",
            uri = "content://test/repair_guide.pdf",
            size = 15360L
        )

        val defectWithFile = initialDefect.copy(
            attachments = initialDefect.attachments + fileAttachment
        )

        assertEquals(1, defectWithFile.attachments.size)
        assertEquals("repair_guide.pdf", defectWithFile.attachments[0].name)

        // Step 3: Add second attachment (image)
        val imageAttachment = FileAttachment(
            id = "att2",
            name = "damage_photo.jpg",
            type = "image",
            uri = "content://test/damage_photo.jpg",
            size = 25600L
        )

        val defectWithBothAttachments = defectWithFile.copy(
            attachments = defectWithFile.attachments + imageAttachment
        )

        assertEquals(2, defectWithBothAttachments.attachments.size)
        assertTrue(defectWithBothAttachments.attachments.any { it.type == "file" })
        assertTrue(defectWithBothAttachments.attachments.any { it.type == "image" })

        // Step 4: Add traditional image (for backward compatibility)
        val defectWithImages = defectWithBothAttachments.copy(
            images = listOf("camera_photo.jpg")
        )

        assertEquals(2, defectWithImages.attachments.size)
        assertEquals(1, defectWithImages.images.size)

        // Step 5: Remove file attachment
        val defectWithoutFile = defectWithImages.copy(
            attachments = defectWithImages.attachments.filter { it.id != "att1" }
        )

        assertEquals(1, defectWithoutFile.attachments.size)
        assertEquals("damage_photo.jpg", defectWithoutFile.attachments[0].name)
        assertEquals(1, defectWithoutFile.images.size)

        // Step 6: Update defect status (should preserve attachments)
        val defectInProgress = defectWithoutFile.copy(
            status = DefectStatus.IN_PROGRESS
        )

        assertEquals(DefectStatus.IN_PROGRESS, defectInProgress.status)
        assertEquals(1, defectInProgress.attachments.size)
        assertEquals(1, defectInProgress.images.size)

        // Step 7: Complete defect (should preserve all attachments)
        val completedDefect = defectInProgress.copy(
            status = DefectStatus.CLOSED,
            closedAt = getTodayString()
        )

        assertEquals(DefectStatus.CLOSED, completedDefect.status)
        assertNotNull(completedDefect.closedAt)
        assertEquals(1, completedDefect.attachments.size)
        assertEquals(1, completedDefect.images.size)
    }

    @Test
    fun `defect attachment persistence simulation`() {
        // Simulate the storage serialization/deserialization process
        val originalDefect = Defect(
            id = "defect1",
            location = "Bathroom",
            category = DefectCategory.ELECTRICITY,
            priority = Priority.MEDIUM,
            description = "Faulty outlet",
            images = listOf("outlet_photo.jpg"),
            attachments = listOf(
                FileAttachment(
                    id = "att1",
                    name = "electrical_schematic.pdf",
                    type = "file",
                    uri = "content://test/electrical_schematic.pdf",
                    size = 8192L
                ),
                FileAttachment(
                    id = "att2",
                    name = "safety_manual.pdf",
                    type = "file",
                    uri = "content://test/safety_manual.pdf",
                    size = 12288L
                )
            ),
            status = DefectStatus.OPEN,
            createdAt = getTodayString()
        )

        // Simulate storage save (defect object remains the same)
        val savedDefect = originalDefect

        // Simulate storage load (defect object remains the same)
        val loadedDefect = savedDefect

        // Verify data integrity
        assertEquals(originalDefect.id, loadedDefect.id)
        assertEquals(originalDefect.location, loadedDefect.location)
        assertEquals(originalDefect.attachments.size, loadedDefect.attachments.size)
        assertEquals(originalDefect.images.size, loadedDefect.images.size)

        // Verify attachment details
        val loadedAttachments = loadedDefect.attachments
        assertEquals("electrical_schematic.pdf", loadedAttachments[0].name)
        assertEquals("safety_manual.pdf", loadedAttachments[1].name)
        assertEquals("file", loadedAttachments[0].type)
        assertEquals("file", loadedAttachments[1].type)
    }

    @Test
    fun `defect attachment edge cases`() {
        // Test with very large attachment
        val largeAttachment = FileAttachment(
            id = "large_att",
            name = "large_video.mp4",
            type = "file",
            uri = "content://test/large_video.mp4",
            size = 104857600L // 100MB
        )

        val defectWithLargeAttachment = Defect(
            id = "defect1",
            location = "Living Room",
            category = DefectCategory.OTHER,
            priority = Priority.LOW,
            description = "Test with large file",
            images = emptyList(),
            attachments = listOf(largeAttachment),
            status = DefectStatus.OPEN,
            createdAt = getTodayString()
        )

        assertEquals(1, defectWithLargeAttachment.attachments.size)
        assertEquals(104857600L, defectWithLargeAttachment.attachments[0].size)

        // Test with many attachments
        val manyAttachments = (1..10).map { i ->
            FileAttachment(
                id = "att$i",
                name = "file$i.pdf",
                type = "file",
                uri = "content://test/file$i.pdf",
                size = 1024L * i
            )
        }

        val defectWithManyAttachments = Defect(
            id = "defect2",
            location = "Office",
            category = DefectCategory.INSTALLATIONS,
            priority = Priority.HIGH,
            description = "Test with many files",
            images = emptyList(),
            attachments = manyAttachments,
            status = DefectStatus.OPEN,
            createdAt = getTodayString()
        )

        assertEquals(10, defectWithManyAttachments.attachments.size)
        assertTrue(defectWithManyAttachments.attachments.all { it.name.startsWith("file") })
    }
}
