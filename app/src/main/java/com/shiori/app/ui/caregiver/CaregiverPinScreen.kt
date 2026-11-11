package com.shiori.app.ui.caregiver

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CaregiverPinScreen(
    onPinCorrect: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun handleDigit(d: String) {
        if (enteredPin.length < 4) {
            val next = enteredPin + d
            enteredPin = next
            isError = false
            if (next.length == 4) {
                if (next == "1234") {
                    onPinCorrect()
                } else {
                    isError = true
                    enteredPin = ""
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBFDF9))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Patient Mode", fontSize = 18.sp, color = Color(0xFF006C50))
            }
        }

        // Title and PIN indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Caregiver Security",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1A)
            )
            Text(
                text = "Enter 4-digit PIN to access dashboard",
                fontSize = 18.sp,
                color = Color(0xFF424742)
            )

            Spacer(Modifier.height(8.dp))

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val filled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (filled) Color(0xFF006C50) else Color(0xFFE0E0E0),
                                shape = CircleShape
                            )
                    )
                }
            }

            if (isError) {
                Text(
                    text = "Incorrect PIN. Default is 1234.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Spacer(Modifier.height(24.dp))
            }
        }

        // Numeric Keypad (0-9, Backspace, Clear)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "⌫")
            )
            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (key in row) {
                        Button(
                            onClick = {
                                when (key) {
                                    "C" -> {
                                        enteredPin = ""
                                        isError = false
                                    }
                                    "⌫" -> {
                                        if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.dropLast(1)
                                            isError = false
                                        }
                                    }
                                    else -> handleDigit(key)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (key == "C" || key == "⌫") Color(0xFFE0F5EA) else Color.White,
                                contentColor = Color(0xFF191C1A)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = key,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
