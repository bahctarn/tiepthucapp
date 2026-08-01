package com.tiepthuc.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.tiepthuc.app.repository.AppRepository
import com.tiepthuc.app.ui.screens.*
import com.tiepthuc.app.viewmodel.*
import java.net.URLDecoder
import java.net.URLEncoder

private sealed class Dest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Pending : Dest("pending", "Món chờ", Icons.Filled.RestaurantMenu)
    object Tables : Dest("tables", "Bàn", Icons.Filled.TableBar)
    object History : Dest("history", "Lịch sử", Icons.Filled.History)
    object Settings : Dest("settings", "Cài đặt", Icons.Filled.Settings)
}

private val bottomDestinations = listOf(Dest.Pending, Dest.Tables, Dest.History, Dest.Settings)

@Composable
fun TiepThucNavHost(repository: AppRepository) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(repository)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                bottomDestinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Pending.route,
            modifier = androidx.compose.ui.Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Dest.Pending.route) {
                val vm: PendingViewModel = viewModel(factory = factory)
                PendingScreen(vm)
            }
            composable(Dest.Tables.route) {
                val vm: TablesViewModel = viewModel(factory = factory)
                TablesScreen(vm) { tableId, tableName ->
                    val encoded = URLEncoder.encode(tableName, "UTF-8")
                    navController.navigate("table_detail/$tableId/$encoded")
                }
            }
            composable(
                route = "table_detail/{tableId}/{tableName}",
                arguments = listOf(
                    navArgument("tableId") { type = NavType.LongType },
                    navArgument("tableName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getLong("tableId") ?: 0L
                val tableNameRaw = backStackEntry.arguments?.getString("tableName") ?: ""
                val tableName = URLDecoder.decode(tableNameRaw, "UTF-8")
                val vm: TableDetailViewModel = viewModel(factory = factory.tableDetailFactory(tableId))
                TableDetailScreen(tableName, vm) { navController.popBackStack() }
            }
            composable(Dest.History.route) {
                val vm: HistoryViewModel = viewModel(factory = factory)
                HistoryScreen(vm)
            }
            composable(Dest.Settings.route) {
                val vm: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(vm)
            }
        }
    }
}
