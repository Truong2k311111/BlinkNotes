import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.blinknotes.navigation.Graph
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.addPhoto.AddPhotoScreen
import com.example.blinknotes.ui.detaill.DetaillScreen
import com.example.blinknotes.ui.home.HomeScreen
import com.example.blinknotes.ui.home.HomeScreenViewModel
import com.example.blinknotes.ui.notify.ChatScreen
import com.example.blinknotes.ui.notify.NotifyScreen
import com.example.blinknotes.ui.notify.NotifyViewModel
import com.example.blinknotes.ui.notify.SearchNotifyScreen
import com.example.blinknotes.ui.profile.ProfileScreen
import com.example.blinknotes.ui.profile.settingProfile.SettingScreenProfile
import com.example.blinknotes.ui.search.SearchScreen
import com.example.blinknotes.ui.search.SearchScreenViewModelFactory
import com.example.blinknotes.ui.profile.EditProfileImageScreen
import com.example.blinknotes.ui.profile.EditCoverImageScreen
import com.example.blinknotes.ui.profile.FollowingAndFollowerScreen
import com.example.blinknotes.ui.profile.ViewCoverImageScreen


fun NavGraphBuilder.navGraph(navController: NavHostController, modifier: Any,
                             viewModel: HomeScreenViewModel
) {

    navigation(
        route = Graph.HOME,
        startDestination = Screens.HomeScreen.route,
    ) {

        composable(route = Screens.HomeScreen.route,
       //     arguments = listOf(navArgument("userId") { type = NavType.StringType })


        ) {backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            HomeScreen(navController = navController, viewmodel = viewModel)
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
        composable(route = Screens.SearchScreen.route) {
            SearchScreen(navController = navController, viewModel = SearchScreenViewModelFactory())
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
            Log.d("NavGraph", "Encoded image URL from arguments: $imageUrl")
          //  val decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8.toString())
         //   Log.d("NavGraph", "Decoded image URL: $decodedUrl")
            ViewCoverImageScreen(
                navController = navController,
                imageUrl = imageUrl
            )
        }

        composable(
            route = Screens.ChatScreen.route+"/{username}/{avatarRes}/{hasMoment}/{isMomentSeen}/{isOnline}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("avatarRes") { type = NavType.StringType },
                navArgument("hasMoment") { type = NavType.BoolType },
                navArgument("isMomentSeen") { type = NavType.BoolType },
                navArgument("isOnline") { type = NavType.BoolType },
            )
        )
        { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val avatarRes = backStackEntry.arguments?.getString("avatarRes") ?: ""
            val hasMoment = backStackEntry.arguments?.getBoolean("hasMoment") ?: false
            val isMomentSeen = backStackEntry.arguments?.getBoolean("isMomentSeen") ?: false
            val isOnline = backStackEntry.arguments?.getBoolean("isOnline") ?: false
             ChatScreen(navController = navController, username = username, avatarRes = avatarRes, hasMoment = hasMoment, isMomentSeen = isMomentSeen, isOnline = isOnline   )
        }
        composable(route = Screens.SearchNotifyScreen.route) {
            val notifyViewModel: NotifyViewModel = viewModel()
            SearchNotifyScreen(
                navController = navController,
                viewModel = notifyViewModel
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

            DetaillScreen(navController = navController, postId = postId, userId = userId)
        }

    }
}
