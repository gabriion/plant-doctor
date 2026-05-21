package com.plantdoctor.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.plantdoctor.ui.camera.CameraScreen
import com.plantdoctor.ui.diagnosis.DiagnosisResultScreen
import com.plantdoctor.ui.history.HistoryScreen
import com.plantdoctor.ui.home.HomeScreen
import com.plantdoctor.ui.journal.AddEditPlantScreen
import com.plantdoctor.ui.journal.PlantDetailScreen
import com.plantdoctor.ui.journal.PlantListScreen
import com.plantdoctor.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object DiagnosisResult : Screen("diagnosis_result/{imageUri}") {
        fun createRoute(imageUri: String): String =
            "diagnosis_result/${Uri.encode(imageUri)}"
    }
    data object PlantList : Screen("plant_list")
    data object PlantDetail : Screen("plant_detail/{plantId}") {
        fun createRoute(plantId: Long): String = "plant_detail/$plantId"
    }
    data object AddEditPlant : Screen("add_edit_plant?plantId={plantId}") {
        fun createRoute(plantId: Long? = null): String =
            if (plantId != null) "add_edit_plant?plantId=$plantId" else "add_edit_plant"
    }
    data object History : Screen("history")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Camera, "Scan", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
    BottomNavItem(Screen.PlantList, "Journal", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    BottomNavItem(Screen.History, "History", Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun PlantDoctorNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                    onNavigateToDiagnosis = { diagnosisId ->
                        // Navigate to a specific diagnosis - reuse result screen
                    },
                    onNavigateToPlant = { plantId ->
                        navController.navigate(Screen.PlantDetail.createRoute(plantId))
                    }
                )
            }

            composable(Screen.Camera.route) {
                CameraScreen(
                    onImageCaptured = { uri ->
                        navController.navigate(Screen.DiagnosisResult.createRoute(uri))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.DiagnosisResult.route,
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType }
                )
            ) {
                DiagnosisResultScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.PlantList.route) {
                PlantListScreen(
                    onNavigateToPlant = { plantId ->
                        navController.navigate(Screen.PlantDetail.createRoute(plantId))
                    },
                    onNavigateToAddPlant = {
                        navController.navigate(Screen.AddEditPlant.createRoute())
                    }
                )
            }

            composable(
                route = Screen.PlantDetail.route,
                arguments = listOf(
                    navArgument("plantId") { type = NavType.LongType }
                )
            ) {
                PlantDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { plantId ->
                        navController.navigate(Screen.AddEditPlant.createRoute(plantId))
                    },
                    onNavigateToDiagnosis = { imageUri ->
                        navController.navigate(Screen.DiagnosisResult.createRoute(imageUri))
                    }
                )
            }

            composable(
                route = Screen.AddEditPlant.route,
                arguments = listOf(
                    navArgument("plantId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                AddEditPlantScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateToDiagnosis = { imageUri ->
                        navController.navigate(Screen.DiagnosisResult.createRoute(imageUri))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
