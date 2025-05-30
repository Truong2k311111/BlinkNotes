package com.example.blinknotes.ui.profile.settingProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blinknotes.R
import com.example.blinknotes.ui.profile.ProfileScreenViewModel

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
            OutlinedTextField(
                value = instagramLink.value,
                onValueChange = { instagramLink.value = it },
                label = { Text("Instagram") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.instagram),
                        contentDescription = "Instagram",
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
            OutlinedTextField(
                value = twitterLink.value,
                onValueChange = { twitterLink.value = it },
                label = { Text("Twitter") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.twitter),
                        contentDescription = "Twitter",
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
                    val userId = viewModel.currentUserId
                    if (userId != null) {
                        viewModel.updateSocialLinks(
                            userId = userId,
                            facebook = facebookLink.value,
                            instagram = instagramLink.value,
                            twitter = twitterLink.value,
                        )
                        showSuccessDialog = true
                    } else {
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