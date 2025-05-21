package com.example.blinknotes.ui.Auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.blinknotes.data.helper.FirestoreHelper.addUser
import com.example.blinknotes.data.repository.AuthRepository
import com.example.blinknotes.navigation.Graph
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    fun loginUser(email: String, password: String, callback: (Boolean, String) -> Unit) {
        authRepository.loginUser(email, password) { success, message ->
            if (success) {
                // Get and update FCM token after successful login
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            FirebaseFirestore.getInstance().collection("users")
                                .document(userId)
                                .update("fcmToken", token)
                                .addOnFailureListener { e ->
                                    Log.e("FCM", "Error updating FCM token", e)
                                }
                        }
                    }
                }
            }
            callback(success, message)
        }
    }

    fun registerUser(
        email: String,
        password: String,
        confirmPassword: String,
        username: String,
        profileImage: String = "",
        callback: (Boolean, String) -> Unit
    ) {
        // Kiểm tra mật khẩu
        if (password != confirmPassword) {
            callback(false, "Passwords do not match")
            return
        }
        if (!isValidPassword(password)) {
            callback(false, "Password must contain at least one uppercase letter, one number, and one special character.")
            return
        }
        // Đăng ký người dùng với Firebase Authentication
        authRepository.registerUser(email, password, confirmPassword) { success, message ->
            if (success) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@registerUser
                addUser(
                    email = email ,
                    userId = userId,
                    username = username,
                    profileImage = profileImage,
                    callback = callback
                    )

            } else {
                callback(false, message)
            }
        }
    }


    // Gửi OTP đến số điện thoại
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        callback: (Boolean, String) -> Unit) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                callback(true, "Auto verification successful")
                            } else {
                                callback(false, "Auto verification failed")
                            }
                        }
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Log.e("PhoneAuth", "Verification failed: ")
                    callback(false, "Verification failed:")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@AuthViewModel.verificationId = verificationId
                    resendToken = token
                    Log.d("Auth", "OTP sent to $phoneNumber")
                    callback(true, "OTP sent successfully")
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Xác thực OTP
    fun verifyOtp(
        otp: String,
        callback: (Boolean, String) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "OTP verified successfully.")
                    callback(true, "Login successful")
                } else {
                    Log.e("Auth", "OTP verification failed.")
                    callback(false, "Invalid OTP")
                }
            }
    }


    fun handleSignInResult(
        result: ActivityResult,
        context: Context,
        onSuccess: (FirebaseUser) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    FirebaseFirestore.getInstance().collection("users")
                                        .document(userId)
                                        .update("fcmToken", token)
                                        .addOnFailureListener { e ->
                                            Log.e("FCM", "Error updating FCM token", e)
                                        }
                                }
                            }
                        }
                        val user = authResult.result?.user
                        if (user != null) {
                            // ✅ Lấy ID token từ Firebase
                            user.getIdToken(true).addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val token = tokenTask.result?.token

                                    // ✅ Lưu token vào SharedPreferences
                                    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().putString("firebase_token", token).apply()

                                    // ✅ Gọi callback thành công
                                    checkAndAddUser(user, context)
                                    onSuccess(user)
                                } else {
                                    Toast.makeText(context, "Không thể lấy token", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: ApiException) {
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedToken = sharedPreferences.getString("firebase_token", null)
        if (savedToken != null) {
            Log.d("Authtoken", "Saved token: $savedToken")
        } else {
            Log.d("Auth", "No token found in SharedPreferences")
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

}
fun isValidPassword(password: String): Boolean {
    val passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$"
    return password.matches(passwordPattern.toRegex())
}
private fun checkAndAddUser(user: FirebaseUser, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(user.uid)

    userRef.get().addOnSuccessListener { document ->
        if (!document.exists()) {
            addUser(
                userId = user.uid,
                username = user.displayName ?: "Unknown",
                email = user.email ?: "",
                profileImage = user.photoUrl?.toString() ?: ""
            ) { success, message ->
                if (!success) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Lỗi khi kiểm tra user!", Toast.LENGTH_SHORT).show()
    }
}
