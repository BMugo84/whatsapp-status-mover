package com.example.whatsappstatusmover

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object PhotoViewer : Screen("photos")
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(route = Screen.Main.route) {
            MainScreen(
                viewModel = viewModel,
                onViewPhotosClick = {
                    navController.navigate(Screen.PhotoViewer.route)
                }
            )
        }

        composable(route = Screen.PhotoViewer.route) {
            PhotoViewerScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}