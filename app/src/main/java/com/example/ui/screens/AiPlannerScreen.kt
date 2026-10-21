package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun AiPlannerScreen(
    currentLanguage: Language,
    goal: PlannerGoal,
    aiPlan: AiGeneratedPlan?,
    isGenerating: Boolean,
    topics: List<StudyTopic>,
    mockTests: List<MockTest>,
    onUpdateGoal: (targetExam: ExamCategory, daysRemaining: Int, targetScore: Int, dailyHours: Int) -> Unit,
    onRegenerateClick: () -> Unit,
    onToggleTaskCompleted: (taskId: String) -> Unit,
    onOpenTopic: (StudyTopic) -> Unit,
    onStartTest: (MockTest) -> Unit
) {
    var isConfigExpanded by remember { mutableStateOf(false) }
    var selectedExam by remember { mutableStateOf(goal.targetExam) }
    var daysSlider by remember { mutableFloatStateOf(goal.targetDateDaysRemaining.toFloat()) }
    var scoreSlider by remember { mutableFloatStateOf(goal.targetScore.toFloat()) }
    var hoursSlider by remember { mutableFloatStateOf(goal.dailyStudyHours.toFloat()) }

    var selectedDayFilter by remember { mutableStateOf("All Days") }

    val daysList = listOf("All Days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_study_planner_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Planner Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NavyDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == Language.ASSAMESE) "এআই অধ্যয়ন পৰিকল্পনাকাৰী" else "AI Study Planner",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Gemini 3.5 Intelligent Scheduling",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent
                                )
                            }
                        }

                        IconButton(
                            onClick = { isConfigExpanded = !isConfigExpanded },
                            modifier = Modifier.testTag("toggle_planner_config")
                        ) {
                            Icon(
                                imageVector = if (isConfigExpanded) Icons.Default.Tune else Icons.Default.Tune,
                                contentDescription = "Adjust Goals",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (currentLanguage == Language.ASSAMESE)
                            aiPlan?.headlineAs ?: "আপোনাৰ লক্ষ্য আৰু দুৰ্বল বিষয়ৰ বিশ্লেষণ"
                        else
                            aiPlan?.headlineEn ?: "Target Schedule & Weak Area Diagnostics",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (currentLanguage == Language.ASSAMESE)
                            aiPlan?.strategicAdviceAs ?: ""
                        else
                            aiPlan?.strategicAdviceEn ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Goal Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Days Left", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                                Text("${goal.targetDateDaysRemaining}d", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Target Score", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                                Text("${goal.targetScore}/${goal.maxScore}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Daily Target", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                                Text("${goal.dailyStudyHours} hrs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Collapsible Goal Customizer Form
                    AnimatedVisibility(visible = isConfigExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Divider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Select Target Examination:", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ExamCategory.values().forEach { cat ->
                                    FilterChip(
                                        selected = selectedExam == cat,
                                        onClick = { selectedExam = cat },
                                        label = { Text(cat.shortName, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GoldAccent,
                                            selectedLabelColor = NavyDark
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Exam Countdown: ${daysSlider.toInt()} Days", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            Slider(
                                value = daysSlider,
                                onValueChange = { daysSlider = it },
                                valueRange = 15f..180f,
                                steps = 10,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                            )

                            Text("Target Score Goal: ${scoreSlider.toInt()} Marks", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            Slider(
                                value = scoreSlider,
                                onValueChange = { scoreSlider = it },
                                valueRange = 80f..190f,
                                steps = 11,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                            )

                            Text("Daily Study Time: ${hoursSlider.toInt()} Hours/Day", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            Slider(
                                value = hoursSlider,
                                onValueChange = { hoursSlider = it },
                                valueRange = 2f..10f,
                                steps = 7,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                            )

                            Button(
                                onClick = {
                                    onUpdateGoal(selectedExam, daysSlider.toInt(), scoreSlider.toInt(), hoursSlider.toInt())
                                    isConfigExpanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_goals_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NavyDark)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Regenerating with Gemini AI...")
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Apply Goals & Regenerate Plan")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Identified Weak Areas Section (Evaluated from test results)
        item {
            Text(
                text = if (currentLanguage == Language.ASSAMESE) "মক টেষ্টৰ ভিত্তিত দুৰ্বল বিষয় চিনাক্তকৰণ" else "Identified Weak Areas (Diagnostics)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
            Text(
                text = "Calculated from your latest Mock Tests & DPP errors",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            aiPlan?.weakAreas?.forEach { area ->
                WeakAreaCard(metric = area, currentLanguage = currentLanguage)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Day of Week Filter Chips
        item {
            Text(
                text = if (currentLanguage == Language.ASSAMESE) "সাপ্তাহিক সূচী আৰু নিৰ্দিষ্ট মডিউল" else "Personalized Weekly Schedule & Modules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(daysList) { day ->
                    FilterChip(
                        selected = selectedDayFilter == day,
                        onClick = { selectedDayFilter = day },
                        label = { Text(day, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Scheduled Study Tasks List
        val filteredTasks = aiPlan?.schedule?.filter {
            selectedDayFilter == "All Days" || it.dayName.equals(selectedDayFilter, ignoreCase = true)
        } ?: emptyList()

        if (filteredTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No scheduled tasks for this day.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(filteredTasks) { task ->
                StudyTaskCard(
                    task = task,
                    currentLanguage = currentLanguage,
                    topics = topics,
                    mockTests = mockTests,
                    onToggleComplete = { onToggleTaskCompleted(task.id) },
                    onOpenTopic = onOpenTopic,
                    onStartTest = onStartTest
                )
            }
        }
    }
}

@Composable
fun WeakAreaCard(metric: WeakAreaMetric, currentLanguage: Language) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metric.subjectTag,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (metric.priorityLevel) {
                        "CRITICAL" -> Color(0xFFDC2626).copy(alpha = 0.15f)
                        "MODERATE" -> Color(0xFFD97706).copy(alpha = 0.15f)
                        else -> Color(0xFF059669).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = "${metric.priorityLevel} PRIORITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (metric.priorityLevel) {
                            "CRITICAL" -> Color(0xFFDC2626)
                            "MODERATE" -> Color(0xFFD97706)
                            else -> Color(0xFF059669)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { metric.accuracyPercent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when (metric.priorityLevel) {
                        "CRITICAL" -> Color(0xFFDC2626)
                        "MODERATE" -> Color(0xFFD97706)
                        else -> Color(0xFF059669)
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${metric.accuracyPercent}% Accuracy",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (currentLanguage == Language.ASSAMESE) metric.adviceAs else metric.adviceEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun StudyTaskCard(
    task: ScheduledStudyItem,
    currentLanguage: Language,
    topics: List<StudyTopic>,
    mockTests: List<MockTest>,
    onToggleComplete: () -> Unit,
    onOpenTopic: (StudyTopic) -> Unit,
    onStartTest: (MockTest) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF059669).copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (task.isCompleted) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF059669)))
        else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${task.dayName} • ${task.timeSlot}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NavyPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = task.type.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (currentLanguage == Language.ASSAMESE) task.titleAs else task.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDark
                    )

                    Text(
                        text = "Subject: ${task.subject} • ${task.durationMinutes} mins",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Buttons to open corresponding topic or mock test
            val topic = topics.firstOrNull { it.id == task.recommendedTopicId }
            val test = mockTests.firstOrNull { it.id == task.recommendedTestId }

            if (topic != null || test != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (topic != null) {
                        OutlinedButton(
                            onClick = { onOpenTopic(topic) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Note", fontSize = 12.sp)
                        }
                    }
                    if (test != null) {
                        Button(
                            onClick = { onStartTest(test) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Practice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
