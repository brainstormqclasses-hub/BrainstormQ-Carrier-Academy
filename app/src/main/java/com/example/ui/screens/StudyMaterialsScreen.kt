package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamCategory
import com.example.data.model.Language
import com.example.data.model.StudyTopic
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun StudyMaterialsScreen(
    currentLanguage: Language,
    topics: List<StudyTopic>,
    selectedTopic: StudyTopic?,
    onSelectTopic: (StudyTopic) -> Unit,
    onCloseTopic: () -> Unit,
    onToggleBookmark: (topicId: String) -> Unit,
    onMarkCompleted: (topicId: String) -> Unit,
    onToggleLanguage: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ExamCategory.APSC) }
    var searchQuery by remember { mutableStateOf("") }

    if (selectedTopic != null) {
        // Detailed In-Depth Topic Reader
        TopicReaderView(
            topic = selectedTopic,
            currentLanguage = currentLanguage,
            onClose = onCloseTopic,
            onToggleBookmark = { onToggleBookmark(selectedTopic.id) },
            onMarkCompleted = { onMarkCompleted(selectedTopic.id) },
            onToggleLanguage = onToggleLanguage
        )
    } else {
        // Topic Directory & Subject Browser
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("study_materials_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = if (currentLanguage == Language.ASSAMESE) "পাঠ্যক্ৰম আৰু অধ্যয়ন সামগ্ৰী" else "Comprehensive Study Materials",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Text(
                    text = if (currentLanguage == Language.ASSAMESE)
                        "প্ৰিলিমছ আৰু মেইনছ কাকতভিত্তিক বিশদ টোকা (অসমীয়া আৰু ইংৰাজী)"
                    else
                        "Subject-wise prelims & mains syllabus coverage with PYQ cross-references",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Category Selector Tabs
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
                            text = { Text(cat.shortName, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_cat_${cat.name.lowercase()}")
                        )
                    }
                }
            }

            // Search Filter
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search topics, Ahom era, polity, wetlands...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_topics_input")
                )
            }

            val filtered = topics.filter {
                (it.category == selectedCategory || searchQuery.isNotBlank()) &&
                (searchQuery.isBlank() || it.titleEn.contains(searchQuery, ignoreCase = true) || it.titleAs.contains(searchQuery, ignoreCase = true) || it.contentEn.contains(searchQuery, ignoreCase = true))
            }

            if (filtered.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No study topics match your search.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                items(filtered) { topic ->
                    TopicListItemCard(
                        topic = topic,
                        currentLanguage = currentLanguage,
                        onClick = { onSelectTopic(topic) },
                        onToggleBookmark = { onToggleBookmark(topic.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TopicListItemCard(
    topic: StudyTopic,
    currentLanguage: Language,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("topic_card_${topic.id}"),
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
                    color = NavyPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${topic.category.shortName} • ${topic.readMinutes} min read",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (topic.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF059669).copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "COMPLETED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (topic.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (topic.isBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (currentLanguage == Language.ASSAMESE) topic.titleAs else topic.titleEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (currentLanguage == Language.ASSAMESE) topic.summaryAs else topic.summaryEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PYQ: ${topic.pyqReferences.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD97706),
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Read Chapter",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopicReaderView(
    topic: StudyTopic,
    currentLanguage: Language,
    onClose: () -> Unit,
    onToggleBookmark: () -> Unit,
    onMarkCompleted: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("topic_reader_view")
    ) {
        // Reader Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick language toggle in reader
                    TextButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (currentLanguage == Language.ENGLISH) "View in অসমীয়া" else "View in English")
                    }

                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (topic.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (topic.isBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Reader Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NavyPrimary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${topic.category.displayName} • ${topic.readMinutes} min study module",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (currentLanguage == Language.ASSAMESE) topic.titleAs else topic.titleEn,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Key Points Summary Callout
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.12f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldAccent))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == Language.ASSAMESE) "গুৰুত্বপূৰ্ণ মূল বিষয়বস্তু" else "High-Yield Key Takeaways",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val points = if (currentLanguage == Language.ASSAMESE) topic.keyPointsAs else topic.keyPointsEn
                    points.forEach { point ->
                        Row(modifier = Modifier.padding(vertical = 3.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            Text(point, style = MaterialTheme.typography.bodySmall, color = NavyDark, lineHeight = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Full In-Depth Content
            Text(
                text = if (currentLanguage == Language.ASSAMESE) topic.contentAs else topic.contentEn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PYQ References Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Related Previous Years' Questions (PYQ):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    topic.pyqReferences.forEach { pyq ->
                        Text(
                            text = "✓ $pyq",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Completion Action
            Button(
                onClick = {
                    onMarkCompleted()
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mark_topic_completed_button"),
                colors = ButtonDefaults.buttonColors(containerColor = if (topic.isCompleted) Color(0xFF059669) else NavyPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (topic.isCompleted) Icons.Default.CheckCircle else Icons.Default.Done,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (topic.isCompleted) "Completed (Tap to review other topics)" else "Mark Chapter as Completed",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
