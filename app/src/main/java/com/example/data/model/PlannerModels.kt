package com.example.data.model

data class PlannerGoal(
    val targetExam: ExamCategory = ExamCategory.APSC,
    val targetDateDaysRemaining: Int = 75,
    val targetScore: Int = 145,
    val maxScore: Int = 200,
    val dailyStudyHours: Int = 4,
    val preferredMedium: Language = Language.ENGLISH
)

data class WeakAreaMetric(
    val subjectTag: String,
    val accuracyPercent: Int,
    val questionsAttempted: Int,
    val priorityLevel: String, // "CRITICAL", "MODERATE", "STRONG"
    val adviceEn: String,
    val adviceAs: String
)

enum class TaskType(val label: String) {
    READ_NOTE("Study Material"),
    PRACTICE_TEST("Mock Practice"),
    PYQ_ANALYSIS("PYQ Drill"),
    REVISION("Revision & Formulas"),
    LIVE_DOUBT("Doubt Session")
}

data class ScheduledStudyItem(
    val id: String,
    val dayName: String, // "Monday", "Tuesday", etc.
    val timeSlot: String, // "06:00 AM - 07:30 AM"
    val subject: String,
    val titleEn: String,
    val titleAs: String,
    val type: TaskType,
    val durationMinutes: Int,
    val recommendedTopicId: String? = null,
    val recommendedTestId: String? = null,
    val isCompleted: Boolean = false
)

data class AiGeneratedPlan(
    val headlineEn: String,
    val headlineAs: String,
    val strategicAdviceEn: String,
    val strategicAdviceAs: String,
    val weakAreas: List<WeakAreaMetric>,
    val schedule: List<ScheduledStudyItem>,
    val generatedDate: String = "This Week"
)
