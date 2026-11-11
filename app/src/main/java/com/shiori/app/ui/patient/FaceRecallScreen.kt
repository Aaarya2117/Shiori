
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class FacePerson(
    val name: String,
    val emoji: String,
    val clue: String
)

@Composable
fun FaceRecallScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val people = remember {
        listOf(
            FacePerson("Ravi", "👨🏻‍🦱", "curly hair"),
            FacePerson("Meera", "👩🏻‍🦰", "red hair"),
            FacePerson("Amit", "👨🏻‍🦳", "grey hair"),
            FacePerson("Sara", "👩🏽", "dark hair")
        )
    }

    val preferences = remember(context) {
        context.getSharedPreferences("face_recall_prefs", Context.MODE_PRIVATE)
    }

    var bestScore by remember {
        mutableIntStateOf(preferences.getInt("best_score", 0))
    }

    var score by remember { mutableIntStateOf(0) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var started by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    val questions = remember {
        people.shuffled()
    }

    val currentPerson = questions[questionIndex % questions.size]

    fun finishGame() {
        finished = true
        if (score > bestScore) {
            bestScore = score
            preferences.edit().putInt("best_score", score).apply()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8F5))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "← Back",
                fontSize = 18.sp,
                color = Color(0xFF286B4D),
                modifier = Modifier.clickable { onBack() }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text("Face Recall", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE1F0E5)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CURRENT SCORE", fontSize = 13.sp)
                    Text(
                        "$score",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF0D5)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("BEST SCORE", fontSize = 13.sp)
                    Text(
                        "$bestScore",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when {
            !started -> {
                Text(
                    "Remember These Faces",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Look carefully at each face and remember their names and features.",
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                people.forEach { person ->
                    FacePersonCard(person)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = { started = true },
                    modifier = Modifier.fillMaxWidth().height(58.dp)
                ) {
                    Text("Start Recall", fontSize = 20.sp)
                }
            }

            finished -> {
                Spacer(modifier = Modifier.height(100.dp))

                Text("🎉", fontSize = 64.sp)
                Text("Game Completed!", fontSize = 26.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Your Score: $score / 4", fontSize = 22.sp)
                Text("Best Score: $bestScore", fontSize = 20.sp)

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        score = 0
                        questionIndex = 0
                        selectedAnswer = null
                        started = false
                        finished = false
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp)
                ) {
                    Text("Play Again", fontSize = 20.sp)
                }
            }

            else -> {
                Text(
                    "Who had ${currentPerson.clue}?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Choose the correct person.",
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                people.shuffled().forEach { person ->
                    val isSelected = selectedAnswer == person.name

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable(enabled = selectedAnswer == null) {
                                selectedAnswer = person.name

                                if (person.name == currentPerson.name) {
                                    score++
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isSelected && person.name == currentPerson.name ->
                                    Color(0xFFC8E6C9)
                                isSelected -> Color(0xFFFFCDD2)
                                else -> Color.White
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(person.emoji, fontSize = 36.sp)
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(person.name, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (selectedAnswer != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        if (selectedAnswer == currentPerson.name) "Correct answer!" else "Correct answer: ${currentPerson.name}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            if (questionIndex == 3) {
                                finishGame()
                            } else {
                                questionIndex++
                                selectedAnswer = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(58.dp)
                    ) {
                        Text(
                            if (questionIndex == 3) "Finish Game" else "Next Question",
                            fontSize = 19.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FacePersonCard(person: FacePerson) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(person.emoji, fontSize = 42.sp)
            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(person.name, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(person.clue.replaceFirstChar { it.uppercase() }, fontSize = 16.sp)
            }
        }
    }
}
