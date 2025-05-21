package com.example.blinknotes.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    content: @Composable (brush: Brush) -> Unit
) {
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    content(brush)
}

@Composable
fun ShimmerMessageItem(
    isSender: Boolean,
    modifier: Modifier = Modifier
) {
    ShimmerEffect(modifier = modifier) { brush ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
        ) {
            if (!isSender) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(brush, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(4.dp),
                horizontalAlignment = if (isSender) Alignment.End else Alignment.Start
            ) {
                // Image placeholder
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(brush, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Message placeholder
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                        .background(brush, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp placeholder
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .background(brush, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                )
            }

            if (isSender) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(brush, shape = androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}

@Composable
fun ShimmerProfileItem(
    modifier: Modifier = Modifier
) {
    ShimmerEffect(modifier = modifier) { brush ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(brush, shape = androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(16.dp)
                        .background(brush, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(12.dp)
                        .background(brush, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun ShimmerImageGrid(
    modifier: Modifier = Modifier
) {
    ShimmerEffect(modifier = modifier) { brush ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            repeat(2) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(2) { col ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(brush, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
} 