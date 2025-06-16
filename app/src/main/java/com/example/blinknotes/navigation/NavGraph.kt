import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.blinknotes.navigation.Graph
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.presentation.ui.Screens.home.HomeScreen
import com.example.blinknotes.presentation.viewModel.NotifyViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.blinknotes.presentation.ui.Screens.addPhoto.AddPhotoScreen
import com.example.blinknotes.presentation.ui.Screens.detaill.DetaillScreen
import com.example.blinknotes.presentation.ui.Screens.notify.ChatScreen
import com.example.blinknotes.presentation.ui.Screens.notify.DetailImageScreen
import com.example.blinknotes.presentation.ui.Screens.notify.NotifyScreen
import com.example.blinknotes.presentation.ui.Screens.notify.SearchNotifyScreen
import com.example.blinknotes.presentation.ui.Screens.notify.StatusScreen
import com.example.blinknotes.presentation.ui.Screens.notify.activity.ActivityNotificationScreen
import com.example.blinknotes.presentation.ui.Screens.notify.notificationSysTem.SystemNotificationScreen
import com.example.blinknotes.presentation.ui.Screens.profile.EditCoverImageScreen
import com.example.blinknotes.presentation.ui.Screens.profile.EditProfileImageScreen
import com.example.blinknotes.presentation.ui.Screens.profile.FollowingAndFollowerScreen
import com.example.blinknotes.presentation.ui.Screens.profile.ProfileScreen
import com.example.blinknotes.presentation.ui.Screens.profile.ViewCoverImageScreen
import com.example.blinknotes.presentation.ui.Screens.profile.settingProfile.LinkScreen
import com.example.blinknotes.presentation.ui.Screens.profile.settingProfile.PrivacySettingsScreen
import com.example.blinknotes.presentation.ui.Screens.profile.settingProfile.SettingScreenProfile


fun NavGraphBuilder.navGraph(navController: NavHostController, modifier: Any,
) {
    navigation(
        route = Graph.HOME,
        startDestination = Screens.HomeScreen.route,
    ) {
        composable(route = Screens.HomeScreen.route,
        ) {backStackEntry ->
            HomeScreen(navController = navController )
        }
        composable(route = Screens.ProfileScreen.route +"/{userId}") {
                backStackEntry ->
            // Lấy userId từ arguments
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            Log.d("Navigation", "Navigated to ProfileScreen with userId: $userId")
            // Gọi hàm getUser() để lấy thông tin người dùng
            ProfileScreen(navController = navController, guestId = userId)
        }
        composable(route = Screens.ProfileScreen.route) {
            ProfileScreen(navController = navController)
        }
        composable(
            route = Screens.SearchScreen.route,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchScreen(
                navController = navController,
                context = LocalContext.current,
                initialQuery = query
            )
        }
        composable(route = Screens.NotifyScreen.route) {
            NotifyScreen( navController = navController,)

        }
        detailsNavGraph(navController)
        // addPhotoNavGraph(navController)
        composable(
            route = Screens.AddPhotoScreen.route,
//            arguments = listOf(navArgument("imageUris") { type = NavType.StringType })
        ) {
//            backStackEntry ->
//            val imageUris = backStackEntry.arguments?.getString("imageUris")
            AddPhotoScreen(navController = navController)
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
                    // Quay lại màn hình trước và truyền URL mới
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("updatedImageUrl", newImageUrl)
                    navController.navigateUp()
                },
                userId = userId
            )
        }

        composable(route = Screens.SettingScreenProfile.route){
            SettingScreenProfile(navController = navController)
        }
        composable(route = Screens.FollowingAndFollowerScreen.route + "/{userId}" ){
              val userId = it.arguments?.getString("userId") ?: ""
            FollowingAndFollowerScreen(navController = navController, userId = userId)
        }

        composable("edit_cover_image_screen/{currentCoverUrl}") { backStackEntry ->
            val currentCoverUrl = backStackEntry.arguments?.getString("currentCoverUrl") ?: ""
            EditCoverImageScreen(
                navController = navController,
                viewModel = hiltViewModel(),
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
            Log.d("NavGraph", "Encoded image URL from arguments: $imageUrl")
          //  val decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8.toString())
         //   Log.d("NavGraph", "Decoded image URL: $decodedUrl")
            ViewCoverImageScreen(
                navController = navController,
                imageUrl = imageUrl
            )
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
            val notifyViewModel: NotifyViewModel = hiltViewModel()
            SearchNotifyScreen(
                navController = navController,
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
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable( route = Screens.PrivacySettingsScreen.route) {
            PrivacySettingsScreen(
                viewModel = hiltViewModel(),
              navController = navController
            )
        }
        composable( route = Screens.ActivityNotificationScreen.route) {
            ActivityNotificationScreen(
                viewModel = hiltViewModel(),
                navController = navController
            )
        }
        composable( route = Screens.SystemNotification.route) {
            SystemNotificationScreen(
                navController = navController
            )
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

            Log.d("Navigation", "Navigated to DetaillScreen with postId: $postId and userId: $userId")
            val notifyViewModel: NotifyViewModel = hiltViewModel()

            DetaillScreen(navController = navController, postId = postId, userId = userId,
                viewModelNotify = notifyViewModel
            )
        }

    }
}
