package com.example.mathrunner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathrunner.R
import com.example.mathrunner.ui.components.LoopingVideoBackground
import kotlinx.coroutines.delay
import kotlin.random.Random

data class MathQuestion(
    val equation: String,
    val options: List<Int>,
    val correctIndex: Int
)

fun generateMathQuestion(): MathQuestion {
    val operations = listOf("+", "-", "×")
    val op = operations[Random.nextInt(operations.size)]
    val (num1, num2, answer) = when (op) {
        "+" -> {
            val a = Random.nextInt(10, 50)
            val b = Random.nextInt(5, 40)
            Triple(a, b, a + b)
        }
        "-" -> {
            val a = Random.nextInt(20, 80)
            val b = Random.nextInt(5, a)
            Triple(a, b, a - b)
        }
        else -> {
            val a = Random.nextInt(3, 12)
            val b = Random.nextInt(2, 10)
            Triple(a, b, a * b)
        }
    }
    val wrong1 = answer + Random.nextInt(1, 5) * if (Random.nextBoolean()) 1 else -1
    val wrong2 = answer + Random.nextInt(6, 10) * if (Random.nextBoolean()) 1 else -1
    val correctPos = Random.nextInt(3)
    val options = mutableListOf(wrong1, wrong2).apply {
        add(correctPos, answer)
    }
    return MathQuestion("$num1 $op $num2 = ?", options, correctPos)
}

@Composable
fun GameplayScreen(
    onExitGame: () -> Unit
) {
    var score by remember { mutableIntStateOf(350) }
    var coins by remember { mutableIntStateOf(18) }
    var combo by remember { mutableIntStateOf(1) }
    var lives by remember { mutableIntStateOf(3) }
    var isPaused by remember { mutableStateOf(false) }
    var currentQuestion by remember { mutableStateOf(generateMathQuestion()) }
    var feedbackText by remember { mutableStateOf<String?>(null) }
    var feedbackColor by remember { mutableStateOf(Color.Green) }

    LaunchedEffect(feedbackText) {
        if (feedbackText != null) {
            delay(1000)
            feedbackText = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Moving Video Animation Background: Boy Collecting Coins On Path
        LoopingVideoBackground(
            videoResId = R.raw.boy_collecting_coins_on_path,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        // Game UI HUD (Top Stats & Questions)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP HUD: Score, Coins, Lives, Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Combo Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F52BA).copy(alpha = 0.85f))
                        .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = "⭐ SCORE: $score",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        if (combo > 1) {
                            Text(
                                text = "🔥 COMBO x$combo",
                                color = Color(0xFFFDE047),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Coins Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F52BA).copy(alpha = 0.85f))
                        .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🪙 $coins",
                        color = Color(0xFFFEF08A),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                // Lives Hearts
                Row {
                    repeat(3) { index ->
                        Text(
                            text = if (index < lives) "❤️" else "🖤",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                // Pause Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { isPaused = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⏸", color = Color.White, fontSize = 16.sp)
                }
            }

            // CENTER: Math Gate Question Banner
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                feedbackText?.let { fb ->
                    Text(
                        text = fb,
                        color = feedbackColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Question Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.9f)),
                    border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF7DD3FC))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SOLVE TO RUN THROUGH! 🏃💨",
                            color = Color(0xFFBAE6FD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentQuestion.equation,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(
                                shadow = Shadow(Color.Black.copy(alpha = 0.6f), Offset(2f, 3f), 4f)
                            )
                        )
                    }
                }
            }

            // BOTTOM: 3 Choice Answer Gates
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TAP THE CORRECT ANSWER GATE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    style = TextStyle(
                        shadow = Shadow(Color.Black, Offset(1f, 2f), 3f)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    currentQuestion.options.forEachIndexed { index, optionVal ->
                        AnswerGateButton(
                            optionText = "$optionVal",
                            accentColor = when (index) {
                                0 -> Color(0xFF10B981) // Green
                                1 -> Color(0xFFF59E0B) // Amber
                                else -> Color(0xFF8B5CF6) // Purple
                            },
                            onClick = {
                                if (index == currentQuestion.correctIndex) {
                                    score += 100 * combo
                                    coins += 5
                                    combo++
                                    feedbackText = "CORRECT! +${100 * combo} pts ✨"
                                    feedbackColor = Color(0xFF22C55E)
                                } else {
                                    combo = 1
                                    lives = (lives - 1).coerceAtLeast(0)
                                    feedbackText = "WRONG GATE! -1 ❤️"
                                    feedbackColor = Color(0xFFEF4444)
                                    if (lives == 0) {
                                        isPaused = true
                                    }
                                }
                                currentQuestion = generateMathQuestion()
                            }
                        )
                    }
                }
            }
        }

        // PAUSE / GAME OVER DIALOG MODAL (Matches paused.png style)
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .shadow(16.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF38BDF8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (lives > 0) "⏸ GAME PAUSED" else "💥 GAME OVER",
                            color = if (lives > 0) Color(0xFFFDE047) else Color(0xFFEF4444),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "⭐ Score: $score\n🪙 Coins Collected: $coins",
                            color = Color(0xFFBAE6FD),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        if (lives > 0) {
                            Button(
                                onClick = { isPaused = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                Text("▶ RESUME RUN", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        } else {
                            Button(
                                onClick = {
                                    lives = 3
                                    score = 0
                                    coins = 0
                                    combo = 1
                                    isPaused = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                Text("🔄 PLAY AGAIN", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Button(
                            onClick = onExitGame,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("🏠 EXIT TO MAIN MENU", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerGateButton(
    optionText: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "GateScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(width = 98.dp, height = 74.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.75f)
                    )
                )
            )
            .border(3.dp, Color.White, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = optionText,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            style = TextStyle(
                shadow = Shadow(Color.Black.copy(alpha = 0.6f), Offset(2f, 2f), 3f)
            )
        )
    }
}
