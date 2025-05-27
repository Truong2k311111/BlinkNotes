package com.example.blinknotes.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthManager {
    private const val ADMIN_PREFS = "admin_prefs"
    private const val IS_ADMIN_KEY = "is_admin"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun checkAdminStatus(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return@withContext false
            }

            // Kiểm tra trong SharedPreferences trước
            val prefs = context.getSharedPreferences(ADMIN_PREFS, Context.MODE_PRIVATE)
            val cachedAdminStatus = prefs.getBoolean(IS_ADMIN_KEY, false)
            if (cachedAdminStatus) {
                return@withContext true
            }

            // Nếu không có trong cache, kiểm tra trong Firestore
            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            val isAdmin = userDoc.getBoolean("isAdmin") ?: false

            // Lưu kết quả vào cache
            prefs.edit().putBoolean(IS_ADMIN_KEY, isAdmin).apply()

            return@withContext isAdmin
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    fun clearAdminStatus(context: Context) {
        val prefs = context.getSharedPreferences(ADMIN_PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(IS_ADMIN_KEY).apply()
    }

    suspend fun refreshAdminStatus(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return@withContext false
            }

            val userDoc = db.collection("users").document(currentUser.uid).get().await()
            val isAdmin = userDoc.getBoolean("isAdmin") ?: false

            // Cập nhật cache
            val prefs = context.getSharedPreferences(ADMIN_PREFS, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(IS_ADMIN_KEY, isAdmin).apply()

            return@withContext isAdmin
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
} 