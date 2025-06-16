package com.example.blinknotes.presentation.ui.Screens.Auth

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.blinknotes.navigation.Screens
import com.example.blinknotes.presentation.viewModel.AuthUiState
import com.example.blinknotes.presentation.viewModel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen( navController: NavController, navBackStackEntry: NavBackStackEntry) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val email = navBackStackEntry.arguments?.getString("email") ?: ""
    val userName = navBackStackEntry.arguments?.getString("userName") ?: ""
    val password = navBackStackEntry.arguments?.getString("password") ?: ""
    val confirmPassword = navBackStackEntry.arguments?.getString("confirmPassword") ?: ""

    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    // isOtpSent state is now managed by AuthUiState in ViewModel
    // var isOtpSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope() // Use coroutine scope for launching ViewModel calls

    val authUiState by authViewModel.uiState.collectAsState()
    val isLoading = authUiState is AuthUiState.Loading || authUiState is AuthUiState.OtpVerificationLoading // Check loading states

    fun formatPhoneNumber(number: String): String {
        return if (number.isNotEmpty()) "+84${number.trimStart('0')}" else ""
    }

    // Observe AuthUiState for navigation and error messages
    LaunchedEffect(authUiState) {
        when (authUiState) {
            is AuthUiState.OtpSent -> {
                // OTP is sent, show OTP input field
                // isOtpSent = true // No longer needed, UI reacts to state
                 errorMessage = ""
            }
            is AuthUiState.OtpVerificationSuccess -> {
                // OTP verification successful, proceed with registration
                // Call register here
                 scope.launch { // Launch coroutine for the suspend function register
                     authViewModel.register(email, password, userName)
                 }
                 errorMessage = ""
            }
            is AuthUiState.Success -> {
                // Registration successful
                navController.navigate(Screens.LoginScreen.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                } // Navigate back to LoginScreen or appropriate screen
                 authViewModel.resetUiState() // Reset state after navigation
            }
            is AuthUiState.Error -> {
                // Handle general errors (e.g., from sendOtp verificationFailed)
                errorMessage = (authUiState as AuthUiState.Error).error.toString()
                 authViewModel.resetUiState() // Reset state after showing error
            }
            is AuthUiState.OtpVerificationError -> {
                // Handle OTP verification specific errors
                errorMessage = (authUiState as AuthUiState.OtpVerificationError).error.toString()
                 authViewModel.resetUiState() // Reset state after showing error
            }
            AuthUiState.Initial, AuthUiState.Loading, AuthUiState.OtpVerificationLoading -> {
                // Do nothing or show loading indicator
                errorMessage = ""
            }
        }
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
        
        // UI reacts to authUiState
        if (authUiState !is AuthUiState.OtpSent && authUiState !is AuthUiState.OtpVerificationSuccess) { // Show phone input initially and on some errors
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
                    authViewModel.sendOtp(formattedNumber, activity = activity)
                     errorMessage = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.height(24.dp)) else Text("Gửi mã OTP", fontSize = 16.sp)
            }
        } else if (authUiState is AuthUiState.OtpSent || authUiState is AuthUiState.OtpVerificationLoading) { // Show OTP input after OTP sent
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
                    authViewModel.verifyOtp(otp)
                     errorMessage = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                 if (isLoading) CircularProgressIndicator(modifier = Modifier.height(24.dp)) else Text("Xác nhận mã OTP", fontSize = 16.sp)
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
