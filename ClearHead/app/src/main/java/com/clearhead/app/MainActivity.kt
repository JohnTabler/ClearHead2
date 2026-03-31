package com.clearhead.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.clearhead.app.ui.screens.*
import com.clearhead.app.ui.theme.ClearHeadTheme
import com.clearhead.app.viewmodel.ClearHeadViewModel

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Log      : Screen("log",      "Log",      Icons.Filled.Edit,           Icons.Outlined.Edit)
    object History  : Screen("history",  "History",  Icons.Filled.CalendarMonth,  Icons.Outlined.CalendarMonth)
    object Analytics: Screen("analytics","Insights", Icons.Filled.Insights,       Icons.Outlined.Insights)
    object Export   : Screen("export",   "Export",   Icons.Filled.Download,       Icons.Outlined.Download)
}

val navItems = listOf(Screen.Log, Screen.History, Screen.Analytics, Screen.Export)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClearHeadTheme {
                ClearHeadApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearHeadApp() {
    val navController = rememberNavController()
    val vm: ClearHeadViewModel = viewModel()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("🌿", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "ClearHead",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Log.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Log.route) {
                LogEntryScreen(viewModel = vm)
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = vm,
                    onDateSelected = {
                        navController.navigate(Screen.Log.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = vm)
            }
            composable(Screen.Export.route) {
                ExportScreen(viewModel = vm)
            }
        }
    }
}
