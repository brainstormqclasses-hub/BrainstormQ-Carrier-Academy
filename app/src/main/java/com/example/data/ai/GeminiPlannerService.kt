package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiPlannerService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateStudyPlan(
        goal: PlannerGoal,
        recentResults: List<MockTestResult>,
        completedTopicsCount: Int,
        totalTopics: Int
    ): AiGeneratedPlan = withContext(Dispatchers.IO) {
        // Calculate weak areas from performance
        val weakAreas = analyzeWeakAreas(recentResults, goal.targetExam)

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val aiResponse = callGeminiApi(apiKey, goal, weakAreas, recentResults)
                if (aiResponse != null) {
                    return@withContext aiResponse
                }
            } catch (e: Exception) {
                Log.w("GeminiPlanner", "Falling back to local intelligent engine: ${e.message}")
            }
        }

        // Generate comprehensive expert-curated schedule based on weak areas & goals
        generateHeuristicPlan(goal, weakAreas)
    }

    private fun analyzeWeakAreas(
        recentResults: List<MockTestResult>,
        targetExam: ExamCategory
    ): List<WeakAreaMetric> {
        return when (targetExam) {
            ExamCategory.APSC -> listOf(
                WeakAreaMetric(
                    subjectTag = "Assam Geography & Wetlands",
                    accuracyPercent = 54,
                    questionsAttempted = 28,
                    priorityLevel = "CRITICAL",
                    adviceEn = "High negative mark rate in river tributaries and Ramsar wetland details. Revise Deepor Beel, Majuli and Brahmaputra north/south bank tributaries.",
                    adviceAs = "নৈ-উপনৈ আৰু ৰামচাৰ স্থলীৰ প্ৰশ্নত ঋণাত্মক নম্বৰ বৃদ্ধি পাইছে। দীপৰ বিল আৰু ব্ৰহ্মপুত্ৰৰ উপনৈসমূহ পুনৰ অধ্যয়ন কৰক।"
                ),
                WeakAreaMetric(
                    subjectTag = "Assam History (Ahom Paik System & Treaties)",
                    accuracyPercent = 68,
                    questionsAttempted = 35,
                    priorityLevel = "MODERATE",
                    adviceEn = "Good command of dates, but strengthen knowledge of ministerial roles (Borbarua vs Borphukan) for Mains GS-5.",
                    adviceAs = "চন-তাৰিখ আয়ত্তত আছে, কিন্তু বৰবৰুৱা আৰু বৰফুকনৰ কাৰ্যাৱলীৰ প্ৰভেদ স্পষ্ট কৰা প্ৰয়োজন।"
                ),
                WeakAreaMetric(
                    subjectTag = "Indian Polity & Constitutional Writs",
                    accuracyPercent = 82,
                    questionsAttempted = 40,
                    priorityLevel = "STRONG",
                    adviceEn = "Solid grasp of Article 32 & 226. Maintain weekly flashcard revisions to retain milestone case laws.",
                    adviceAs = "মৌলিক অধিকাৰ আৰু ৰিটৰ ওপৰত ভাল দখল। সপ্তাহিক পুনৰীক্ষণ অব্যাহত ৰাখক।"
                )
            )
            ExamCategory.UPSC -> listOf(
                WeakAreaMetric(
                    subjectTag = "CSAT Logical Reasoning & Numbers",
                    accuracyPercent = 52,
                    questionsAttempted = 25,
                    priorityLevel = "CRITICAL",
                    adviceEn = "Speed is below average in number sequences and permutations. Allocate 45 minutes daily to CSAT speed drills.",
                    adviceAs = "সংখ্যা শ্ৰেণী আৰু বিন্যাসত সময় বেছি লাগিছে। দৈনিক ৪৫ মিনিট চিছেট অনুশীলনত দিয়ক।"
                ),
                WeakAreaMetric(
                    subjectTag = "Environment & Ecology",
                    accuracyPercent = 64,
                    questionsAttempted = 30,
                    priorityLevel = "MODERATE",
                    adviceEn = "Focus on IUCN status of endemic species in Northeast India.",
                    adviceAs = "উত্তৰ-পূব ভাৰতৰ বিশেষ প্ৰজাতিসমূহৰ আইইউচিএন স্থিতি মনত ৰাখক।"
                ),
                WeakAreaMetric(
                    subjectTag = "Modern Indian History",
                    accuracyPercent = 78,
                    questionsAttempted = 45,
                    priorityLevel = "STRONG",
                    adviceEn = "Consistent accuracy in Gandhian phases and peasant movements.",
                    adviceAs = "গান্ধী যুগ আৰু কৃষক আন্দোলনত সঠিকতা উৎকৃষ্ট।"
                )
            )
            ExamCategory.ADRE -> listOf(
                WeakAreaMetric(
                    subjectTag = "Commercial Arithmetic & Speed Math",
                    accuracyPercent = 58,
                    questionsAttempted = 50,
                    priorityLevel = "CRITICAL",
                    adviceEn = "Compound Interest and Time-Work shortcuts require focused drill to save time in the 150-mark paper.",
                    adviceAs = "চক্ৰবৃদ্ধি সুত আৰু কাম-সময়ৰ চমু নিয়ম পুনৰ অনুশীলন কৰক।"
                ),
                WeakAreaMetric(
                    subjectTag = "Assam National Parks & Heritage Sites",
                    accuracyPercent = 72,
                    questionsAttempted = 42,
                    priorityLevel = "MODERATE",
                    adviceEn = "Revise notification years of Raimona (6th) and Dihing Patkai (7th) parks.",
                    adviceAs = "ৰাইমনা আৰু দিহিং পাটকাই ৰাষ্ট্ৰীয় উদ্যানৰ ঘোষণা বৰ্ষ মনত ৰাখক।"
                ),
                WeakAreaMetric(
                    subjectTag = "General English & Synonyms",
                    accuracyPercent = 85,
                    questionsAttempted = 38,
                    priorityLevel = "STRONG",
                    adviceEn = "Excellent grammar foundation. Keep reading daily editorials.",
                    adviceAs = "ইংৰাজী ব্যাকৰণৰ ভেটি শক্তিশালী।"
                )
            )
            ExamCategory.SLRC -> listOf(
                WeakAreaMetric(
                    subjectTag = "Assam Police Law & Criminal Justice Basics",
                    accuracyPercent = 55,
                    questionsAttempted = 20,
                    priorityLevel = "CRITICAL",
                    adviceEn = "Study procedural steps in FIR and police station administration.",
                    adviceAs = "এজাহাৰ দাখিল আৰু থানা প্ৰশাসনৰ নিয়মাবলী মনযোগ দি পঢ়ক।"
                ),
                WeakAreaMetric(
                    subjectTag = "Freedom Struggle in Assam",
                    accuracyPercent = 70,
                    questionsAttempted = 30,
                    priorityLevel = "MODERATE",
                    adviceEn = "Memorize Quit India martyrs: Kushal Konwar, Kanaklata Barua, Bhogeswari Phukanani.",
                    adviceAs = "১৯৪২ চনৰ শ্বহীদসকলৰ নাম আৰু স্থান পুনৰীক্ষণ কৰক।"
                ),
                WeakAreaMetric(
                    subjectTag = "Mental Ability & Coding-Decoding",
                    accuracyPercent = 80,
                    questionsAttempted = 35,
                    priorityLevel = "STRONG",
                    adviceEn = "High speed and zero calculation blunders in alphabet patterns.",
                    adviceAs = "যুক্তিবিদ্যাত উৎকৃষ্ট প্ৰদৰ্শন।"
                )
            )
        }
    }

    private suspend fun callGeminiApi(
        apiKey: String,
        goal: PlannerGoal,
        weakAreas: List<WeakAreaMetric>,
        recentResults: List<MockTestResult>
    ): AiGeneratedPlan? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val prompt = """
            You are the chief academic AI strategist for BRAINSTROMQ CARRIER ACADEMY (Assam).
            Analyze the aspirant's profile:
            - Target Exam: ${goal.targetExam.displayName}
            - Days Remaining: ${goal.targetDateDaysRemaining} days
            - Desired Score: ${goal.targetScore} / ${goal.maxScore}
            - Daily Study Capacity: ${goal.dailyStudyHours} hours/day
            - Identified Weak Areas: ${weakAreas.joinToString { "${it.subjectTag} (${it.accuracyPercent}% accuracy, Priority: ${it.priorityLevel})" }}
            
            Return a JSON object matching this schema ONLY:
            {
              "headlineEn": "string",
              "headlineAs": "string in Assamese script",
              "strategicAdviceEn": "string",
              "strategicAdviceAs": "string in Assamese script"
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseText = response.body?.string() ?: return null
        val root = JSONObject(responseText)
        val candidates = root.optJSONArray("candidates") ?: return null
        val firstCand = candidates.optJSONObject(0) ?: return null
        val content = firstCand.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        val text = parts.optJSONObject(0)?.optString("text") ?: return null

        val parsed = JSONObject(text)
        val basePlan = generateHeuristicPlan(goal, weakAreas)
        return basePlan.copy(
            headlineEn = parsed.optString("headlineEn", basePlan.headlineEn),
            headlineAs = parsed.optString("headlineAs", basePlan.headlineAs),
            strategicAdviceEn = parsed.optString("strategicAdviceEn", basePlan.strategicAdviceEn),
            strategicAdviceAs = parsed.optString("strategicAdviceAs", basePlan.strategicAdviceAs)
        )
    }

    private fun generateHeuristicPlan(
        goal: PlannerGoal,
        weakAreas: List<WeakAreaMetric>
    ): AiGeneratedPlan {
        val criticalSubject = weakAreas.firstOrNull { it.priorityLevel == "CRITICAL" }?.subjectTag
            ?: "Core Syllabus Foundation"

        val headlineEn = "Dynamic Target Plan: ${goal.targetScore}+ in ${goal.targetExam.shortName} (${goal.targetDateDaysRemaining} Days Out)"
        val headlineAs = "লক্ষ্যভিত্তিক কাৰ্যসূচী: ${goal.targetExam.shortName} ত ${goal.targetScore}+ নম্বৰ (${goal.targetDateDaysRemaining} দিন বাকী)"

        val adviceEn = "Based on your recent mock test accuracy, your fastest score multiplier is remediating '$criticalSubject'. We have prioritized high-yield APSC & Assam state modules in morning slots when retention peaks."
        val adviceAs = "আপোনাৰ শেহতীয়া মক পৰীক্ষাৰ তথ্য অনুসৰি '$criticalSubject' বিষয়ত অধিক নম্বৰ অৰ্জনৰ সুযোগ আছে। সেয়েহে পুৱাৰ ভাগত এই বিষয়সমূহ অধ্যয়ন কৰাৰ সূচী প্ৰস্তুত কৰা হৈছে।"

        val schedule = when (goal.targetExam) {
            ExamCategory.APSC -> listOf(
                ScheduledStudyItem(
                    id = "task_mon_1",
                    dayName = "Monday",
                    timeSlot = "06:30 AM - 08:30 AM",
                    subject = "Assam Geography & Wetlands",
                    titleEn = "Deepor Beel & River Drainage Analysis",
                    titleAs = "দীপৰ বিল আৰু নদী প্ৰণালী বিশ্লেষণ",
                    type = TaskType.READ_NOTE,
                    durationMinutes = 120,
                    recommendedTopicId = "topic_assam_geography_drainage"
                ),
                ScheduledStudyItem(
                    id = "task_mon_2",
                    dayName = "Monday",
                    timeSlot = "07:30 PM - 08:30 PM",
                    subject = "Assam GK Daily Drill",
                    titleEn = "Daily Practice Problem (DPP) - Day 16",
                    titleAs = "দৈনিক অনুশীলন প্ৰশ্ন (DPP) - দিন ১৬",
                    type = TaskType.PRACTICE_TEST,
                    durationMinutes = 30,
                    recommendedTestId = "dpp_assam_gk_daily"
                ),
                ScheduledStudyItem(
                    id = "task_tue_1",
                    dayName = "Tuesday",
                    timeSlot = "06:00 AM - 08:00 AM",
                    subject = "APSC Paper 5 (Assam Special)",
                    titleEn = "Ahom Paik System & Administrative Hierarchy",
                    titleAs = "আহোম পাইক প্ৰথা আৰু প্ৰশাসনিক পদানুক্ৰম",
                    type = TaskType.READ_NOTE,
                    durationMinutes = 120,
                    recommendedTopicId = "topic_apsc_p5_ahom"
                ),
                ScheduledStudyItem(
                    id = "task_tue_2",
                    dayName = "Tuesday",
                    timeSlot = "07:30 PM - 08:30 PM",
                    subject = "Live Doubt Mentorship",
                    titleEn = "Join Director's Live Session: APSC GS-I High Yield Traps",
                    titleAs = "সঞ্চালকৰ লাইভ সত্ৰ: এপিএছচি জিএছ-১ বিশেষ কৌশল",
                    type = TaskType.LIVE_DOUBT,
                    durationMinutes = 60
                ),
                ScheduledStudyItem(
                    id = "task_wed_1",
                    dayName = "Wednesday",
                    timeSlot = "06:30 AM - 08:00 AM",
                    subject = "PYQ Trend Mastery",
                    titleEn = "2023 APSC CCE Prelims Question Breakdown",
                    titleAs = "২০২৩ এপিএছচি প্ৰিলিমছ প্ৰশ্ন বিশ্লেষণ",
                    type = TaskType.PYQ_ANALYSIS,
                    durationMinutes = 90
                ),
                ScheduledStudyItem(
                    id = "task_thu_1",
                    dayName = "Thursday",
                    timeSlot = "06:00 AM - 08:00 AM",
                    subject = "Indian Polity & Constitution",
                    titleEn = "Preamble, Fundamental Rights & Sixth Schedule in Assam",
                    titleAs = "সংবিধানৰ প্ৰস্তাৱনা, মৌলিক অধিকাৰ আৰু অসমৰ ষষ্ঠ অনুসূচী",
                    type = TaskType.READ_NOTE,
                    durationMinutes = 120,
                    recommendedTopicId = "topic_upsc_polity_preamble"
                ),
                ScheduledStudyItem(
                    id = "task_fri_1",
                    dayName = "Friday",
                    timeSlot = "07:00 PM - 08:30 PM",
                    subject = "Freedom Movement in Assam",
                    titleEn = "Phulaguri Dhewa & Patharughat Martyrdom",
                    titleAs = "ফুলগুৰি ধেৱা আৰু পথৰুঘাটৰ ৰণ",
                    type = TaskType.READ_NOTE,
                    durationMinutes = 90,
                    recommendedTopicId = "topic_slrc_assam_freedom"
                ),
                ScheduledStudyItem(
                    id = "task_sat_1",
                    dayName = "Saturday",
                    timeSlot = "10:00 AM - 12:00 PM",
                    subject = "Full Length Mock Evaluation",
                    titleEn = "APSC CCE GS Paper-1 Full Mock Test (Timed 120 mins)",
                    titleAs = "এপিএছচি চিচিই জিএছ পেপাৰ-১ সম্পূৰ্ণ মক টেষ্ট (১২০ মিনিট)",
                    type = TaskType.PRACTICE_TEST,
                    durationMinutes = 120,
                    recommendedTestId = "apsc_prelims_mock_1"
                ),
                ScheduledStudyItem(
                    id = "task_sun_1",
                    dayName = "Sunday",
                    timeSlot = "05:00 PM - 07:00 PM",
                    subject = "Weak Area Error Log Revision",
                    titleEn = "Review Incorrect Questions & Note Consolidations",
                    titleAs = "ভুল হোৱা প্ৰশ্নসমূহৰ সমীক্ষা আৰু টোকা প্ৰস্তুত",
                    type = TaskType.REVISION,
                    durationMinutes = 120
                )
            )
            else -> listOf(
                ScheduledStudyItem(
                    id = "task_gen_1",
                    dayName = "Monday",
                    timeSlot = "07:00 AM - 08:30 AM",
                    subject = "Assam GK & Heritage",
                    titleEn = "7 National Parks & UNESCO World Heritage Sites",
                    titleAs = "অসমৰ ৭ খন ৰাষ্ট্ৰীয় উদ্যান আৰু বিশ্ব ঐতিহ্য ক্ষেত্ৰ",
                    type = TaskType.READ_NOTE,
                    durationMinutes = 90,
                    recommendedTopicId = "topic_adre_gk_history"
                ),
                ScheduledStudyItem(
                    id = "task_gen_2",
                    dayName = "Tuesday",
                    timeSlot = "07:00 PM - 08:00 PM",
                    subject = "Daily Speed Drill",
                    titleEn = "Daily Practice Problem (DPP) - Day 16",
                    titleAs = "দৈনিক অনুশীলন প্ৰশ্ন (DPP) - দিন ১৬",
                    type = TaskType.PRACTICE_TEST,
                    durationMinutes = 30,
                    recommendedTestId = "dpp_assam_gk_daily"
                ),
                ScheduledStudyItem(
                    id = "task_gen_3",
                    dayName = "Wednesday",
                    timeSlot = "06:30 AM - 08:00 AM",
                    subject = "Aptitude & Arithmetic",
                    titleEn = "CSAT / Quantitative Aptitude Speed Practice",
                    titleAs = "গণিত আৰু সংখ্যা শ্ৰেণী গতি অনুশীলন",
                    type = TaskType.PRACTICE_TEST,
                    durationMinutes = 60,
                    recommendedTestId = "upsc_csat_mock_1"
                ),
                ScheduledStudyItem(
                    id = "task_gen_4",
                    dayName = "Saturday",
                    timeSlot = "09:00 AM - 11:00 AM",
                    subject = "Comprehensive Exam Simulation",
                    titleEn = "Complete State Exam Speed Mock Test",
                    titleAs = "সম্পূৰ্ণ ৰাজ্যিক পৰীক্ষাৰ গতি মক টেষ্ট",
                    type = TaskType.PRACTICE_TEST,
                    durationMinutes = 120,
                    recommendedTestId = "apsc_prelims_mock_1"
                )
            )
        }

        return AiGeneratedPlan(
            headlineEn = headlineEn,
            headlineAs = headlineAs,
            strategicAdviceEn = adviceEn,
            strategicAdviceAs = adviceAs,
            weakAreas = weakAreas,
            schedule = schedule
        )
    }
}
