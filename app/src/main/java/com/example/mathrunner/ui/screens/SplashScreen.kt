package com.example.mathrunner.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathrunner.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashScreenFinished: () -> Unit,
    autoNavigate: Boolean = true // Auto navigate to HomeScreen after 6 seconds loading finish
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var isCompleted by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "ProgressAnimation"
    )

    // Animated dots for "Loading..."
    var dotCount by remember { mutableStateOf(3) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val totalDuration = 6000L // Exactly 6 seconds smooth loading fill

        while (progress < 1.0f) {
            val elapsed = System.currentTimeMillis() - startTime
            val currentProgress = (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
            progress = currentProgress
            dotCount = ((elapsed / 400) % 3).toInt() + 1
            if (currentProgress >= 1.0f) break
            delay(30)
        }

        progress = 1.0f
        isCompleted = true

        if (autoNavigate) {
            delay(500) // Brief pause at 100% before transition
            onSplashScreenFinished()
        }
    }

    // Shimmer/stripe animation across the progress bar
    val infiniteTransition = rememberInfiniteTransition(label = "StripeTransition")
    val stripeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "StripeOffset"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tap anywhere on the splash screen to navigate directly to Home
                onSplashScreenFinished()
            }
    ) {
        // 1. High-fidelity Splash Background Image matching Math Runner reference
        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = "Math Runner Splash Screen Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Bottom Animated Progress Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Loading Bar Outer Pill Frame
            Box(
                modifier = Modifier
                    .width(290.dp)
                    .height(28.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color.Black.copy(alpha = 0.6f)
                    )
                    .clip(CircleShape)
                    .background(Color(0xCC2A1B28)) // Dark plum-tinted semi-transparent pill background
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color(0xFF7DD3FC).copy(alpha = 0.6f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Inner Animated Sky Blue Cyan Progress Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(22.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFF00E5FF),
                                    Color(0xFF0284C7)
                                )
                            )
                        )
                ) {
                    // Diagonal Light Stripes Canvas Pattern
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stripeWidth = 14f
                        val stripeGap = 12f
                        val totalWidth = size.width
                        val height = size.height

                        clipRect {
                            var x = -height + (stripeOffset % (stripeWidth + stripeGap))
                            while (x < totalWidth + height) {
                                val path = Path().apply {
                                    moveTo(x, 0f)
                                    lineTo(x + stripeWidth, 0f)
                                    lineTo(x + stripeWidth - height, height)
                                    lineTo(x - height, height)
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = Color.White.copy(alpha = 0.28f)
                                )
                                x += stripeWidth + stripeGap
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Loading / Tap to Start Text
            val textToShow = if (isCompleted) "TAP ANYWHERE TO START ▶" else "Loading" + ".".repeat(dotCount)
            val textAlpha = if (isCompleted) pulseAlpha else 1.0f

            Text(
                text = textToShow,
                color = Color.White.copy(alpha = textAlpha),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(2f, 3f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
