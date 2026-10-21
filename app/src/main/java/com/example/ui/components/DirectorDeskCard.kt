package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Language
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun DirectorDeskCard(
    currentLanguage: Language,
    onJoinDoubtSession: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("director_desk_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Director's Real Photo from user request
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldAccent, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_director),
                        contentDescription = "Chief Mentor & Academy Director",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentLanguage == Language.ASSAMESE) "সঞ্চালকৰ বাৰ্তা" else "Director's Desk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "BRAINSTROMQ CARRIER ACADEMY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = if (currentLanguage == Language.ASSAMESE)
                            "এপিএছচি আৰু ইউপিএছচি পৰীক্ষাত সফলতাৰ মাৰ্গদৰ্শন"
                        else
                            "Chief Mentor & State Competitive Exam Adviser",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLanguage == Language.ASSAMESE)
                            "\"অধ্যৱসায়, শুদ্ধ দিশ আৰু আত্মবিশ্বাসেই এপিএছচি আৰু ৰাজ্যিক পৰীক্ষাৰ সফল চাবিকাঠি। অসমীয়া আৰু ইংৰাজী উভয় মাধ্যমত যুগুত কৰা বিষয়বস্তু আৰু এই এআই প্লেনাৰে আপোনাক প্ৰতিদিনে লক্ষ্যৰ ওচৰলৈ লৈ যাব।\""
                        else
                            "\"Consistency, sharp PYQ analysis, and bilingual clarity will take you straight to the Assam Secretariat. Make the AI Study Planner your daily compass, and attend our live doubt sessions.\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.95f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onJoinDoubtSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("director_live_doubt_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = NavyDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLanguage == Language.ASSAMESE)
                        "সঞ্চালকৰ লাইভ ডাউট সত্ৰত যোগ দিয়ক"
                    else
                        "Join Director's Live Doubt Session (7:30 PM)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
