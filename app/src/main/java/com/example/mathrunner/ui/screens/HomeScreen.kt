package com.example.mathrunner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathrunner.R
import com.example.mathrunner.ui.components.LoopingVideoBackground
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onBackToSplash: () -> Unit = {},
    onStartGame: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    var activeModal by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fallback static background image
        Image(
            painter = painterResource(id = R.drawable.main_menu_bg),
            contentDescription = "Math Runner Fallback Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 1. Moving Video Animation Background: Boy Running Toward Cloud Castle
        LoopingVideoBackground(
            videoResId = R.raw.boy_running_toward_cloud_castle,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay for readability of UI overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.28f)
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -30 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ==================== TOP BAR WIDGETS (FROM a1.png) ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // XP Widget Asset
                    Image(
                        painter = painterResource(id = R.drawable.widget_xp),
                        contentDescription = "XP Stat Widget",
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { activeModal = "STATISTICS" },
                        contentScale = ContentScale.FillHeight
                    )

                    // Coins Widget Asset
                    Image(
                        painter = painterResource(id = R.drawable.widget_coins),
                        contentDescription = "Coins Widget",
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { activeModal = "DAILY" },
                        contentScale = ContentScale.FillHeight
                    )

                    // Rank Title Widget Asset
                    Image(
                        painter = painterResource(id = R.drawable.widget_rank),
                        contentDescription = "Smart Runner Rank Widget",
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { activeModal = "ACHIEVEMENTS" },
                        contentScale = ContentScale.FillHeight
                    )

                    // Settings Gear Button Asset
                    InteractiveImageButton(
                        drawableId = R.drawable.btn_settings,
                        contentDescription = "Settings Button",
                        modifier = Modifier.size(42.dp),
                        onClick = { activeModal = "SETTINGS" }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ==================== SIDE MENU BUTTONS (FROM a1.png) ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column (Achievements & Statistics)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        InteractiveImageButton(
                            drawableId = R.drawable.btn_achievements,
                            contentDescription = "Achievements Button",
                            modifier = Modifier.size(width = 72.dp, height = 80.dp),
                            onClick = { activeModal = "ACHIEVEMENTS" }
                        )
                        InteractiveImageButton(
                            drawableId = R.drawable.btn_statistics,
                            contentDescription = "Statistics Button",
                            modifier = Modifier.size(width = 72.dp, height = 78.dp),
                            onClick = { activeModal = "STATISTICS" }
                        )
                    }

                    // Right Column (Leaderboard & Daily Challenge)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        InteractiveImageButton(
                            drawableId = R.drawable.btn_leaderboard,
                            contentDescription = "Leaderboard Button",
                            modifier = Modifier.size(width = 72.dp, height = 80.dp),
                            onClick = { activeModal = "LEADERBOARD" }
                        )
                        InteractiveImageButton(
                            drawableId = R.drawable.btn_daily,
                            contentDescription = "Daily Challenge Button",
                            modifier = Modifier.size(width = 72.dp, height = 80.dp),
                            onClick = { activeModal = "DAILY" }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ==================== BOTTOM CONTROLS (FROM a1.png) ====================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Glossy PLAY Button Asset -> Launches animated gameplay screen!
                    InteractiveImageButton(
                        drawableId = R.drawable.btn_play,
                        contentDescription = "Play Button",
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(76.dp),
                        onClick = { onStartGame() }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Glossy LEVELS Button Asset
                    InteractiveImageButton(
                        drawableId = R.drawable.btn_levels,
                        contentDescription = "Levels Button",
                        modifier = Modifier
                            .fillMaxWidth(0.64f)
                            .height(48.dp),
                        onClick = { activeModal = "LEVELS" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Level 25/80 & Best Score 3,250 Card Asset
                    Image(
                        painter = painterResource(id = R.drawable.card_level_score),
                        contentDescription = "Level and Best Score Summary Card",
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .height(64.dp)
                            .clickable { activeModal = "LEVELS" },
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }

        // ==================== INTERACTIVE MODALS / DIALOGS ====================
        activeModal?.let { modal ->
            AlertDialog(
                onDismissRequest = { activeModal = null },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xFF0F172A),
                title = {
                    Text(
                        text = when (modal) {
                            "LEVELS" -> "🗺️ SELECT LEVEL"
                            "ACHIEVEMENTS" -> "🏆 ACHIEVEMENTS"
                            "STATISTICS" -> "📊 PLAYER STATISTICS"
                            "LEADERBOARD" -> "🥇 LEADERBOARD"
                            "DAILY" -> "📅 DAILY CHALLENGE"
                            "SETTINGS" -> "⚙️ GAME SETTINGS"
                            else -> ""
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (modal) {
                            "LEVELS" -> {
                                Text(
                                    text = "Current Stage: World 1 (Level 25/80)",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(5),
                                    modifier = Modifier.height(180.dp)
                                ) {
                                    items(25) { idx ->
                                        val lvlNum = idx + 1
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (lvlNum == 25) Color(0xFFF59E0B) else Color(0xFF0284C7))
                                                .clickable {
                                                    activeModal = null
                                                    onStartGame()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$lvlNum",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                            "ACHIEVEMENTS" -> {
                                Text(
                                    text = "⭐ Math Speed Demon - 100% Unlocked\n🧠 Master Solver - 50 Equations Solved\n🏃 Marathon Runner - 5,000 Meters",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 13.sp
                                )
                            }
                            "STATISTICS" -> {
                                Text(
                                    text = "Equations Solved: 1,420\nAccuracy: 94.2%\nTotal Coins: 2,450 🪙",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 14.sp
                                )
                            }
                            "LEADERBOARD" -> {
                                Text(
                                    text = "1. Alex_Math - 12,450 pts 🥇\n2. Sarah_Runner - 9,800 pts 🥈\n3. You (Level 25) - 3,250 pts 🥉",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 13.sp
                                )
                            }
                            "DAILY" -> {
                                Text(
                                    text = "Today's Quest:\nSolve 10 Multiplication equations in 60 seconds!\nReward: 🪙 +500 Coins",
                                    color = Color(0xFFBAE6FD),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                            "SETTINGS" -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Music: ON 🎵  |  Sound: ON 🔊", color = Color.White)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            activeModal = null
                                            onBackToSplash()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("✨ Replay Splash Screen", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeModal = null }) {
                        Text("Close", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

// ==================== INTERACTIVE BUTTON HELPER ====================

@Composable
private fun InteractiveImageButton(
    drawableId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ButtonScale"
    )

    Image(
        painter = painterResource(id = drawableId),
        contentDescription = contentDescription,
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentScale = ContentScale.Fit
    )
}
