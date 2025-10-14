package com.example.movein.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
// Use widely available icons to avoid unresolved refs
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movein.utils.ImageUtils
import com.example.movein.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageAttachButton(
    onImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    currentCount: Int = 0,
    maxCount: Int = 10
) {
    val context = LocalContext.current
    var showSheet by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* handled inline */ }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        showSheet = false
        if (success) pendingCameraUri?.let { onImageSelected(it.toString()) }
        pendingCameraUri = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        showSheet = false
        uris?.forEach { uri -> if (uri != null) onImageSelected(uri.toString()) }
    }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { showSheet = true },
            enabled = currentCount < maxCount,
            modifier = Modifier.height(40.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Attach Images", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentCount < maxCount) "Attach ($currentCount/$maxCount)" else "Limit reached ($currentCount/$maxCount)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        if (showSheet) {
            ModalBottomSheet(onDismissRequest = { showSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Attach images", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Choose a source. You can select multiple from gallery.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ListItem(
                        headlineContent = { Text("Take a photo") },
                        supportingContent = { Text("Open camera to capture a new photo") },
                        leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showSheet = false
                            if (PermissionUtils.hasCameraPermission(context)) {
                                try {
                                    val imageFile = ImageUtils.createImageFile(context)
                                    val imageUri = ImageUtils.getImageUri(context, imageFile)
                                    pendingCameraUri = imageUri
                                    cameraLauncher.launch(imageUri)
                                } catch (_: Exception) { /* ignore */ }
                            } else {
                                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                            }
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Choose from gallery") },
                        supportingContent = { Text("Pick one or more images from your library") },
                        leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                        modifier = Modifier.clickable {
                            if (PermissionUtils.hasStoragePermission(context)) {
                                galleryLauncher.launch("image/*")
                            } else {
                                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                                } else {
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                                permissionLauncher.launch(permissions)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
