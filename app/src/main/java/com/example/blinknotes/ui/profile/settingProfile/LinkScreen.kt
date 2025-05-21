package com.example.blinknotes.ui.profile.settingProfile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blinknotes.ui.profile.ProfileScreenViewModel
import com.example.blinknotes.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkScreen(
    viewModel: ProfileScreenViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val user = viewModel.user.collectAsState().value
    val facebookLink = remember { mutableStateOf(user?.facebookLink ?: "") }
    val instagramLink = remember { mutableStateOf(user?.instagramLink ?: "") }
    val twitterLink = remember { mutableStateOf(user?.twitterLink ?: "") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Liên kết mạng xã hội",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Thêm liên kết mạng xã hội của bạn",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Facebook Link
            OutlinedTextField(
                value = facebookLink.value,
                onValueChange = { facebookLink.value = it },
                label = { Text("Facebook") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.facebook),
                        contentDescription = "Facebook",
                        tint = Color(0xFF1877F2)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1877F2),
                    unfocusedBorderColor = Color.LightGray
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            // Instagram Link
            OutlinedTextField(
                value = instagramLink.value,
                onValueChange = { instagramLink.value = it },
                label = { Text("Instagram") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.instagram),                        contentDescription = "Instagram",
                        tint = Color(0xFFE4405F)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFE4405F),
                    unfocusedBorderColor = Color.LightGray
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            // Twitter Link
            OutlinedTextField(
                value = twitterLink.value,
                onValueChange = { twitterLink.value = it },
                label = { Text("Twitter") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.twitter),                        contentDescription = "Twitter",
                        tint = Color(0xFF1DA1F2)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1DA1F2),
                    unfocusedBorderColor = Color.LightGray
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val userId = viewModel.currentUserId // Get the current user's ID
                    if (userId != null) {
                        viewModel.updateSocialLinks(
                            userId = userId,
                            facebook = facebookLink.value,
                            instagram = instagramLink.value,
                            twitter = twitterLink.value,
                        )
                        showSuccessDialog = true
                    } else {
                        // Handle the case where the user ID is null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C78A)
                )
            ) {
                Text(
                    "Lưu thay đổi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = {
                    Text(
                        "Thành công",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("Liên kết mạng xã hội đã được cập nhật")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSuccessDialog = false
                            onBackClick()
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}