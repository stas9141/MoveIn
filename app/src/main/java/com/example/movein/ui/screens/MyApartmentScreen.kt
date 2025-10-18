package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.movein.shared.data.UserData

@Composable
fun MyApartmentScreen(
    userData: UserData?,
    hasCompletedOnboarding: Boolean = false,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveChanges: (UserData) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedUserData by remember { mutableStateOf(userData) }
    
    // Editing state variables
    var editedRooms by remember { mutableStateOf(userData?.rooms ?: 0) }
    var editedBathrooms by remember { mutableStateOf(userData?.bathrooms ?: 0) }
    var editedParking by remember { mutableStateOf(userData?.parking ?: 0) }
    var editedWarehouse by remember { mutableStateOf(userData?.warehouse ?: false) }
    var editedGarden by remember { mutableStateOf(userData?.garden ?: false) }
    var editedBalconies by remember { mutableStateOf(userData?.balconies ?: 0) }
    
    // Update editedUserData when userData changes
    LaunchedEffect(userData) {
        editedUserData = userData
        userData?.let {
            editedRooms = it.rooms
            editedBathrooms = it.bathrooms
            editedParking = it.parking
            editedWarehouse = it.warehouse
            editedGarden = it.garden
            editedBalconies = it.balconies
        }
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "My Apartment",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            
            if (isEditing) {
                // Save and Cancel buttons when editing
                Row {
                    IconButton(onClick = {
                        // Save changes
                        userData?.let { originalData ->
                            val updatedData = originalData.copy(
                                rooms = editedRooms,
                                bathrooms = editedBathrooms,
                                parking = editedParking,
                                warehouse = editedWarehouse,
                                garden = editedGarden,
                                balconies = editedBalconies
                            )
                            onSaveChanges(updatedData)
                        }
                        isEditing = false
                    }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save Changes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        // Cancel editing - reset to original data
                        userData?.let {
                            editedRooms = it.rooms
                            editedBathrooms = it.bathrooms
                            editedParking = it.parking
                            editedWarehouse = it.warehouse
                            editedGarden = it.garden
                            editedBalconies = it.balconies
                        }
                        isEditing = false
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                IconButton(onClick = {
                    isEditing = true
                }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Apartment Details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        if (editedUserData != null && hasCompletedOnboarding) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Apartment Overview Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Apartment",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Your Apartment Details",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "All the information you provided during setup",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Rooms Information
                item {
                    if (isEditing) {
                        EditableApartmentCard(
                            title = "Rooms",
                            icon = Icons.Default.Person,
                            content = {
                                OutlinedTextField(
                                    value = editedRooms.toString(),
                                    onValueChange = { value ->
                                        editedRooms = value.toIntOrNull() ?: 0
                                    },
                                    label = { Text("Number of Rooms") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                    } else {
                        ApartmentDetailCard(
                            title = "Rooms",
                            icon = Icons.Default.Person,
                            details = listOf(
                                "Total Rooms: ${editedUserData!!.rooms}",
                                "Selected Rooms: ${editedUserData!!.selectedRoomNames.joinToString(", ")}"
                            )
                        )
                    }
                }
                
                // Bathrooms Information
                item {
                    if (isEditing) {
                        EditableApartmentCard(
                            title = "Bathrooms",
                            icon = Icons.Default.Star,
                            content = {
                                OutlinedTextField(
                                    value = editedBathrooms.toString(),
                                    onValueChange = { value ->
                                        editedBathrooms = value.toIntOrNull() ?: 0
                                    },
                                    label = { Text("Number of Bathrooms") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                    } else {
                        ApartmentDetailCard(
                            title = "Bathrooms",
                            icon = Icons.Default.Star,
                            details = listOf("Number of Bathrooms: ${editedUserData!!.bathrooms}")
                        )
                    }
                }
                
                // Parking Information
                item {
                    if (isEditing) {
                        EditableApartmentCard(
                            title = "Parking",
                            icon = Icons.Default.Star,
                            content = {
                                OutlinedTextField(
                                    value = editedParking.toString(),
                                    onValueChange = { value ->
                                        editedParking = value.toIntOrNull() ?: 0
                                    },
                                    label = { Text("Parking Spaces") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                    } else {
                        ApartmentDetailCard(
                            title = "Parking",
                            icon = Icons.Default.Star,
                            details = listOf("Parking Spaces: ${editedUserData!!.parking}")
                        )
                    }
                }
                
                // Additional Features
                item {
                    if (isEditing) {
                        EditableApartmentCard(
                            title = "Additional Features",
                            icon = Icons.Default.Star,
                            content = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = editedBalconies.toString(),
                                        onValueChange = { value ->
                                            editedBalconies = value.toIntOrNull() ?: 0
                                        },
                                        label = { Text("Number of Balconies") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Warehouse/Storage")
                                        Switch(
                                            checked = editedWarehouse,
                                            onCheckedChange = { editedWarehouse = it }
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Garden")
                                        Switch(
                                            checked = editedGarden,
                                            onCheckedChange = { editedGarden = it }
                                        )
                                    }
                                }
                            }
                        )
                    } else {
                        val additionalFeatures = mutableListOf<String>()
                        if (editedUserData!!.warehouse) additionalFeatures.add("Warehouse/Storage")
                        if (editedUserData!!.garden) additionalFeatures.add("Garden")
                        if (editedUserData!!.balconies > 0) additionalFeatures.add("Balconies: ${editedUserData!!.balconies}")
                        
                        if (additionalFeatures.isNotEmpty()) {
                            ApartmentDetailCard(
                                title = "Additional Features",
                                icon = Icons.Default.Star,
                                details = additionalFeatures
                            )
                        }
                    }
                }
                
                // Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Summary",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Text(
                                text = buildString {
                                    append("Your apartment has ${editedUserData!!.rooms} rooms")
                                    if (editedUserData!!.bathrooms > 0) append(", ${editedUserData!!.bathrooms} bathroom${if (editedUserData!!.bathrooms > 1) "s" else ""}")
                                    if (editedUserData!!.parking > 0) append(", ${editedUserData!!.parking} parking space${if (editedUserData!!.parking > 1) "s" else ""}")
                                    if (editedUserData!!.warehouse) append(", warehouse")
                                    if (editedUserData!!.garden) append(", garden")
                                    if (editedUserData!!.balconies > 0) append(", ${editedUserData!!.balconies} balcon${if (editedUserData!!.balconies > 1) "ies" else "y"}")
                                    append(".")
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // No data state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "No Data",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (hasCompletedOnboarding) "No Apartment Data" else "Complete Onboarding First",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (hasCompletedOnboarding) {
                            "Complete the onboarding process to see your apartment details here."
                        } else {
                            "Please complete the apartment setup process first to view and manage your apartment details."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ApartmentDetailCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    details: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            
            details.forEach { detail ->
                Text(
                    text = "• $detail",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun EditableApartmentCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            
            content()
        }
    }
}
