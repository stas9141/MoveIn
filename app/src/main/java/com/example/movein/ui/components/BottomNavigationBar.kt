package com.example.movein.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.movein.navigation.Screen

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onNavigateTo: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        NavigationItem(
            screen = Screen.Dashboard,
            icon = Icons.Default.Home,
            label = "Dashboard"
        ),
        NavigationItem(
            screen = Screen.Calendar,
            icon = Icons.Default.DateRange,
            label = "Calendar"
        ),
        NavigationItem(
            screen = Screen.DefectList,
            icon = Icons.Default.Warning,
            label = "Defects"
        ),
        NavigationItem(
            screen = Screen.Settings,
            icon = Icons.Default.Settings,
            label = "Settings"
        )
    )
    
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = isCurrentScreen(currentScreen, item.screen),
                onClick = { onNavigateTo(item.screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

private fun isCurrentScreen(currentScreen: Screen, targetScreen: Screen): Boolean {
    return when {
        currentScreen is Screen.TaskDetail && targetScreen == Screen.Dashboard -> true
        currentScreen is Screen.DefectDetail && targetScreen == Screen.DefectList -> true
        currentScreen is Screen.AddEditDefect && targetScreen == Screen.DefectList -> true
        currentScreen is Screen.ReportConfiguration && targetScreen == Screen.Settings -> true
        else -> currentScreen == targetScreen
    }
}

data class NavigationItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)
