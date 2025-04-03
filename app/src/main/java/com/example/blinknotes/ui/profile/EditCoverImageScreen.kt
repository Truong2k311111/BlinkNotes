package com.example.blinknotes.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCoverImageScreen(
    navController: NavController,
    viewModel: ProfileScreenViewModel,
    currentCoverUrl: String
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val storage = FirebaseStorage.getInstance()
    val context = LocalContext.current

    // Decode URL
    val decodedCoverUrl = URLDecoder.decode(currentCoverUrl, StandardCharsets.UTF_8.toString())

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa ảnh bìa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Current Cover Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray)
                ) {
                    AsyncImage(
                        model = selectedImageUri ?: decodedCoverUrl,
                        contentDescription = "Cover Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.accounticon),
                        fallback = painterResource(id = R.drawable.accounticon)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Image Button
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4)
                    ),
                    enabled = !isLoading
                ) {
                    Text("Chọn ảnh mới")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Update Button
                Button(
                    onClick = {
                        selectedImageUri?.let { uri ->
                            isLoading = true
                            val imageRef = storage.reference.child("cover_images/${UUID.randomUUID()}")
                            val uploadTask = imageRef.putFile(uri)

                            uploadTask.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let { throw it }
                                }
                                imageRef.downloadUrl
                            }.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val downloadUri = task.result.toString()
                                    currentUser?.uid?.let { userId ->
                                        viewModel.updateCoverImage(userId, downloadUri) {
                                            // Refresh user data after update
                                            viewModel.getCurrentUser { updatedUser ->
                                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                                    "updatedCoverUrl",
                                                    downloadUri
                                                )
                                                navController.popBackStack()
                                            }
                                        }
                                    }
                                }
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedImageUri != null && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Cập nhật")
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
} 