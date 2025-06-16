package com.example.blinknotes.presentation.ui.Screens.notify

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.blinknotes.R
import com.example.blinknotes.presentation.viewModel.NotifyViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onCloseClick: () -> Unit,
    onSettingsClick: () -> Unit,
    currentUserAvatar: String,
    onAddMomentClick: () -> Unit,
    onPostClick: (String) -> Unit,
    notes: String,
) {

    val viewModel: NotifyViewModel = hiltViewModel()

    var backgroundBrush by remember { mutableStateOf(Brush.linearGradient(listOf(Color(0xFF00FFFF), Color(0xFF0000FF)))) }
    var inputText by remember { mutableStateOf(TextFieldValue("")) }

    val colors = listOf(
        listOf(Color(0xFFFFA500), Color(0xFFFF4500)),
        listOf(Color(0xFF00FF00), Color(0xFF008000)),
        listOf(Color(0xFF00FFFF), Color(0xFF0000FF)),
        listOf(Color(0xFFFF00FF), Color(0xFFFF1493)),
        listOf(Color(0xFFFFFF00), Color(0xFFFFD700)),
        listOf(Color(0xFF8A2BE2), Color(0xFF4B0082)),
        listOf(Color(0xFF00FA9A), Color(0xFF006400)),
        listOf(Color(0xFFFF6347), Color(0xFFFF4500)),
        listOf(Color(0xFF4682B4), Color(0xFF000080)),
        listOf(Color(0xFFADFF2F), Color(0xFF7FFF00))
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
                                        containerColor =  Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
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