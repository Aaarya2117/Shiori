
package com.shiori.app.ui.patient

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val GameColors = listOf(
    Color(0xFFE53935),
    Color(0xFF1E88E5),
    Color(0xFFFFB300),
    Color(0xFF43A047)
)

@Composable
fun PatternSequenceScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Save best score locally
    val preferences = remember(context) {
        context.getSharedPreferences(
            "pattern_sequence_prefs",
            Context.MODE_PRIVATE
        )
    }

    var bestScore by remember {
        mutableIntStateOf(
            preferences.getInt("best_score", 0)
        )
    }

    val sequence = remember {
        mutableStateListOf<Int>()
    }

    var activeButton by remember { mutableIntStateOf(-1) }
    var isShowing by remember { mutableStateOf(false) }
    var gameStarted by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var attempts by remember { mutableIntStateOf(0) }
    var inputPosition by remember { mutableIntStateOf(0) }

    // Update and save best score
    fun updateBestScore() {
        if (score > bestScore) {
            bestScore = score

            preferences.edit()
                .putInt("best_score", bestScore)
                .apply()
        }
    }

    fun nextRound() {
        scope.launch {
            isShowing = true
            delay(600)

            sequence.add(Random.nextInt(4))

            for (button in sequence) {
                activeButton = button
                delay(650)
                activeButton = -1
                delay(250)
            }

            isShowing = false
            inputPosition = 0
        }
    }

    fun startGame() {
        sequence.clear()
        score = 0
        attempts = 0
        inputPosition = 0
        activeButton = -1
        gameOver = false
        gameStarted = true

        nextRound()
    }

    fun handleInput(index: Int) {
        if (!gameStarted || isShowing || gameOver) return

        attempts++

        if (sequence[inputPosition] != index) {
            gameOver = true
            updateBestScore()
            return
        }

        inputPosition++

        if (inputPosition == sequence.size) {
            score++
            updateBestScore()
            nextRound()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFDF9))
            .padding(20.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Back",
                color = Color(0xFF006C50),
                fontSize = 16.sp,
                modifier = Modifier.clickable { onBack() }
            )

            Text(
                text = "PATTERN SEQUENCE",
                color = Color(0xFF006C50),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Remember the Pattern!",
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Watch the colours carefully and repeat the same sequence.",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Score and Level
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScoreCard(
                title = "Score",
                value = score.toString(),
                modifier = Modifier.weight(1f)
            )

            ScoreCard(
                title = "Level",
                value = if (gameStarted) sequence.size.toString() else "0",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Best Score
        ScoreCard(
            title = "🏆 Best Score",
            value = bestScore.toString(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Game Status
        Text(
            text = when {
                gameOver -> "Game Over!"
                !gameStarted -> "Ready to Play?"
                isShowing -> "Watch the pattern..."
                else -> "Repeat the pattern!"
            },
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF006C50)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4 Colour Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (row in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (column in 0..1) {
                        val index = row * 2 + column

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    color = if (activeButton == index) {
                                        GameColors[index]
                                    } else {
                                        GameColors[index].copy(alpha = 0.65f)
                                    },
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .clickable(
                                    enabled = gameStarted &&
                                            !isShowing &&
                                            !gameOver
                                ) {
                                    handleInput(index)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "●",
                                fontSize = 42.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Attempts: $attempts",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            fontSize = 15.sp
        )

        // Game Over message
        if (gameOver) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (score >= bestScore && score > 0) {
                    "🎉 New Best Score!"
                } else {
                    "Your Score: $score  |  Best: $bestScore"
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006C50),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!gameStarted || gameOver) {
            Button(
                onClick = { startGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006C50)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (gameOver) "Restart Game" else "Start Game",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ScoreCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFE0F2E9),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006C50)
            )
        }
    }
}
