package com.example.movein.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("Images")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
    
    fun getImageUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}

@Composable
fun rememberCameraLauncher(
    onImageCaptured: (String) -> Unit = {},
    onError: () -> Unit = {}
): MutableState<Boolean> {
    val isCameraLaunched = remember { mutableStateOf(false) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        isCameraLaunched.value = false
        if (success) {
            // The image was captured successfully
            // The file path will be handled by the caller
            onImageCaptured("Camera_${System.currentTimeMillis()}.jpg")
        } else {
            onError()
        }
    }
    
    return isCameraLaunched
}

@Composable
fun rememberGalleryLauncher(
    onImageSelected: (String) -> Unit = {},
    onError: () -> Unit = {}
): MutableState<Boolean> {
    val isGalleryLaunched = remember { mutableStateOf(false) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        isGalleryLaunched.value = false
        if (uri != null) {
            // The image was selected successfully
            onImageSelected("Gallery_${System.currentTimeMillis()}.jpg")
        } else {
            onError()
        }
    }
    
    return isGalleryLaunched
}
