package com.example.data.model

enum class Language(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    ASSAMESE("অসমীয়া (Assamese)", "as")
}

enum class ExamCategory(val displayName: String, val shortName: String, val description: String) {
    APSC("APSC Combined Competitive Exam", "APSC CCE", "Assam Public Service Commission (Prelims & Mains GS I-V)"),
    UPSC("UPSC Civil Services Examination", "UPSC CSE", "Union Public Service Commission (IAS/IPS/IFS)"),
    ADRE("Assam Direct Recruitment Exam", "ADRE", "State Level Recruitment for Grade III & Grade IV Posts"),
    SLRC("Assam Police & SLRC Govt Exams", "SLRC / Police", "Sub-Inspector, Constable, Forest Guard & State Govt Jobs")
}

enum class ExamStage(val displayName: String) {
    PRELIMS("Prelims Examination"),
    MAINS("Mains Examination"),
    GENERAL("General Preparation")
}

data class SubjectPaper(
    val id: String,
    val examCategory: ExamCategory,
    val stage: ExamStage,
    val code: String, // e.g. "GS Paper 1", "Paper 5 (Assam Special)"
    val titleEn: String,
    val titleAs: String,
    val totalTopics: Int,
    val completedTopics: Int,
    val pyqQuestionsCount: Int
)

data class StudyTopic(
    val id: String,
    val paperId: String,
    val category: ExamCategory,
    val titleEn: String,
    val titleAs: String,
    val summaryEn: String,
    val summaryAs: String,
    val contentEn: String,
    val contentAs: String,
    val keyPointsEn: List<String>,
    val keyPointsAs: List<String>,
    val pyqReferences: List<String>,
    val readMinutes: Int = 8,
    val isBookmarked: Boolean = false,
    val isCompleted: Boolean = false,
    val isPremiumOnly: Boolean = false
)

data class QuizQuestion(
    val id: String,
    val questionEn: String,
    val questionAs: String,
    val optionsEn: List<String>,
    val optionsAs: List<String>,
    val correctOptionIndex: Int,
    val explanationEn: String,
    val explanationAs: String,
    val topicTag: String,
    val pyqYear: String? = null
)

data class MockTest(
    val id: String,
    val titleEn: String,
    val titleAs: String,
    val examCategory: ExamCategory,
    val stage: ExamStage,
    val durationMinutes: Int,
    val totalMarks: Int,
    val negativeMarks: Float,
    val questions: List<QuizQuestion>,
    val isDailyPractice: Boolean = false,
    val isPremium: Boolean = false
)

data class MockTestResult(
    val testId: String,
    val testTitle: String,
    val score: Float,
    val totalMarks: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val skippedCount: Int,
    val accuracy: Float,
    val timeTakenSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class PyqYearAnalysis(
    val id: String,
    val examCategory: ExamCategory,
    val year: String,
    val cutoffGeneral: String,
    val cutoffOBC: String,
    val keyTrendEn: String,
    val keyTrendAs: String,
    val subjectWeightage: Map<String, Int>, // Subject -> % of questions
    val topQuestions: List<QuizQuestion>
)

data class ForumComment(
    val id: String,
    val authorName: String,
    val authorRole: String, // "Aspirant", "APSC Ranker", "Faculty Mentor"
    val content: String,
    val timestamp: String,
    val likes: Int = 0
)

data class ForumPost(
    val id: String,
    val title: String,
    val content: String,
    val authorName: String,
    val examTag: ExamCategory,
    val timeAgo: String,
    val upvotes: Int,
    val hasUpvoted: Boolean = false,
    val comments: List<ForumComment> = emptyList()
)

data class LiveDoubtSession(
    val id: String,
    val titleEn: String,
    val titleAs: String,
    val mentorName: String,
    val mentorTitle: String,
    val examCategory: ExamCategory,
    val scheduledTime: String,
    val duration: String,
    val isLiveNow: Boolean,
    val agenda: List<String>,
    val meetingLink: String = "https://meet.google.com/brainstormq-academy-live"
)

data class UserProfile(
    val name: String = "Aspirant",
    val phoneOrEmail: String = "aspirant@brainstormq.org",
    val targetExam: ExamCategory = ExamCategory.APSC,
    val targetYear: String = "2026-2027",
    val preferredLanguage: Language = Language.ENGLISH,
    val isBiometricEnabled: Boolean = true,
    val isPremiumUnlocked: Boolean = false, // Lifetime pass Rs. 99/-
    val dailyStreak: Int = 14,
    val totalQuestionsSolved: Int = 128,
    val averageScorePercent: Int = 76,
    val completedTopicsCount: Int = 42
)

data class DailyQuizState(
    val examCategory: ExamCategory,
    val questions: List<QuizQuestion>,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val answersMap: Map<Int, Int> = emptyMap(),
    val correctCount: Int = 0,
    val isCompleted: Boolean = false,
    val streakAdded: Boolean = false,
    val timeTakenSeconds: Int = 0
)
