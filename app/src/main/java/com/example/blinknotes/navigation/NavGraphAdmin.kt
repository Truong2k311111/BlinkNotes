package com.example.blinknotes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.blinknotes.presentation.ui.Screens.admin.AdminDashboard
import com.example.blinknotes.presentation.ui.Screens.admin.UserManagement
import com.example.blinknotes.presentation.ui.Screens.admin.PostManagement
import com.example.blinknotes.presentation.ui.Screens.admin.ReportManagement

fun NavGraphBuilder.adminNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.ADMIN,
        startDestination = Screens.AdminDashboard.route
    ) {
        composable(Screens.AdminDashboard.route) {
            AdminDashboard(
                navController = navController,
            )
        }
        composable(Screens.AdminUserManagement.route) {
            UserManagement(
                navController = navController,
            )
        }
        composable(Screens.AdminPostManagement.route) {
            PostManagement(
                navController = navController,
            )
        }
        composable(Screens.AdminReportManagement.route) {
            ReportManagement(navController = navController)
        }
    }
} 