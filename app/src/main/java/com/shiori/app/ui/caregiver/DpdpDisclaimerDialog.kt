package com.shiori.app.ui.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DpdpDisclaimerDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "DPDP Act 2023 & Medical Notice",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006C50)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. Data Protection Compliance (India DPDP Act 2023):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Shiori operates under strict proxy-consent principles. All patient cognitive gameplay scores, daily routine status, and medication adherence logs remain stored and encrypted directly on this physical device in Local-Only Mode. No personal health records are shared with unauthenticated third parties.",
                    fontSize = 14.sp,
                    color = Color(0xFF424742)
                )

                HorizontalDivider()

                Text(
                    text = "2. Medical & Clinical Disclaimer:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Shiori is an assistive cognitive stimulation and routine management application. It is NOT a clinical medical diagnostic device and does not diagnose, treat, cure, or prevent dementia, Alzheimer's, or any neurological condition. Always consult qualified healthcare professionals for medical advice and clinical assessments.",
                    fontSize = 14.sp,
                    color = Color(0xFF424742)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("I Understand & Acknowledge", fontSize = 15.sp)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFFFBFDF9)
    )
}
