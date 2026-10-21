package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.MockTest
import com.example.ui.TestTakingState
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun MockTestsScreen(
    currentLanguage: Language,
    mockTests: List<MockTest>,
    activeTestState: TestTakingState?,
    onStartTest: (MockTest) -> Unit,
    onSelectOption: (questionIdx: Int, optionIdx: Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPrevQuestion: () -> Unit,
    onJumpQuestion: (Int) -> Unit,
    onSubmitTest: () -> Unit,
    onExitTest: () -> Unit,
    onOpenAiPlanner: () -> Unit,
    onStartDailyQuiz: (() -> Unit)? = null
) {
    if (activeTestState != null) {
        if (activeTestState.isSubmitted) {
            // Test Result & Solution Analysis View
            TestResultReviewView(
                state = activeTestState,
                currentLanguage = currentLanguage,
                onExit = onExitTest,
                onOpenAiPlanner = onOpenAiPlanner
            )
        } else {
            // Live Test Taking Interface
            ActiveTestTakingView(
                state = activeTestState,
                currentLanguage = currentLanguage,
                onSelectOption = onSelectOption,
                onNext = onNextQuestion,
                onPrev = onPrevQuestion,
                onJump = onJumpQuestion,
                onSubmit = onSubmitTest,
                onExit = onExitTest
            )
        }
    } else {
        // Mock Tests Directory
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("mock_tests_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = if (currentLanguage == Language.ASSAMESE) "মক টেষ্ট আৰু দৈনিক অনুশীলন (DPP)" else "Mock Tests & Daily Practice (DPP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Text(
                    text = if (currentLanguage == Language.ASSAMESE)
                        "প্ৰকৃত পৰীক্ষাৰ নিচিনা সময়সীমা আৰু ঋণাত্মক নম্বৰসহ টেষ্ট শৃংখলা"
                    else
                        "Real exam timed simulations with negative marking & instant percentile analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onStartDailyQuiz != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartDailyQuiz() }
                            .testTag("mock_screen_daily_quiz_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, tint = NavyDark, modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily 5-Question Sprint",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Instant feedback • Subject-wise • Streak booster",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldAccent
                                )
                            }

                            Button(
                                onClick = onStartDailyQuiz,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Play", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            items(mockTests) { test ->
                MockTestItemCard(
                    test = test,
                    currentLanguage = currentLanguage,
                    onStart = { onStartTest(test) }
                )
            }
        }
    }
}

@Composable
fun MockTestItemCard(
    test: MockTest,
    currentLanguage: Language,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mock_card_${test.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (test.isDailyPractice) Color(0xFFD97706).copy(alpha = 0.15f)
                    else NavyPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (test.isDailyPractice) "DAILY DPP" else "${test.examCategory.shortName} • ${test.stage.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (test.isDailyPractice) Color(0xFFD97706) else NavyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${test.questions.size} Questions",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (currentLanguage == Language.ASSAMESE) test.titleAs else test.titleEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${test.durationMinutes} mins", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Grade, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${test.totalMarks} Marks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("-${test.negativeMarks} Neg", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_test_${test.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Practice Test", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActiveTestTakingView(
    state: TestTakingState,
    currentLanguage: Language,
    onSelectOption: (questionIdx: Int, optionIdx: Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onJump: (Int) -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit
) {
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var questionLang by remember { mutableStateOf(currentLanguage) }

    val currentQ = state.test.questions[state.currentQuestionIndex]
    val selectedOption = state.selectedOptionIndices[state.currentQuestionIndex]

    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("active_test_taking_view")
    ) {
        // Sticky Header with Timer & Submit
        Surface(
            color = NavyDark,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (state.remainingSeconds < 300) Color(0xFFDC2626) else GoldAccent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyDark
                        )
                    }
                }

                Button(
                    onClick = { showSubmitConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("submit_test_button")
                ) {
                    Text("Submit", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Question Navigator Palette
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(state.test.questions) { idx, _ ->
                val isAnswered = state.selectedOptionIndices.containsKey(idx)
                val isCurrent = state.currentQuestionIndex == idx

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> NavyPrimary
                                isAnswered -> Color(0xFF059669)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable { onJump(idx) }
                        .testTag("palette_q_$idx"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${idx + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent || isAnswered) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Question Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${state.currentQuestionIndex + 1} of ${state.test.questions.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                // Bilingual question toggle
                TextButton(
                    onClick = {
                        questionLang = if (questionLang == Language.ENGLISH) Language.ASSAMESE else Language.ENGLISH
                    }
                ) {
                    Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (questionLang == Language.ENGLISH) "অসমীয়া" else "English")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (questionLang == Language.ASSAMESE) currentQ.questionAs else currentQ.questionEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = NavyDark,
                lineHeight = 22.sp
            )

            if (currentQ.pyqYear != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "PYQ: ${currentQ.pyqYear}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Options List
            val options = if (questionLang == Language.ASSAMESE) currentQ.optionsAs else currentQ.optionsEn
            options.forEachIndexed { optIdx, optText ->
                val isSelected = selectedOption == optIdx
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectOption(state.currentQuestionIndex, optIdx) }
                        .testTag("option_${state.currentQuestionIndex}_$optIdx"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NavyPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) NavyPrimary else MaterialTheme.colorScheme.outlineVariant)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NavyPrimary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = listOf("A", "B", "C", "D")[optIdx],
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = NavyDark
                        )
                    }
                }
            }
        }

        // Bottom Nav Bar (Prev / Next)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onPrev,
                    enabled = state.currentQuestionIndex > 0,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Previous")
                }

                Button(
                    onClick = onNext,
                    enabled = state.currentQuestionIndex < state.test.questions.size - 1,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Next Question")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showSubmitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = { Text("Submit Mock Test?") },
            text = {
                val answeredCount = state.selectedOptionIndices.size
                val totalCount = state.test.questions.size
                Text("You have answered $answeredCount of $totalCount questions. Are you ready to submit and calculate your score & AI analysis?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        onSubmit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("Confirm Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("Keep Reviewing")
                }
            }
        )
    }
}

@Composable
fun TestResultReviewView(
    state: TestTakingState,
    currentLanguage: Language,
    onExit: () -> Unit,
    onOpenAiPlanner: () -> Unit
) {
    val result = state.completedResult ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("test_result_review_view")
    ) {
        // Result Top Bar
        Surface(
            color = NavyDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Performance Report & Solutions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Score Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = result.testTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${result.score.toInt()} / ${result.totalMarks}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )

                        Text(
                            text = "Score Achieved",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${result.correctCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                Text("Correct", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${result.incorrectCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                Text("Incorrect", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${result.skippedCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Skipped", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(result.accuracy * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GoldAccent)
                                Text("Accuracy", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // Sync with AI Study Planner Callout
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldAccent))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Updated in AI Study Planner", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NavyDark)
                            Text("Your weak areas have been recalculated based on these answers.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = onOpenAiPlanner,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View Plan", fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Question Solutions & Explanations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
            }

            // Detailed Question-by-Question Solution
            itemsIndexed(state.test.questions) { idx, q ->
                val userPick = state.selectedOptionIndices[idx]
                val isCorrect = userPick == q.correctOptionIndex

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (userPick == null) Color.Gray
                            else if (isCorrect) Color(0xFF059669)
                            else Color(0xFFDC2626)
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Q${idx + 1} • ${q.topicTag}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (userPick == null) Color.Gray.copy(alpha = 0.15f)
                                else if (isCorrect) Color(0xFF059669).copy(alpha = 0.15f)
                                else Color(0xFFDC2626).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (userPick == null) "SKIPPED" else if (isCorrect) "CORRECT" else "INCORRECT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (userPick == null) Color.Gray else if (isCorrect) Color(0xFF059669) else Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (currentLanguage == Language.ASSAMESE) q.questionAs else q.questionEn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyDark
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val options = if (currentLanguage == Language.ASSAMESE) q.optionsAs else q.optionsEn
                        options.forEachIndexed { optIdx, text ->
                            val isUserPicked = userPick == optIdx
                            val isCorrectAnswer = optIdx == q.correctOptionIndex

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    isCorrectAnswer -> Color(0xFF059669).copy(alpha = 0.12f)
                                    isUserPicked -> Color(0xFFDC2626).copy(alpha = 0.12f)
                                    else -> Color.Transparent
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isCorrectAnswer -> Icons.Default.Check
                                            isUserPicked -> Icons.Default.Close
                                            else -> Icons.Default.RadioButtonUnchecked
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            isCorrectAnswer -> Color(0xFF059669)
                                            isUserPicked -> Color(0xFFDC2626)
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${listOf("A", "B", "C", "D")[optIdx]}. $text",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isCorrectAnswer) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCorrectAnswer) Color(0xFF059669) else if (isUserPicked) Color(0xFFDC2626) else NavyDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Explanation:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentLanguage == Language.ASSAMESE) q.explanationAs else q.explanationEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
