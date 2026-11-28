package com.shiori.app.ui.caregiver

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shiori.app.domain.model.PatientProfile
import com.shiori.app.domain.model.SyncStatus
import com.shiori.app.domain.repository.IPatientRepository
import com.shiori.app.voice.DEFAULT_BHASHINI_URL
import com.shiori.app.voice.IVoiceReminderSettings
import com.shiori.app.voice.ReminderLanguage
import com.shiori.app.voice.VoiceConfig
import com.shiori.app.voice.VoicePlaybackResult
import com.shiori.app.voice.VoiceReminderSpeaker
import kotlinx.coroutines.launch

private val VoiceSectionGreen = Color(0xFF006C50)

@Composable
fun CaregiverVoiceSettingsSection(
    voiceSettings: IVoiceReminderSettings?,
    voiceSpeaker: VoiceReminderSpeaker?,
    patientRepository: IPatientRepository?
) {
    val scope = rememberCoroutineScope()
    val config by (voiceSettings?.configFlow ?: remember {
        kotlinx.coroutines.flow.MutableStateFlow(VoiceConfig())
    }).collectAsState()

    var apiKeyInput by remember(config.bhashiniApiKey) { mutableStateOf(config.bhashiniApiKey) }
    var userIdInput by remember(config.bhashiniUserId) { mutableStateOf(config.bhashiniUserId) }
    var ulcaKeyInput by remember(config.bhashiniUlcaApiKey) { mutableStateOf(config.bhashiniUlcaApiKey) }
    var endpointInput by remember(config.bhashiniEndpointUrl) {
        mutableStateOf(config.bhashiniEndpointUrl.ifBlank { DEFAULT_BHASHINI_URL })
    }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingVoice by remember { mutableStateOf(false) }

    val selectedLanguage = ReminderLanguage.fromCode(config.preferredLanguage)

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        // Master Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Enable Voice Reminders",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C1A)
                    )
                    Text(
                        text = "Speak scheduled reminders aloud in ${selectedLanguage.nativeName} (${selectedLanguage.englishName}) using Bhashini API with offline TTS fallback.",
                        fontSize = 14.sp,
                        color = Color(0xFF424742)
                    )
                }
                Switch(
                    checked = config.voiceRemindersEnabled,
                    onCheckedChange = { enabled ->
                        voiceSettings?.setVoiceRemindersEnabled(enabled)
                        statusMessage = if (enabled) {
                            "Voice reminders enabled (${selectedLanguage.englishName})."
                        } else {
                            "Voice reminders disabled."
                        }
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = VoiceSectionGreen)
                )
            }
        }

        // Local Language Selector
        Text(
            text = "Local Language for Voice Reminders",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191C1A)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ReminderLanguage.entries.forEach { lang ->
                    val isSelected = lang == selectedLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                voiceSettings?.setPreferredLanguage(lang.code)
                                scope.launch {
                                    val existing = patientRepository?.getPatientProfile()
                                    if (existing != null) {
                                        patientRepository.savePatientProfile(
                                            existing.copy(
                                                preferredLanguage = lang.code,
                                                updatedAt = System.currentTimeMillis(),
                                                syncStatus = SyncStatus.PENDING
                                            )
                                        )
                                    } else {
                                        patientRepository?.savePatientProfile(
                                            PatientProfile(
                                                name = "Primary",
                                                preferredLanguage = lang.code,
                                                caregiverPhoneNumber = "",
                                                isLocalOnlyMode = true
                                            )
                                        )
                                    }
                                }
                                statusMessage = "Selected ${lang.nativeName} (${lang.englishName})."
                            }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = VoiceSectionGreen)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${lang.nativeName} (${lang.englishName})",
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = Color(0xFF191C1A)
                        )
                    }
                }
            }
        }

        // Test Voice Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F5EA))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Test Voice Output",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1A)
                )
                Button(
                    onClick = {
                        if (voiceSpeaker == null) {
                            statusMessage = "Voice speaker unavailable in preview."
                            return@Button
                        }
                        isTestingVoice = true
                        statusMessage = "Generating ${selectedLanguage.englishName} voice reminder..."
                        scope.launch {
                            val result = voiceSpeaker.speakReminderNow(
                                title = "Take Morning Medication",
                                details = "1 tablet after breakfast"
                            )
                            isTestingVoice = false
                            statusMessage = when (result) {
                                is VoicePlaybackResult.PlayedViaBhashini ->
                                    "Playing via Bhashini Cloud TTS (${result.language.englishName})."
                                is VoicePlaybackResult.PlayedViaDeviceTts ->
                                    "Playing via Offline Device TTS (${result.language.englishName})."
                                VoicePlaybackResult.Disabled ->
                                    "Voice reminders are disabled."
                                is VoicePlaybackResult.Failed ->
                                    "Playback failed: ${result.reason}"
                            }
                        }
                    },
                    enabled = !isTestingVoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VoiceSectionGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isTestingVoice) "Speaking..." else "🔊 Test Voice Reminder",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = VoiceSectionGreen
                    )
                }
            }
        }

        // Bhashini API Configuration Card
        Text(
            text = "Bhashini API Configuration",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191C1A)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Provide your Bhashini Dhruva Inference API Key (Authorization) or ULCA User ID + ULCA API Key. If left blank or offline, Shiori falls back to on-device TTS.",
                    fontSize = 13.sp,
                    color = Color(0xFF424742)
                )

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Bhashini Inference API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = userIdInput,
                    onValueChange = { userIdInput = it },
                    label = { Text("ULCA User ID (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ulcaKeyInput,
                    onValueChange = { ulcaKeyInput = it },
                    label = { Text("ULCA API Key (Optional)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = endpointInput,
                    onValueChange = { endpointInput = it },
                    label = { Text("Pipeline Endpoint URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        voiceSettings?.saveBhashiniCredentials(
                            apiKey = apiKeyInput,
                            userId = userIdInput,
                            ulcaApiKey = ulcaKeyInput,
                            endpointUrl = endpointInput
                        )
                        statusMessage = "Bhashini API settings saved."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VoiceSectionGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Bhashini API Settings", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
