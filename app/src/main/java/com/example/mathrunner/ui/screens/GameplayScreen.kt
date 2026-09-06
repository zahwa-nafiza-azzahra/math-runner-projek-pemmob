package com.example.mathrunner.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

// Represents a math question
data class RunnerMathQuestion(
    val equation: String,
    val options: List<Int>,
    val correctIndex: Int
)

// Represents dynamic coins on the track
data class TrackCoin(
    val id: Long,
    val lane: Int, // -1: Left, 0: Mid, 1: Right
    var yPos: Float, // 0.0 (far top) to 1.0 (player position)
    var collected: Boolean = false
)

// Generate balanced arithmetic questions
fun createRunnerQuestion(): RunnerMathQuestion {
    val operations = listOf("+", "-", "×")
    val op = operations[Random.nextInt(operations.size)]
    val (num1, num2, answer) = when (op) {
        "+" -> {
            val a = Random.nextInt(5, 30)
            val b = Random.nextInt(5, 30)
            Triple(a, b, a + b)
        }
        "-" -> {
            val a = Random.nextInt(15, 50)
            val b = Random.nextInt(3, a)
            Triple(a, b, a - b)
        }
        else -> {
            val a = Random.nextInt(2, 10)
            val b = Random.nextInt(2, 9)
            Triple(a, b, a * b)
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

    return RunnerMathQuestion("$num1 $op $num2 = ?", finalOptions, correctPos)
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
    var distanceProgress by remember { mutableFloatStateOf(0.15f) } // 0.0 to 1.0
    var questionsAnswered by remember { mutableIntStateOf(0) }
    val totalQuestionsForLevel = 5

    // Question State
    var currentQuestion by remember { mutableStateOf(createRunnerQuestion()) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isQuestionCorrect by remember { mutableStateOf<Boolean?>(null) }

    // Game State
    var isPaused by remember { mutableStateOf(false) }
    var isLevelComplete by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Runner character position & animation
    // Target lane: -1 (Left), 0 (Middle), 1 (Right)
    var currentLane by remember { mutableIntStateOf(0) }
    val animatedLaneX by animateFloatAsState(
        targetValue = currentLane.toFloat(),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "LaneX"
    )

    // Running bounce & jump animation
    val infiniteTransition = rememberInfiniteTransition(label = "RunBob")
    val runBobbing by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(260, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bob"
    )

    // Jump state for leaping over obstacles / answering correctly
    var isJumping by remember { mutableStateOf(false) }
    val jumpAnim = remember { Animatable(0f) }

    // Coins on track
    val coinsList = remember {
        mutableStateListOf(
            TrackCoin(1L, -1, 0.25f),
            TrackCoin(2L, 0, 0.45f),
            TrackCoin(3L, 0, 0.65f),
            TrackCoin(4L, 1, 0.35f),
            TrackCoin(5L, 1, 0.75f)
        )
    }

    // Road scroll effect
    var roadScrollOffset by remember { mutableFloatStateOf(0f) }

    // Main game loop (runs while not paused and not complete)
    LaunchedEffect(isPaused, isLevelComplete, isGameOver) {
        while (!isPaused && !isLevelComplete && !isGameOver) {
            delay(30)
            roadScrollOffset = (roadScrollOffset + 0.035f) % 1.0f

            // Move coins down towards player
            for (i in coinsList.indices) {
                val coin = coinsList[i]
                if (!coin.collected) {
                    val nextY = coin.yPos + 0.025f
                    if (nextY >= 0.85f && nextY <= 1.05f) {
                        // Check collision with player's lane
                        if (coin.lane == currentLane) {
                            coinsList[i] = coin.copy(collected = true)
                            coinsCollected += 1
                            score += 10 * combo
                        }
                    }
                    if (nextY > 1.2f) {
                        // Respawn at top with random lane
                        coinsList[i] = TrackCoin(
                            id = System.currentTimeMillis() + i,
                            lane = Random.nextInt(-1, 2),
                            yPos = 0.1f,
                            collected = false
                        )
                    } else {
                        coinsList[i] = coin.copy(yPos = nextY)
                    }
                }
            }
        }
    }

    // Function when user submits / taps an answer
    fun handleAnswer(index: Int) {
        if (selectedOptionIndex != null || isLevelComplete || isGameOver) return
        selectedOptionIndex = index

        if (index == currentQuestion.correctIndex) {
            // Correct Answer!
            isQuestionCorrect = true
            score += 150 * combo
            coinsCollected += 5
            combo++
            questionsAnswered++
            distanceProgress = (distanceProgress + (1.0f / totalQuestionsForLevel)).coerceAtMost(1.0f)
            feedbackMessage = "PERFECT! +${150 * combo} ⭐"

            // Trigger character leap animation!
            coroutineScope.launch {
                isJumping = true
                jumpAnim.animateTo(
                    targetValue = -70f,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                )
                jumpAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
                isJumping = false
            }

            // Check if level completed
            coroutineScope.launch {
                delay(800)
                if (distanceProgress >= 1.0f || questionsAnswered >= totalQuestionsForLevel) {
                    isLevelComplete = true
                } else {
                    // Next question
                    selectedOptionIndex = null
                    isQuestionCorrect = null
                    feedbackMessage = null
                    currentQuestion = createRunnerQuestion()
                }
            }
        } else {
            // Wrong Answer!
            isQuestionCorrect = false
            combo = 1
            lives = (lives - 1).coerceAtLeast(0)
            feedbackMessage = "WRONG! -1 ❤️"

            coroutineScope.launch {
                delay(900)
                if (lives <= 0) {
                    isGameOver = true
                } else {
                    selectedOptionIndex = null
                    isQuestionCorrect = null
                    feedbackMessage = null
                    currentQuestion = createRunnerQuestion()
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Swipe & Drag Gesture to steer the runner left, middle, right
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 20) {
                            // Swiped right
                            if (currentLane < 1) currentLane++
                        } else if (dragAmount < -20) {
                            // Swiped left
                            if (currentLane > -1) currentLane--
                        }
                    }
                )
            }
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // ==========================================
        // 1. 3D PERSPECTIVE RUNNER TRACK & SCENERY CANVAS
        // ==========================================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sky gradient (Vibrant blue with clouds)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF38BDF8),
                        Color(0xFF7DD3FC),
                        Color(0xFFBAE6FD),
                        Color(0xFFE0F2FE)
                    ),
                    startY = 0f,
                    endY = h * 0.45f
                ),
                size = Size(w, h * 0.45f)
            )

            // Horizon Clouds / Floating Islands backdrop
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = w * 0.35f,
                center = Offset(w * 0.2f, h * 0.22f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = w * 0.45f,
                center = Offset(w * 0.8f, h * 0.20f)
            )

            // Side scenery (Lush green cliffs)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF15803D), Color(0xFF166534)),
                    startY = h * 0.35f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.35f),
                size = Size(w, h * 0.65f)
            )

            // 3D Perspective Road Track
            // Top vanishing point (x: center, y: h * 0.35)
            val topRoadW = w * 0.22f
            val bottomRoadW = w * 0.92f
            val roadTopY = h * 0.35f
            val roadBottomY = h

            val roadPath = Path().apply {
                moveTo((w - topRoadW) / 2f, roadTopY)
                lineTo((w + topRoadW) / 2f, roadTopY)
                lineTo((w + bottomRoadW) / 2f, roadBottomY)
                lineTo((w - bottomRoadW) / 2f, roadBottomY)
                close()
            }

            // Road surface (Warm cobblestone yellow-orange)
            drawPath(
                path = roadPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFD97706),
                        Color(0xFFF59E0B),
                        Color(0xFFFBBF24),
                        Color(0xFFFDE68A)
                    ),
                    startY = roadTopY,
                    endY = roadBottomY
                )
            )

            // Road borders (Wooden / stone curbs)
            val leftCurb = Path().apply {
                moveTo((w - topRoadW) / 2f - 8f, roadTopY)
                lineTo((w - topRoadW) / 2f, roadTopY)
                lineTo((w - bottomRoadW) / 2f, roadBottomY)
                lineTo((w - bottomRoadW) / 2f - 30f, roadBottomY)
                close()
            }
            drawPath(leftCurb, Color(0xFF92400E))

            val rightCurb = Path().apply {
                moveTo((w + topRoadW) / 2f, roadTopY)
                lineTo((w + topRoadW) / 2f + 8f, roadTopY)
                lineTo((w + bottomRoadW) / 2f + 30f, roadBottomY)
                lineTo((w + bottomRoadW) / 2f, roadBottomY)
                close()
            }
            drawPath(rightCurb, Color(0xFF92400E))

            // Dynamic Road Stripes (Horizontal tiles scrolling forward)
            val numStripes = 8
            for (i in 0 until numStripes) {
                val t = (i.toFloat() / numStripes + roadScrollOffset / numStripes) % 1.0f
                // Perspective exponential curve
                val progress = t * t
                val curY = roadTopY + progress * (roadBottomY - roadTopY)
                val curW = topRoadW + progress * (bottomRoadW - topRoadW)
                val leftX = (w - curW) / 2f

                drawLine(
                    color = Color(0xFFB45309).copy(alpha = 0.45f),
                    start = Offset(leftX, curY),
                    end = Offset(leftX + curW, curY),
                    strokeWidth = 3f + progress * 6f
                )
            }

            // Lane Dividers (Dashed lines between Left, Mid, Right)
            for (laneDiv in listOf(-0.33f, 0.33f)) {
                val startX = w / 2f + (topRoadW / 2f) * laneDiv
                val endX = w / 2f + (bottomRoadW / 2f) * laneDiv
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(startX, roadTopY),
                    end = Offset(endX, roadBottomY),
                    strokeWidth = 3.5f
                )
            }

            // Draw Floating Track Coins
            coinsList.filter { !it.collected }.forEach { coin ->
                val progress = (coin.yPos * coin.yPos).coerceIn(0f, 1f)
                val curY = roadTopY + progress * (roadBottomY - roadTopY)
                val curRoadW = topRoadW + progress * (bottomRoadW - topRoadW)
                val laneOffsetFrac = when (coin.lane) {
                    -1 -> -0.33f
                    1 -> 0.33f
                    else -> 0f
                }
                val curX = w / 2f + (curRoadW / 2f) * laneOffsetFrac
                val coinRadius = (10f + progress * 24f)

                // Golden Coin glow & body
                drawCircle(
                    color = Color(0xFFFEF08A),
                    radius = coinRadius,
                    center = Offset(curX, curY)
                )
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = coinRadius * 0.8f,
                    center = Offset(curX, curY)
                )
                drawCircle(
                    color = Color(0xFFFDE047),
                    radius = coinRadius * 0.6f,
                    center = Offset(curX, curY)
                )
                // Star inside coin
                drawCircle(
                    color = Color(0xFFB45309),
                    radius = coinRadius * 0.25f,
                    center = Offset(curX, curY)
                )
            }
        }

        // ==========================================
        // 2. INTERACTIVE RUNNER CHARACTER (BOY SPRITE)
        // ==========================================
        val playerLaneOffsetPx = (screenWidth.value * 0.28f) * animatedLaneX
        val playerBaseY = screenHeight.value * 0.44f

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(
                    x = playerLaneOffsetPx.dp,
                    y = (playerBaseY + runBobbing + jumpAnim.value).dp
                )
                .size(130.dp, 160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Character Shadow on Track
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width((80 - jumpAnim.value * 0.4f).dp)
                    .height(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
            )

            // Runner Character Image / Silhouette
            Image(
                painter = painterResource(
                    id = if (isJumping) R.drawable.boy_jumping else R.drawable.runner_boy_sprite
                ),
                contentDescription = "Runner Character",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ==========================================
        // 3. TOP HUD: LEVEL, SCORE, LIVES, PROGRESS & PAUSE
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 14.dp, end = 14.dp)
        ) {
            // Top Pill Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEVEL Badge (Matches tampilan-main.png)
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

                // SCORE Badge with Trophy (Matches tampilan-main.png)
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

                // Pause Button (Matches tampilan-main.png)
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

            // Running Distance Progress Bar with Flag (Matches tampilan-main.png)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0C4A6E).copy(alpha = 0.8f))
                    .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
            ) {
                // Progress Fill
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

                // Running Boy Icon on Progress Bar
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

                // Finish Checkered Flag at end
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
        // 4. FLOATING FEEDBACK MESSAGE & COMBO
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
        // 5. BOTTOM SECTION: MATH QUESTION CARD & 4 ANSWER PILLS
        // (Matches tampilan-main.png exact layout)
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Swipe instruction hint
            Text(
                text = "👉 Geser jari ke Kiri/Tengah/Kanan untuk menggerakkan pelari 👈",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(Color.Black.copy(alpha = 0.8f), Offset(1f, 1f), 3f)
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Glossy Math Question Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF93C5FD))
                        )
                    )
                    .border(3.dp, Color.White, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Decorative math symbols in corners
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

                // The prominent equation: 7 × 6 = ?
                Text(
                    text = currentQuestion.equation,
                    color = Color(0xFF0F172A),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Answer Option Pill Buttons (2x2 Grid matching tampilan-main.png)
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

        // ==========================================
        // 6. PAUSED MODAL (Matches paused.png)
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
                        // PAUSED Title Banner
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

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Level info
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("LEVEL", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("$levelNumber", fontSize = 24.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Black)
                            }
                            // Score info
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SCORE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(String.format("%,d", score), fontSize = 24.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // RESUME Button (Green Pill)
                        ModalActionButton(
                            text = "▶  RESUME",
                            gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                            onClick = { isPaused = false }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // RESTART Button (Yellow/Orange Pill)
                        ModalActionButton(
                            text = "🔄  RESTART",
                            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                            onClick = {
                                score = 1250
                                lives = 3
                                distanceProgress = 0.15f
                                questionsAnswered = 0
                                selectedOptionIndex = null
                                isQuestionCorrect = null
                                currentQuestion = createRunnerQuestion()
                                isPaused = false
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // EXIT TO LEVELS Button (Red Pill)
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
        // 7. LEVEL COMPLETE MODAL (Matches setelah-main.png)
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
                        // Level Complete Header with celebration banner
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

                        // Score & Best Score Cards Row
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

                        // Level Statistics Row (Correct, Accuracy, Distance, Bonus)
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

                        // Action Buttons: NEXT LEVEL, REPLAY, LEVELS
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ModalActionButton(
                                text = "▶  NEXT LEVEL",
                                gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A)),
                                onClick = {
                                    score += 500
                                    distanceProgress = 0.1f
                                    questionsAnswered = 0
                                    selectedOptionIndex = null
                                    isQuestionCorrect = null
                                    isLevelComplete = false
                                    currentQuestion = createRunnerQuestion()
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
                                            distanceProgress = 0.15f
                                            questionsAnswered = 0
                                            selectedOptionIndex = null
                                            isQuestionCorrect = null
                                            isLevelComplete = false
                                            currentQuestion = createRunnerQuestion()
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

// 4 Answer Option Pill Component (Matches tampilan-main.png)
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
