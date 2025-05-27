package com.example.blinknotes.ui.addPhoto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisibilityBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onVisibilitySelected: (String) -> Unit,
    currentVisibility: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = showBottomSheet,
        enter = scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + fadeIn(),
        exit = scaleOut(
            targetScale = 0.8f,
            transformOrigin = TransformOrigin(0.5f, 1f)
        ) + fadeOut(),
        modifier = modifier.padding(bottom = 86.dp, start = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.6f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                VisibilityOption(
                    text = "Mọi người",
                    isSelected = currentVisibility == "public",
                    onClick = { 
                        onVisibilitySelected("public")
                        onDismiss()
                    }
                )

                VisibilityOption(
                    text = "Không ai cả",
                    isSelected = currentVisibility == "private",
                    onClick = {
                        onVisibilitySelected("private")
                        onDismiss()
                    }
                )
                VisibilityOption(
                    text = "Bạn bè",
                    isSelected = currentVisibility == "friends",
                    onClick = {
                        onVisibilitySelected("friends")
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun VisibilityOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF000000),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
