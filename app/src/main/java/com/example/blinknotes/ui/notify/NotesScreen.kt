package com.example.blinknotes.ui.notify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.random.Random
import com.example.blinknotes.R
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onCloseClick: () -> Unit,
    onSettingsClick: () -> Unit,
    currentUserAvatar: String,
    onAddMomentClick: () -> Unit,
    onPostClick: (String) -> Unit,
    notes: String,
    viewModel: NotifyViewModel = viewModel()
) {
    var backgroundBrush by remember { mutableStateOf(Brush.linearGradient(listOf(Color(0xFF00FFFF), Color(0xFF0000FF)))) }
    var inputText by remember { mutableStateOf(TextFieldValue("")) }

    val colors = listOf(
        listOf(Color(0xFFFFA500), Color(0xFFFF4500)), // Orange to Red
        listOf(Color(0xFF00FF00), Color(0xFF008000)), // Light Green to Dark Green
        listOf(Color(0xFF00FFFF), Color(0xFF0000FF)), // Cyan to Blue
        listOf(Color(0xFFFF00FF), Color(0xFFFF1493)), // Magenta to Deep Pink
        listOf(Color(0xFFFFFF00), Color(0xFFFFD700)), // Yellow to Gold
        listOf(Color(0xFF8A2BE2), Color(0xFF4B0082)), // Blue Violet to Indigo
        listOf(Color(0xFF00FA9A), Color(0xFF006400)), // Medium Spring Green to Dark Green
        listOf(Color(0xFFFF6347), Color(0xFFFF4500)), // Tomato to Orange Red
        listOf(Color(0xFF4682B4), Color(0xFF000080)), // Steel Blue to Navy
        listOf(Color(0xFFADFF2F), Color(0xFF7FFF00))  // Green Yellow to Chartreuse
    )
    var notes by remember { mutableStateOf(notes) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onCloseClick() }
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onSettingsClick() }
                )

            }
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.updateUserNote(notes)
                    onPostClick (notes)
                    onCloseClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp,end = 16.dp, start = 16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource( id = R.color.azure ),
                    contentColor = Color.Black
                ),

            ) {
                Text(text = "Đăng", fontSize = 16.sp)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end =16.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.coral))
                    .border(4.dp, Color.Gray, CircleShape)
                    .clickable {
                        val randomColors = colors[Random.nextInt(colors.size)]
                        backgroundBrush = Brush.linearGradient(randomColors)
                    },
                contentAlignment = Alignment.Center
            ) {
            }
            Column(modifier = Modifier
                .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(176.dp)
                        .clickable { onAddMomentClick() },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = currentUserAvatar,
                        contentDescription = "Your Avatar",
                        modifier = Modifier
                            .size(164.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .offset( x = (12).dp,y = (-80).dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .shadow(
                                        elevation = 5.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = Color(0xFF000000),
                                        spotColor = Color(0xFF000000)
                                    )
                                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                            ) {
                                TextField(
                                    value = notes,
                                    onValueChange = { notes = it },
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        lineHeight = 17.sp
                                    ),
                                    colors = TextFieldDefaults.textFieldColors(
                                        containerColor =  Color.Transparent, // Bỏ màu nền
                                        focusedIndicatorColor = Color.Transparent, // Bỏ gạch chân khi focus
                                        unfocusedIndicatorColor = Color.Transparent, // Bỏ gạch chân khi unfocus
                                        disabledIndicatorColor = Color.Transparent // Bỏ gạch chân khi disabled
                                    ),
                                    placeholder = {
                                        Text(
                                            text = "Nhập ghi chú của bạn",
                                            fontSize = 16.sp,
                                            color = Color.Gray
                                        )
                                    },
                                    maxLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )

                            }
                        }
                    }                }
                Spacer(modifier = Modifier.height(4.dp))
            }

        }
    }
}