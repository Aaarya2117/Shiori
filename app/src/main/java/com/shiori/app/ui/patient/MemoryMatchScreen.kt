package com.shiori.app.ui.patient

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class MemoryCard(
    val id: Int,
    val symbol: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

private fun createMemoryDeck(): List<MemoryCard> {
    val symbols = listOf(
        "🧠", "🌸", "⭐", "🐶",
        "🍎", "🚗", "🎵", "🏠"
    )

    return (symbols + symbols)
        .shuffled()
        .mapIndexed { index, symbol ->
            MemoryCard(
                id = index,
                symbol = symbol
            )
        }
}

@Composable
fun MemoryMatchScreen(
    onBack: () -> Unit,
    onGameCompleted: ((score: Int, accuracyPercent: Float, durationSecs: Long) -> Unit)? = null
) {
    val startTime = remember { System.currentTimeMillis() }
    var cards by remember { mutableStateOf(createMemoryDeck()) }
    var firstSelected by remember { mutableIntStateOf(-1) }
    var attempts by remember { mutableIntStateOf(0) }
    var matches by remember { mutableIntStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }
    var recordedCompletion by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val gameComplete = matches == 8

    if (gameComplete && !recordedCompletion) {
        LaunchedEffect(matches) {
            recordedCompletion = true
            val durationSecs = ((System.currentTimeMillis() - startTime) / 1000).coerceAtLeast(1)
            val accuracy = if (attempts > 0) ((8f / attempts) * 100f).coerceIn(10f, 100f) else 100f
            val score = accuracy.toInt()
            onGameCompleted?.invoke(score, accuracy, durationSecs)
        }
    }

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFDF9))
            .padding(16.dp)
    ) {

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "← Back",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "MEMORY MATCH",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006C50)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Find the matching pairs!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191C1A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap two cards and remember their positions.",
            fontSize = 18.sp,
            color = Color(0xFF424742)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Score section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScoreBox(
                title = "Matches",
                value = "$matches / 8",
                modifier = Modifier.weight(1f)
            )

            ScoreBox(
                title = "Attempts",
                value = attempts.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4 x 4 game grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(
                items = cards,
                key = { _, card -> card.id }
            ) { index, card ->

                val revealed = card.isFaceUp || card.isMatched

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .semantics {
                            contentDescription =
                                if (card.isMatched) {
                                    "Matched card ${card.symbol}"
                                } else if (revealed) {
                                    "Revealed card ${card.symbol}"
                                } else {
                                    "Hidden card. Tap to reveal."
                                }
                        }
                        .clickable(
                            enabled = !isChecking &&
                                    !card.isMatched &&
                                    !card.isFaceUp &&
                                    !gameComplete
                        ) {
                            if (firstSelected == -1) {

                                cards = cards.mapIndexed { i, item ->
                                    if (i == index) {
                                        item.copy(isFaceUp = true)
                                    } else {
                                        item
                                    }
                                }

                                firstSelected = index

                            } else {

                                attempts++

                                cards = cards.mapIndexed { i, item ->
                                    if (i == index) {
                                        item.copy(isFaceUp = true)
                                    } else {
                                        item
                                    }
                                }

                                val firstCard = cards[firstSelected]
                                val secondCard = cards[index]

                                if (firstCard.symbol == secondCard.symbol) {

                                    cards = cards.mapIndexed { i, item ->
                                        if (i == firstSelected || i == index) {
                                            item.copy(
                                                isMatched = true,
                                                isFaceUp = true
                                            )
                                        } else {
                                            item
                                        }
                                    }

                                    matches++
                                    firstSelected = -1

                                } else {

                                    isChecking = true

                                    val previousIndex = firstSelected
                                    val currentIndex = index

                                    scope.launch {
                                        delay(1000)

                                        cards = cards.mapIndexed { i, item ->
                                            if (
                                                i == previousIndex ||
                                                i == currentIndex
                                            ) {
                                                item.copy(isFaceUp = false)
                                            } else {
                                                item
                                            }
                                        }

                                        firstSelected = -1
                                        isChecking = false
                                    }
                                }
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        card.isMatched -> Color(0xFFB9F6CA)
                        revealed -> Color.White
                        else -> Color(0xFF006C50)
                    },
                    shadowElevation = 3.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                revealed -> card.symbol
                                else -> "?"
                            },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (revealed) {
                                Color(0xFF191C1A)
                            } else {
                                Color.White
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Completion message
        if (gameComplete) {
            Text(
                text = "🎉 Excellent! All pairs matched!",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006C50)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Restart button
        Button(
            onClick = {
                cards = createMemoryDeck()
                firstSelected = -1
                attempts = 0
                matches = 0
                isChecking = false
                recordedCompletion = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF006C50)
            )
        ) {
            Text(
                text = "Restart Game",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScoreBox(
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
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xFF424742)
            )

            Text(
                text = value,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006C50)
            )
        }
    }
}