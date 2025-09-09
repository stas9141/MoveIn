package com.example.movein.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun DatePickerDialog(
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select Due Date"
) {
    var currentMonth by remember { mutableStateOf(0) } // 0 = current month
    var selectedLocalDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // Initialize selected date if provided
    LaunchedEffect(selectedDate) {
        selectedDate?.let { dateString ->
            selectedLocalDate = parseDate(dateString)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Quick selection buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { 
                            selectedLocalDate = LocalDate.now()
                            onDateSelected(formatDate(LocalDate.now()))
                            onDismiss()
                        }
                    ) {
                        Text("Today")
                    }
                    
                    OutlinedButton(
                        onClick = { 
                            selectedLocalDate = LocalDate.now().plusDays(1)
                            onDateSelected(formatDate(LocalDate.now().plusDays(1)))
                            onDismiss()
                        }
                    ) {
                        Text("Tomorrow")
                    }
                    
                    OutlinedButton(
                        onClick = { 
                            selectedLocalDate = LocalDate.now().plusWeeks(1)
                            onDateSelected(formatDate(LocalDate.now().plusWeeks(1)))
                            onDismiss()
                        }
                    ) {
                        Text("Next Week")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calendar picker
                CalendarPicker(
                    currentMonth = currentMonth,
                    selectedDate = selectedLocalDate,
                    onDateClick = { date ->
                        selectedLocalDate = date
                        onDateSelected(formatDate(date))
                        onDismiss()
                    },
                    onMonthChange = { currentMonth = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Remove date button
                OutlinedButton(
                    onClick = { 
                        selectedLocalDate = null
                        onDateSelected(null)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("No Due Date")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CalendarPicker(
    currentMonth: Int,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    onMonthChange: (Int) -> Unit
) {
    val targetDate = LocalDate.now().plusMonths(currentMonth.toLong())
    val firstDayOfMonth = targetDate.withDayOfMonth(1)
    val lastDayOfMonth = targetDate.withDayOfMonth(targetDate.lengthOfMonth())
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Convert to 0-6 (Sunday = 0)
    
    Column {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth - 1) }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous month")
            }
            
            Text(
                text = targetDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            IconButton(onClick = { onMonthChange(currentMonth + 1) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next month")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Calendar days
        val weeks = mutableListOf<List<LocalDate?>>()
        var currentWeek = mutableListOf<LocalDate?>()
        
        // Add empty cells for days before the first day of the month
        repeat(firstDayOfWeek) {
            currentWeek.add(null) // Placeholder for empty cells
        }
        
        // Add all days of the month
        for (day in 1..targetDate.lengthOfMonth()) {
            currentWeek.add(targetDate.withDayOfMonth(day))
            
            if (currentWeek.size == 7) {
                weeks.add(currentWeek)
                currentWeek = mutableListOf()
            }
        }
        
        // Add remaining empty cells for the last week
        while (currentWeek.size < 7 && currentWeek.isNotEmpty()) {
            currentWeek.add(null)
        }
        if (currentWeek.isNotEmpty()) {
            weeks.add(currentWeek)
        }
        
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    CalendarDay(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        onClick = { if (date != null) onDateClick(date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val borderColor = if (isToday && !isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (borderColor != Color.Transparent) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// Helper functions
private fun parseDate(dateString: String): LocalDate? {
    return try {
        val parts = dateString.split("/")
        if (parts.size == 3) {
            val month = parts[0].toInt()
            val day = parts[1].toInt()
            val year = parts[2].toInt()
            LocalDate.of(year, month, day)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.monthValue}/${date.dayOfMonth}/${date.year}"
}

