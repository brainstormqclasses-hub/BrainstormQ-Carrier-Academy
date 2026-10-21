package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
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
import com.example.data.model.ExamCategory
import com.example.data.model.Language
import com.example.data.model.PyqYearAnalysis
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun PyqAnalysisScreen(
    currentLanguage: Language,
    pyqAnalyses: List<PyqYearAnalysis>
) {
    var selectedCategory by remember { mutableStateOf(ExamCategory.APSC) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pyq_analysis_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = if (currentLanguage == Language.ASSAMESE) "পূৰ্ববৰ্তী বৰ্ষৰ প্ৰশ্ন বিশ্লেষণ (PYQ)" else "Previous Years' Question (PYQ) Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
            Text(
                text = if (currentLanguage == Language.ASSAMESE)
                    "এপিএছচি চিচিই, ইউপিএছচি আৰু এডিআৰইৰ কাট-অফ আৰু বিষয়ভিত্তিক শতাংশ"
                else
                    "Official cutoffs, topic distribution trends, and high-yield scoring patterns",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Exam Category Filter
        item {
            ScrollableTabRow(
                selectedTabIndex = ExamCategory.values().indexOf(selectedCategory),
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = NavyPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                ExamCategory.values().forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = { Text(cat.shortName, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        val filtered = pyqAnalyses.filter { it.examCategory == selectedCategory }

        if (filtered.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(28.dp), contentAlignment = Alignment.Center) {
                        Text("Detailed trend data for this exam is updating.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(filtered) { analysis ->
                PyqYearCard(analysis = analysis, currentLanguage = currentLanguage)
            }
        }

        // APSC Paper 5 Special Guidance Note
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.12f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldAccent))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == Language.ASSAMESE) "এপিএছচি পেপাৰ ৫ (অসম বিশেষ) বিশেষ টিপছ" else "APSC Mains Paper 5 Strategic Insight",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentLanguage == Language.ASSAMESE)
                            "মেইনছ পেপাৰ ৫ ত ২৫০ নম্বৰৰ ভিতৰত ৭০% প্ৰশ্ন আহোম প্ৰশাসন, জনজাতীয় স্বায়ত্তশাসন আৰু অসমৰ নদী ব্যৱস্থাপনাৰ পৰা আহিছে। ব্ৰহ্মপুত্ৰৰ মানচিত্ৰ আঁকি উত্তৰ লিখিলে অতিৰিক্ত ৩-৪ নম্বৰ লাভ কৰিব পাৰি।"
                        else
                            "In Mains GS Paper 5 (250 Marks), 70% of high-scoring answers incorporate hand-drawn maps of the Brahmaputra basin, Ahom military encampments, and Sixth Schedule district boundaries. Our study notes provide ready-to-reproduce templates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavyDark,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PyqYearCard(analysis: PyqYearAnalysis, currentLanguage: Language) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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
                    shape = RoundedCornerShape(8.dp),
                    color = NavyPrimary
                ) {
                    Text(
                        text = "${analysis.examCategory.shortName} • ${analysis.year}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verified Cutoffs", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cutoffs Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("General Cutoff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(analysis.cutoffGeneral, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = NavyDark)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("OBC / MOBC Cutoff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(analysis.cutoffOBC, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = NavyDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Key Paper Trend & Pattern:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )

            Text(
                text = if (currentLanguage == Language.ASSAMESE) analysis.keyTrendAs else analysis.keyTrendEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Subject Weightage in Paper (%):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            analysis.subjectWeightage.forEach { (subject, percent) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(subject, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$percent%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NavyPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
