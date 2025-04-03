//import com.example.blinknotes.ui.home.User
//
//suspend fun getUser(userId: String): User? {
//    return try {
//        val document = db.collection("users").document(userId).get().await()
//        if (document.exists()) {
//            val data = document.data
//            if (data != null) {
//                User(
//                    userId = document.id,
//                    username = data["username"] as? String ?: "",
//                    email = data["email"] as? String ?: "",
//                    profileImage = data["profileImage"] as? String ?: "",
//                    blinkNotesId = data["blinkNotesId"] as? String ?: "",
//                    bio = data["bio"] as? String ?: ""
//                )
//            } else null
//        } else null
//    } catch (e: Exception) {
//        Log.e("FirestoreHelper", "Error getting user: ${e.message}")
//        null
//    }
//}
//
//suspend fun updateUserProfile(userId: String, username: String, blinkNotesId: String, bio: String) {
//    try {
//        db.collection("users").document(userId).update(
//            mapOf(
//                "username" to username,
//                "blinkNotesId" to blinkNotesId,
//                "bio" to bio
//            )
//        ).await()
//    } catch (e: Exception) {
//        Log.e("FirestoreHelper", "Error updating user profile: ${e.message}")
//        throw e
//    }
//}