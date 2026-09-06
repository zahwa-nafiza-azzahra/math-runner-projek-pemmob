package com.example.mathrunner.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathrunner.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Supported Character Animation States matching the 7 official game animations
 */
enum class PlayerAnimState {
    IDLE,           // 1. Idle: Karakter berdiri, gerakan kecil pada tubuh & backpack
    RUN,            // 2. Run: Karakter berlari dari belakang, gerakan kaki natural seamless loop
    JUMP,           // 3. Jump: Karakter melompat dengan take-off dan landing
    COLLECT_COIN,   // 4. Collect Coin: Karakter mengambil koin + sparkle effect
    CORRECT_ANSWER, // 5. Correct Answer: Gerakan positif/fist-pump + green aura
    WRONG_ANSWER,   // 6. Wrong Answer: Terkena obstacle, gerakan kaget & berhenti
    VICTORY         // 7. Victory: Gerakan kemenangan selebrasi finish
}

@Composable
fun AnimatedPlayerCharacter(
    animState: PlayerAnimState = PlayerAnimState.RUN,
    modifier: Modifier = Modifier
) {
    // ----------------------------------------------------
    // 1. SPRITE FRAME CYCLE FOR RUNNING
    // ----------------------------------------------------
    val runFrames = listOf(
        R.drawable.player_run_frame1,
        R.drawable.player_run_frame2,
        R.drawable.player_run_frame3,
        R.drawable.player_run_frame4
    )
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(animState) {
        if (animState == PlayerAnimState.RUN) {
            while (true) {
                delay(120) // 8.3 FPS frame rate for natural cartoon stepping
                currentFrameIndex = (currentFrameIndex + 1) % runFrames.size
            }
        }
    }

    // ----------------------------------------------------
    // 2. PROCEDURAL ANIMATIONS (Bobbing, Breathing, Jump, Wobble)
    // ----------------------------------------------------
    val infiniteTransition = rememberInfiniteTransition(label = "CharacterMotion")

    // Idle subtle breathing on body & backpack
    val idleBreathingY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleBreathing"
    )
    val idleScaleY by infiniteTransition.animateFloat(
        initialValue = 0.99f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleScale"
    )

    // Run dynamic body vertical bobbing & stride tilt
    val runBobY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RunBob"
    )
    val runTiltZ by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RunTilt"
    )

    // Victory celebration bounce
    val victoryBounceY by infiniteTransition.animateFloat(
        initialValue = -24f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VictoryBounce"
    )

    // Dynamic jump elevation
    val jumpOffsetY = remember { Animatable(0f) }
    val jumpSquashScaleX = remember { Animatable(1f) }
    val jumpSquashScaleY = remember { Animatable(1f) }

    // Hit wobble for Wrong Answer
    val wrongShakeX = remember { Animatable(0f) }

    LaunchedEffect(animState) {
        when (animState) {
            PlayerAnimState.JUMP, PlayerAnimState.CORRECT_ANSWER -> {
                // Take-off anticipation squash
                jumpSquashScaleX.animateTo(1.15f, tween(80))
                jumpSquashScaleY.animateTo(0.85f, tween(80))
                // Take-off leap!
                jumpSquashScaleX.animateTo(0.92f, tween(120))
                jumpSquashScaleY.animateTo(1.10f, tween(120))
                jumpOffsetY.animateTo(-65f, tween(260, easing = FastOutSlowInEasing))
                // Landing descent
                jumpOffsetY.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
                // Landing compression squash
                jumpSquashScaleX.animateTo(1.18f, tween(90))
                jumpSquashScaleY.animateTo(0.82f, tween(90))
                // Restore to normal
                jumpSquashScaleX.animateTo(1f, tween(120))
                jumpSquashScaleY.animateTo(1f, tween(120))
            }
            PlayerAnimState.WRONG_ANSWER -> {
                // Stumble recoil shake
                repeat(4) {
                    wrongShakeX.animateTo(14f, tween(50))
                    wrongShakeX.animateTo(-14f, tween(50))
                }
                wrongShakeX.animateTo(0f, tween(80))
            }
            else -> {
                jumpOffsetY.snapTo(0f)
                jumpSquashScaleX.snapTo(1f)
                jumpSquashScaleY.snapTo(1f)
                wrongShakeX.snapTo(0f)
            }
        }
    }

    // Determine current visual drawable
    val currentDrawable = when (animState) {
        PlayerAnimState.IDLE -> R.drawable.player_run_frame1
        PlayerAnimState.RUN -> runFrames[currentFrameIndex]
        PlayerAnimState.JUMP -> R.drawable.player_run_frame4
        PlayerAnimState.COLLECT_COIN -> R.drawable.player_run_frame2
        PlayerAnimState.CORRECT_ANSWER -> R.drawable.player_run_frame4
        PlayerAnimState.WRONG_ANSWER -> R.drawable.player_run_frame3
        PlayerAnimState.VICTORY -> R.drawable.player_run_frame4
    }

    // Determine transforms based on state
    val totalOffsetY = when (animState) {
        PlayerAnimState.IDLE -> idleBreathingY
        PlayerAnimState.RUN -> runBobY
        PlayerAnimState.JUMP, PlayerAnimState.CORRECT_ANSWER -> jumpOffsetY.value
        PlayerAnimState.COLLECT_COIN -> runBobY - 6f
        PlayerAnimState.WRONG_ANSWER -> 6f
        PlayerAnimState.VICTORY -> victoryBounceY
    }

    val totalScaleX = when (animState) {
        PlayerAnimState.IDLE -> 1f
        PlayerAnimState.RUN -> 1f
        PlayerAnimState.JUMP, PlayerAnimState.CORRECT_ANSWER -> jumpSquashScaleX.value
        PlayerAnimState.COLLECT_COIN -> 1.05f
        PlayerAnimState.WRONG_ANSWER -> 0.95f
        PlayerAnimState.VICTORY -> 1.1f
    }

    val totalScaleY = when (animState) {
        PlayerAnimState.IDLE -> idleScaleY
        PlayerAnimState.RUN -> 1f
        PlayerAnimState.JUMP, PlayerAnimState.CORRECT_ANSWER -> jumpSquashScaleY.value
        PlayerAnimState.COLLECT_COIN -> 1.05f
        PlayerAnimState.WRONG_ANSWER -> 0.90f
        PlayerAnimState.VICTORY -> 1.1f
    }

    val rotationZ = when (animState) {
        PlayerAnimState.RUN -> runTiltZ
        PlayerAnimState.WRONG_ANSWER -> -8f
        PlayerAnimState.COLLECT_COIN -> 4f
        else -> 0f
    }

    Box(
        modifier = modifier.size(width = 120.dp, height = 150.dp),
        contentAlignment = Alignment.Center
    ) {
        // ----------------------------------------------------
        // GROUND SHADOW (Grows and shrinks with jump)
        // ----------------------------------------------------
        val shadowScale = (1.0f - (-totalOffsetY / 120f)).coerceIn(0.4f, 1.2f)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-6).dp)
                .width((74 * shadowScale).dp)
                .height((16 * shadowScale).dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f * shadowScale))
        )

        // ----------------------------------------------------
        // SPARKLE / CELEBRATION AURA OVERLAYS
        // ----------------------------------------------------
        if (animState == PlayerAnimState.COLLECT_COIN) {
            // Gold Sparkles around character
            Text(
                text = "✨",
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-8).dp)
            )
            Text(
                text = "🪙",
                fontSize = 18.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-8).dp, y = 4.dp)
            )
        }

        if (animState == PlayerAnimState.CORRECT_ANSWER) {
            // Green Positive Aura & Stars
            Text(
                text = "⭐",
                fontSize = 22.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-28).dp)
            )
            Text(
                text = "✨",
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 12.dp)
            )
        }

        if (animState == PlayerAnimState.WRONG_ANSWER) {
            // Shock / Sweat drops
            Text(
                text = "💥",
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-24).dp)
            )
        }

        if (animState == PlayerAnimState.VICTORY) {
            // Trophy and Stars
            Text(
                text = "🏆",
                fontSize = 26.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-36).dp)
            )
        }

        // ----------------------------------------------------
        // MAIN CHARACTER 2D SPRITE
        // (Clean back view with hoodie, Sigma cap Σ, Sigma backpack Σ, sneakers)
        // ----------------------------------------------------
        Image(
            painter = painterResource(id = currentDrawable),
            contentDescription = "Math Runner Character Sprite",
            contentScale = ContentScale.Fit,
            colorFilter = if (animState == PlayerAnimState.WRONG_ANSWER) {
                ColorFilter.tint(Color(0xFFFF6B6B).copy(alpha = 0.45f))
            } else null,
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = wrongShakeX.value.roundToInt(),
                        y = totalOffsetY.roundToInt()
                    )
                }
                .rotate(rotationZ)
                .scale(scaleX = totalScaleX, scaleY = totalScaleY)
        )
    }
}
