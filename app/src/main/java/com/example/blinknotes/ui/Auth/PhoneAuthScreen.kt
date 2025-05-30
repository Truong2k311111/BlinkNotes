package com.example.blinknotes.ui.Auth

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.blinknotes.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(authViewModel: AuthViewModel, navController: NavController, navBackStackEntry: NavBackStackEntry) {
    val email = navBackStackEntry.arguments?.getString("email") ?: ""
    val userName = navBackStackEntry.arguments?.getString("userName") ?: ""
    val password = navBackStackEntry.arguments?.getString("password") ?: ""
    val confirmPassword = navBackStackEntry.arguments?.getString("confirmPassword") ?: ""

    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val activity = LocalContext.current as Activity

    fun formatPhoneNumber(number: String): String {
        return if (number.isNotEmpty()) "+84${number.trimStart('0')}" else ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Xác thực số điện thoại",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Vui lòng nhập số điện thoại của bạn để tiếp tục",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        if (!isOtpSent) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() }) {
                        phoneNumber = it
                    }
                },
                label = { Text("Số điện thoại") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                prefix = { Text("+84") }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val formattedNumber = formatPhoneNumber(phoneNumber)
                    authViewModel.sendOtp(formattedNumber, activity = activity) { success, message ->
                        if (success) isOtpSent = true
                        else errorMessage = message
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Gửi mã OTP", fontSize = 16.sp)
            }
        } else {
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text("Nhập mã OTP") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    authViewModel.verifyOtp(otp) { success, message ->
                        if (success) {
                            authViewModel.registerUser(
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                username = userName,
                                profileImage = ""
                            ) { success, message ->
                                if (success) {
                                    navController.navigate(Screens.LoginScreen.route)
                                } else {
                                    errorMessage = message
                                }
                            }
                        } else {
                            errorMessage = message
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Xác nhận mã OTP", fontSize = 16.sp)
            }
        }
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }
    }
}
