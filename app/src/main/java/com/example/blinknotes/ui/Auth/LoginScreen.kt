package com.example.blinknotes.ui.Auth

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.blinknotes.R
import com.example.blinknotes.navigation.Graph
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.ui.Auth.Component.CustomButton
import com.example.blinknotes.ui.Auth.Component.CustomTextField
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(authViewModel: AuthViewModel, navController: NavController) {
    val activity = LocalContext.current as Activity
    val context = LocalContext.current
    val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(stringResource(R.string.key_web_firebase))
            .requestEmail()
            .build()
    )
    fun signInWithGoogle(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        authViewModel.handleSignInResult(result, context) { user ->
            Toast.makeText(context, "Chào ${user.displayName}", Toast.LENGTH_SHORT).show()
            navController.navigate(Graph.HOME) {
                popUpTo(Graph.AUTHENTICATION) { inclusive = true }
            }
        }
    }
    Scaffold { paddingValues ->
        ContentLoginScreen(
            onclickLogin = { email, password ->
                authViewModel.loginUser(email, password) { success, message ->
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@loginUser

                    if (success) {
                        navController.navigate(Graph.HOME) {
                            popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                        }
                    }
                }
                },

            onClickNavigation = {
                navController.navigate(Screens.RegisterScreen.route)
            },
            loginGoogle = {
                signInWithGoogle(googleSignInLauncher)
            },
            onPrivacyClick = {},
            onTermsClick = {},
            modifier = Modifier.padding(paddingValues),
            authViewModel = authViewModel,
            navController = navController,
            onclickReSetPass = { email ->
                if (email.isNotEmpty()) {
                    authViewModel.sendPasswordResetEmail(email,context)
                } else {
                    Toast.makeText(context, "Vui lòng nhập email trước khi đặt lại mật khẩu!", Toast.LENGTH_SHORT).show()
                }

            }

        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentLoginScreen(
    onclickLogin: (String, String) -> Unit,
    onClickNavigation: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController,
    loginGoogle: () -> Unit,
    onclickReSetPass: (String)-> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        ViewPagerLoginScreen(3000L)
        Spacer(modifier = Modifier.height(8.dp))
        ViewPagerLoginScreen(5000L)

        Text(
            text = "Chia sẻ những khoảnh khắc đáng nhớ trên Blink Notes",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        CustomTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Địa chỉ Email",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Mật khẩu",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }

        CustomButton(text = "Tiếp tục", color = Color.Red) {
            onclickLogin(email, password)
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomButton(text = "Tiếp tục bằng Google", color = Color.LightGray, hasIcon = true) {
            loginGoogle()
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Quên mật khẩu ?",
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .clickable(
                    onClick = {
                        onclickReSetPass(email)
                    }
                )
                .align(Alignment.CenterHorizontally),
            color = colorResource(R.color.tab_line),
        )
        ClickableTextSection(
            onClickNavigation = onClickNavigation,
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
            modifier = Modifier
        )
    }
}




@Composable
fun ClickableTextSection(
    onClickNavigation: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier,
) {
    val registerString = buildAnnotatedString {
        pushStringAnnotation(tag = "REGISTER", annotation = "REGISTER")
        withStyle(style = SpanStyle(color = Color.Red)) {
            append("Đăng Ký")
        }
        pop()
        append(" tài khoản với Email của bạn ")
    }

    val termsString = buildAnnotatedString {
        append("Bằng việc tiếp tục, bạn đồng ý với ")
        pushStringAnnotation(tag = "TERMS", annotation = "TERMS")
        withStyle(style = SpanStyle(color = Color.Blue)) {
            append("Điều khoản Dịch vụ")
        }
        pop()
        append(" và xác nhận rằng bạn đã đọc ")
        pushStringAnnotation(tag = "PRIVACY", annotation = "PRIVACY")
        withStyle(style = SpanStyle(color = Color.Blue)) {
            append("Chính sách quyền riêng tư")
        }
        pop()
    }

    ClickableText(
        text = registerString,
        style = TextStyle(fontSize = 18.sp, textAlign = TextAlign.Center),
        modifier = Modifier.fillMaxWidth(),
        onClick = { offset ->
            registerString.getStringAnnotations(tag = "REGISTER", start = offset, end = offset)
                .firstOrNull()?.let { onClickNavigation() }
        }
    )

    ClickableText(
        text = termsString,
        style = TextStyle(fontSize = 16.sp, textAlign = TextAlign.Center),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        onClick = { offset ->
            termsString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                .firstOrNull()?.let { onTermsClick() }
            termsString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                .firstOrNull()?.let { onPrivacyClick() }
        }
    )
}



@Composable
fun ViewPagerLoginScreen(
    delay : Long
){
    val pagerState = rememberPagerState(pageCount = { Int.MAX_VALUE })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
//        snapshotFlow {
//            pagerState.currentPage
//        }
//            .collectLatest { currentPage ->
//                delay(timeMillis = delay )
//                coroutineScope.launch {
//                    val nextPage = (pagerState.currentPage + 1) % images.size
//                    pagerState.animateScrollToPage(nextPage,
//                        animationSpec = tween(
//                            durationMillis = 800,
//                            easing = LinearOutSlowInEasing))
//            }
        while (true) {
            delay(timeMillis = delay )
            coroutineScope.launch {
                val nextPage = (pagerState.currentPage + 1) % images.size
                pagerState.animateScrollToPage(nextPage,
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = LinearOutSlowInEasing))
                pagerState.scroll { scrollBy(1f) }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) { page ->
            val index = page % images.size
            Image(
                painter = painterResource(id = images[index]),
                contentDescription = "Ảnh $index",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// Danh sách ảnh
val images = listOf(
    R.drawable.image1,
    R.drawable.image2,
    R.drawable.image1,
    R.drawable.image4,
    R.drawable.image1,
    R.drawable.image2,
    R.drawable.image1,
    R.drawable.image4,
    R.drawable.image1,
    R.drawable.image2,
    R.drawable.image1,
    R.drawable.image4,

)

//@Preview(showBackground = true)
//@Composable
//fun PreviewLoginScreen() {
//ContentLoginScreen(onclickLogin = {}, onClickNavigation = {}, modifier = Modifier, onNoticeClick = {}, onTermsClick = {}, onPrivacyClick = {})
//    //ViewPagerLoginScreen()
//}
