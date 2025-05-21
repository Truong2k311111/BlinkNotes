package com.example.blinknotes.ui.profile.settingProfile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColorsWithIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Graph
import com.example.blinknotes.navigation.Screens
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreenProfile(navController: NavController) {
    val settingsList = listOf(
        SettingItem("Tài khoản", 2, listOf("Tài khoản", "Quyền riêng tư"), listOf(R.drawable.account, R.drawable.lock), R.drawable.icon_arrow_right),
        SettingItem("Nội dung & Hiển thị", 3, listOf("Thông Báo", "Liên Kết","Gia đình Thông minh"), listOf(R.drawable.bell, R.drawable.link_box_variant_outline,R.drawable.home_heart), R.drawable.icon_arrow_right),
        SettingItem("Bộ nhớ đệm & Dữ liệu di động", 1, listOf("Giải phóng dung lượng"), listOf(R.drawable.trash_can), R.drawable.icon_arrow_right),
        SettingItem("Hỗ trợ & Giới thiệu", 3, listOf("Báo cáo vấn đề", "Hỗ trợ","Điều khoản và Đhính sách"), listOf(R.drawable.flag_variant, R.drawable.comment_question,R.drawable.information), R.drawable.icon_arrow_right),
        SettingItem("Đăng nhập", 1, listOf("Đăng xuất",), listOf(R.drawable.logout), R.drawable.icon_arrow_right),
    )
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current

    fun signOut(context: Context, onSignOut: () -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)

        auth.signOut() // Đăng xuất khỏi Firebase
        googleSignInClient.signOut().addOnCompleteListener {
            onSignOut() // Callback để cập nhật UI sau khi đăng xuất
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.lightgray),
                    titleContentColor = colorResource(R.color.black),
                ),
                title = {
                    Text(
                        "Cài đặt và quyền riêng tư",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.icon_back),
                        contentDescription = "",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(Screens.ProfileScreen.route) {
                                    popUpTo(Screens.SettingScreenProfile.route) { inclusive = true }
                                }
                            }
                    )
                },
                actions = {},
                scrollBehavior = scrollBehavior
            )
        },
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 30.dp),
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .background(color = colorResource(R.color.lightgray))
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(settingsList) { setting ->
                ItemsSetting(setting,
                    onItemClick = { itemsLabel ->
                        when(itemsLabel) {
                            "Đăng xuất" -> {
                                signOut(context = context) {
                                    Toast.makeText(context, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screens.LoginScreen.route) {
                                        popUpTo(Graph.HOME) { inclusive = true }
                                    }
                                }
                            }
                        }
                        when(itemsLabel){
                            "Tài khoản" -> {
//                                navController.navigate(Screens.AccountScreen.route)
                            }
                            "Quyền riêng tư" -> {
                                navController.navigate(Screens.PrivacySettingsScreen.route)
                            }
                            "Thông Báo" -> {
//                                navController.navigate(Screens.NotificationScreen.route)
                            }
                            "Liên Kết" -> {
                                navController.navigate(Screens.LinkScreen.route)
                            }
                            "Giải phóng dung lượng" -> {
//                                navController.navigate(Screens.FreeUpSpaceScreen.route)
                            }
                            "Báo cáo vấn đề" -> {
//                                navController.navigate(Screens.ReportProblemScreen.route)
                            }
                            "Hỗ trợ" -> {
//                                navController.navigate(Screens.SupportScreen.route)
                            }
                            "Điều khoản và Đhính sách" -> {
//                                navController.navigate(Screens.TermsAndPoliciesScreen.route)
                            }
                        }
                    }
                )
            }
        }
    }
}
@Composable
fun ItemsSetting(
    itemsListSetting: SettingItem,
    onItemClick: (String) -> Unit,
){
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = itemsListSetting.name,
            fontSize = 13.sp,
            color = colorResource(R.color.text_my_qr),
            modifier = Modifier
                .padding(bottom = 8.dp, start = 12.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsListSetting.labels.forEachIndexed { index, label ->
                ItemsListSetting(
                    lable = label,
                    icons = itemsListSetting.icons[index],
                    iconNext = itemsListSetting.iconNext,
                    onClick = {
                        onItemClick(label)
                    }


                )
            }
        }
    }
}
@Composable
fun ItemsListSetting(
    lable: String,
    @DrawableRes icons: Int,
    @DrawableRes iconNext: Int,
    onClick: () -> Unit
){
    Row (
        modifier = Modifier
            .background(
                color = colorResource(R.color.white),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Row (
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                .clickable (
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ){
                    onClick()
                }
                .weight(1f),
        ){
            Image(
                painter = painterResource(id = icons),
                contentDescription = "",
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp),
                alignment = Alignment.CenterStart,
                colorFilter = ColorFilter.tint(color = colorResource(R.color.bgr))
            )
            Text(
                text = lable,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(alignment = Alignment.CenterVertically)
            )
        }
        Image(
            painter = painterResource(id = iconNext),
            contentDescription = "",
            modifier = Modifier
                .size(24.dp)
                .padding(end = 16.dp),
            alignment = Alignment.CenterEnd
        )
    }
}
data class SettingItem(
    val name: String,
    val numItems: Int,
    val labels: List<String>,
    @DrawableRes val icons: List<Int>,
    @DrawableRes val iconNext: Int
)

//@Preview(showBackground = true)
//@Composable
//fun Preview(){
//    //ItemsListSetting(lable = "Tai khoan", iconNext = R.drawable.icon_arrow_right, icons = R.drawable.icon_info)
//
//    SettingScreenProfile(navController = NavController(context = LocalContext.current))
//}