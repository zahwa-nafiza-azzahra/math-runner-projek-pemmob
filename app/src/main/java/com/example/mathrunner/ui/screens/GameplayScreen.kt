package com.example.mathrunner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathrunner.R
import com.example.mathrunner.ui.components.AnimatedPlayerCharacter
import com.example.mathrunner.ui.components.PlayerAnimState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

// Math Question Data Class
data class OfficialMathQuestion(
    val equation: String,
    val options: List<Int>,
    val correctIndex: Int
)

fun createOfficialMathQuestion(): OfficialMathQuestion {
    val operations = listOf("×", "+", "-")
    val op = operations[Random.nextInt(operations.size)]
    val (num1, num2, answer) = when (op) {
        "×" -> {
            val a = Random.nextInt(3, 10)
            val b = Random.nextInt(2, 9)
            Triple(a, b, a * b)
        }
        "+" -> {
            val a = Random.nextInt(8, 45)
            val b = Random.nextInt(5, 40)
            Triple(a, b, a + b)
        }
        else -> {
            val a = Random.nextInt(20, 70)
            val b = Random.nextInt(5, a)
            Triple(a, b, a - b)
        }
    }

    val wrongOptions = mutableSetOf<Int>()
    while (wrongOptions.size < 3) {
        val delta = Random.nextInt(-10, 11)
        val candidate = answer + delta
        if (candidate != answer && candidate > 0) {
            wrongOptions.add(candidate)
        }
    }
    val optionList = wrongOptions.toList()
    val correctPos = Random.nextInt(4)
    val finalOptions = mutableListOf<Int>().apply {
        addAll(optionList.take(3))
        add(correctPos, answer)
    }

    return OfficialMathQuestion("$num1 $op $num2 = ?", finalOptions, correctPos)
}

@Composable
fun GameplayScreen(
    levelNumber: Int = 25,
    onExitGame: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Game stats
    var score by remember { mutableIntStateOf(1250) }
    var coinsCollected by remember { mutableIntStateOf(24) }
    var combo by remember { mutableIntStateOf(1) }
    var lives by remember { mutableIntStateOf(3) }
    var distanceProgress by remember { mutableFloatStateOf(0.12f) }
    var questionsAnswered by remember { mutableIntStateOf(0) }
    val totalQuestionsForLevel = 5

    // Character Animation State (1. Idle, 2. Run, 3. Jump, 4. Collect Coin, 5. Correct, 6. Wrong, 7. Victory)
    var playerAnimState by remember { mutableStateOf(PlayerAnimState.RUN) }

    // Runner lane: -1 (Left), 0 (Middle), 1 (Right)
    var currentLane by remember { mutableIntStateOf(0) }
    val animatedLaneX by animateFloatAsState(
        targetValue = currentLane.toFloat(),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "LaneX"
    )

    // Question State & Stop flow
    var isAnsweringQuestion by remember { mutableStateOf(false) }
    var currentQuestion by remember { mutableStateOf(createOfficialMathQuestion()) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isQuestionCorrect by remember { mutableStateOf<Boolean?>(null) }

    // Game Modals State
    var isPaused by remember { mutableStateOf(false) }
    var isLevelComplete by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Running loop & Question Encountes
    LaunchedEffect(isAnsweringQuestion, isPaused, isLevelComplete, isGameOver) {
        if (!isAnsweringQuestion && !isPaused && !isLevelComplete && !isGameOver) {
            playerAnimState = PlayerAnimState.RUN

            // Run for 3.5 seconds collecting coins
            val runTime = 3500L
            val startT = System.currentTimeMillis()
            while (System.currentTimeMillis() - startT < runTime) {
                delay(120)
                if (isPaused || isLevelComplete || isGameOver) break
                distanceProgress = (distanceProgress + 0.006f).coerceAtMost(0.99f)
            }

            if (!isPaused && !isLevelComplete && !isGameOver) {
                // STOP RUNNER! Enter IDLE state and present Math Question
                currentQuestion = createOfficialMathQuestion()
                selectedOptionIndex = null
                isQuestionCorrect = null
                feedbackMessage = null
                playerAnimState = PlayerAnimState.IDLE
                isAnsweringQuestion = true
            }
        }
    }

    // Handle user answering question
    fun handleAnswer(index: Int) {
        if (selectedOptionIndex != null || isLevelComplete || isGameOver) return
        selectedOptionIndex = index

        if (index == currentQuestion.correctIndex) {
            // 5. CORRECT ANSWER: Positive celebration leap + green aura
            isQuestionCorrect = true
            val earnedScore = 150 * combo
            score += earnedScore
            coinsCollected += 5
            combo++
            questionsAnswered++
            distanceProgress = (distanceProgress + (1.0f / totalQuestionsForLevel)).coerceAtMost(1.0f)
            feedbackMessage = "✨ BENAR! +$earnedScore ⭐"
            playerAnimState = PlayerAnimState.CORRECT_ANSWER

            coroutineScope.launch {
                delay(850)
                if (distanceProgress >= 1.0f || questionsAnswered >= totalQuestionsForLevel) {
                    // 7. VICTORY ANIMATION
                    playerAnimState = PlayerAnimState.VICTORY
                    delay(300)
                    isLevelComplete = true
                } else {
                    // Resume running!
                    selectedOptionIndex = null
                    isQuestionCorrect = null
                    feedbackMessage = null
                    isAnsweringQuestion = false
                    playerAnimState = PlayerAnimState.RUN
                }
            }
        } else {
            // 6. WRONG ANSWER: Obstacle hit stumble shake + red tint
            isQuestionCorrect = false
            combo = 1
            lives = (lives - 1).coerceAtLeast(0)
            feedbackMessage = "SALAH! -1 ❤️"
            playerAnimState = PlayerAnimState.WRONG_ANSWER

            coroutineScope.launch {
                delay(950)
                if (lives <= 0) {
                    isGameOver = true
                } else {
                    selectedOptionIndex = null
                    isQuestionCorrect = null
                    feedbackMessage = null
                    playerAnimState = PlayerAnimState.IDLE
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Touch & Drag controls
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount.x > 25) {
                            if (currentLane < 1) currentLane++
                            // 4. Collect coin state on active steering
                            if (!isAnsweringQuestion) {
                                coinsCollected++
                                score += 10
                                playerAnimState = PlayerAnimState.COLLECT_COIN
                            }
                        } else if (dragAmount.x < -25) {
                            if (currentLane > -1) currentLane--
                            if (!isAnsweringQuestion) {
                                coinsCollected++
                                score += 10
                                playerAnimState = PlayerAnimState.COLLECT_COIN
                            }
                        } else if (dragAmount.y < -30 && !isAnsweringQuestion) {
                            // 3. Jump gesture!
                            playerAnimState = PlayerAnimState.JUMP
                        }
                    }
                )
            }
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // ==========================================
        // 1. GAME ENVIRONMENT BACKGROUND (game_background.png)
        // ==========================================
        Image(
            painter = painterResource(id = R.drawable.game_background),
            contentDescription = "Math Runner Floating Island Scenery",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay for contrast and ambiance
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (isAnsweringQuestion) 0.55f else 0.25f)
                        )
                    )
                )
        )

        // ==========================================
        // 2. OBSTACLES & MATH PLATFORM ON PATH
        // ==========================================
        if (!isAnsweringQuestion) {
            // Floating Coin Item along the path
            Image(
                painter = painterResource(id = R.drawable.item_coin_single),
                contentDescription = "Collectible Coin",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (currentLane * 60).dp, y = (-70).dp)
                    .size(36.dp)
            )

            // Red Block (X) / Green Block (÷) Obstacles
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-130).dp)
                    .fillMaxWidth(0.65f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.obstacle_block_red),
                    contentDescription = "Red Block X",
                    modifier = Modifier.size(44.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.obstacle_block_green),
                    contentDescription = "Green Block Divide",
                    modifier = Modifier.size(44.dp)
                )
            }
        } else {
            // When answering question, show Math Answer Platform underneath runner!
            Image(
                painter = painterResource(id = R.drawable.math_answer_platform),
                contentDescription = "Math Answer Platform",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 110.dp)
                    .size(width = 160.dp, height = 70.dp),
                contentScale = ContentScale.Fit
            )
        }

        // ==========================================
        // 3. 2D SPRITE ANIMATED CHARACTER
        // (1. Idle, 2. Run, 3. Jump, 4. Collect Coin, 5. Correct, 6. Wrong, 7. Victory)
        // ==========================================
        val playerXOffset = (screenWidth.value * 0.26f) * animatedLaneX

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(
                    x = playerXOffset.dp,
                    y = if (isAnsweringQuestion) 40.dp else 55.dp
                )
        ) {
            AnimatedPlayerCharacter(
                animState = if (isPaused) PlayerAnimState.IDLE else playerAnimState,
                modifier = Modifier.size(width = 135.dp, height = 165.dp)
            )
        }

        // ==========================================
        // 4. TOP HUD: LEVEL, SCORE, PROGRESS BAR, PAUSE
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 14.dp, end = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEVEL 25 Badge
                Box(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                            )
                        )
                        .border(2.5.dp, Color(0xFF7DD3FC), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LEVEL ",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$levelNumber",
                            color = Color(0xFFFDE047),
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                }

                // SCORE 1,250 Badge with Trophy
                Box(
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                            )
                        )
                        .border(2.5.dp, Color(0xFF7DD3FC), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆 ", fontSize = 16.sp)
                        Column {
                            Text(
                                text = "SCORE",
                                color = Color(0xFFBAE6FD),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                            Text(
                                text = String.format("%,d", score),
                                color = Color(0xFFFDE047),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Pause Button (⏸)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                            )
                        )
                        .border(2.5.dp, Color(0xFF7DD3FC), RoundedCornerShape(12.dp))
                        .clickable { isPaused = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏸",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar with Running Boy Avatar and Finish Flag 🏁
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0C4A6E).copy(alpha = 0.85f))
                    .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(distanceProgress)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF22C55E), Color(0xFF4ADE80), Color(0xFF86EFAC))
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(distanceProgress)
                ) {
                    Text(
                        text = "🏃",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 6.dp)
                    )
                }

                Text(
                    text = "🏁",
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                )
            }
        }

        // ==========================================
        // 5. RUNNING STATUS BAR (When running)
        // ==========================================
        if (!isAnsweringQuestion && !isPaused && !isLevelComplete) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0284C7).copy(alpha = 0.90f))
                    .border(2.dp, Color(0xFF7DD3FC), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏃💨 ", fontSize = 16.sp)
                    Text(
                        text = "Usap Kiri/Kanan untuk hindari obstacle & ambil koin!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ==========================================
        // 6. FLOATING FEEDBACK BANNER
        // ==========================================
        feedbackMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 110.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isQuestionCorrect == true) Color(0xFF15803D) else Color(0xFFB91C1C)
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }

        // ==========================================
        // 7. QUESTION PANEL (7 x 6 = ?) & 4 ANSWER BUTTONS
        // ==========================================
        AnimatedVisibility(
            visible = isAnsweringQuestion && !isPaused && !isLevelComplete,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 200 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 200 }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // STOPPED NOTIFICATION
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.9f))
                        .border(1.5.dp, Color.White, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🛑 BERHENTI! Jawab soal untuk lanjut lari 🛑",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Question Card (7 x 6 = ?)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .shadow(14.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF93C5FD))
                            )
                        )
                        .border(3.dp, Color.White, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "➕",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 10.dp, top = 6.dp)
                    )
                    Text(
                        text = "➖",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 10.dp, bottom = 6.dp)
                    )
                    Text(
                        text = "✖️",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 10.dp, bottom = 6.dp)
                    )

                    Text(
                        text = currentQuestion.equation,
                        color = Color(0xFF0F172A),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4 Answer Option Pill Buttons (2x2 Grid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnswerOptionPill(
                        optionValue = currentQuestion.options[0],
                        isChosen = selectedOptionIndex == 0,
                        isCorrect = isQuestionCorrect,
                        modifier = Modifier.weight(1f),
                        onClick = { handleAnswer(0) }
                    )
                    AnswerOptionPill(
                        optionValue = currentQuestion.options[1],
                        isChosen = selectedOptionIndex == 1,
                        isCorrect = isQuestionCorrect,
                        modifier = Modifier.weight(1f),
                        onClick = { handleAnswer(1) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnswerOptionPill(
                        optionValue = currentQuestion.options[2],
                        isChosen = selectedOptionIndex == 2,
                        isCorrect = isQuestionCorrect,
                        modifier = Modifier.weight(1f),
                        onClick = { handleAnswer(2) }
                    )
                    AnswerOptionPill(
                        optionValue = currentQuestion.options[3],
                        isChosen = selectedOptionIndex == 3,
                        isCorrect = isQuestionCorrect,
                        modifier = Modifier.weight(1f),
                        onClick = { handleAnswer(3) }
                    )
                }
            }
        }

        // ==========================================
        // 8. PAUSED MODAL (Matches paused.png)
        // ==========================================
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .shadow(16.dp, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF38BDF8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨ PAUSED ✨",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("LEVEL", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("$levelNumber", fontSize = 24.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SCORE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(String.format("%,d", score), fontSize = 24.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        ModalActionButton(
                            text = "▶  RESUME",
                            gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                            onClick = { isPaused = false }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ModalActionButton(
                            text = "🔄  RESTART",
                            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                            onClick = {
                                score = 1250
                                lives = 3
                                distanceProgress = 0.12f
                                questionsAnswered = 0
                                isAnsweringQuestion = false
                                playerAnimState = PlayerAnimState.RUN
                                selectedOptionIndex = null
                                isQuestionCorrect = null
                                isPaused = false
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ModalActionButton(
                            text = "🏠  EXIT TO LEVELS",
                            gradientColors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)),
                            onClick = onExitGame
                        )
                    }
                }
            }
        }

        // ==========================================
        // 9. LEVEL COMPLETE MODAL (Matches setelah-main.png)
        // ==========================================
        if (isLevelComplete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f)
                        .shadow(20.dp, RoundedCornerShape(32.dp)),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF38BDF8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎉 LEVEL COMPLETE! 🎉",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScoreStatCard(
                                title = "CURRENT SCORE",
                                value = String.format("%,d", score),
                                icon = "🏅",
                                modifier = Modifier.weight(1f)
                            )
                            ScoreStatCard(
                                title = "BEST SCORE",
                                value = String.format("%,d", score + 100),
                                icon = "👑",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF0F9FF))
                                .border(1.5.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp))
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(title = "Correct", value = "$questionsAnswered/$totalQuestionsForLevel", icon = "✅")
                            StatItem(title = "Accuracy", value = "100%", icon = "🎯")
                            StatItem(title = "Distance", value = "1.5 km", icon = "🏃")
                            StatItem(title = "Bonus", value = "+$coinsCollected", icon = "🪙")
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            ModalActionButton(
                                text = "▶  NEXT LEVEL",
                                gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                                onClick = {
                                    score += 500
                                    distanceProgress = 0.12f
                                    questionsAnswered = 0
                                    selectedOptionIndex = null
                                    isQuestionCorrect = null
                                    isAnsweringQuestion = false
                                    playerAnimState = PlayerAnimState.RUN
                                    isLevelComplete = false
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ModalActionButton(
                                        text = "🔄 REPLAY",
                                        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                                        onClick = {
                                            score = 1250
                                            distanceProgress = 0.12f
                                            questionsAnswered = 0
                                            selectedOptionIndex = null
                                            isQuestionCorrect = null
                                            isAnsweringQuestion = false
                                            playerAnimState = PlayerAnimState.RUN
                                            isLevelComplete = false
                                        }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ModalActionButton(
                                        text = "🏠 LEVELS",
                                        gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                                        onClick = onExitGame
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4 Answer Option Pill Component
@Composable
private fun AnswerOptionPill(
    optionValue: Int,
    isChosen: Boolean,
    isCorrect: Boolean?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "PillScale"
    )

    val backgroundBrush = when {
        isChosen && isCorrect == true -> Brush.verticalGradient(
            listOf(Color(0xFF22C55E), Color(0xFF15803D))
        )
        isChosen && isCorrect == false -> Brush.verticalGradient(
            listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF0284C7), Color(0xFF0369A1))
        )
    }

    val borderColor = when {
        isChosen && isCorrect == true -> Color(0xFF86EFAC)
        isChosen && isCorrect == false -> Color(0xFFFCA5A5)
        else -> Color(0xFF7DD3FC)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundBrush)
            .border(2.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$optionValue",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            style = TextStyle(
                shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(2f, 2f), 3f)
            )
        )
    }
}

// Modal Action Button Component
@Composable
private fun ModalActionButton(
    text: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "ModalBtn"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth()
            .height(50.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(gradientColors))
            .border(2.5.dp, Color.White, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

// Score Stat Card in Level Complete
@Composable
private fun ScoreStatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF0F9FF))
            .border(1.5.dp, Color(0xFFBAE6FD), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 20.sp)
            Text(text = title, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 18.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
        }
    }
}

// Level Stat Item
@Composable
private fun StatItem(title: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 16.sp)
        Text(text = value, fontSize = 14.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
        Text(text = title, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}
