package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiPlannerService
import com.example.data.model.*
import com.example.data.repository.AcademyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object StudyMaterials : Screen("study_materials")
    object MockTests : Screen("mock_tests")
    object AiPlanner : Screen("ai_planner")
    object PyqAnalysis : Screen("pyq_analysis")
    object LiveDoubtAndForum : Screen("live_doubt_and_forum")
    object Profile : Screen("profile")
    object DailyQuiz : Screen("daily_quiz")
}

data class TestTakingState(
    val test: MockTest,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndices: Map<Int, Int> = emptyMap(), // QuestionIdx -> SelectedOption
    val remainingSeconds: Int,
    val isSubmitted: Boolean = false,
    val completedResult: MockTestResult? = null
)

class AcademyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AcademyRepository(application)
    private val geminiPlannerService = GeminiPlannerService()

    val userProfile = repository.userProfile
    val currentLanguage = repository.currentLanguage
    val topics = repository.topics
    val mockTests = repository.mockTests
    val recentResults = repository.recentResults
    val forumPosts = repository.forumPosts
    val doubtSessions = repository.doubtSessions
    val pyqAnalyses = repository.pyqAnalyses

    // Navigation & Modals
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isBiometricModalVisible = MutableStateFlow(true) // Opens with pop-up on launch
    val isBiometricModalVisible: StateFlow<Boolean> = _isBiometricModalVisible.asStateFlow()

    private val _isPaymentModalVisible = MutableStateFlow(false)
    val isPaymentModalVisible: StateFlow<Boolean> = _isPaymentModalVisible.asStateFlow()

    private val _selectedTopicForReading = MutableStateFlow<StudyTopic?>(null)
    val selectedTopicForReading: StateFlow<StudyTopic?> = _selectedTopicForReading.asStateFlow()

    // Active Test State
    private val _testTakingState = MutableStateFlow<TestTakingState?>(null)
    val testTakingState: StateFlow<TestTakingState?> = _testTakingState.asStateFlow()

    private var testTimerJob: Job? = null

    // AI Study Planner State
    private val _plannerGoal = MutableStateFlow(PlannerGoal())
    val plannerGoal: StateFlow<PlannerGoal> = _plannerGoal.asStateFlow()

    private val _aiPlan = MutableStateFlow<AiGeneratedPlan?>(null)
    val aiPlan: StateFlow<AiGeneratedPlan?> = _aiPlan.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    // Daily Quiz State
    private val _dailyQuizState = MutableStateFlow<DailyQuizState?>(null)
    val dailyQuizState: StateFlow<DailyQuizState?> = _dailyQuizState.asStateFlow()

    init {
        // Automatically generate initial study plan
        generateAiPlan()
    }

    fun navigateTo(screen: Screen) {
        _selectedTopicForReading.value = null
        _currentScreen.value = screen
    }

    fun openTopic(topic: StudyTopic) {
        _selectedTopicForReading.value = topic
    }

    fun closeTopic() {
        _selectedTopicForReading.value = null
    }

    fun toggleLanguage() {
        val next = if (currentLanguage.value == Language.ENGLISH) Language.ASSAMESE else Language.ENGLISH
        repository.setLanguage(next)
    }

    fun setLanguage(language: Language) {
        repository.setLanguage(language)
    }

    fun dismissBiometricModal() {
        _isBiometricModalVisible.value = false
    }

    fun showPaymentModal() {
        _isPaymentModalVisible.value = true
    }

    fun dismissPaymentModal() {
        _isPaymentModalVisible.value = false
    }

    fun unlockPremium(txId: String) {
        repository.unlockPremium(txId)
        _isPaymentModalVisible.value = false
    }

    fun toggleBookmark(topicId: String) {
        repository.toggleBookmark(topicId)
    }

    fun markTopicDone(topicId: String) {
        repository.markTopicCompleted(topicId)
        if (_selectedTopicForReading.value?.id == topicId) {
            _selectedTopicForReading.update { it?.copy(isCompleted = true) }
        }
    }

    // Mock Test taking
    fun startTest(test: MockTest) {
        testTimerJob?.cancel()
        _testTakingState.value = TestTakingState(
            test = test,
            currentQuestionIndex = 0,
            selectedOptionIndices = emptyMap(),
            remainingSeconds = test.durationMinutes * 60,
            isSubmitted = false
        )

        testTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _testTakingState.value ?: break
                if (current.isSubmitted) break
                if (current.remainingSeconds <= 1) {
                    submitTest()
                    break
                }
                _testTakingState.update { it?.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
        }
    }

    fun selectTestOption(questionIdx: Int, optionIdx: Int) {
        _testTakingState.update { state ->
            if (state == null || state.isSubmitted) return@update state
            val map = state.selectedOptionIndices.toMutableMap()
            map[questionIdx] = optionIdx
            state.copy(selectedOptionIndices = map)
        }
    }

    fun nextQuestion() {
        _testTakingState.update { state ->
            if (state == null) return@update null
            val nextIdx = (state.currentQuestionIndex + 1).coerceAtMost(state.test.questions.size - 1)
            state.copy(currentQuestionIndex = nextIdx)
        }
    }

    fun prevQuestion() {
        _testTakingState.update { state ->
            if (state == null) return@update null
            val prevIdx = (state.currentQuestionIndex - 1).coerceAtLeast(0)
            state.copy(currentQuestionIndex = prevIdx)
        }
    }

    fun jumpToQuestion(idx: Int) {
        _testTakingState.update { state ->
            state?.copy(currentQuestionIndex = idx.coerceIn(0, (state.test.questions.size - 1).coerceAtLeast(0)))
        }
    }

    fun submitTest() {
        testTimerJob?.cancel()
        val state = _testTakingState.value ?: return
        if (state.isSubmitted) return

        var correct = 0
        var incorrect = 0
        var skipped = 0

        state.test.questions.forEachIndexed { idx, q ->
            val userPick = state.selectedOptionIndices[idx]
            if (userPick == null) {
                skipped++
            } else if (userPick == q.correctOptionIndex) {
                correct++
            } else {
                incorrect++
            }
        }

        val marksPerQ = state.test.totalMarks.toFloat() / state.test.questions.size.coerceAtLeast(1)
        val rawScore = (correct * marksPerQ) - (incorrect * state.test.negativeMarks)
        val finalScore = rawScore.coerceAtLeast(0f)
        val accuracy = if ((correct + incorrect) > 0) correct.toFloat() / (correct + incorrect) else 0f
        val timeSpent = (state.test.durationMinutes * 60) - state.remainingSeconds

        val result = MockTestResult(
            testId = state.test.id,
            testTitle = state.test.titleEn,
            score = finalScore,
            totalMarks = state.test.totalMarks,
            correctCount = correct,
            incorrectCount = incorrect,
            skippedCount = skipped,
            accuracy = accuracy,
            timeTakenSeconds = timeSpent
        )

        repository.saveTestResult(result)

        _testTakingState.update {
            it?.copy(
                isSubmitted = true,
                completedResult = result
            )
        }

        // Trigger AI plan refresh to account for new score & weak areas
        generateAiPlan()
    }

    fun exitTest() {
        testTimerJob?.cancel()
        _testTakingState.value = null
    }

    // AI Study Planner
    fun updatePlannerGoal(targetExam: ExamCategory, daysRemaining: Int, targetScore: Int, dailyHours: Int) {
        _plannerGoal.value = PlannerGoal(
            targetExam = targetExam,
            targetDateDaysRemaining = daysRemaining,
            targetScore = targetScore,
            maxScore = if (targetExam == ExamCategory.ADRE) 150 else 200,
            dailyStudyHours = dailyHours,
            preferredMedium = currentLanguage.value
        )
        generateAiPlan()
    }

    fun generateAiPlan() {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            try {
                val plan = geminiPlannerService.generateStudyPlan(
                    goal = _plannerGoal.value,
                    recentResults = repository.recentResults.value,
                    completedTopicsCount = repository.userProfile.value.completedTopicsCount,
                    totalTopics = repository.topics.value.size
                )
                _aiPlan.value = plan
            } finally {
                _isGeneratingPlan.value = false
            }
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        _aiPlan.update { plan ->
            if (plan == null) return@update null
            val updatedSchedule = plan.schedule.map {
                if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
            }
            plan.copy(schedule = updatedSchedule)
        }
    }

    // Forum
    fun postForumQuestion(title: String, content: String, exam: ExamCategory) {
        repository.addForumPost(title, content, exam)
    }

    fun togglePostUpvote(postId: String) {
        repository.toggleUpvote(postId)
    }

    fun addComment(postId: String, text: String) {
        repository.addForumComment(postId, text)
    }

    fun updateProfile(name: String, targetExam: ExamCategory, targetYear: String) {
        repository.updateProfile(name, targetExam, targetYear)
        _plannerGoal.update { it.copy(targetExam = targetExam) }
        generateAiPlan()
    }

    // Daily Quiz Methods
    fun startDailyQuiz(category: ExamCategory? = null) {
        val targetCat = category ?: userProfile.value.targetExam
        val questions = repository.getDailyQuizQuestions(targetCat)
        _dailyQuizState.value = DailyQuizState(
            examCategory = targetCat,
            questions = questions,
            currentQuestionIndex = 0,
            selectedOptionIndex = null,
            isAnswerRevealed = false,
            answersMap = emptyMap(),
            correctCount = 0,
            isCompleted = false,
            streakAdded = false,
            timeTakenSeconds = 0
        )
        _selectedTopicForReading.value = null
        _testTakingState.value = null
        _currentScreen.value = Screen.DailyQuiz
    }

    fun selectDailyQuizOption(optionIndex: Int) {
        _dailyQuizState.update { current ->
            if (current == null || current.isAnswerRevealed || current.isCompleted) return@update current
            val q = current.questions.getOrNull(current.currentQuestionIndex) ?: return@update current
            val isCorrect = optionIndex == q.correctOptionIndex
            val newAnswers = current.answersMap + (current.currentQuestionIndex to optionIndex)
            val newCorrectCount = if (isCorrect) current.correctCount + 1 else current.correctCount

            current.copy(
                selectedOptionIndex = optionIndex,
                isAnswerRevealed = true,
                answersMap = newAnswers,
                correctCount = newCorrectCount
            )
        }
    }

    fun nextDailyQuizQuestion() {
        val current = _dailyQuizState.value ?: return
        if (current.currentQuestionIndex < current.questions.size - 1) {
            _dailyQuizState.update {
                it?.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerRevealed = false
                )
            }
        } else {
            // Completed the 5 questions
            val total = current.questions.size.coerceAtLeast(1)
            val accuracy = current.correctCount.toFloat() / total
            val result = MockTestResult(
                testId = "daily_quiz_${System.currentTimeMillis()}",
                testTitle = "Daily Quiz (${current.examCategory.shortName})",
                score = current.correctCount.toFloat() * 2f,
                totalMarks = total * 2,
                correctCount = current.correctCount,
                incorrectCount = total - current.correctCount,
                skippedCount = 0,
                accuracy = accuracy,
                timeTakenSeconds = 60
            )

            repository.recordDailyQuiz(result, incrementStreak = true)
            _dailyQuizState.update {
                it?.copy(
                    isCompleted = true,
                    streakAdded = true
                )
            }
            generateAiPlan()
        }
    }

    fun exitDailyQuiz() {
        _dailyQuizState.value = null
        _currentScreen.value = Screen.Dashboard
    }
}
