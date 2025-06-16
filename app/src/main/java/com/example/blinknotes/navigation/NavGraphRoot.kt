package com.example.blinknotes.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import navGraph
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RootNavigationGraph(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userSignedIn = currentUser != null

    val homeRoutes = listOf(
        Screens.HomeScreen.route,
        Screens.ProfileScreen.route,
        Screens.NotifyScreen.route,
    )
    val bottomNavigationItems = listOf(
        NavigationItem(Icons.Filled.Home,Icons.Outlined.Home, Screens.HomeScreen.route),
        NavigationItem(Icons.Filled.Search,Icons.Outlined.Search,Screens.SearchScreen.route),
        NavigationItem(Icons.Filled.Add,Icons.Outlined.Add,Screens.AddPhotoScreen.route),
        NavigationItem(Icons.Filled.Notifications,Icons.Outlined.Notifications, Screens.NotifyScreen.route),
        NavigationItem(Icons.Filled.Person,Icons.Outlined.Person, Screens.ProfileScreen.route),
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in homeRoutes) {
                BottomNavigationBar(navController = navController, items = bottomNavigationItems)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues = paddingValues)) {
            NavHost(
                navController = navController,
                route = Graph.ROOT,
                startDestination = Graph.HOME,
            ) {
                authNavGraph(navController)
                adminNavGraph(navController)
                navGraph(
                    navController, 
                    modifier = paddingValues,
                )
            }
        }
    }
}
object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val HOME = "home_graph"
    const val DETAILS = "details_graph"
    const val ADMIN = "admin_graph"
}