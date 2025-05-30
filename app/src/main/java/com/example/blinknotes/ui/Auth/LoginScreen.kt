package com.example.blinknotes.ui.Auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.blinknotes.ui.home.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun LoginScreen(authViewModel: AuthViewModel, navController: NavController) {
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
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task, navController, context)
        } else {
            Toast.makeText(context, "Đăng nhập bằng Google thất bại", Toast.LENGTH_SHORT).show()
        }
    }
    Scaffold { paddingValues ->
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            Image(
                painter = painterResource(id = R.drawable.bgr_login),
                contentDescription = "Background",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                contentScale = ContentScale.Crop,
            )
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
            .background( Color.White , shape = RoundedCornerShape(topEnd = 32.dp, topStart = 32.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        Spacer(modifier = Modifier.height(8.dp))
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }
        CustomButton(text = "Tiếp tục", color = Color.Red) {
            onclickLogin(email, password)
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
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
private fun handleGoogleSignInResult(
    task: Task<GoogleSignInAccount>,
    navController: NavController,
    context: Context
) {
    try {
        val account = task.getResult(ApiException::class.java)
        firebaseAuthWithGoogle(account.idToken!!, navController, context)
    } catch (e: ApiException) {
        Toast.makeText(context, "Đăng nhập thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
private fun firebaseAuthWithGoogle(
    idToken: String,
    navController: NavController,
    context: Context
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { fcmTask ->
                        if (fcmTask.isSuccessful) {
                            val fcmToken = fcmTask.result
                            db.collection("users").document(user.uid)
                                .update("fcmToken", fcmToken)
                                .addOnFailureListener { e ->
                                    Log.e("FCM", "Lỗi cập nhật FCM token", e)
                                }
                        } else {
                            Log.e("FCM", "Không thể lấy FCM token", fcmTask.exception)
                        }
                    }
                    db.collection("users").document(user.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val isAdmin = document.getBoolean("isAdmin") ?: false
                                if (isAdmin) {
                                    val prefs = context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putBoolean("is_admin", true).apply()

                                    navController.navigate(Screens.AdminDashboard.route) {
                                        popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(Graph.HOME) {
                                        popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                                    }
                                }
                            } else {
                                val newUser = User(
                                    userId = user.uid,
                                    username = user.displayName ?: "User",
                                    email = user.email ?: "",
                                    profileImage = user.photoUrl?.toString() ?: "",
                                    createdAt = System.currentTimeMillis(),
                                    isAdmin = false,
                                    followersCount = 0,
                                    followingCount = 0
                                )
                                db.collection("users").document(user.uid)
                                    .set(newUser)
                                    .addOnSuccessListener {
                                        navController.navigate(Graph.HOME) {
                                            popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Lỗi khi tạo tài khoản: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Lỗi khi kiểm tra tài khoản: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else {
                Toast.makeText(context, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
            }
        }
}

