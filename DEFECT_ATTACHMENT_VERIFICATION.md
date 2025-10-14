# Defect Attachment Feature Verification Guide

## Overview
This document outlines the manual verification steps to ensure defect file attachments work correctly across all user flows.

## Test Scenarios

### 1. Add New Defect with Attachments

**Steps:**
1. Navigate to Dashboard → Defects tab
2. Tap "Add New Defect" button
3. Fill in required fields (Location, Category, Priority, Description)
4. In the "Attachments" section:
   - Tap "Attach Files" button
   - Select "Attach File" from dialog
   - Choose a PDF/document from file picker
   - Verify file appears in attachments list
5. In the "Images" section:
   - Use existing image attachment functionality
   - Add a photo via camera or gallery
6. Tap "Save" button

**Expected Results:**
- ✅ Defect is created successfully
- ✅ File attachment appears in defect list
- ✅ Image attachment appears in defect list
- ✅ Both attachments persist after app restart

### 2. Edit Existing Defect - Add More Attachments

**Steps:**
1. Open an existing defect from the defects list
2. Tap "Edit" button (pencil icon)
3. In the "Attachments" section:
   - Tap "Add Attachment" button
   - Select "Take Photo" to add camera image
   - Select "Choose from Gallery" to add gallery image
   - Select "Attach File" to add document
4. Verify all attachments appear in the list
5. Tap "Save" button

**Expected Results:**
- ✅ New attachments are added to existing ones
- ✅ All attachments (old + new) are preserved
- ✅ Changes persist after saving

### 3. Review Attachments

**Steps:**
1. Open a defect with attachments
2. In the attachments list, tap on any attachment
3. Verify FileReviewDialog opens with:
   - File name and type displayed
   - File size shown
   - Preview (if supported file type)
   - Action buttons: "Open File", "Share", "Delete File"

**Expected Results:**
- ✅ FileReviewDialog displays correctly
- ✅ File information is accurate
- ✅ Preview works for supported file types
- ✅ All action buttons are functional

### 4. Delete Attachments

**Steps:**
1. Open a defect with attachments
2. In the attachments list, tap the delete (trash) icon on any attachment
3. Verify attachment is removed from the list
4. Alternatively, use FileReviewDialog:
   - Tap on attachment to open review dialog
   - Tap "Delete File" button
   - Confirm deletion in confirmation dialog
   - Verify attachment is removed

**Expected Results:**
- ✅ Attachment is immediately removed from UI
- ✅ Deletion persists after saving defect
- ✅ Other attachments remain intact

### 5. Share Attachments

**Steps:**
1. Open a defect with attachments
2. Tap on an attachment to open FileReviewDialog
3. Tap "Share" button
4. Verify system share dialog opens with the file

**Expected Results:**
- ✅ Share dialog opens with correct file
- ✅ File can be shared via email, messaging, etc.

### 6. Open Attachments

**Steps:**
1. Open a defect with attachments
2. Tap on an attachment to open FileReviewDialog
3. Tap "Open File" button
4. Verify external app opens with the file

**Expected Results:**
- ✅ Appropriate external app opens
- ✅ File opens correctly in the external app

### 7. Duplicate Defect with Attachments

**Steps:**
1. Open a defect with attachments
2. Tap "Duplicate" button (copy icon)
3. Verify new defect is created with:
   - Same attachments as original
   - "(Copy)" suffix in location
   - Status reset to "Open"

**Expected Results:**
- ✅ New defect created with all attachments
- ✅ Original defect remains unchanged
- ✅ Both defects can be edited independently

### 8. Delete Defect with Attachments

**Steps:**
1. Open a defect with attachments
2. Tap "Delete" button (trash icon)
3. Confirm deletion in dialog
4. Verify defect is removed from list

**Expected Results:**
- ✅ Defect is completely removed
- ✅ No orphaned attachments remain
- ✅ Defect list updates correctly

## Data Persistence Verification

### Storage Verification
1. Add attachments to defects
2. Force close the app
3. Reopen the app
4. Navigate to defects
5. Open defects with attachments

**Expected Results:**
- ✅ All attachments are preserved
- ✅ File paths and metadata are intact
- ✅ No data corruption or loss

### Cross-Session Verification
1. Create defects with attachments
2. Use the app for multiple sessions
3. Add/edit/delete attachments across sessions
4. Verify data consistency

**Expected Results:**
- ✅ Data remains consistent across sessions
- ✅ No memory leaks or performance issues
- ✅ All CRUD operations work reliably

## Performance Considerations

### Large Files
- Test with files > 10MB
- Verify UI remains responsive
- Check memory usage

### Many Attachments
- Test with 10+ attachments per defect
- Verify list scrolling performance
- Check save/load times

## Error Handling

### Network Issues
- Test with poor/no internet connection
- Verify offline functionality works
- Check sync behavior when connection restored

### File System Issues
- Test with corrupted files
- Test with missing files
- Verify graceful error handling

## Browser/Platform Compatibility

### File Types
- PDF documents
- Images (JPG, PNG, GIF)
- Text files
- Office documents
- Video files (if supported)

### File Sources
- Camera capture
- Gallery selection
- File manager selection
- External app sharing

## Success Criteria

All test scenarios should pass with:
- ✅ No crashes or ANRs
- ✅ Data persistence across app restarts
- ✅ Consistent UI behavior
- ✅ Proper error handling
- ✅ Good performance with large files
- ✅ Cross-platform compatibility

## Known Limitations

1. File size limits may apply based on device storage
2. Some file types may not be previewable
3. External app availability varies by device
4. Network-dependent features require connectivity

## Reporting Issues

When reporting issues, include:
- Device model and OS version
- File type and size
- Steps to reproduce
- Expected vs actual behavior
- Screenshots or logs if available
