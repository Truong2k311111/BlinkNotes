package com.example.blinknotes.ui.Auth

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.blinknotes.data.helper.FirestoreHelper.addUser
import com.example.blinknotes.data.repository.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    fun loginUser(email: String, password: String, callback: (Boolean, String) -> Unit) {
        authRepository.loginUser(email, password) { success, message ->
            if (success) {
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
        if (password != confirmPassword) {
            callback(false, "Passwords do not match")
            return
        }
        if (!isValidPassword(password)) {
            callback(false, "Password must contain at least one uppercase letter, one number, and one special character.")
            return
        }
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
