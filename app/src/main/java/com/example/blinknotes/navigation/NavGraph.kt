package com.example.blinknotes.navigation

import SearchScreen
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.blinknotes.ui.addPhoto.AddPhotoScreen
import com.example.blinknotes.ui.admin.AdminDashboard
import com.example.blinknotes.ui.admin.PostManagement
import com.example.blinknotes.ui.admin.ReportManagement
import com.example.blinknotes.ui.admin.UserManagement
import com.example.blinknotes.ui.detaill.DetaillScreen
import com.example.blinknotes.ui.home.HomeScreen
import com.example.blinknotes.ui.home.HomeScreenViewModel
import com.example.blinknotes.ui.notify.ChatScreen
import com.example.blinknotes.ui.notify.DetailImageScreen
import com.example.blinknotes.ui.notify.NotifyScreen
import com.example.blinknotes.ui.notify.NotifyViewModel
import com.example.blinknotes.ui.notify.SearchNotifyScreen
import com.example.blinknotes.ui.notify.StatusScreen
import com.example.blinknotes.ui.notify.activity.ActivityNotificationScreen
import com.example.blinknotes.ui.notify.notificationSysTem.SystemNotificationScreen
import com.example.blinknotes.ui.profile.EditCoverImageScreen
import com.example.blinknotes.ui.profile.EditProfileImageScreen
import com.example.blinknotes.ui.profile.FollowingAndFollowerScreen
import com.example.blinknotes.ui.profile.ProfileScreen
import com.example.blinknotes.ui.profile.ViewCoverImageScreen
import com.example.blinknotes.ui.profile.settingProfile.LinkScreen
import com.example.blinknotes.ui.profile.settingProfile.PrivacySettingsScreen
import com.example.blinknotes.ui.profile.settingProfile.SettingScreenProfile


fun NavGraphBuilder.navGraph(navController: NavHostController, modifier: Any,
                             viewModel: HomeScreenViewModel
) {
    navigation(
        route = Graph.HOME,
        startDestination = Screens.HomeScreen.route,
    ) {
        composable(route = Screens.HomeScreen.route,
        ) {backStackEntry ->
            HomeScreen(navController = navController, viewmodel = viewModel)
        }
        composable(route = Screens.ProfileScreen.route +"/{userId}") {
                backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(navController = navController, guestId = userId)
        }
        composable(route = Screens.SettingScreenProfile.route) {
            SettingScreenProfile(navController = navController)
        }
        composable(route = Screens.ProfileScreen.route) {
            ProfileScreen(navController = navController)
        }
        composable(
            route = Screens.SearchScreen.route ,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchScreen(navController = navController, context = context, initialQuery = query)
        }
        composable(route = Screens.NotifyScreen.route) {
            NotifyScreen( navController = navController,)
        }
        detailsNavGraph(navController)
        composable(
            route = Screens.AddPhotoScreen.route ,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            AddPhotoScreen(navController = navController)
        }
        composable(route = Screens.AdminDashboard.route) {
            val context = LocalContext.current
            AdminDashboard(navController = navController, context = context)
        }
        composable(route = Screens.AdminUserManagement.route) {
            val context = LocalContext.current
            UserManagement(navController, context = context)
        }
        composable(route = Screens.AdminPostManagement.route) {
            val context = LocalContext.current
            PostManagement(navController, context = context)
        }
        composable(route = Screens.AdminReportManagement.route) {
            ReportManagement(navController)
        }
        composable(route = Screens.SystemNotification.route) {
            SystemNotificationScreen(
                navController = navController)
        }
        composable(
            route = Screens.ChatScreen.route+"/{username}/{avatarRes}/{hasMoment}/{isMomentSeen}/{isOnline}/{userId}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("avatarRes") { type = NavType.StringType },
                navArgument("hasMoment") { type = NavType.BoolType },
                navArgument("isMomentSeen") { type = NavType.BoolType },
                navArgument("isOnline") { type = NavType.BoolType },
                navArgument("userId") { type = NavType.StringType }
            )
        )
        { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val avatarRes = backStackEntry.arguments?.getString("avatarRes") ?: ""
            val hasMoment = backStackEntry.arguments?.getBoolean("hasMoment") ?: false
            val isMomentSeen = backStackEntry.arguments?.getBoolean("isMomentSeen") ?: false
            val isOnline = backStackEntry.arguments?.getBoolean("isOnline") ?: false
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
             ChatScreen(navController = navController, username = username, avatarRes = avatarRes, hasMoment = hasMoment, isMomentSeen = isMomentSeen, isOnline = isOnline, userOtherId =userId )
        }
        composable(route = Screens.SearchNotifyScreen.route) {
            val notifyViewModel: NotifyViewModel = viewModel()
            SearchNotifyScreen(
                navController = navController,
                viewModel = notifyViewModel
            )
        }
        composable(
            route = Screens.DetailImageScreen.route + "/{imageUrl}",
            arguments = listOf(navArgument("imageUrl") { type = NavType.StringType }
        )
        )
        { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            DetailImageScreen(
                imageUrl = imageUrl,
                onBack =  { navController.popBackStack() },
            )
        }
        composable( route = Screens.StatusScreen.route) {
            StatusScreen(navController = navController)
        }
        composable( route = Screens.LinkScreen.route) {
            LinkScreen(
                viewModel = viewModel(),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable( route = Screens.PrivacySettingsScreen.route) {
            PrivacySettingsScreen(
                viewModel = viewModel(),
              navController = navController
            )
        }
        composable(
            route = Screens.EditProfileImageScreen.route,
            arguments = listOf(
                navArgument("currentImageUrl") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val currentImageUrl = backStackEntry.arguments?.getString("currentImageUrl") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            EditProfileImageScreen(
                currentImageUrl = currentImageUrl,
                onDismiss = { navController.navigateUp() },
                onImageUpdated = { newImageUrl ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("updatedImageUrl", newImageUrl)
                    navController.navigateUp()
                },
                userId = userId
            )
        }
        composable("edit_cover_image_screen/{currentCoverUrl}") { backStackEntry ->
            val currentCoverUrl = backStackEntry.arguments?.getString("currentCoverUrl") ?: ""
            EditCoverImageScreen(
                navController = navController,
                viewModel = viewModel(),
                currentCoverUrl = currentCoverUrl
            )
        }
        composable(
            route = Screens.ViewCoverImageScreen.route,
            arguments = listOf(
                navArgument("imageUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            ViewCoverImageScreen(
                navController = navController,
                imageUrl = imageUrl
            )
        }
        composable(route = Screens.FollowingAndFollowerScreen.route + "/{userId}" ){
            val userId = it.arguments?.getString("userId") ?: ""
            FollowingAndFollowerScreen(navController = navController, userId = userId)
        }
        composable(route = Screens.ActivityNotificationScreen.route) {
            ActivityNotificationScreen(navController = navController)
        }
    }
}
fun NavGraphBuilder.detailsNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.DETAILS,
        startDestination = Screens.DetaillScreen.route

    ) {
        composable(route = Screens.DetaillScreen.route + "/{postId}/{userId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val notifyViewModel: NotifyViewModel = viewModel()
            DetaillScreen(navController = navController, postId = postId, userId = userId,
                viewModelNotify = notifyViewModel
            )
        }
    }
}
