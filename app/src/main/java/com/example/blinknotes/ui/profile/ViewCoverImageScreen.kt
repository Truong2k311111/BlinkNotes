package com.example.blinknotes.ui.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.blinknotes.R

@Composable
fun ViewCoverImageScreen(
    navController: NavController,
    imageUrl: String
) {
    Log.d("ViewCoverImageScreen", "Received image URL: $imageUrl")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.window_close),
                contentDescription = "Close",
                tint = Color.White
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Cover Image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit,
                onLoading = {
                    // You can add a loading indicator here if needed
                },
                onError = { error ->
                    // You can handle error state here
                    println("Error loading image: ${error.result.throwable.localizedMessage}")
                }
            )
        }
    }
} 