package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ExamCategory
import com.example.data.model.ForumPost
import com.example.data.model.Language
import com.example.data.model.LiveDoubtSession
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun LiveDoubtAndForumScreen(
    currentLanguage: Language,
    doubtSessions: List<LiveDoubtSession>,
    forumPosts: List<ForumPost>,
    onUpvotePost: (postId: String) -> Unit,
    onAddComment: (postId: String, text: String) -> Unit,
    onPostQuestion: (title: String, content: String, exam: ExamCategory) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Doubts, 1: Student Forum
    var showNewQuestionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("live_doubt_and_forum_screen")
    ) {
        // Tab Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = NavyPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Live Doubt Sessions", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.LiveTv, contentDescription = null) },
                    modifier = Modifier.testTag("tab_live_doubts")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Student Forum", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                    modifier = Modifier.testTag("tab_student_forum")
                )
            }
        }

        if (selectedTab == 0) {
            // Live Doubt Sessions List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = if (currentLanguage == Language.ASSAMESE) "প্ৰত্যক্ষ সন্দেহ নিবাৰণ সত্ৰ (Live Doubts)" else "Live Faculty Doubt Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Interactive video mentorship directly with Academy Director & Faculty",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    CommunityLinksCard(currentLanguage = currentLanguage)
                }

                items(doubtSessions) { session ->
                    LiveDoubtCard(session = session, currentLanguage = currentLanguage)
                }
            }
        } else {
            // Student Forum List
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (currentLanguage == Language.ASSAMESE) "শিক্ষাৰ্থী আলোচনা চক্ৰ" else "Aspirant Community Forum",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                Text(
                                    text = "Peer discussions, notes sharing & exam doubts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        CommunityLinksCard(currentLanguage = currentLanguage)
                    }

                    items(forumPosts) { post ->
                        ForumPostCard(
                            post = post,
                            onUpvote = { onUpvotePost(post.id) },
                            onAddComment = { text -> onAddComment(post.id, text) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }

                // Floating Ask Question Button
                FloatingActionButton(
                    onClick = { showNewQuestionDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyDark,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .testTag("fab_ask_question")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ask Doubt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showNewQuestionDialog) {
        NewQuestionDialog(
            onDismiss = { showNewQuestionDialog = false },
            onSubmit = { title, content, exam ->
                onPostQuestion(title, content, exam)
                showNewQuestionDialog = false
            }
        )
    }
}

@Composable
fun LiveDoubtCard(session: LiveDoubtSession, currentLanguage: Language) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (session.isLiveNow) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDC2626)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LIVE NOW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavyPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = session.scheduledTime,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${session.examCategory.shortName} • ${session.duration}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (currentLanguage == Language.ASSAMESE) session.titleAs else session.titleEn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mentor Detail Row with photo if Director
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.dp, GoldAccent, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_director),
                        contentDescription = "Mentor Portrait",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(session.mentorName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = NavyDark)
                    Text(session.mentorTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Agenda List
            Column(modifier = Modifier.padding(start = 4.dp)) {
                session.agenda.forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(session.meetingLink))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Opening Google Meet link: ${session.meetingLink}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (session.isLiveNow) Color(0xFFDC2626) else NavyPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.VideoCall, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (session.isLiveNow) "Join Live Video Session" else "Register / Set Reminder",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ForumPostCard(
    post: ForumPost,
    onUpvote: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var isCommentsExpanded by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(post.authorName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(post.timeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = post.examTag.shortName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Upvote Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (post.hasUpvoted) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clickable(onClick = onUpvote)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (post.hasUpvoted) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.upvotes}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (post.hasUpvoted) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(onClick = { isCommentsExpanded = !isCommentsExpanded }) {
                    Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.comments.size} Replies")
                }
            }

            // Replies section
            AnimatedVisibility(visible = isCommentsExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    post.comments.forEach { comment ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(comment.authorName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                    Text(comment.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(comment.content, style = MaterialTheme.typography.bodySmall, color = NavyDark)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Add reply input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Write helpful reply...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onAddComment(replyText)
                                    replyText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = NavyPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewQuestionDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, content: String, exam: ExamCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedExam by remember { mutableStateOf(ExamCategory.APSC) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ask Doubt to Academy Community") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Question Title / Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Explain your doubt in detail...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tag Examination:", style = MaterialTheme.typography.labelMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExamCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedExam == cat,
                            onClick = { selectedExam = cat },
                            label = { Text(cat.shortName, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSubmit(title, content, selectedExam)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Post Question")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CommunityLinksCard(currentLanguage: Language) {
    val context = LocalContext.current
    val whatsappUrl = "https://chat.whatsapp.com/JbYsM834wvF6RkDEcpjKjg"
    val facebookUrl = "https://www.facebook.com/TPISSc/"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forum_community_links_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (currentLanguage == Language.ASSAMESE)
                    "অফিচিয়েল কমিউনিটিত সংযোগ স্থাপন কৰক"
                else
                    "Connect on Official Channels",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )

            Text(
                text = if (currentLanguage == Language.ASSAMESE)
                    "দৈনিক আপডেট, লাইভ সন্দেহ ক্লাছৰ জাননী আৰু অন্যান্য ছাত্ৰ-ছাত্ৰীৰ সৈতে যোগাযোগ"
                else
                    "Real-time notifications for live sessions, doubt clearing & state exam alerts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening WhatsApp Group...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("forum_whatsapp_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening Facebook Page...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1877F2),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("forum_facebook_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Facebook",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Facebook", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

