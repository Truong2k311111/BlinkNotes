package com.example.blinknotes.navigation

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.wear.compose.material3.IconButton
import com.example.blinknotes.R
import com.example.blinknotes.ui.addPhoto.AddPhotoScreenViewModel
import com.example.blinknotes.ui.profile.ProfileScreenViewModel


data class NavigationItem(
    val icon: ImageVector,
    val iconOutline : ImageVector,
    val route: String,
)

@Composable
fun BottomNavigationBar(navController: NavHostController, items: List<NavigationItem>, viewModel: AddPhotoScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val selectedImages by viewModel.selectedImages.collectAsState()

    val viewModelProfile = viewModel<ProfileScreenViewModel>()

    // Get current route to determine which icon should be filled
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            viewModel.addSelectedImages(uris)
        }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEachIndexed { index, item ->
                if (index == 2) {
                    // Custom plus button design
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF00C6FB),
                                        Color(0xFF005BEA)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                spotColor = Color(0x40000000)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(Screens.AddPhotoScreen.route)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            navController.navigate(
                                if (
                                item.route == Screens.ProfileScreen.route + "/userId"
                            ) {
                                Screens.ProfileScreen.route + "/userId=${viewModelProfile.currentUserId}"
                            } else {
                                item.route
                            }
                            ){
                                // Prevent multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }
                        },
                    ) {
                        val isSelected = currentRoute == item.route
                        Icon(
                            imageVector = if (isSelected) item.icon else item.iconOutline,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = if (isSelected) Color(0xFF005BEA) else Color.Black
                        )
                    }
                }
            }
        }
    }
}