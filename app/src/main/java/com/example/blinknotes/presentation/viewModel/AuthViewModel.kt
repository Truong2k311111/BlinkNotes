package com.example.blinknotes.presentation.viewModel

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinknotes.data.helper.FirestoreHelper.addUser
import com.example.blinknotes.domain.model.AuthError
import com.example.blinknotes.domain.model.User
import com.example.blinknotes.domain.repository.AuthRepository
import com.example.blinknotes.domain.usecase.auth.LoginUseCase
import com.example.blinknotes.domain.usecase.auth.RegisterUseCase
import com.example.blinknotes.domain.usecase.auth.ResetPasswordUseCase
import com.example.blinknotes.domain.usecase.auth.SignInWithGoogleUseCase
import com.example.blinknotes.domain.usecase.auth.UpdateUserProfileUseCase
import com.example.blinknotes.domain.usecase.user.GetUserProfileUseCase
import com.example.blinknotes.utils.isValidPassword
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val error: AuthError) : AuthUiState()
    object OtpSent : AuthUiState()
    object OtpVerificationLoading : AuthUiState()
    object OtpVerificationSuccess : AuthUiState()
    data class OtpVerificationError(val error: AuthError) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error(AuthError.ValidationError("Email and password cannot be empty"))
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                loginUseCase(email, password)
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> AuthError.AuthenticationError(e.message ?: "Authentication error")
                        else -> AuthError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun register(email: String, password: String, username: String) {
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _uiState.value = AuthUiState.Error(AuthError.ValidationError("All fields are required"))
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                registerUseCase(email, password, username)

                val firebaseUser = auth.currentUser ?: throw Exception("Firebase User is null after registration")
                val userId = firebaseUser.uid

                val userDoc = firestore.collection("users").document(userId).get().await()

                if (!userDoc.exists()) {
                    val newUser = User(
                        userId = userId,
                        username = username,
                        email = firebaseUser.email ?: "",
                        profileImage = firebaseUser.photoUrl?.toString() ?: "",
                        createdAt = System.currentTimeMillis(),
                        isAdmin = false,
                        followersCount = 0,
                        followingCount = 0,
                        blinkNotesId = "",
                        bio = "",
                        coverImage = "",
                        recentPost = emptyList()
                    )
                    firestore.collection("users").document(userId)
                        .set(newUser)
                        .await()
                }

                FirebaseMessaging.getInstance().token.addOnCompleteListener { fcmTask ->
                    if (fcmTask.isSuccessful) {
                        val fcmToken = fcmTask.result
                        firestore.collection("users").document(userId)
                            .update("fcmToken", fcmToken)
                            .addOnFailureListener { e ->
                                Log.e("FCM", "Lỗi cập nhật FCM token", e)
                            }
                    } else {
                        Log.e("FCM", "Không thể lấy FCM token", fcmTask.exception)
                    }
                }

                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> AuthError.AuthenticationError(e.message ?: "Registration failed")
                        else -> AuthError.NetworkError(e.message ?: "Unknown error during registration")
                    }
                )
                Log.e("AuthViewModel", "Error during registration: ", e)
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error(AuthError.ValidationError("Email cannot be empty"))
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                resetPasswordUseCase(email)
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> AuthError.AuthenticationError(e.message ?: "Authentication error")
                        else -> AuthError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun updateUserProfile(username: String, bio: String, profileImageUrl: String?) {
        if (username.isBlank()) {
            _uiState.value = AuthUiState.Error(AuthError.ValidationError("Username cannot be empty"))
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                updateUserProfileUseCase(username, bio, profileImageUrl)
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> AuthError.AuthenticationError(e.message ?: "Authentication error")
                        else -> AuthError.NetworkError(e.message ?: "Unknown error")
                    }
                )
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                val userId = signInWithGoogleUseCase(credential)

                val firebaseUser = auth.currentUser ?: throw Exception("Firebase User is null after Google Sign-In")

                val userDoc = firestore.collection("users").document(userId).get().await()

                if (!userDoc.exists()) {
                    val newUser = User(
                        userId = userId,
                        username = firebaseUser.displayName ?: "User",
                        email = firebaseUser.email ?: "",
                        profileImage = firebaseUser.photoUrl?.toString() ?: "",
                        createdAt = System.currentTimeMillis(),
                        isAdmin = false,
                        followersCount = 0,
                        followingCount = 0,
                        blinkNotesId = "",
                        bio = "",
                        coverImage = "",
                        recentPost = emptyList()
                    )
                    firestore.collection("users").document(userId)
                        .set(newUser)
                        .await()
                }

                FirebaseMessaging.getInstance().token.addOnCompleteListener { fcmTask ->
                    if (fcmTask.isSuccessful) {
                        val fcmToken = fcmTask.result
                        firestore.collection("users").document(userId)
                            .update("fcmToken", fcmToken)
                            .addOnFailureListener { e ->
                                Log.e("FCM", "Lỗi cập nhật FCM token", e)
                            }
            } else {
                        Log.e("FCM", "Không thể lấy FCM token", fcmTask.exception)
                    }
                }

                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    when (e) {
                        is FirebaseAuthException -> AuthError.AuthenticationError(e.message ?: "Google Sign-In failed")
                        else -> AuthError.NetworkError(e.message ?: "Unknown error during Google Sign-In")
                    }
                )
                Log.e("AuthViewModel", "Error during Google Sign-In: ", e)
            }
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
         _uiState.value = AuthUiState.Loading
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    viewModelScope.launch {
                        try {
                             auth.signInWithCredential(credential).await()
                             _uiState.value = AuthUiState.OtpVerificationSuccess
                        } catch (e: Exception) {
                             _uiState.value = AuthUiState.OtpVerificationError(AuthError.AuthenticationError(e.message ?: "Auto verification failed")) // Error state
                        }
                    }
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("PhoneAuth", "Verification failed: ", e)
                    _uiState.value = AuthUiState.Error(AuthError.AuthenticationError(e.message ?: "Verification failed")) // Update main error state
                }
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@AuthViewModel.verificationId = verificationId
                    resendToken = token
                    Log.d("Auth", "OTP sent to $phoneNumber")
                    _uiState.value = AuthUiState.OtpSent
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
             _uiState.value = AuthUiState.OtpVerificationLoading
            try {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                auth.signInWithCredential(credential).await()
                 _uiState.value = AuthUiState.OtpVerificationSuccess
            } catch (e: Exception) {
                Log.e("Auth", "OTP verification failed.", e)
                _uiState.value = AuthUiState.OtpVerificationError(AuthError.AuthenticationError(e.message ?: "Invalid OTP")) // Error state
            }
        }
    }

    fun sendPasswordResetEmail(email: String, context: Context) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Đã gửi email khôi phục. Vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Gửi email thất bại. Kiểm tra lại địa chỉ email!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Initial
    }

}
