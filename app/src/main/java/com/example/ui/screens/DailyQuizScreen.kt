package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyQuizState
import com.example.data.model.Language
import com.example.data.model.UserProfile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun DailyQuizScreen(
    quizState: DailyQuizState?,
    userProfile: UserProfile,
    currentLanguage: Language,
    onSelectOption: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onRestartQuiz: () -> Unit,
    onExitQuiz: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenAiPlanner: () -> Unit
) {
    if (quizState == null || quizState.questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = NavyPrimary)
        }
        return
    }

    var localLang by remember { mutableStateOf(currentLanguage) }

    if (quizState.isCompleted) {
        // Daily Quiz Completion & Performance Tracking Screen
        DailyQuizCompletedView(
            quizState = quizState,
            userProfile = userProfile,
            currentLanguage = localLang,
            onRestart = onRestartQuiz,
            onExit = onExitQuiz,
            onOpenAiPlanner = onOpenAiPlanner
        )
    } else {
        val currentQ = quizState.questions[quizState.currentQuestionIndex]
        val totalQ = quizState.questions.size
        val hasAnswered = quizState.isAnswerRevealed
        val userPick = quizState.selectedOptionIndex

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("daily_quiz_screen")
        ) {
            // Quiz Header Bar
            Surface(
                color = NavyDark,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onExitQuiz, modifier = Modifier.testTag("exit_daily_quiz_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Quiz", tint = Color.White)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DAILY QUIZ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${quizState.examCategory.shortName} Track",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Quick bilingual toggle
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                localLang = if (localLang == Language.ENGLISH) Language.ASSAMESE else Language.ENGLISH
                                onToggleLanguage()
                            }
                            .testTag("quiz_lang_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (localLang == Language.ENGLISH) "অসমীয়া" else "EN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Question Progress Indicators (Dots)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${quizState.currentQuestionIndex + 1} of $totalQ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NavyPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = currentQ.topicTag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until totalQ) {
                            val answered = quizState.answersMap.containsKey(i)
                            val isCurrent = i == quizState.currentQuestionIndex
                            val wasCorrect = answered && quizState.answersMap[i] == quizState.questions[i].correctOptionIndex

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            isCurrent -> NavyPrimary
                                            answered && wasCorrect -> Color(0xFF059669)
                                            answered && !wasCorrect -> Color(0xFFDC2626)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            // Question Content & Options Scrollable Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                if (currentQ.pyqYear != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldAccent.copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Previous Year Trend: ${currentQ.pyqYear}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                Text(
                    text = if (localLang == Language.ASSAMESE) currentQ.questionAs else currentQ.questionEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Options with Instant Feedback styling
                val options = if (localLang == Language.ASSAMESE) currentQ.optionsAs else currentQ.optionsEn
                options.forEachIndexed { optIdx, optText ->
                    val isSelected = userPick == optIdx
                    val isCorrectOption = optIdx == currentQ.correctOptionIndex

                    val containerColor = when {
                        !hasAnswered -> MaterialTheme.colorScheme.surface
                        isCorrectOption -> Color(0xFF059669).copy(alpha = 0.15f)
                        isSelected && !isCorrectOption -> Color(0xFFDC2626).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    }

                    val borderColor = when {
                        !hasAnswered -> MaterialTheme.colorScheme.outlineVariant
                        isCorrectOption -> Color(0xFF059669)
                        isSelected && !isCorrectOption -> Color(0xFFDC2626)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !hasAnswered) { onSelectOption(optIdx) }
                            .testTag("quiz_option_${optIdx}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor)),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Option Letter / Result Indicator
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            !hasAnswered -> MaterialTheme.colorScheme.surfaceVariant
                                            isCorrectOption -> Color(0xFF059669)
                                            isSelected && !isCorrectOption -> Color(0xFFDC2626)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasAnswered && isCorrectOption) {
                                    Icon(Icons.Default.Check, contentDescription = "Correct", tint = Color.White, modifier = Modifier.size(18.dp))
                                } else if (hasAnswered && isSelected && !isCorrectOption) {
                                    Icon(Icons.Default.Close, contentDescription = "Wrong", tint = Color.White, modifier = Modifier.size(18.dp))
                                } else {
                                    Text(
                                        text = listOf("A", "B", "C", "D")[optIdx],
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (hasAnswered && (isCorrectOption || isSelected)) FontWeight.Bold else FontWeight.Normal,
                                color = NavyDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Instant Feedback & Explanation Card
                AnimatedVisibility(
                    visible = hasAnswered,
                    enter = fadeIn() + slideInVertically()
                ) {
                    val wasCorrect = userPick == currentQ.correctOptionIndex

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .testTag("quiz_explanation_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (wasCorrect) Color(0xFF059669).copy(alpha = 0.1f)
                            else Color(0xFFDC2626).copy(alpha = 0.08f)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (wasCorrect) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (wasCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (wasCorrect) Color(0xFF059669) else Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (wasCorrect) "Correct Answer! (+2 Marks)" else "Incorrect! Correct answer is Option ${listOf("A", "B", "C", "D")[currentQ.correctOptionIndex]}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (wasCorrect) Color(0xFF059669) else Color(0xFFDC2626)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Explanation / ব্যাখ্যা:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (localLang == Language.ASSAMESE) currentQ.explanationAs else currentQ.explanationEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Bottom Action Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!hasAnswered) "Select an option for instant answer"
                        else if (quizState.currentQuestionIndex < totalQ - 1) "Review explanation & proceed"
                        else "Last Question! Submit for Streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onNextQuestion,
                        enabled = hasAnswered,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (quizState.currentQuestionIndex == totalQ - 1) Color(0xFF059669) else NavyPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quiz_next_button")
                    ) {
                        Text(
                            text = if (quizState.currentQuestionIndex < totalQ - 1) "Next Question" else "Finish & Track",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DailyQuizCompletedView(
    quizState: DailyQuizState,
    userProfile: UserProfile,
    currentLanguage: Language,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    onOpenAiPlanner: () -> Unit
) {
    val score = quizState.correctCount
    val total = quizState.questions.size
    val percent = (score.toFloat() / total * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("daily_quiz_completed_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Celebration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(GoldAccent, Color(0xFFD97706)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = NavyDark, modifier = Modifier.size(40.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (percent >= 80) "Outstanding Performance!" else if (percent >= 60) "Great Daily Effort!" else "Keep Practicing Daily!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Daily Quiz completed for ${quizState.examCategory.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldAccent,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "$score / $total",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )

                    Text(
                        text = "$percent% Accuracy",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daily Streak Increment Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Streak Extended: ${userProfile.dailyStreak} Days Active!",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Performance & Weak Area Diagnostic Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Instant Diagnostic Insights",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subject-wise performance in this quiz
                    quizState.questions.forEachIndexed { idx, q ->
                        val userPick = quizState.answersMap[idx]
                        val isCorrect = userPick == q.correctOptionIndex

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isCorrect) Color(0xFF059669) else Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = q.topicTag,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = NavyDark
                                )
                            }

                            Text(
                                text = if (isCorrect) "+2 Marks" else "0 Marks",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }

        // Synced with AI Study Planner card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldAccent))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AI Study Planner Updated", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NavyDark)
                        Text("These 5 question metrics have refreshed your weekly study timetable.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = onOpenAiPlanner,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("View Plan", fontSize = 12.sp)
                    }
                }
            }
        }

        // Actions
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_try_another_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Practice Another 5 Questions", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_back_home_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Back to Dashboard")
                }
            }
        }
    }
}
