# File Persistence Fix - Implementation Summary

## Problem
Attached images and files disappear after app restart because the app stores temporary content URIs (like `content://`) which become invalid after restart.

## Solution
Implement a file persistence system that copies files from temporary URIs to the app's internal storage.

## Changes Made

### 1. Created FileManager.kt ✅
- Location: `app/src/main/java/com/example/movein/utils/FileManager.kt`
- Handles copying files from temporary URIs to permanent internal storage
- Provides methods for:
  - `persistFile()` - Copy file to internal storage
  - `getFileUri()` - Get URI for persisted file
  - `deleteFile()` - Delete persisted file
  - `cleanupOrphanedFiles()` - Remove unused files

### 2. Updated AppState.kt ✅
- Added `fileManager` parameter to constructor
- Added file persistence helper functions:
  - `persistFileAttachment()`
  - `getFileUri()`
  - `deleteFileAttachment()`
  - `cleanupOrphanedFiles()`

### 3. Updated MainActivity.kt ✅
- Created FileManager instance
- Passed fileManager to AppState
- Passed appState to TaskDetailScreen

### 4. Updated TaskDetailScreen.kt ✅
- Added `appState` parameter
- Updated camera, gallery, and file launchers to use `appState.persistFileAttachment()`
- Files are now copied to internal storage before being added to tasks

## Still TODO

### 5. Update DefectDetailScreen.kt
- Add `appState` parameter
- Update camera, gallery, and file launchers to use file persistence
- Update MainActivity to pass appState to DefectDetailScreen

### 6. Update AddEditDefectScreen.kt
- Add `appState` parameter  
- Update camera, gallery, and file launchers to use file persistence
- Update MainActivity to pass appState to AddEditDefectScreen

### 7. Update FileReviewUtils.kt
- Handle both content URIs and file paths
- Update `openFileWithExternalApp()` to work with file:// URIs
- Update `rememberImageBitmap()` to work with file:// URIs

### 8. Testing
- Test adding images from camera
- Test adding images from gallery
- Test adding files
- Test app restart - files should persist
- Test file deletion
- Test file preview/open

## How It Works

**Before:**
1. User selects file → Get temporary URI (`content://...`)
2. Store URI in FileAttachment
3. App restart → URI invalid → File gone ❌

**After:**
1. User selects file → Get temporary URI
2. Copy file to internal storage → Get permanent path
3. Store permanent path in FileAttachment
4. App restart → File still accessible ✅

## File Storage Location
Files are stored in: `{app_internal_storage}/attachments/`
Each file is renamed with a UUID to avoid conflicts.


