package com.example.blinknotes.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun EditProfileImageScreen(
    currentImageUrl: String,
    onDismiss: () -> Unit,
    onImageUpdated: (String) -> Unit
) {
    val context = LocalContext.current
    val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TopBar với nút back
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_back),
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Chỉnh sửa ảnh đại diện",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Ảnh đại diện ở giữa
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected profile image",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = currentImageUrl,
                        contentDescription = "Current profile image",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Các nút ở dưới
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.bgr)
                    )
                ) {
                    Text(
                        text = "Chọn ảnh đại diện",
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }

                if (selectedImageUri != null) {
                    Button(
                        onClick = {
                            isLoading = true
                            val imageRef = storage.reference
                                .child("profile_images")
                                .child("${currentUser?.uid}/${UUID.randomUUID()}")
                            
                            imageRef.putFile(selectedImageUri!!)
                                .addOnSuccessListener {
                                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                        // Cập nhật URL trong Firestore
                                        db.collection("users")
                                            .document(currentUser?.uid!!)
                                            .update("profileImage", downloadUrl.toString())
                                            .addOnSuccessListener {
                                                onImageUpdated(downloadUrl.toString())
                                                onDismiss()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.bgr)
                        ),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isLoading) "Đang cập nhật..." else "Cập nhật ảnh",
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
} 