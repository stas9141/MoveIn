package com.example.movein.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.movein.shared.data.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentDetailsScreen(
    onContinueClick: (UserData) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRooms by remember { mutableStateOf(setOf("Salon + Kitchen", "Mamad")) }
    var bedrooms by remember { mutableStateOf(1) }
    var bathrooms by remember { mutableStateOf(1) }
    var parking by remember { mutableStateOf(1) }
    var warehouse by remember { mutableStateOf(false) }
    var balconies by remember { mutableStateOf(1) }
    
    var showBedroomDialog by remember { mutableStateOf(false) }
    var showBathroomDialog by remember { mutableStateOf(false) }
    var showParkingDialog by remember { mutableStateOf(false) }
    var showBalconyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "Tell us about your apartment",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            // Spacer to balance the back button
            Spacer(modifier = Modifier.size(48.dp))
        }
        
        Text(
            text = "We'll create personalized checklists based on your apartment details",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Room Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Your Rooms",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Choose the rooms in your apartment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                val availableRooms = listOf(
                    "Salon + Kitchen",
                    "Mamad"
                )
                
                availableRooms.chunked(2).forEach { roomPair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roomPair.forEach { room ->
                            FilterChip(
                                onClick = {
                                    selectedRooms = if (selectedRooms.contains(room)) {
                                        selectedRooms - room
                                    } else {
                                        selectedRooms + room
                                    }
                                },
                                label = { Text(room) },
                                selected = selectedRooms.contains(room),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Add empty space if odd number of rooms
                        if (roomPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Number of Bedrooms
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Number of Bedrooms",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                ExposedDropdownMenuBox(
                    expanded = showBedroomDialog,
                    onExpandedChange = { showBedroomDialog = it },
                    modifier = Modifier.width(80.dp)
                ) {
                    OutlinedTextField(
                        value = "$bedrooms",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBedroomDialog) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showBedroomDialog,
                        onDismissRequest = { showBedroomDialog = false }
                    ) {
                        listOf(1, 2, 3, 4).forEach { bedroomCount ->
                            DropdownMenuItem(
                                text = { Text("$bedroomCount") },
                                onClick = {
                                    bedrooms = bedroomCount
                                    showBedroomDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Number of Bathrooms
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Number of Bathrooms",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                ExposedDropdownMenuBox(
                    expanded = showBathroomDialog,
                    onExpandedChange = { showBathroomDialog = it },
                    modifier = Modifier.width(80.dp)
                ) {
                    OutlinedTextField(
                        value = "$bathrooms",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBathroomDialog) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showBathroomDialog,
                        onDismissRequest = { showBathroomDialog = false }
                    ) {
                        listOf(1, 2, 3).forEach { bathroomCount ->
                            DropdownMenuItem(
                                text = { Text("$bathroomCount") },
                                onClick = {
                                    bathrooms = bathroomCount
                                    showBathroomDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Number of Balconies
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Number of Balconies",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                ExposedDropdownMenuBox(
                    expanded = showBalconyDialog,
                    onExpandedChange = { showBalconyDialog = it },
                    modifier = Modifier.width(80.dp)
                ) {
                    OutlinedTextField(
                        value = "$balconies",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBalconyDialog) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showBalconyDialog,
                        onDismissRequest = { showBalconyDialog = false }
                    ) {
                        listOf(1, 2, 3).forEach { balconyCount ->
                            DropdownMenuItem(
                                text = { Text("$balconyCount") },
                                onClick = {
                                    balconies = balconyCount
                                    showBalconyDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Number of Parking Spaces
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Number of Parking Spaces",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                ExposedDropdownMenuBox(
                    expanded = showParkingDialog,
                    onExpandedChange = { showParkingDialog = it },
                    modifier = Modifier.width(80.dp)
                ) {
                    OutlinedTextField(
                        value = "$parking",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showParkingDialog) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showParkingDialog,
                        onDismissRequest = { showParkingDialog = false }
                    ) {
                        listOf(1, 2, 3).forEach { parkingCount ->
                            DropdownMenuItem(
                                text = { Text("$parkingCount") },
                                onClick = {
                                    parking = parkingCount
                                    showParkingDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Machsan (Storage Room)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Machsan (Storage Room)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Do you have a storage room?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = warehouse,
                    onCheckedChange = { warehouse = it }
                )
            }
        }

        // Continue Button
        Button(
            onClick = {
                val userData = UserData(
                    rooms = selectedRooms.size + bedrooms,
                    selectedRoomNames = selectedRooms.toList() + (1..bedrooms).map { "Bedroom $it" },
                    bathrooms = bathrooms,
                    parking = parking,
                    warehouse = warehouse,
                    balconies = balconies
                )
                onContinueClick(userData)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        

    }
}
