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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun EnhancedDatePicker(
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select Due Date"
) {
    var showFullCalendar by remember { mutableStateOf(false) }
    
    // Full-screen calendar modal
    if (showFullCalendar) {
        FullScreenCalendar(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                onDateSelected(date)
                onDismiss()
            },
            onDismiss = { showFullCalendar = false }
        )
    } else {
        // Bottom sheet style dialog
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    
                    DatePickerBottomSheet(
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
                        onDismiss = onDismiss,
                        onShowFullCalendar = { showFullCalendar = true },
                        title = title
                    )
                }
            }
        }
    }
}

@Composable
private fun DatePickerBottomSheet(
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    onShowFullCalendar: () -> Unit,
    title: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Select Buttons
        Text(
            text = "Quick Select",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today
            QuickSelectButton(
                text = "Today",
                subtitle = formatDateForDisplay(LocalDate.now()),
                isSelected = isDateSelected(selectedDate, LocalDate.now()),
                onClick = { onDateSelected(formatDate(LocalDate.now())) }
            )
            
            // Tomorrow
            QuickSelectButton(
                text = "Tomorrow",
                subtitle = formatDateForDisplay(LocalDate.now().plusDays(1)),
                isSelected = isDateSelected(selectedDate, LocalDate.now().plusDays(1)),
                onClick = { onDateSelected(formatDate(LocalDate.now().plusDays(1))) }
            )
            
            // This Weekend
            val thisWeekend = getThisWeekend()
            QuickSelectButton(
                text = "This Weekend",
                subtitle = formatDateForDisplay(thisWeekend),
                isSelected = isDateSelected(selectedDate, thisWeekend),
                onClick = { onDateSelected(formatDate(thisWeekend)) }
            )
            
            // Next Week
            QuickSelectButton(
                text = "Next Week",
                subtitle = formatDateForDisplay(LocalDate.now().plusWeeks(1)),
                isSelected = isDateSelected(selectedDate, LocalDate.now().plusWeeks(1)),
                onClick = { onDateSelected(formatDate(LocalDate.now().plusWeeks(1))) }
            )
            
            // No Due Date
            QuickSelectButton(
                text = "No Due Date",
                subtitle = "Remove due date",
                isSelected = selectedDate == null,
                onClick = { onDateSelected(null) }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Custom Date Selection Button
        OutlinedButton(
            onClick = onShowFullCalendar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Choose Custom Date",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuickSelectButton(
    text: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenCalendar(
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(0) } // 0 = current month
    var selectedLocalDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // Initialize selected date if provided
    LaunchedEffect(selectedDate) {
        selectedDate?.let { dateString ->
            selectedLocalDate = parseDate(dateString)
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    
                    Text(
                        text = "Select Date",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.size(48.dp)) // Balance the close button
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calendar
                CalendarPicker(
                    currentMonth = currentMonth,
                    selectedDate = selectedLocalDate,
                    onDateClick = { date ->
                        selectedLocalDate = date
                        onDateSelected(formatDate(date))
                    },
                    onMonthChange = { currentMonth = it }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDateSelected(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("No Due Date")
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            
            IconButton(onClick = { onMonthChange(currentMonth + 1) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next month")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
            Spacer(modifier = Modifier.height(4.dp))
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
            .padding(4.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (borderColor != Color.Transparent) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 16.sp,
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

private fun formatDateForDisplay(date: LocalDate): String {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    
    return when {
        date == today -> "Today"
        date == tomorrow -> "Tomorrow"
        else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

private fun isDateSelected(selectedDate: String?, targetDate: LocalDate): Boolean {
    return selectedDate == formatDate(targetDate)
}

private fun getThisWeekend(): LocalDate {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.value // 1 = Monday, 7 = Sunday
    
    return when {
        dayOfWeek <= 5 -> today.plusDays((6 - dayOfWeek).toLong()) // Next Saturday
        else -> today.plusDays(1L) // Next day if it's already weekend
    }
}
