package com.milkhisab.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.milkhisab.app.ui.addedit.AddEditScreen
import com.milkhisab.app.ui.home.HomeScreen
import com.milkhisab.app.ui.records.RecordsScreen
import com.milkhisab.app.ui.settings.SettingsScreen
import com.milkhisab.app.ui.strings.LocalStrings
import com.milkhisab.app.ui.summary.SummaryScreen

/** Route table - the only place routes are defined. */
object Routes {
    const val HOME = "home"
    const val RECORDS = "records"
    const val SUMMARY = "summary"
    const val SETTINGS = "settings"
    const val ADD = "add"
    const val EDIT = "edit/{recordId}"

    fun edit(recordId: Long) = "edit/$recordId"
}

private data class BottomTab(
    val route: String,
    val icon: ImageVector
)

private val bottomTabRoutes = listOf(
    BottomTab(Routes.HOME, Icons.Filled.Home),
    BottomTab(Routes.RECORDS, Icons.AutoMirrored.Filled.ReceiptLong),
    BottomTab(Routes.SUMMARY, Icons.Filled.CalendarMonth)
)

/**
 * Bottom navigation with exactly three destinations: गृह, रेकर्ड, हिसाब.
 *
 * Settings is deliberately *not* a fourth tab - it lives behind the icon
 * in the Home header, so the bar stays short and the three main jobs stay
 * one tap away. Labels are always visible (icon-only would confuse parents).
 */
@Composable
fun MilkBottomBar(navController: NavHostController) {
    val s = LocalStrings.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Hidden on the full-screen destinations (add/edit, settings).
    if (currentRoute !in setOf(Routes.HOME, Routes.RECORDS, Routes.SUMMARY)) return

    val labels = mapOf(
        Routes.HOME to s.navHome,
        Routes.RECORDS to s.navRecords,
        Routes.SUMMARY to s.navSummary
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomTabRoutes.forEach { tab ->
            val selected = currentRoute == tab.route
            val label = labels.getValue(tab.route)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        // The label below already names the destination.
                        contentDescription = null
                    )
                },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * The whole navigation graph.
 *
 * Back behaviour:
 *  - add/edit -> previous screen (home or records)
 *  - settings -> back to where it was opened from
 *  - tabs keep their own state
 */
@Composable
fun MilkNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onMessage: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddToday = { navController.navigate(Routes.ADD) },
                onEditToday = { recordId -> navController.navigate(Routes.edit(recordId)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.RECORDS) {
            RecordsScreen(
                onAddRecord = { navController.navigate(Routes.ADD) },
                onEditRecord = { recordId -> navController.navigate(Routes.edit(recordId)) },
                onMessage = onMessage
            )
        }

        composable(Routes.SUMMARY) {
            SummaryScreen(
                onMessage = onMessage,
                onOpenRecords = { navController.navigate(Routes.RECORDS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onMessage = onMessage
            )
        }

        composable(Routes.ADD) {
            AddEditScreen(
                recordId = null,
                onDone = { navController.popBackStack() },
                onMessage = onMessage
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) { entry ->
            val recordId = entry.arguments?.getLong("recordId")
            AddEditScreen(
                recordId = recordId,
                onDone = { navController.popBackStack() },
                onMessage = onMessage
            )
        }
    }
}
