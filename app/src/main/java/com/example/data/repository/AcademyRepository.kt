package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AcademyRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("brainstormq_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _currentLanguage = MutableStateFlow(
        if (prefs.getString("pref_lang", "en") == "as") Language.ASSAMESE else Language.ENGLISH
    )
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val _topics = MutableStateFlow(createInitialTopics())
    val topics: StateFlow<List<StudyTopic>> = _topics.asStateFlow()

    private val _mockTests = MutableStateFlow(createInitialMockTests())
    val mockTests: StateFlow<List<MockTest>> = _mockTests.asStateFlow()

    private val _recentResults = MutableStateFlow<List<MockTestResult>>(loadRecentResults())
    val recentResults: StateFlow<List<MockTestResult>> = _recentResults.asStateFlow()

    private val _forumPosts = MutableStateFlow(createInitialForumPosts())
    val forumPosts: StateFlow<List<ForumPost>> = _forumPosts.asStateFlow()

    private val _doubtSessions = MutableStateFlow(createInitialDoubtSessions())
    val doubtSessions: StateFlow<List<LiveDoubtSession>> = _doubtSessions.asStateFlow()

    private val _pyqAnalyses = MutableStateFlow(createInitialPyqAnalyses())
    val pyqAnalyses: StateFlow<List<PyqYearAnalysis>> = _pyqAnalyses.asStateFlow()

    fun setLanguage(lang: Language) {
        _currentLanguage.value = lang
        prefs.edit().putString("pref_lang", lang.code).apply()
        _userProfile.update { it.copy(preferredLanguage = lang) }
    }

    fun updateTargetExam(exam: ExamCategory) {
        _userProfile.update { it.copy(targetExam = exam) }
        prefs.edit().putString("target_exam", exam.name).apply()
    }

    fun updateProfile(name: String, targetExam: ExamCategory, targetYear: String) {
        _userProfile.update {
            it.copy(
                name = name,
                targetExam = targetExam,
                targetYear = targetYear
            )
        }
        prefs.edit()
            .putString("user_name", name)
            .putString("target_exam", targetExam.name)
            .putString("target_year", targetYear)
            .apply()
    }

    fun unlockPremium(transactionId: String) {
        _userProfile.update { it.copy(isPremiumUnlocked = true) }
        prefs.edit()
            .putBoolean("is_premium", true)
            .putString("payment_tx_id", transactionId)
            .putLong("payment_time", System.currentTimeMillis())
            .apply()
    }

    fun toggleBookmark(topicId: String) {
        _topics.update { list ->
            list.map {
                if (it.id == topicId) it.copy(isBookmarked = !it.isBookmarked) else it
            }
        }
    }

    fun markTopicCompleted(topicId: String) {
        _topics.update { list ->
            list.map {
                if (it.id == topicId) it.copy(isCompleted = true) else it
            }
        }
        _userProfile.update { it.copy(completedTopicsCount = it.completedTopicsCount + 1) }
    }

    fun saveTestResult(result: MockTestResult) {
        _recentResults.update { listOf(result) + it }
        _userProfile.update {
            it.copy(
                totalQuestionsSolved = it.totalQuestionsSolved + result.correctCount + result.incorrectCount,
                averageScorePercent = ((it.averageScorePercent + (result.accuracy * 100).toInt()) / 2).coerceIn(0, 100)
            )
        }
    }

    fun addForumPost(title: String, content: String, exam: ExamCategory) {
        val newPost = ForumPost(
            id = "fp_${System.currentTimeMillis()}",
            title = title,
            content = content,
            authorName = _userProfile.value.name,
            examTag = exam,
            timeAgo = "Just now",
            upvotes = 1,
            hasUpvoted = true,
            comments = emptyList()
        )
        _forumPosts.update { listOf(newPost) + it }
    }

    fun toggleUpvote(postId: String) {
        _forumPosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val newUpvoted = !post.hasUpvoted
                    post.copy(
                        hasUpvoted = newUpvoted,
                        upvotes = if (newUpvoted) post.upvotes + 1 else (post.upvotes - 1).coerceAtLeast(0)
                    )
                } else post
            }
        }
    }

    fun addForumComment(postId: String, commentText: String) {
        val newComment = ForumComment(
            id = "fc_${System.currentTimeMillis()}",
            authorName = _userProfile.value.name,
            authorRole = "Aspirant",
            content = commentText,
            timestamp = "Just now",
            likes = 0
        )
        _forumPosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    post.copy(comments = post.comments + newComment)
                } else post
            }
        }
    }

    private fun loadUserProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("user_name", "Pranjal Saikia") ?: "Pranjal Saikia",
            phoneOrEmail = "aspirant@brainstormq.org",
            targetExam = try {
                ExamCategory.valueOf(prefs.getString("target_exam", ExamCategory.APSC.name) ?: ExamCategory.APSC.name)
            } catch (e: Exception) {
                ExamCategory.APSC
            },
            targetYear = prefs.getString("target_year", "2026-2027") ?: "2026-2027",
            preferredLanguage = if (prefs.getString("pref_lang", "en") == "as") Language.ASSAMESE else Language.ENGLISH,
            isBiometricEnabled = prefs.getBoolean("biometric_enabled", true),
            isPremiumUnlocked = prefs.getBoolean("is_premium", false),
            dailyStreak = prefs.getInt("daily_streak", 16),
            totalQuestionsSolved = prefs.getInt("total_solved", 142),
            averageScorePercent = prefs.getInt("avg_score", 78),
            completedTopicsCount = prefs.getInt("topics_done", 38)
        )
    }

    private fun loadRecentResults(): List<MockTestResult> {
        return listOf(
            MockTestResult(
                testId = "apsc_prelims_mock_1",
                testTitle = "APSC CCE GS-I Full Mock Test 01",
                score = 138.5f,
                totalMarks = 200,
                correctCount = 74,
                incorrectCount = 19,
                skippedCount = 7,
                accuracy = 0.79f,
                timeTakenSeconds = 6420
            ),
            MockTestResult(
                testId = "adre_gk_mock_1",
                testTitle = "ADRE Grade-III Assam GK Sprint",
                score = 86f,
                totalMarks = 100,
                correctCount = 44,
                incorrectCount = 4,
                skippedCount = 2,
                accuracy = 0.91f,
                timeTakenSeconds = 2100
            )
        )
    }

    private fun createInitialTopics(): List<StudyTopic> {
        return listOf(
            // APSC Paper 5 Special Assam Paper
            StudyTopic(
                id = "topic_apsc_p5_ahom",
                paperId = "apsc_mains_gs5",
                category = ExamCategory.APSC,
                titleEn = "Ahom Administration & Paik System",
                titleAs = "আহোম প্ৰশাসন আৰু পাইক ব্যৱস্থা",
                summaryEn = "In-depth breakdown of Sukaphaa's lineage, Khel/Paik system, Borbarua & Borphukan offices, and administrative reforms under Pratap Singha.",
                summaryAs = "চুকাফাৰ বংশাৱলী, খেল আৰু পাইক প্ৰথা, বৰবৰুৱা আৰু বৰফুকন পদবী আৰু প্ৰতাপ সিংহৰ প্ৰশাসনিক সংস্কাৰৰ বিতং পৰ্যালোচনা।",
                contentEn = """
                    The Ahom kingdom (1228–1826 CE) established by Chaolung Sukaphaa in Upper Assam pioneered a distinctive administrative, military, and socio-economic framework.
                    
                    1. The Paik System:
                    - Every adult male between 16 and 50 years was registered as a Paik (foot soldier and state laborer).
                    - Four Paiks formed a 'Got' (reduced to three under Rajeswar Singha). In rotation, one Paik served the monarch, defense, or infrastructure while the others tilled his paddy.
                    - Units of Paiks: Bora (over 20 paiks), Saikia (over 100), Hazarika (over 1,000), Rajkhowa (3,000), Phukan (6,000).
                    
                    2. Ministerial Council (Patra-Mantri):
                    - Borgohain, Burhagohain, Barpatrogohain (the three great councillors from specific Ahom clans).
                    - Momai Tamuli Borbarua during Swargadeo Pratap Singha's reign reorganized the entire village structure and standardized the Paik/Khel census.
                    
                    3. Battle of Saraighat (1671):
                    - Led by Mahabir Lachit Borphukan on the Brahmaputra waters, showcasing indigenous naval warfare and diplomatic decoy tactics defeating Ram Singh I.
                """.trimIndent(),
                contentAs = """
                    চাওলুং চুকাফাই ১২২৮ খ্ৰীষ্টাব্দত উজনি অসমত প্ৰতিষ্ঠা কৰা আহোম সাম্ৰাজ্যৰ প্ৰশাসন এক সুসংহত আৰ্হি আছিল।
                    
                    ১. পাইক প্ৰথা:
                    - ১৬ ৰ পৰা ৫০ বছৰ বয়সলৈকে প্ৰতিজন সক্ষম পুৰুষ পাইক হিচাপে ৰাজ্যসেৱাত বাধ্য আছিল।
                    - চাৰিজন পাইকেৰে এটা 'গোট' গঠন হৈছিল (পিছলৈ ৰাজেশ্বৰ সিংহৰ দিনত তিনিজন কৰা হয়)। এজন সদস্য ৰাজ্যৰ কামলৈ যোৱাৰ সময়ত বাকী তিনিজনে তেওঁৰ খেতি-বাতি চোৱা-চিতা কৰিছিল।
                    - পদানুক্ৰম: বৰা (২০ জন), শইকীয়া (১০০ জন), হাজৰিকা (১,০০০ জন), ৰাজখোৱা (৩,০০০ জন), ফুকন (৬,০০০ জন)।
                    
                    ২. মন্ত্ৰী পৰিষদ আৰু মোমাই তামুলী বৰবৰুৱা:
                    - বুঢ়াগোহাঁই, বৰগোঁহাই আৰু বৰপাত্ৰগোঁহাই।
                    - স্বৰ্গদেউ প্ৰতাপ সিংহৰ দিনত মোমাই তামুলী বৰবৰুৱাই অসমৰ গাওঁসমূহ পুনৰ্সংগঠিত কৰে।
                    
                    ৩. শৰাইঘাটৰ যুদ্ধ (১৬৭১):
                    - মহাবীৰ লাচিত বৰফুকনৰ অতুলনীয় নেতৃত্বত ব্ৰহ্মপুত্ৰৰ বুকুত মোগল সেনাপতি ৰাম সিংহক পৰাস্ত কৰা হয়।
                """.trimIndent(),
                keyPointsEn = listOf(
                    "Paik system ensured standing militia without cash salary drain",
                    "Momai Tamuli Borbarua created self-sufficient village economies",
                    "Saraighat battle strategy: Riverine choke-point at Guwahati hills",
                    "Frequent APSC Mains GS-5 essay and 15-mark question"
                ),
                keyPointsAs = listOf(
                    "পাইক প্ৰথাই নগদ ধন খৰচ নকৰাকৈ শক্তিশালী সৈন্যবাহিনী নিশ্চিত কৰিছিল",
                    "মোমাই তামুলী বৰবৰুৱাই স্বাৱলম্বী গ্ৰাম্য অৰ্থনীতি গঢ়ি তুলিছিল",
                    "শৰাইঘাটৰ কৌশল: ব্ৰহ্মপুত্ৰৰ সংকীৰ্ণ জলপথৰ সদ্ব্যৱহাৰ",
                    "এপিএছচি মেইনছ জিএছ-৫ কাকতৰ বাবে অতি গুৰুত্বপূৰ্ণ প্ৰশ্ন"
                ),
                pyqReferences = listOf("APSC Mains 2023 Q12", "APSC Mains 2020 Q4", "APSC Prelims 2022 Q38"),
                readMinutes = 10,
                isCompleted = true
            ),
            // APSC Prelims / Assam Geography
            StudyTopic(
                id = "topic_assam_geography_drainage",
                paperId = "apsc_prelims_gs1",
                category = ExamCategory.APSC,
                titleEn = "Assam Physiography, River Drainage & Wetlands",
                titleAs = "অসমৰ ভূ-প্ৰকৃতি, নদী প্ৰণালী আৰু জলাশয়",
                summaryEn = "Brahmaputra and Barak river basins, North & South bank tributaries, Majuli river island, and Ramsar site Deepor Beel.",
                summaryAs = "ব্ৰহ্মপুত্ৰ আৰু বৰাক নদী উপত্যকা, উত্তৰ আৰু দক্ষিণ পাৰৰ উপনদী, মাজুলী নদীদ্বীপ আৰু ৰামচাৰ ক্ষেত্ৰ দীপৰ বিল।",
                contentEn = """
                    Assam occupies a strategic geomorphological location in the Eastern Himalayas and Shillong Plateau transition zone.
                    
                    1. Brahmaputra Drainage:
                    - Originates as Yarlung Tsangpo in Chemayungdung glacier (Tibet), enters Arunachal as Siang/Dihang, meets Dibang and Lohit at Kobo to form Brahmaputra.
                    - North Bank Tributaries: Subansiri (largest tributary), Jia Bharali (Kameng), Manas, Sankosh, Pagladiya, Barnadi.
                    - South Bank Tributaries: Burhi Dihing, Disang, Dikhow, Dhansiri, Kopili, Kulsi.
                    
                    2. Majuli:
                    - World's largest inhabited freshwater river island, formed due to tectonic shifts and 1750 Great Flood when Brahmaputra met Burhi Dihing.
                    
                    3. Deepor Beel:
                    - Permanent freshwater lake and only Ramsar site in Assam (designated 2002), critical for migratory avifauna and elephant corridor.
                """.trimIndent(),
                contentAs = """
                    অসম পূব হিমালয় আৰু মেঘালয় মালভূমিৰ মাজৰ কৌশলগত ভৌগোলিক স্থানত অৱস্থিত।
                    
                    ১. ব্ৰহ্মপুত্ৰ নদী প্ৰণালী:
                    - তিব্বতৰ চেমায়ুংদুং হিমবাহৰ পৰা য়াৰলুং চাংপো নামেৰে উৎপত্তি হৈ অৰুণাচলত চিয়াং আৰু দিহং ৰূপ লয়। শদিয়াৰ ওচৰত দিবাং আৰু লোহিত নদীৰ সৈতে মিলি ব্ৰহ্মপুত্ৰ নাম পায়।
                    - উত্তৰ পাৰৰ উপনৈ: সোৱণশিৰি (বৃহত্তম), জিয়া ভৰলী, মানাহ, সোণকোষ, পাগলাদিয়া।
                    - দক্ষিণ পাৰৰ উপনৈ: বুঢ়ী দিহিং, দিচাং, দিখৌ, ধনশিৰি, কপিলী।
                    
                    ২. মাজুলী: বিশ্বৰ সৰ্ববৃহৎ জনবসতিপূৰ্ণ নদীদ্বীপ, ১৭৫০ চনৰ প্ৰলয়ংকৰী বানত ব্ৰহ্মপুত্ৰ আৰু বুঢ়ী দিহিঙৰ গতিপথ সলনি হৈ গঠিত হৈছিল।
                    
                    ৩. দীপৰ বিল: অসমৰ একমাত্ৰ ৰামচাৰ স্থলী (২০০২), গুৱাহাটীৰ অন্যতম গুৰুত্বপূৰ্ণ জৈৱ-বৈচিত্ৰ্য অঞ্চল।
                """.trimIndent(),
                keyPointsEn = listOf(
                    "Subansiri is the largest tributary of Brahmaputra",
                    "Majuli is recognized as India's first island district",
                    "Deepor Beel is Assam's single Ramsar site with vital elephant migratory path",
                    "Recurrent floods cause annual bank erosion of over 8,000 hectares"
                ),
                keyPointsAs = listOf(
                    "সোৱণশিৰি ব্ৰহ্মপুত্ৰৰ আটাইতকৈ ডাঙৰ উপনৈ",
                    "মাজুলী ভাৰতৰ প্ৰথমখন নদীদ্বীপ জিলা",
                    "দীপৰ বিল অসমৰ একমাত্ৰ ৰামচাৰ আৰ্দ্ৰভূমি",
                    "প্ৰতিবছৰে গৰাখহনীয়াত ৮,০০০ হেক্টৰৰো অধিক মাটি বিলীন হয়"
                ),
                pyqReferences = listOf("APSC Prelims 2023 Q18", "APSC Prelims 2021 Q42", "ADRE 2022 Q15"),
                readMinutes = 7,
                isCompleted = true
            ),
            // UPSC / Indian Polity
            StudyTopic(
                id = "topic_upsc_polity_preamble",
                paperId = "upsc_prelims_gs1",
                category = ExamCategory.UPSC,
                titleEn = "Preamble, Fundamental Rights & Sixth Schedule",
                titleAs = "সংবিধানৰ প্ৰস্তাৱনা, মৌলিক অধিকাৰ আৰু ষষ্ঠ অনুসূচী",
                summaryEn = "Salient features of Indian Constitution, Kesavananda Bharati basic structure doctrine, Article 32, and Sixth Schedule autonomous tribal councils in Assam.",
                summaryAs = "ভাৰতীয় সংবিধানৰ বৈশিষ্ট্যাৱলী, কেশৱানন্দ ভাৰতীৰ মূল গাঁথনি তত্ত্ব, ৩২ নং অনুচ্ছেদ আৰু অসমৰ ষষ্ঠ অনুসূচীৰ স্বায়ত্তশাসিত পৰিষদ।",
                contentEn = """
                    The Constitution of India balances parliamentary sovereignty with judicial supremacy.
                    
                    1. The Preamble:
                    - Based on Jawaharlal Nehru's 'Objective Resolution'.
                    - Declares India Sovereign, Socialist, Secular, Democratic, Republic.
                    - 42nd Amendment Act (1976) added three terms: 'Socialist', 'Secular', and 'Integrity'.
                    - Kesavananda Bharati (1973): Preamble is an integral part of the Constitution and can be amended under Article 368 without violating Basic Structure.
                    
                    2. Sixth Schedule (Articles 244(2) and 275(1)):
                    - Deals with administration of tribal areas in Assam, Meghalaya, Tripura, and Mizoram (AMTM).
                    - In Assam: Bodoland Territorial Council (BTC), Karbi Anglong Autonomous Council (KAAC), Dima Hasao Autonomous Council (DHAC).
                    - Councils possess legislative, judicial, and financial self-governance.
                """.trimIndent(),
                contentAs = """
                    ভাৰতীয় সংবিধানে সংসদীয় সাৰ্বভৌমত্ব আৰু ন্যায়িক সৰ্বোচ্চতাৰ মাজত ভাৰসাম্য ৰক্ষা কৰে।
                    
                    ১. প্ৰস্তাৱনা:
                    - পণ্ডিত জৱাহৰলাল নেহৰুৰ 'উদ্দেশ্যমূলক প্ৰস্তাৱ' (Objective Resolution) ৰ ওপৰত ভিত্তি কৰি গঠিত।
                    - ৪২তম সংবিধান সংশোধনী (১৯৭৬) ৰ জৰিয়তে তিনিটা শব্দ সংযোজিত কৰা হয়: 'সমাজবাদী', 'ধৰ্মনিৰপেক্ষ' আৰু 'সংহতি'।
                    - কেশৱানন্দ ভাৰতী গোচৰ (১৯৭৩): প্ৰস্তাৱনা সংবিধানৰ অবিচ্ছেদ্য অংগ আৰু মূল গাঁথনি অক্ষুণ্ণ ৰাখি ইয়াক সংশোধন কৰিব পাৰি।
                    
                    ২. ষষ্ঠ অনুসূচী (অসমৰ প্ৰসংগ):
                    - অসমৰ বড়োলেণ্ড স্বায়ত্তশাসিত পৰিষদ (BTC), কাৰ্বি আংলং (KAAC), আৰু ডিমা হাছাও স্বায়ত্তশাসিত পৰিষদ (DHAC)।
                """.trimIndent(),
                keyPointsEn = listOf(
                    "Preamble is non-justiciable in court of law",
                    "Basic Structure doctrine originated in 1973 Kesavananda Bharati",
                    "Assam has three Sixth Schedule Autonomous District Councils (ADCs)",
                    "High relevance for both UPSC CSE GS-II and APSC CCE GS-II"
                ),
                keyPointsAs = listOf(
                    "প্ৰস্তাৱনা ন্যায়ালয়ত বলবৎযোগ্য নহয়",
                    "১৯৭৩ চনৰ কেশৱানন্দ ভাৰতী গোচৰত মূল গাঁথনি তত্ত্বৰ উৎপত্তি",
                    "অসমত ষষ্ঠ অনুসূচীৰ অধীনত ৩খন স্বায়ত্তশাসিত জিলা পৰিষদ আছে",
                    "ইউপিএছচি আৰু এপিএছচি দুয়োটা পৰীক্ষাৰ বাবে অতি প্ৰয়োজনীয়"
                ),
                pyqReferences = listOf("UPSC CSE Prelims 2021 Q3", "APSC Prelims 2023 Q5", "UPSC Mains 2022 GS2 Q2"),
                readMinutes = 8,
                isCompleted = false
            ),
            // ADRE Special / Assam GK & Numerical Ability
            StudyTopic(
                id = "topic_adre_gk_history",
                paperId = "adre_grade3_paper",
                category = ExamCategory.ADRE,
                titleEn = "ADRE Assam History & National Parks Quick Capsule",
                titleAs = "এডিআৰই অসমৰ বুৰঞ্জী আৰু ৰাষ্ট্ৰীয় উদ্যানৰ চমু হাতপুথি",
                summaryEn = "7 National Parks of Assam, UNESCO World Heritage Sites, Treaty of Yandaboo (1826), and First Assamese Printing Press.",
                summaryAs = "অসমৰ ৭ খন ৰাষ্ট্ৰীয় উদ্যান, ইউনেস্কো বিশ্ব ঐতিহ্য ক্ষেত্ৰ, ইয়াণ্ডাবু সন্ধি (১৮২৬) আৰু অসমীয়া প্ৰথম ছপাশাল।",
                contentEn = """
                    Key facts distilled strictly for Assam Direct Recruitment (ADRE Grade III & IV) exams:
                    
                    1. 7 National Parks of Assam:
                    - Kaziranga (1974) - UNESCO World Heritage Site (1985), Famous for One-horned Rhinoceros.
                    - Manas (1990) - UNESCO World Heritage, Project Tiger, Biosphere Reserve.
                    - Nameri (1998) - Home of White-winged Wood Duck (Deo Hah).
                    - Dibru-Saikhowa (1999) - Feral horses, Biosphere reserve.
                    - Orang (2016) - Known as 'Mini Kaziranga'.
                    - Raimona (2021) - 6th National Park, located in Kokrajhar (Golden Langur).
                    - Dihing Patkai (2021) - 7th National Park, Jeypore rainforest area.
                    
                    2. Historic Landmarks:
                    - Treaty of Yandaboo: Signed 24 February 1826, concluding First Anglo-Burmese War.
                    - Orunodoi (অৰুণোদই): First Assamese magazine published in 1846 from Sibsagar Mission Press.
                """.trimIndent(),
                contentAs = """
                    অসম প্ৰত্যক্ষ নিযুক্তি (ADRE ৩য় আৰু ৪র্থ বৰ্গ) পৰীক্ষাৰ বাবে গুৰুত্বপূৰ্ণ তথ্য:
                    
                    ১. অসমৰ ৭ খন ৰাষ্ট্ৰীয় উদ্যান:
                    - কাজিৰঙা (১৯৭৪) - ইউনেস্কো বিশ্ব ঐতিহ্য ক্ষেত্ৰ (১৯৮৫), এশিঙীয়া গঁড়।
                    - মানাহ (১৯৯০) - ইউনেস্কো ঐতিহ্য ক্ষেত্ৰ, ব্যাঘ্ৰ প্ৰকল্প।
                    - নামেৰি (১৯৯৮) - দেওহাঁহৰ বাসস্থান।
                    - ডিব্ৰু-ছৈখোৱা (১৯৯৯) - বনৰীয়া ঘোঁৰা।
                    - ওৰাং (২০১৬) - 'মিনি কাজিৰঙা'।
                    - ৰাইমনা (২০২১) - ৬ষ্ঠ ৰাষ্ট্ৰীয় উদ্যান, কোকৰাঝাৰ (সোণালী বান্দৰ)।
                    - দিহিং পাটকাই (২০২১) - ৭ম ৰাষ্ট্ৰীয় উদ্যান, অসমৰ বৰ্ষাৰণ্য।
                    
                    ২. বুৰঞ্জীৰ মাইলৰ খুঁটি:
                    - ইয়াণ্ডাবু সন্ধি: ১৮২৬ চনৰ ২৪ ফেব্ৰুৱাৰী, ব্ৰিটিছ আৰু ব্ৰহ্মদেশৰ মাজত।
                    - অৰুণোদই: ১৮৪৬ চনত শিৱসাগৰ মিছন প্ৰেছৰ পৰা প্ৰকাশিত প্ৰথম অসমীয়া আলোচনী।
                """.trimIndent(),
                keyPointsEn = listOf(
                    "Assam holds 7 National Parks, second highest in Northeast India",
                    "Raimona (6th) and Dihing Patkai (7th) notified in June 2021",
                    "Treaty of Yandaboo marked the beginning of British East India Company rule in Assam",
                    "High probability question in ADRE 2024-2026 exams"
                ),
                keyPointsAs = listOf(
                    "অসমত মুঠ ৭ খন ৰাষ্ট্ৰীয় উদ্যান আছে",
                    "ৰাইমনা (৬ষ্ঠ) আৰু দিহিং পাটকাই (৭ম) ২০২১ চনৰ জুনত ঘোষিত",
                    "১৮২৬ চনৰ ইয়াণ্ডাবু সন্ধিয়ে অসমত ব্ৰিটিছ শাসনৰ সূচনা কৰে",
                    "এডিআৰই পৰীক্ষাত বাৰে বাৰে অহা প্ৰশ্ন"
                ),
                pyqReferences = listOf("ADRE 2022 Grade III Q8", "SLRC SI 2023 Q14", "Forest Guard 2022 Q2"),
                readMinutes = 6,
                isCompleted = false
            ),
            // SLRC Police / Assam Modern History
            StudyTopic(
                id = "topic_slrc_assam_freedom",
                paperId = "slrc_assam_police",
                category = ExamCategory.SLRC,
                titleEn = "Freedom Movement in Assam & Peasant Uprisings",
                titleAs = "অসমৰ স্বাধীনতা আন্দোলন আৰু কৃষক বিদ্ৰোহ",
                summaryEn = "Phulaguri Dhewa (1861), Patharughat massacre (1894), Kushal Konwar, Kanaklata Barua, and Bhogeswari Phukanani.",
                summaryAs = "ফুলগুৰি ধেৱা (১৮৬১), পথৰুঘাটৰ ৰণ (১৮৯৪), কুশল কোঁৱৰ, কনকলতা বৰুৱা আৰু ভোগেশ্বৰী ফুকননী।",
                contentEn = """
                    The anti-colonial resistance in Assam saw profound grassroots peasant participation.
                    
                    1. Phulaguri Dhewa (1861):
                    - First peasant revolt in Assam, in Nagaon district against the British ban on opium cultivation and proposed taxes on betel leaf/nut.
                    - British Assistant Commissioner Lt. Singer was killed by the agitating ryots.
                    
                    2. Patharughat Revolt (1894):
                    - In Darrang district, known as the 'Jallianwala Bagh of Assam'.
                    - Unarmed peasants protested steep land revenue hikes. British forces opened fire, killing over 140 peasants.
                    
                    3. Quit India Movement (1942) Martyrs:
                    - Kanaklata Barua & Mukunda Kakati shot while hoisting the Tricolor at Gohpur police station (20 Sept 1942).
                    - Kushal Konwar: Only martyr in India hanged for the Quit India movement (Sarupathar train derailment conspiracy).
                """.trimIndent(),
                contentAs = """
                    ব্ৰিটিছ বিৰোধী সংগ্ৰামত অসমৰ কৃষকে অগ্ৰণী ভূমিকা পালন কৰিছিল।
                    
                    ১. ফুলগুৰি ধেৱা (১৮৬১):
                    - অসমৰ প্ৰথম কৃষক বিদ্ৰোহ। কানি খেতি নিষিদ্ধ কৰা আৰু তামোল-পাণৰ ওপৰত কৰ লগোৱাৰ বিৰুদ্ধে নগাঁৱত সংঘটিত হৈছিল।
                    
                    ২. পথৰুঘাটৰ ৰণ (১৮৯৪):
                    - দৰং জিলাত সংঘটিত, যাক 'অসমৰ জালিয়ানৱালাবাগ' বুলি জনা যায়। প্ৰায় ১৪০ জন নিৰীহ কৃষক ব্ৰিটিছৰ গুলীত শ্বহীদ হয়।
                    
                    ৩. ভাৰত ত্যাগ আন্দোলন (১৯৪২) ৰ শ্বহীদ:
                    - কনকলতা বৰুৱা আৰু মুকুন্দ কাকতি: গহপুৰ থানাত ত্ৰিৰংগা উত্তোলন কৰিবলৈ গৈ শ্বহীদ হয়।
                    - কুশল কোঁৱৰ: সমগ্ৰ ভাৰততে ভাৰত ত্যাগ আন্দোলনত ফাঁচী কাঠত ওলমা একমাত্ৰ শ্বহীদ।
                """.trimIndent(),
                keyPointsEn = listOf(
                    "Phulaguri Dhewa (1861) was Assam's earliest agrarian uprising",
                    "Patharughat (1894) saw over 140 martyrs against exorbitant land taxes",
                    "Kushal Konwar was hanged at Jorhat Jail on June 15, 1943",
                    "Frequently asked in SLRC, Assam Police SI & Constable exams"
                ),
                keyPointsAs = listOf(
                    "ফুলগুৰি ধেৱা (১৮৬১) অসমৰ প্ৰথম কৃষক বিদ্ৰোহ",
                    "পথৰুঘাটৰ ৰণত ১৪০ জনতকৈ অধিক নিৰীহ কৃষক শ্বহীদ হৈছিল",
                    "১৯৪৩ চনৰ ১৫ জুনত যোৰহাট কাৰাগাৰত কুশল কোঁৱৰক ফাঁচী দিয়া হয়",
                    "এছএলআৰচি আৰু অসম আৰক্ষীৰ পৰীক্ষাত সঘনাই সোধা হয়"
                ),
                pyqReferences = listOf("SLRC SI 2022 Q29", "APSC Prelims 2020 Q11", "Constable 2023 Q6"),
                readMinutes = 7,
                isCompleted = false
            )
        )
    }

    private fun createInitialMockTests(): List<MockTest> {
        return listOf(
            MockTest(
                id = "apsc_prelims_mock_1",
                titleEn = "APSC CCE GS Paper-1 Full Mock Test",
                titleAs = "এপিএছচি চিচিই প্ৰিলিমছ জিএছ কাকত-১ সম্পূৰ্ণ মক টেষ্ট",
                examCategory = ExamCategory.APSC,
                stage = ExamStage.PRELIMS,
                durationMinutes = 120,
                totalMarks = 200,
                negativeMarks = 0.5f,
                questions = listOf(
                    QuizQuestion(
                        id = "q1",
                        questionEn = "Who among the following Ahom kings created the offices of Borbarua and Borphukan?",
                        questionAs = "তলৰ কোনজন আহোম ৰজাই বৰবৰুৱা আৰু বৰফুকন পদবীৰ সৃষ্টি কৰিছিল?",
                        optionsEn = listOf("Suhungmung", "Pratap Singha", "Rudra Singha", "Gadadhar Singha"),
                        optionsAs = listOf("চুহুংমুং", "প্ৰতাপ সিংহ", "ৰুদ্ৰ সিংহ", "গদাধৰ সিংহ"),
                        correctOptionIndex = 1,
                        explanationEn = "Swargadeo Pratap Singha (Susenghphaa) established the offices of Borbarua (chief civil & judicial officer) and Borphukan (governor and military commander of Lower Assam).",
                        explanationAs = "স্বৰ্গদেউ প্ৰতাপ সিংহই বৰবৰুৱা আৰু বৰফুকন পদ দুটা সৃষ্টি কৰি মোমাই তামুলী আৰু লাঙি পানীপ্ৰিয়াক নিযুক্তি দিছিল।",
                        topicTag = "Assam History",
                        pyqYear = "APSC Prelims 2022"
                    ),
                    QuizQuestion(
                        id = "q2",
                        questionEn = "Which of the following is the only Ramsar Wetland site in Assam?",
                        questionAs = "তলৰ কোনটো অসমৰ একমাত্ৰ ৰামচাৰ আৰ্দ্ৰভূমি ক্ষেত্ৰ?",
                        optionsEn = listOf("Sareswar Beel", "Deepor Beel", "Son Beel", "Urpad Beel"),
                        optionsAs = listOf("সৰেশ্বৰ বিল", "দীপৰ বিল", "সোণ বিল", "উৰপদ বিল"),
                        correctOptionIndex = 1,
                        explanationEn = "Deepor Beel located on the southwestern edge of Guwahati city was designated as a Ramsar site in November 2002.",
                        explanationAs = "২০০২ চনৰ নৱেম্বৰ মাহত গুৱাহাটীৰ কাষৰ দীপৰ বিলক ৰামচাৰ স্থলীৰূপে স্বীকৃতি দিয়া হয়।",
                        topicTag = "Assam Geography & Ecology",
                        pyqYear = "APSC Prelims 2021"
                    ),
                    QuizQuestion(
                        id = "q3",
                        questionEn = "Under which Article of the Indian Constitution can the Supreme Court issue writs for enforcement of Fundamental Rights?",
                        questionAs = "ভাৰতীয় সংবিধানৰ কোনটো অনুচ্ছেদৰ অধীনত উচ্চতম ন্যায়ালয়ে মৌলিক অধিকাৰ বলবৎকৰণৰ বাবে ৰিট জাৰি কৰিব পাৰে?",
                        optionsEn = listOf("Article 226", "Article 32", "Article 131", "Article 143"),
                        optionsAs = listOf("২২৬ নং অনুচ্ছেদ", "৩২ নং অনুচ্ছেদ", "১৩১ নং অনুচ্ছেদ", "১৪৩ নং অনুচ্ছেদ"),
                        correctOptionIndex = 1,
                        explanationEn = "Article 32 guarantees the Right to Constitutional Remedies, conferring power upon the Supreme Court to issue Habeas Corpus, Mandamus, Prohibition, Quo-Warranto, and Certiorari.",
                        explanationAs = "৩২ নং অনুচ্ছেদে সাংবিধানিক প্ৰতিকাৰৰ অধিকাৰ প্ৰদান কৰে যাৰ জৰিয়তে উচ্চতম ন্যায়ালয়ে ৰিট জাৰি কৰিব পাৰে।",
                        topicTag = "Indian Polity",
                        pyqYear = "UPSC 2020 / APSC 2023"
                    ),
                    QuizQuestion(
                        id = "q4",
                        questionEn = "In which year did the historic Patharughat peasant rebellion take place?",
                        questionAs = "ঐতিহাসিক পথৰুঘাটৰ কৃষক বিদ্ৰোহ কোন চনত সংঘটিত হৈছিল?",
                        optionsEn = listOf("1861", "1894", "1857", "1942"),
                        optionsAs = listOf("১৮৬১", "১৮৯৪", "১৮৫৭", "১৯৪২"),
                        correctOptionIndex = 1,
                        explanationEn = "On 28 January 1894, at Patharughat in Darrang district, the British army fired at unarmed peasants peacefully protesting against astronomical tax enhancements, killing approximately 140 peasants.",
                        explanationAs = "১৮৯৪ চনৰ ২৮ জানুৱাৰীত দৰঙৰ পথৰুঘাটত ব্ৰিটিছ চৰকাৰৰ খাজনা বৃদ্ধিৰ বিৰুদ্ধে হোৱা শান্তিপূৰ্ণ প্ৰতিবাদত গুলীচালনা কৰা হৈছিল।",
                        topicTag = "Freedom Struggle in Assam",
                        pyqYear = "APSC CCE 2020"
                    ),
                    QuizQuestion(
                        id = "q5",
                        questionEn = "The sixth National Park of Assam, 'Raimona National Park', is situated in which district?",
                        questionAs = "অসমৰ ৬ষ্ঠ ৰাষ্ট্ৰীয় উদ্যান 'ৰাইমনা ৰাষ্ট্ৰীয় উদ্যান' কোন জিলাত অৱস্থিত?",
                        optionsEn = listOf("Kokrajhar", "Baksa", "Chirang", "Tinsukia"),
                        optionsAs = listOf("কোকৰাঝাৰ", "বাক্সা", "চিৰাং", "তিনিচুকীয়া"),
                        correctOptionIndex = 0,
                        explanationEn = "Raimona National Park is situated in Gossaigaon sub-division of Kokrajhar district within the Bodoland Territorial Region (BTR).",
                        explanationAs = "ৰাইমনা ৰাষ্ট্ৰীয় উদ্যান কোকৰাঝাৰ জিলাৰ গোসাইগাঁও মহকুমাত অৱস্থিত।",
                        topicTag = "Assam Environment",
                        pyqYear = "APSC Prelims 2023"
                    )
                ),
                isDailyPractice = false,
                isPremium = false
            ),
            MockTest(
                id = "dpp_assam_gk_daily",
                titleEn = "Daily Practice Problem (DPP) - Day 16",
                titleAs = "দৈনিক অনুশীলন প্ৰশ্ন (DPP) - দিন ১৬",
                examCategory = ExamCategory.ADRE,
                stage = ExamStage.GENERAL,
                durationMinutes = 15,
                totalMarks = 20,
                negativeMarks = 0.25f,
                questions = listOf(
                    QuizQuestion(
                        id = "dpp1",
                        questionEn = "What was the capital of the Chutiya kingdom?",
                        questionAs = "চুতীয়া ৰাজ্যৰ ৰাজধানী ক'ত আছিল?",
                        optionsEn = listOf("Sadiya", "Charaideo", "Garhgaon", "Dimapur"),
                        optionsAs = listOf("শদিয়া", "চৰাইদেউ", "গড়গাঁও", "ডিমাপুৰ"),
                        correctOptionIndex = 0,
                        explanationEn = "The Chutiya kings ruled from Sadiya (Swarnagiri) in upper northeastern Assam before being absorbed by the Ahoms.",
                        explanationAs = "চুতীয়া ৰজাসকলৰ ৰাজধানী আছিল শদিয়া।",
                        topicTag = "Assam History",
                        pyqYear = "ADRE 2022"
                    ),
                    QuizQuestion(
                        id = "dpp2",
                        questionEn = "The river Manas joins the Brahmaputra near which town?",
                        questionAs = "মানাহ নদীয়ে কোন চহৰৰ ওচৰত ব্ৰহ্মপুত্ৰত মিলিছে?",
                        optionsEn = listOf("Jogighopa", "Goalpara", "Tezpur", "Dhubri"),
                        optionsAs = listOf("যোগীঘোপা", "গোৱালপাৰা", "তেজপুৰ", "ধুবুৰী"),
                        correctOptionIndex = 0,
                        explanationEn = "The transboundary Manas River enters the Brahmaputra at Jogighopa in Bongaigaon district.",
                        explanationAs = "মানাহ নদী যোগীঘোপাৰ ওচৰত ব্ৰহ্মপুত্ৰৰ লগত মিলিত হৈছে।",
                        topicTag = "Assam Geography",
                        pyqYear = "ADRE 2022"
                    ),
                    QuizQuestion(
                        id = "dpp3",
                        questionEn = "Who was the first woman martyr of the 1942 Quit India movement in Assam?",
                        questionAs = "১৯৪২ চনৰ ভাৰত ত্যাগ আন্দোলনত অসমৰ প্ৰথম মহিলা শ্বহীদ কোন আছিল?",
                        optionsEn = listOf("Kanaklata Barua", "Bhogeswari Phukanani", "Pushpalata Das", "Chandraprabha Saikiani"),
                        optionsAs = listOf("কনকলতা বৰুৱা", "ভোগেশ্বৰী ফুকননী", "পুষ্পলতা দাস", "চন্দ্ৰপ্ৰভা শইকীয়ানী"),
                        correctOptionIndex = 0,
                        explanationEn = "17-year-old Kanaklata Barua was martyred at Gohpur Police Station on 20 September 1942 while leading the Mrityu Bahini procession.",
                        explanationAs = "১৭ বছৰীয়া কনকলতা বৰুৱা গহপুৰ থানাত ত্ৰিৰংগা পতাকা উত্তোলন কৰিবলৈ গৈ শ্বহীদ হয়।",
                        topicTag = "Freedom Struggle",
                        pyqYear = "SLRC 2022"
                    )
                ),
                isDailyPractice = true,
                isPremium = false
            ),
            MockTest(
                id = "upsc_csat_mock_1",
                titleEn = "UPSC CSE Prelims CSAT Speed Practice",
                titleAs = "ইউপিএছচি চিভিল চাৰ্ভিছেছ চিছেট গতি অনুশীলন",
                examCategory = ExamCategory.UPSC,
                stage = ExamStage.PRELIMS,
                durationMinutes = 45,
                totalMarks = 50,
                negativeMarks = 0.83f,
                questions = listOf(
                    QuizQuestion(
                        id = "csat1",
                        questionEn = "If a sum of money doubles itself in 8 years at simple interest, what is the annual rate of interest?",
                        questionAs = "যদি এটা নিৰ্দিষ্ট মূলধন সৰল সুতত ৮ বছৰত দুগুণ হয়, তেন্তে বাৰ্ষিক সুতৰ হাৰ কিমান?",
                        optionsEn = listOf("10%", "12.5%", "15%", "8%"),
                        optionsAs = listOf("১০%", "১২.৫%", "১৫%", "৮%"),
                        correctOptionIndex = 1,
                        explanationEn = "Simple Interest formula: SI = (P * R * T)/100. If sum doubles, SI = P. Thus P = (P * R * 8)/100 => R = 100/8 = 12.5%.",
                        explanationAs = "সৰল সুতৰ সূত্ৰ: সুত = (মূলধন * হাৰ * সময়)/১০০। গতিকে হাৰ = ১০০/৮ = ১২.৫%।",
                        topicTag = "CSAT Quantitative Aptitude",
                        pyqYear = "UPSC CSAT 2021"
                    ),
                    QuizQuestion(
                        id = "csat2",
                        questionEn = "Complete the sequence: 3, 7, 15, 31, 63, ?",
                        questionAs = "শ্ৰেণীটো সম্পূৰ্ণ কৰক: ৩, ৭, ১৫, ৩১, ৬৩, ?",
                        optionsEn = listOf("125", "127", "129", "131"),
                        optionsAs = listOf("১২৫", "১২৭", "১২৯", "১৩১"),
                        correctOptionIndex = 1,
                        explanationEn = "Pattern is: (2 * previous) + 1. 3*2+1=7, 7*2+1=15, 15*2+1=31, 31*2+1=63, 63*2+1 = 127.",
                        explanationAs = "নিয়ম: (২ * পূৰ্ববৰ্তী সংখ্যা) + ১। ৬৩ * ২ + ১ = ১২৭।",
                        topicTag = "CSAT Logical Reasoning",
                        pyqYear = "CSAT 2022"
                    )
                ),
                isDailyPractice = false,
                isPremium = true
            )
        )
    }

    private fun createInitialPyqAnalyses(): List<PyqYearAnalysis> {
        return listOf(
            PyqYearAnalysis(
                id = "pyq_apsc_2023",
                examCategory = ExamCategory.APSC,
                year = "2023 CCE",
                cutoffGeneral = "74.50 Marks",
                cutoffOBC = "68.25 Marks",
                keyTrendEn = "Heavy weightage on Assam Art, Culture & Tribal Administration (32% of total questions). Analytical assertion-reason format aligned strictly with UPSC pattern.",
                keyTrendAs = "অসমৰ কলা-সংস্কৃতি আৰু জনজাতীয় প্ৰশাসনৰ ওপৰত সৰ্বাধিক গুৰুত্ব (মুঠ প্ৰশ্নৰ ৩২%)। ইউপিএছচিৰ আৰ্হিত বিশ্লেষণাত্মক প্ৰশ্নৰ প্ৰাচুৰ্য।",
                subjectWeightage = mapOf(
                    "Assam Art, Culture & History" to 32,
                    "Indian Polity & Governance" to 22,
                    "Assam & Indian Geography" to 18,
                    "Current Affairs & Assam Schemes" to 16,
                    "Environment & Science" to 12
                ),
                topQuestions = emptyList()
            ),
            PyqYearAnalysis(
                id = "pyq_apsc_2022",
                examCategory = ExamCategory.APSC,
                year = "2022 CCE",
                cutoffGeneral = "91.50 Marks",
                cutoffOBC = "84.00 Marks",
                keyTrendEn = "First full implementation of UPSC syllabus model. Assam Paper 5 introduced in Mains with 250 marks.",
                keyTrendAs = "ইউপিএছচি পাঠ্যক্ৰমৰ প্ৰথম পূৰ্ণ পৰীক্ষা। মেইনছত ২৫০ নম্বৰৰ সুকীয়া অসম কাকত (Paper 5) প্ৰৱৰ্তন।",
                subjectWeightage = mapOf(
                    "Assam History & Ahom Era" to 28,
                    "Constitution & Polity" to 24,
                    "Indian Economy & Assam Budget" to 18,
                    "Geography & Drainage" to 16,
                    "General Science" to 14
                ),
                topQuestions = emptyList()
            ),
            PyqYearAnalysis(
                id = "pyq_adre_2022",
                examCategory = ExamCategory.ADRE,
                year = "2022 Grade III",
                cutoffGeneral = "124/150",
                cutoffOBC = "118/150",
                keyTrendEn = "Extremely competitive. Direct historical dates, national parks, wildlife sanctuaries, and basic arithmetic shortcuts determined selection.",
                keyTrendAs = "অতি তীব্ৰ প্ৰতিযোগিতা। ইতিহাসৰ চন-তাৰিখ, ৰাষ্ট্ৰীয় উদ্যান আৰু পাটীগণিতৰ চমু নিয়মে উত্তীৰ্ণ নিৰ্ধাৰণ কৰে।",
                subjectWeightage = mapOf(
                    "Assam General Knowledge" to 35,
                    "General English" to 25,
                    "Elementary Mathematics" to 25,
                    "Mental Ability & Reasoning" to 15
                ),
                topQuestions = emptyList()
            )
        )
    }

    private fun createInitialForumPosts(): List<ForumPost> {
        return listOf(
            ForumPost(
                id = "post_1",
                title = "Strategy for APSC Mains Paper 5 (Assam Special Paper) - Sources & Booklist",
                content = "For Paper 5, what is the best strategy for Assam Polity and Social Reforms? I have read Edward Gait and Priyam Goswami. Any recommended notes for Sixth Schedule administration in Karbi Anglong & Dima Hasao?",
                authorName = "Anamika Baruah",
                examTag = ExamCategory.APSC,
                timeAgo = "2 hours ago",
                upvotes = 34,
                hasUpvoted = false,
                comments = listOf(
                    ForumComment(
                        id = "c1",
                        authorName = "Dr. Rajen Sharma (Director)",
                        authorRole = "Faculty Mentor",
                        content = "Focus closely on the legislative powers of KAAC vs State Assembly under paragraph 3 of the Sixth Schedule. Our Academy topic notes under GS-5 cover all recent amendments. Best wishes!",
                        timestamp = "1 hour ago",
                        likes = 18
                    ),
                    ForumComment(
                        id = "c2",
                        authorName = "Bikash Kalita",
                        authorRole = "Aspirant",
                        content = "The bilingual notes provided in this app's Study Materials section for Ahom Administration are very crisp and directly answerable for 15-markers.",
                        timestamp = "45 mins ago",
                        likes = 7
                    )
                )
            ),
            ForumPost(
                id = "post_2",
                title = "ADRE Grade-III Math Shortcuts for Percentage & Compound Interest",
                content = "Does ADRE focus heavily on Advanced Math or mostly Commercial Arithmetic? How much time should we allocate during the 2-hour exam?",
                authorName = "Hirakjyoti Das",
                examTag = ExamCategory.ADRE,
                timeAgo = "5 hours ago",
                upvotes = 21,
                hasUpvoted = false,
                comments = listOf(
                    ForumComment(
                        id = "c3",
                        authorName = "Javed Ahmed",
                        authorRole = "APSC Ranker",
                        content = "ADRE is 95% commercial arithmetic (Ratio, Profit/Loss, Time & Work, Speed). Keep 35 minutes for Maths section.",
                        timestamp = "3 hours ago",
                        likes = 9
                    )
                )
            ),
            ForumPost(
                id = "post_3",
                title = "APSC Prelims GS-I Assamese medium aspirants - Current Affairs sources?",
                content = "অসমীয়া মাধ্যমত প্ৰস্তুতি চলোৱা সকলে চলিত পৰিক্ৰমাৰ বাবে কোনবোৰ আলোচনী পঢ়িছে? ইয়াত উপলব্ধ অসমীয়া সামগ্ৰীখিনি খুবেই সহায়ক হৈছে।",
                authorName = "ৰূপজ্যোতি বৰা",
                examTag = ExamCategory.APSC,
                timeAgo = "1 day ago",
                upvotes = 29,
                hasUpvoted = false,
                comments = emptyList()
            )
        )
    }

    private fun createInitialDoubtSessions(): List<LiveDoubtSession> {
        return listOf(
            LiveDoubtSession(
                id = "doubt_1",
                titleEn = "Live Doubts: APSC CCE Prelims GS-I Strategy & High Yield Topics",
                titleAs = "লাইভ আলোচনা: এপিএছচি চিচিই প্ৰিলিমছ জিএছ-১ কৌশল আৰু সৰ্বাধিক গুৰুত্বপূৰ্ণ বিষয়",
                mentorName = "Dr. Rajen Sharma",
                mentorTitle = "Founder & Chief Mentor, Ex-State Academic Adviser",
                examCategory = ExamCategory.APSC,
                scheduledTime = "Today, 7:30 PM IST",
                duration = "60 Mins",
                isLiveNow = true,
                agenda = listOf(
                    "Breakdown of 2023 APSC cutoffs and negative marking traps",
                    "Assam Art & Architecture question patterns",
                    "Direct live Q&A with enrolled aspirants"
                ),
                meetingLink = "https://meet.google.com/brainstormq-live-apsc"
            ),
            LiveDoubtSession(
                id = "doubt_2",
                titleEn = "Mains Answer Writing Masterclass: Ethics Case Studies & Assam Paper 5",
                titleAs = "মেইনছ উত্তৰ লিখন কৰ্মশালা: নীতিশাস্ত্ৰ আৰু অসম পেপাৰ ৫",
                mentorName = "Dr. Rajen Sharma & Faculty Team",
                mentorTitle = "BrainstormQ Senior Faculty",
                examCategory = ExamCategory.APSC,
                scheduledTime = "Tomorrow, 6:00 PM IST",
                duration = "90 Mins",
                isLiveNow = false,
                agenda = listOf(
                    "Structuring 10-mark vs 15-mark answers within space constraints",
                    "Incorporating maps and flowcharts for Assam Geography & History",
                    "Review of student sample answers"
                ),
                meetingLink = "https://meet.google.com/brainstormq-mains-ethics"
            ),
            LiveDoubtSession(
                id = "doubt_3",
                titleEn = "ADRE & SLRC Math Shortcuts & Reasoning Drill",
                titleAs = "এডিআৰই আৰু এছএলআৰচি গণিতৰ চমু নিয়ম আৰু যুক্তিবিদ্যা",
                mentorName = "Prof. Manoj Sarma",
                mentorTitle = "Aptitude Specialist",
                examCategory = ExamCategory.ADRE,
                scheduledTime = "Thursday, 5:00 PM IST",
                duration = "60 Mins",
                isLiveNow = false,
                agenda = listOf(
                    "Time & Work shortcuts without formula memorization",
                    "Syllogism and Blood Relations tricks",
                    "Live speed test walkthrough"
                ),
                meetingLink = "https://meet.google.com/brainstormq-adre-math"
            )
        )
    }

    fun recordDailyQuiz(result: MockTestResult, incrementStreak: Boolean) {
        _recentResults.update { listOf(result) + it }
        val newStreak = if (incrementStreak) _userProfile.value.dailyStreak + 1 else _userProfile.value.dailyStreak
        val newSolved = _userProfile.value.totalQuestionsSolved + result.correctCount + result.incorrectCount
        val newAvg = ((_userProfile.value.averageScorePercent + (result.accuracy * 100).toInt()) / 2).coerceIn(0, 100)

        prefs.edit()
            .putInt("daily_streak", newStreak)
            .putInt("total_solved", newSolved)
            .putInt("avg_score", newAvg)
            .apply()

        _userProfile.update {
            it.copy(
                dailyStreak = newStreak,
                totalQuestionsSolved = newSolved,
                averageScorePercent = newAvg
            )
        }
    }

    fun getDailyQuizQuestions(examCategory: ExamCategory): List<QuizQuestion> {
        val pool = when (examCategory) {
            ExamCategory.APSC -> listOf(
                QuizQuestion(
                    id = "apsc_dq_1",
                    questionEn = "Under whose reign was the first census of the Paik system conducted in the Ahom kingdom?",
                    questionAs = "কাৰ ৰাজত্বকালত আহোম ৰাজ্যত পাইক প্ৰথাৰ প্ৰথম পিয়ল কৰা হৈছিল?",
                    optionsEn = listOf("Suhungmung", "Pratap Singha", "Rajeswar Singha", "Chakradhwaj Singha"),
                    optionsAs = listOf("চুহুংমুং", "প্ৰতাপ সিংহ", "ৰাজেশ্বৰ সিংহ", "চক্ৰধ্বজ সিংহ"),
                    correctOptionIndex = 1,
                    explanationEn = "Swargadeo Pratap Singha appointed Momai Tamuli Borbarua who conducted a thorough census and village reorganization of the Paik system.",
                    explanationAs = "স্বৰ্গদেউ প্ৰতাপ সিংহৰ নিৰ্দেশত মোমাই তামুলী বৰবৰুৱাই পাইক পিয়ল আৰু গাওঁ সংস্কাৰ সম্পন্ন কৰে।",
                    topicTag = "Assam History & Administration",
                    pyqYear = "APSC CCE 2022"
                ),
                QuizQuestion(
                    id = "apsc_dq_2",
                    questionEn = "Which tributary of the Brahmaputra is known as the 'River of Gold' due to ancient gold washing practices?",
                    questionAs = "প্ৰাচীন কালত সোণ ধোৱা কাৰ্যৰ বাবে ব্ৰহ্মপুত্ৰৰ কোনখন উপনৈক 'সোণৰ নদী' বুলি জনা যায়?",
                    optionsEn = listOf("Subansiri", "Manas", "Jia Bharali", "Dhansiri"),
                    optionsAs = listOf("সোৱণশিৰি", "মানাহ", "জিয়া ভৰলী", "ধনশিৰি"),
                    correctOptionIndex = 0,
                    explanationEn = "The name 'Subansiri' is derived from Sanskrit 'Suvarna-shri' meaning 'ornament of gold', famous for Sonowal gold washers.",
                    explanationAs = "'সোৱণশিৰি' শব্দটো সংস্কৃত 'সুৱৰ্ণ-শ্ৰী'ৰ পৰা আহিছে, য'ত সোণোৱালসকলে নদীৰ বালিৰ পৰা সোণ আহৰণ কৰিছিল।",
                    topicTag = "Assam Geography & Drainage",
                    pyqYear = "APSC Prelims 2021"
                ),
                QuizQuestion(
                    id = "apsc_dq_3",
                    questionEn = "The Bodoland Territorial Council (BTC) was created under which schedule of the Indian Constitution?",
                    questionAs = "বড়োলেণ্ড টেৰিটৰিয়েল কাউন্সিল (BTC) ভাৰতীয় সংবিধানৰ কোনখন অনুসূচীৰ অধীনত গঠন কৰা হৈছিল?",
                    optionsEn = listOf("Fifth Schedule", "Sixth Schedule", "Seventh Schedule", "Eighth Schedule"),
                    optionsAs = listOf("পঞ্চম অনুসূচী", "ষষ্ঠ অনুসূচী", "সপ্তম অনুসূচী", "অষ্টম অনুসূচী"),
                    correctOptionIndex = 1,
                    explanationEn = "BTC was constituted under the Sixth Schedule of the Indian Constitution following the 2003 tripartite Bodo Accord.",
                    explanationAs = "২০০৩ চনৰ বড়ো চুক্তিৰ পিছত ভাৰতীয় সংবিধানৰ ষষ্ঠ অনুসূচীৰ অধীনত বিটিচি গঠন কৰা হয়।",
                    topicTag = "Indian Polity & Assam Governance",
                    pyqYear = "APSC GS-II 2023"
                ),
                QuizQuestion(
                    id = "apsc_dq_4",
                    questionEn = "The historic battle of Saraighat was fought on the Brahmaputra in which year?",
                    questionAs = "ঐতিহাসিক শৰাইঘাটৰ যুদ্ধ ব্ৰহ্মপুত্ৰৰ বুকুত কোন চনত সংঘটিত হৈছিল?",
                    optionsEn = listOf("1662", "1667", "1671", "1682"),
                    optionsAs = listOf("১৬৬২", "১৬৬৭", "১৬৭১", "১৬৮২"),
                    correctOptionIndex = 2,
                    explanationEn = "The Battle of Saraighat was fought in March 1671 between the Ahom forces commanded by Lachit Borphukan and the Mughal army led by Raja Ram Singh I.",
                    explanationAs = "১৬৭১ চনৰ মাৰ্চ মাহত লাচিত বৰফুকন আৰু মোগল সেনাপতি ৰাম সিংহৰ মাজত শৰাইঘাটৰ যুদ্ধ হয়।",
                    topicTag = "Assam Military History",
                    pyqYear = "APSC Prelims 2020"
                ),
                QuizQuestion(
                    id = "apsc_dq_5",
                    questionEn = "Which Sattra was established by Mahapurush Srimanta Sankardeva at Majuli?",
                    questionAs = "মহাপুৰুষ শ্ৰীমন্ত শংকৰদেৱে মাজুলীত কোনখন সত্ৰ প্ৰতিষ্ঠা কৰিছিল?",
                    optionsEn = listOf("Dhuwahat Belaguri Sattra", "Auniati Sattra", "Kamalabari Sattra", "Garamur Sattra"),
                    optionsAs = listOf("ধুৱাহাট বেলগুৰি সত্ৰ", "আউনীআটী সত্ৰ", "কমলাবাৰী সত্ৰ", "গড়মূৰ সত্ৰ"),
                    correctOptionIndex = 0,
                    explanationEn = "Mahapurush Sankardeva established Dhuwahat Belaguri Sattra in Majuli where the historic meeting (Moni-Kanchan Sanjog) with Sri Madhavdeva took place.",
                    explanationAs = "শংকৰদেৱে মাজুলীৰ ধুৱাহাট বেলগুৰিত সত্ৰ স্থাপন কৰে, য'ত মাধৱদেৱৰ সৈতে ঐতিহাসিক মণিকাঞ্চন সংযোগ ঘটিছিল।",
                    topicTag = "Assam Art, Culture & Heritage",
                    pyqYear = "APSC 2022"
                ),
                QuizQuestion(
                    id = "apsc_dq_6",
                    questionEn = "What is the primary export from Assam's Digboi refinery, Asia's oldest operational oil refinery?",
                    questionAs = "এছিয়াৰ আটাইতকৈ পুৰণি ডিগবৈ তেল শোধনাগাৰ ১৮৮৯ চনত অসমৰ কোন জিলাত আৰম্ভ হৈছিল?",
                    optionsEn = listOf("Tinsukia", "Dibrugarh", "Sivasagar", "Jorhat"),
                    optionsAs = listOf("তিনিচুকীয়া", "ডিব্ৰুগড়", "শিৱসাগৰ", "যোৰহাট"),
                    correctOptionIndex = 0,
                    explanationEn = "Digboi, Asia's oldest oil refinery established in 1899, is located in the Tinsukia district of Assam.",
                    explanationAs = "১৮৯৯ চনত স্থাপিত এছিয়াৰ প্ৰথমটো তেল শোধনাগাৰ ডিগবৈ অসমৰ তিনিচুকীয়া জিলাত অৱস্থিত।",
                    topicTag = "Assam Economy & Natural Resources",
                    pyqYear = "APSC GS-V"
                ),
                QuizQuestion(
                    id = "apsc_dq_7",
                    questionEn = "Under Article 226 of the Constitution, which court in Assam has jurisdiction to issue writs?",
                    questionAs = "ভাৰতীয় সংবিধানৰ ২২৬ নং অনুচ্ছেদৰ অধীনত অসমত ৰিট জাৰি কৰাৰ অধিকাৰ কোনটো ন্যায়ালয়ৰ আছে?",
                    optionsEn = listOf("Gauhati High Court", "Supreme Court only", "Assam Administrative Tribunal", "Kamrup Sessions Court"),
                    optionsAs = listOf("গুৱাহাটী উচ্চ ন্যায়ালয়", "কেৱল উচ্চতম ন্যায়ালয়", "অসম প্ৰশাসনিক ন্যায়াধিকৰণ", "কামৰূপ সত্ৰ ন্যায়ালয়"),
                    correctOptionIndex = 0,
                    explanationEn = "Article 226 empowers High Courts (Gauhati High Court for Assam, Nagaland, Mizoram, and Arunachal Pradesh) to issue prerogative writs.",
                    explanationAs = "২২৬ নং অনুচ্ছেদে উচ্চ ন্যায়ালয়ক মৌলিক অধিকাৰ আৰু অন্যান্য বিষয়ত ৰিট জাৰি কৰাৰ ক্ষমতা দিয়ে।",
                    topicTag = "Indian Polity & Judiciary",
                    pyqYear = "APSC CCE 2023"
                )
            )
            ExamCategory.UPSC -> listOf(
                QuizQuestion(
                    id = "upsc_dq_1",
                    questionEn = "Which Constitutional Amendment Act added the terms 'Socialist', 'Secular', and 'Integrity' to the Preamble?",
                    questionAs = "কোনটো সংবিধান সংশোধনীৰ দ্বাৰা প্ৰস্তাৱনাত 'সমাজবাদী', 'ধৰ্মনিৰপেক্ষ' আৰু 'সংহতি' শব্দকেইটা সংযোজন কৰা হৈছিল?",
                    optionsEn = listOf("42nd Amendment (1976)", "44th Amendment (1978)", "52nd Amendment (1985)", "86th Amendment (2002)"),
                    optionsAs = listOf("৪২তম সংশোধনী (১৯৭৬)", "৪৪তম সংশোধনী (১৯৭৮)", "৫২তম সংশোধনী (১৯৮৫)", "৮৬তম সংশোধনী (২০০২)"),
                    correctOptionIndex = 0,
                    explanationEn = "The 42nd Constitutional Amendment Act of 1976, enacted during Emergency, added Socialist, Secular, and Integrity to the Preamble.",
                    explanationAs = "১৯৭৬ চনৰ ৪২তম সংবিধান সংশোধনীৰ জৰিয়তে প্ৰস্তাৱনাত এই তিনিটা শব্দ সন্নিবিষ্ট কৰা হয়।",
                    topicTag = "Indian Polity & Constitution",
                    pyqYear = "UPSC CSE 2021"
                ),
                QuizQuestion(
                    id = "upsc_dq_2",
                    questionEn = "The term 'Basic Structure of the Constitution' was propounded by the Supreme Court in which landmark case?",
                    questionAs = "উচ্চতম ন্যায়ালয়ে কোনটো ঐতিহাসিক গোচৰত সংবিধানৰ 'মূল গাঁথনি তত্ত্ব' ঘোষণা কৰিছিল?",
                    optionsEn = listOf("Kesavananda Bharati case (1973)", "Golaknath case (1967)", "Minerva Mills case (1980)", "Maneka Gandhi case (1978)"),
                    optionsAs = listOf("কেশৱানন্দ ভাৰতী গোচৰ (১৯৭৩)", "গোলকনাথ গোচৰ (১৯৬৭)", "মিনেৰ্ভা মিলছ গোচৰ (১৯৮০)", "মানেকা গান্ধী গোচৰ (১৯৭৮)"),
                    correctOptionIndex = 0,
                    explanationEn = "In Kesavananda Bharati v. State of Kerala (1973), a 13-judge bench ruled that Parliament cannot alter the Basic Structure under Article 368.",
                    explanationAs = "১৯৭৩ চনৰ কেশৱানন্দ ভাৰতী বনাম কেৰালা চৰকাৰ গোচৰত ১৩ জনীয়া বিচাৰপীঠে মূল গাঁথনি তত্ত্ব প্ৰৱৰ্তন কৰে।",
                    topicTag = "Indian Polity & Jurisprudence",
                    pyqYear = "UPSC CSE 2020"
                ),
                QuizQuestion(
                    id = "upsc_dq_3",
                    questionEn = "Which of the following Ramsar wetland sites is located in Northeast India and known for floating 'phumdis'?",
                    questionAs = "উত্তৰ-পূব ভাৰতৰ কোনখন ৰামচাৰ জলাশয় ওপঙা 'ফুমদি'ৰ বাবে বিখ্যাত?",
                    optionsEn = listOf("Loktak Lake (Manipur)", "Deepor Beel (Assam)", "Rudrasagar (Tripura)", "Palak Dil (Mizoram)"),
                    optionsAs = listOf("লোকটক হ্ৰদ (মণিপুৰ)", "দীপৰ বিল (অসম)", "ৰুদ্ৰসাগৰ (ত্ৰিপুৰা)", "পালাক দিল (মিজোৰাম)"),
                    correctOptionIndex = 0,
                    explanationEn = "Loktak Lake in Manipur is famous for floating vegetation called Phumdis and the endangered Sangai brow-antlered deer in Keibul Lamjao National Park.",
                    explanationAs = "মণিপুৰৰ লোকটক হ্ৰদ ফুমদি আৰু বিশ্বৰ একমাত্ৰ ওপঙা ৰাষ্ট্ৰীয় উদ্যান কেইবুল লামজাওৰ বাবে বিখ্যাত।",
                    topicTag = "Environment & Biodiversity",
                    pyqYear = "UPSC CSE 2022"
                ),
                QuizQuestion(
                    id = "upsc_dq_4",
                    questionEn = "A train 150 meters long takes 15 seconds to cross a telegraph post. What is the speed of the train in km/h?",
                    questionAs = "১৫০ মিটাৰ দীঘল ৰে'ল এখনৰ এটা পোষ্ট পাৰ হ'বলৈ ১৫ ছেকেণ্ড সময় লাগে। ৰে'লখনৰ প্ৰতি ঘণ্টাত গতিবেগ কিমান?",
                    optionsEn = listOf("36 km/h", "40 km/h", "54 km/h", "30 km/h"),
                    optionsAs = listOf("৩৬ কিঃমিঃ/ঘণ্টা", "৪০ কিঃমিঃ/ঘণ্টা", "৫৪ কিঃমিঃ/ঘণ্টা", "৩০ কিঃমিঃ/ঘণ্টা"),
                    correctOptionIndex = 0,
                    explanationEn = "Speed = Distance / Time = 150m / 15s = 10 m/s. In km/h: 10 * (18/5) = 36 km/h.",
                    explanationAs = "গতিবেগ = দূৰত্ব / সময় = ১৫০ / ১৫ = ১০ মিটাৰ/ছেকেণ্ড। কিঃমিঃ/ঘণ্টাত: ১০ * (১৮/৫) = ৩৬ কিঃমিঃ/ঘণ্টা।",
                    topicTag = "CSAT Quantitative Aptitude",
                    pyqYear = "UPSC CSAT 2023"
                ),
                QuizQuestion(
                    id = "upsc_dq_5",
                    questionEn = "Which sector contributes the highest share to India's Gross Value Added (GVA)?",
                    questionAs = "ভাৰতৰ মুঠ মূল্য সংযোজনত (GVA) সৰ্বাধিক অংশ কোনটো খণ্ডই আগবঢ়ায়?",
                    optionsEn = listOf("Services Sector", "Agriculture & Allied", "Manufacturing Industry", "Mining & Quarrying"),
                    optionsAs = listOf("সেৱা খণ্ড (Services)", "কৃষি খণ্ড", "উৎপাদন উদ্যোগ", "খনন খণ্ড"),
                    correctOptionIndex = 0,
                    explanationEn = "The services sector accounts for over 53% of India's GVA, followed by industry and agriculture.",
                    explanationAs = "ভাৰতীয় অৰ্থনীতিত সেৱা খণ্ডই ৫৩% তকৈ অধিক অৱদান আগবঢ়ায়।",
                    topicTag = "Indian Economy & National Accounts",
                    pyqYear = "UPSC CSE 2021"
                ),
                QuizQuestion(
                    id = "upsc_dq_6",
                    questionEn = "Who was the Viceroy of India when the Partition of Bengal was announced in 1905?",
                    questionAs = "১৯০৫ চনত বংগ ভংগ ঘোষণা কৰাৰ সময়ত ভাৰতৰ ভাইচৰয় কোন আছিল?",
                    optionsEn = listOf("Lord Curzon", "Lord Minto", "Lord Hardinge", "Lord Chelmsford"),
                    optionsAs = listOf("লৰ্ড কাৰ্জন", "লৰ্ড মিন্টো", "লৰ্ড হাৰ্ডিঞ্জ", "লৰ্ড চেমচফৰ্ড"),
                    correctOptionIndex = 0,
                    explanationEn = "Lord Curzon announced the partition of Bengal on 19 July 1905, triggering the Swadeshi Movement across India.",
                    explanationAs = "লৰ্ড কাৰ্জনে ১৯০৫ চনত বংগ বিভাজনৰ আদেশ জাৰি কৰে, যাৰ ফলত স্বদেশী আন্দোলনৰ সূচনা হয়।",
                    topicTag = "Modern Indian History",
                    pyqYear = "UPSC Prelims 2020"
                )
            )
            ExamCategory.ADRE -> listOf(
                QuizQuestion(
                    id = "adre_dq_1",
                    questionEn = "How many National Parks are there in Assam currently?",
                    questionAs = "বৰ্তমান অসমত মুঠ কেইখন ৰাষ্ট্ৰীয় উদ্যান আছে?",
                    optionsEn = listOf("5", "6", "7", "8"),
                    optionsAs = listOf("৫", "৬", "৭", "৮"),
                    correctOptionIndex = 2,
                    explanationEn = "Assam has 7 National Parks: Kaziranga, Manas, Nameri, Orang, Dibru-Saikhowa, Raimona, and Dihing Patkai.",
                    explanationAs = "অসমত মুঠ ৭খন ৰাষ্ট্ৰীয় উদ্যান আছে (কাজিৰঙা, মানাহ, নামেৰি, ওৰাং, ডিব্ৰু-ছৈখোৱা, ৰাইমনা আৰু দিহিং পাটকাই)।",
                    topicTag = "Assam GK & Wildlife",
                    pyqYear = "ADRE 2022"
                ),
                QuizQuestion(
                    id = "adre_dq_2",
                    questionEn = "What is the capital of Assam located at Dispur, Guwahati?",
                    questionAs = "শ্বিলঙৰ পৰা দিছপুৰলৈ অসমৰ ৰাজধানী কোন চনত স্থানান্তৰ কৰা হৈছিল?",
                    optionsEn = listOf("1971", "1972", "1973", "1975"),
                    optionsAs = listOf("১৯৭১", "১৯৭২", "১৯৭৩", "১৯৭৫"),
                    correctOptionIndex = 2,
                    explanationEn = "Following the carving out of Meghalaya from Assam, the capital was formally shifted to Dispur in 1973.",
                    explanationAs = "মেঘালয় পৃথক হোৱাৰ পিছত ১৯৭৩ চনত অসমৰ ৰাজধানী শ্বিলঙৰ পৰা দিছপুৰলৈ স্থানান্তৰ কৰা হয়।",
                    topicTag = "Assam Polity & History",
                    pyqYear = "ADRE Grade-III"
                ),
                QuizQuestion(
                    id = "adre_dq_3",
                    questionEn = "If a shopkeeper sells an article for ₹450 at a 10% loss, what was its cost price?",
                    questionAs = "যদি এজন দোকানীয়ে ১০% লোকচানত এটা বস্তু ৪৫০ টকাত বিক্ৰী কৰে, তেন্তে তাৰ ক্ৰয় মূল্য কিমান আছিল?",
                    optionsEn = listOf("₹480", "₹500", "₹520", "₹550"),
                    optionsAs = listOf("৪৮০ টকা", "৫০০ টকা", "৫২০ টকা", "৫৫০ টকা"),
                    correctOptionIndex = 1,
                    explanationEn = "Selling Price = 90% of Cost Price. CP = 450 / 0.90 = ₹500.",
                    explanationAs = "বিক্ৰয় মূল্য = ক্ৰয় মূল্যৰ ৯০%। ক্ৰয় মূল্য = ৪৫০ / ০.৯০ = ৫০০ টকা।",
                    topicTag = "Elementary Mathematics",
                    pyqYear = "ADRE Math"
                ),
                QuizQuestion(
                    id = "adre_dq_4",
                    questionEn = "Choose the correct synonym for the word 'CANDID':",
                    questionAs = "'CANDID' শব্দটোৰ সঠিক সমাৰ্থক শব্দ বাছক:",
                    optionsEn = listOf("Frank / Honest", "Secretive", "Cruel", "Timid"),
                    optionsAs = listOf("সৰল / অকপট (Frank)", "গোপনীয়", "নিষ্ঠুৰ", "ভয়াতুৰ"),
                    correctOptionIndex = 0,
                    explanationEn = "'Candid' means truthful, straightforward, and frank in speech or manner.",
                    explanationAs = "'Candid' মানে মুকলিমূৰীয়া, স্পষ্টবাদী বা সৰলভাৱে প্ৰকাশ কৰা।",
                    topicTag = "General English",
                    pyqYear = "ADRE Grade-III"
                ),
                QuizQuestion(
                    id = "adre_dq_5",
                    questionEn = "The famous Battle of Itakhuli took place in which year between Ahoms and Mughals?",
                    questionAs = "আহোম আৰু মোগলৰ মাজত বিখ্যাত ইটাখুলিৰ যুদ্ধ কোন চনত হৈছিল?",
                    optionsEn = listOf("1681", "1682", "1671", "1669"),
                    optionsAs = listOf("১৬৮১", "১৬৮২", "১৬৭১", "১৬৬৯"),
                    correctOptionIndex = 1,
                    explanationEn = "The Battle of Itakhuli in 1682 under King Gadadhar Singha permanently ended Mughal presence in Kamrup/Assam.",
                    explanationAs = "১৬৮২ চনত গদাধৰ সিংহৰ দিনত হোৱা ইটাখুলিৰ ৰণে অসমৰ পৰা মোগলক চিৰদিনৰ বাবে বিতাড়িত কৰে।",
                    topicTag = "Assam History",
                    pyqYear = "ADRE Grade-IV"
                ),
                QuizQuestion(
                    id = "adre_dq_6",
                    questionEn = "If DAY is coded as 4125, how will CAT be coded?",
                    questionAs = "যদি DAY ৰ ক'ড 4125 হয়, তেন্তে CAT ৰ ক'ড কি হ'ব?",
                    optionsEn = listOf("3120", "3119", "3220", "3121"),
                    optionsAs = listOf("3120", "3119", "3220", "3121"),
                    correctOptionIndex = 0,
                    explanationEn = "Alphabet letter positions: C=3, A=1, T=20. Hence CAT = 3120.",
                    explanationAs = "বৰ্ণমালাৰ স্থান: C=3, A=1, T=20। গতিকে CAT = 3120।",
                    topicTag = "Mental Ability & Reasoning",
                    pyqYear = "ADRE 2022"
                )
            )
            ExamCategory.SLRC -> listOf(
                QuizQuestion(
                    id = "slrc_dq_1",
                    questionEn = "Under Criminal Procedure, what is the maximum time a person arrested without a warrant can be detained without producing before a Magistrate?",
                    questionAs = "আৰক্ষীয়ে ৱাৰেণ্ট অবিহনে গ্ৰেপ্তাৰ কৰা এজন ব্যক্তিক দণ্ডাধীশৰ ওচৰত হাজিৰ নকৰাকৈ সৰ্বাধিক কিমান সময় থানাত ৰাখিব পাৰে?",
                    optionsEn = listOf("12 Hours", "24 Hours", "48 Hours", "72 Hours"),
                    optionsAs = listOf("১২ ঘণ্টা", "২৪ ঘণ্টা", "৪৮ ঘণ্টা", "৭২ ঘণ্টা"),
                    correctOptionIndex = 1,
                    explanationEn = "Under Article 22(2) of Constitution and CrPC Section 57, an arrested individual must be produced before the nearest magistrate within 24 hours.",
                    explanationAs = "সংবিধানৰ ২২(২) অনুচ্ছেদ আৰু ফৌজদাৰী প্ৰক্ৰিয়া বিধিৰ ৫৭ নং ধাৰা অনুসৰি ২৪ ঘণ্টাৰ ভিতৰত দণ্ডাধীশৰ আদালতত হাজিৰ কৰাব লাগিব।",
                    topicTag = "Police Law & Procedure",
                    pyqYear = "SLRC SI 2022"
                ),
                QuizQuestion(
                    id = "slrc_dq_2",
                    questionEn = "Who was hanged at Jorhat Jail on 15 June 1943 during the Quit India movement?",
                    questionAs = "ভাৰত ত্যাগ আন্দোলনৰ সময়ত ১৯৪৩ চনৰ ১৫ জুনত যোৰহাট কাৰাগাৰত কাক ফাঁচী দিয়া হৈছিল?",
                    optionsEn = listOf("Kushal Konwar", "Maniram Dewan", "Piyoli Phukan", "Bhogeswari Phukanani"),
                    optionsAs = listOf("কুশল কোঁৱৰ", "মণিৰাম দেৱান", "পিয়লি ফুকন", "ভোগেশ্বৰী ফুকননী"),
                    correctOptionIndex = 0,
                    explanationEn = "Kushal Konwar was the only martyr in all of India to be hanged during the 1942 Quit India Movement.",
                    explanationAs = "১৯৪২ চনৰ ভাৰত ত্যাগ আন্দোলনত সমগ্ৰ ভাৰততে ফাঁচী কাঠত ওলমা একমাত্ৰ শ্বহীদ আছিল কুশল কোঁৱৰ।",
                    topicTag = "Freedom Movement in Assam",
                    pyqYear = "Assam Police SI 2023"
                ),
                QuizQuestion(
                    id = "slrc_dq_3",
                    questionEn = "The river Brahmaputra enters Assam through which district?",
                    questionAs = "ব্ৰহ্মপুত্ৰ নদীয়ে অসমৰ কোনখন জিলাৰে প্ৰৱেশ কৰে?",
                    optionsEn = listOf("Tinsukia", "Dhemaji", "Dibrugarh", "Sivasagar"),
                    optionsAs = listOf("তিনিচুকীয়া", "ধেমাজী", "ডিব্ৰুগড়", "শিৱসাগৰ"),
                    correctOptionIndex = 0,
                    explanationEn = "The Siang river meets Dibang and Lohit near Kobo in Tinsukia / Sadiya border to formally form the Brahmaputra in Assam.",
                    explanationAs = "শদিয়াৰ সমীপত তিনিচুকীয়া জিলাৰ সীমাৰে ব্ৰহ্মপুত্ৰ নদীয়ে অসমত প্ৰৱেশ কৰে।",
                    topicTag = "Assam Geography",
                    pyqYear = "Constable 2022"
                ),
                QuizQuestion(
                    id = "slrc_dq_4",
                    questionEn = "In which year did the historic Phulaguri peasant uprising take place against British opium tax?",
                    questionAs = "কানি আৰু তামোলৰ ওপৰত ব্ৰিটিছ কৰৰ বিৰুদ্ধে ফুলগুৰি ধেৱা কোন চনত সংঘটিত হৈছিল?",
                    optionsEn = listOf("1857", "1861", "1894", "1921"),
                    optionsAs = listOf("১৮৫৭", "১৮৬১", "১৮৯৪", "১৯২১"),
                    correctOptionIndex = 1,
                    explanationEn = "Phulaguri Dhewa (October 1861) in Nagaon was the earliest agrarian peasant revolt against British colonial economic exploitation in Assam.",
                    explanationAs = "১৮৬১ চনৰ অক্টোবৰত নগাঁৱত ফুলগুৰি ধেৱা সংঘটিত হৈছিল, যি অসমৰ প্ৰথম কৃষক বিদ্ৰোহ।",
                    topicTag = "Assam History & Movements",
                    pyqYear = "Assam Forest Guard"
                ),
                QuizQuestion(
                    id = "slrc_dq_5",
                    questionEn = "Point P is 10 meters East of Q. Point R is 10 meters North of P. In which direction is R with respect to Q?",
                    questionAs = "P বিন্দু Q ৰ পূবে ১০ মিটাৰ দূৰত। R বিন্দু P ৰ উত্তৰে ১০ মিটাৰ দূৰত। Q ৰ সাপেক্ষে R কোন দিশত আছে?",
                    optionsEn = listOf("North-East", "North-West", "South-East", "South-West"),
                    optionsAs = listOf("উত্তৰ-পূব (North-East)", "উত্তৰ-পশ্চিম", "দক্ষিণ-পূব", "দক্ষিণ-পশ্চিম"),
                    correctOptionIndex = 0,
                    explanationEn = "From Q, moving East to P and then North to R puts point R in the North-East direction from Q.",
                    explanationAs = "Q ৰ পৰা পূবলৈ গৈ তাৰ পৰা উত্তৰলৈ গ'লে স্থানটো উত্তৰ-পূব দিশত অৱস্থিত হ'ব।",
                    topicTag = "Logical Reasoning & Direction Sense",
                    pyqYear = "SLRC Reasoning"
                ),
                QuizQuestion(
                    id = "slrc_dq_6",
                    questionEn = "Which gas is mainly responsible for the natural green plant photosynthesis?",
                    questionAs = "উদ্ভিদৰ সালোক সংশ্লেষণ প্ৰক্ৰিয়াৰ বাবে কোনটো গেছ প্ৰধানকৈ প্ৰয়োজন?",
                    optionsEn = listOf("Carbon Dioxide (CO2)", "Oxygen (O2)", "Nitrogen (N2)", "Methane (CH4)"),
                    optionsAs = listOf("কাৰ্বন ডাই অক্সাইড (CO2)", "অক্সিজেন (O2)", "নাইট্ৰ'জেন (N2)", "মিথেন (CH4)"),
                    correctOptionIndex = 0,
                    explanationEn = "Plants absorb Carbon Dioxide (CO2) from the atmosphere and sunlight water to produce glucose and release oxygen.",
                    explanationAs = "সালোক সংশ্লেষণৰ বাবে উদ্ভিদে বায়ুমণ্ডলৰ পৰা কাৰ্বন ডাই অক্সাইড গেছ গ্ৰহণ কৰে।",
                    topicTag = "General Science & Ecology",
                    pyqYear = "SLRC General Science"
                )
            )
        }
        return pool.shuffled().take(5)
    }
}
