package com.example.blinknotes.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.blinknotes.ui.Auth.AuthViewModel
import com.example.blinknotes.ui.Auth.LoginScreen
import com.example.blinknotes.ui.Auth.PhoneAuthScreen
import com.example.blinknotes.ui.Auth.RegisterScreen

fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.AUTHENTICATION,
        startDestination = Screens.LoginScreen.route
    ) {
        composable(Screens.LoginScreen.route) {
            LoginScreen(
                authViewModel = AuthViewModel(),
                navController = navController,
            )
        }
        composable(Screens.RegisterScreen.route) {
            RegisterScreen(
                navController = navController,
                authViewModel = AuthViewModel()
            )
        }
        composable (Screens.PhoneAuthScreen.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType },
                navArgument("confirmPassword") { type = NavType.StringType }

            )
        ){ backStackEntry ->
        PhoneAuthScreen(
                navController = navController,
                authViewModel = AuthViewModel(),
            navBackStackEntry = backStackEntry
            )
        }
    }
}