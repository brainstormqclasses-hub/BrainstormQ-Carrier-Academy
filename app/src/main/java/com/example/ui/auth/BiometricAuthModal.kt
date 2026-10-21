package com.example.ui.auth

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExamCategory
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun BiometricAuthModal(
    userName: String,
    targetExam: ExamCategory,
    onAuthenticated: (name: String, exam: ExamCategory) -> Unit
) {
    var isAuthenticating by remember { mutableStateOf(false) }
    var authSuccess by remember { mutableStateOf(false) }
    var usePinFallback by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }
    var selectedExam by remember { mutableStateOf(targetExam) }
    var nameInput by remember { mutableStateOf(userName) }
    var showProfileConfig by remember { mutableStateOf(false) }

    // Pulsing animation for biometric sensor
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = { /* Modal must be authenticated or bypassed */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("biometric_login_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Secure Login",
                            tint = NavyPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SECURE BIOMETRIC ACCESS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "BRAINSTORMQ CARRIER ACADEMY",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "APSC • UPSC • ADRE • SLRC Assam Exams",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!usePinFallback) {
                    // Biometric Sensor Visual
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(if (isAuthenticating) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (authSuccess) Color(0xFF059669) else GoldAccent.copy(alpha = 0.25f),
                                        if (authSuccess) Color(0xFF047857) else NavyPrimary.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .clickable(
                                enabled = !isAuthenticating && !authSuccess,
                                onClick = {
                                    isAuthenticating = true
                                    // Simulate biometric scan verification
                                }
                            )
                            .testTag("biometric_sensor_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (authSuccess) Icons.Default.CheckCircle else Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Sensor",
                            tint = if (authSuccess) Color.White else NavyPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (authSuccess) "Verified Successfully!" else if (isAuthenticating) "Scanning Biometrics..." else "Touch Fingerprint Sensor to Unlock",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (authSuccess) Color(0xFF059669) else MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Encrypted On-Device Credential Validation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LaunchedEffect(isAuthenticating) {
                        if (isAuthenticating) {
                            kotlinx.coroutines.delay(800)
                            authSuccess = true
                            kotlinx.coroutines.delay(400)
                            onAuthenticated(nameInput, selectedExam)
                        }
                    }
                } else {
                    // PIN Fallback
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 4) pinText = it },
                        label = { Text("Enter 4-Digit Security PIN") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_input_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (pinText.length >= 4) {
                                onAuthenticated(nameInput, selectedExam)
                            }
                        },
                        enabled = pinText.length >= 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_pin_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Unlock with PIN")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Profile Configuration Accordion Toggle
                TextButton(
                    onClick = { showProfileConfig = !showProfileConfig },
                    modifier = Modifier.testTag("toggle_profile_setup")
                ) {
                    Icon(
                        imageVector = if (showProfileConfig) Icons.Default.ExpandLess else Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showProfileConfig) "Hide Profile Settings" else "Personalize Aspirant Profile")
                }

                AnimatedVisibility(visible = showProfileConfig) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Aspirant Full Name") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Target Examination",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExamCategory.values().take(2).forEach { cat ->
                                FilterChip(
                                    selected = selectedExam == cat,
                                    onClick = { selectedExam = cat },
                                    label = { Text(cat.shortName, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExamCategory.values().drop(2).forEach { cat ->
                                FilterChip(
                                    selected = selectedExam == cat,
                                    onClick = { selectedExam = cat },
                                    label = { Text(cat.shortName, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider()

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { usePinFallback = !usePinFallback },
                        modifier = Modifier.testTag("switch_auth_mode_button")
                    ) {
                        Text(if (usePinFallback) "Use Fingerprint" else "Use PIN Code")
                    }

                    Button(
                        onClick = {
                            // Quick one-tap biometric bypass for instant demo entry
                            onAuthenticated(nameInput, selectedExam)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.testTag("one_tap_auth_button")
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instant Login")
                    }
                }
            }
        }
    }
}
