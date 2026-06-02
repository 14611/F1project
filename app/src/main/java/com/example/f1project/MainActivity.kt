package com.example.f1project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.f1project.ui.calendar.CalendarScreen
import com.example.f1project.ui.profile.ConstructorProfileScreen
import com.example.f1project.ui.profile.DriverProfileScreen
import com.example.f1project.ui.results.ResultsListScreen
import com.example.f1project.ui.results.ResultsScreen
import com.example.f1project.ui.season.SeasonViewModel
import com.example.f1project.ui.settings.SettingsScreen
import com.example.f1project.ui.standings.StandingsScreen
import com.example.f1project.ui.start.StartScreen
import com.example.f1project.ui.theme.F1ProjectTheme
import com.example.f1project.ui.theme.ThemeViewModel
import androidx.activity.enableEdgeToEdge

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Standings : Screen("standings")
    object Calendar : Screen("calendar")
    object ResultsList : Screen("results_list")
    object Settings : Screen("settings")
    object ResultsDetail : Screen("results/{season}/{round}/{sessionType}/{location}")
    object DriverProfile : Screen("driver/{driverId}/{season}")
    object ConstructorProfile : Screen("constructor/{constructorId}/{season}")
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Start, "Start", Icons.Default.Home),
    BottomNavItem(Screen.Standings, "Klasyfikacja", Icons.AutoMirrored.Filled.List), // Poprawione
    BottomNavItem(Screen.Calendar, "Kalendarz", Icons.Default.DateRange),
    BottomNavItem(Screen.ResultsList, "Wyniki", Icons.Default.CheckCircle),
    BottomNavItem(Screen.Settings, "Ustawienia", Icons.Default.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val seasonViewModel: SeasonViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            F1ProjectTheme(isDarkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val currentRoute = currentRoute(navController)
                val showBottomBar = bottomNavItems.any { it.screen.route == currentRoute }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                bottomNavItems.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = null) },
                                        label = { Text(item.label) },
                                        selected = currentRoute == item.screen.route,
                                        onClick = {
                                            if (currentRoute != item.screen.route) {
                                                navController.navigate(item.screen.route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            indicatorColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Start.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Start.route) {
                            StartScreen()
                        }
                        composable(Screen.Standings.route) {
                            StandingsScreen(
                                seasonViewModel = seasonViewModel,
                                onDriverClick = { driverId ->
                                    navController.navigate(
                                        "driver/$driverId/${seasonViewModel.selectedSeason.value}"
                                    )
                                },
                                onConstructorClick = { constructorId ->
                                    navController.navigate(
                                        "constructor/$constructorId/${seasonViewModel.selectedSeason.value}"
                                    )
                                }
                            )
                        }
                        composable(Screen.Calendar.route) {
                            CalendarScreen(seasonViewModel = seasonViewModel)
                        }
                        composable(Screen.ResultsList.route) {
                            ResultsListScreen(
                                seasonViewModel = seasonViewModel,
                                onSessionClick = { season, round, sessionType, location ->
                                    navController.navigate(
                                        "results/$season/$round/$sessionType/$location"
                                    )
                                }
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(themeViewModel = themeViewModel)
                        }
                        composable(
                            route = Screen.ResultsDetail.route,
                            arguments = listOf(
                                navArgument("season") { type = NavType.StringType },
                                navArgument("round") { type = NavType.StringType },
                                navArgument("sessionType") { type = NavType.StringType },
                                navArgument("location") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val season = backStackEntry.arguments?.getString("season") ?: ""
                            val round = backStackEntry.arguments?.getString("round") ?: ""
                            val sessionType = backStackEntry.arguments?.getString("sessionType") ?: ""
                            val location = backStackEntry.arguments?.getString("location") ?: ""
                            ResultsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                season = season,
                                round = round,
                                sessionType = sessionType,
                                location = location
                            )
                        }
                        composable(
                            route = Screen.DriverProfile.route,
                            arguments = listOf(
                                navArgument("driverId") { type = NavType.StringType },
                                navArgument("season") { type = NavType.StringType }
                            )
                        ) {
                            DriverProfileScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = Screen.ConstructorProfile.route,
                            arguments = listOf(
                                navArgument("constructorId") { type = NavType.StringType },
                                navArgument("season") { type = NavType.StringType }
                            )
                        ) {
                            ConstructorProfileScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}