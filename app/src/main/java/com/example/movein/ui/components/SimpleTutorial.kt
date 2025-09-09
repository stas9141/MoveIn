package com.example.movein.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SimpleTutorialDialog(
    isVisible: Boolean,
    title: String,
    description: String,
    onClose: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Tutorial",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Got it!")
                    }
                }
            }
        }
    }
}

@Composable
fun rememberSimpleTutorialState() = remember { SimpleTutorialState() }

class SimpleTutorialState {
    var isVisible by mutableStateOf(false)
        private set
    
    var title by mutableStateOf("")
        private set
        
    var description by mutableStateOf("")
        private set
    
    fun showTutorial(tutorialTitle: String, tutorialDescription: String) {
        title = tutorialTitle
        description = tutorialDescription
        isVisible = true
    }
    
    fun closeTutorial() {
        isVisible = false
    }
}
