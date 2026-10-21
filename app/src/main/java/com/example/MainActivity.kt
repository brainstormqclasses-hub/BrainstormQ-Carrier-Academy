package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AcademyViewModel
import com.example.ui.Screen
import com.example.ui.auth.BiometricAuthModal
import com.example.ui.components.AcademyTopBar
import com.example.ui.payment.PaymentBottomSheet
import com.example.ui.screens.*
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

class MainActivity : ComponentActivity() {

    private val viewModel: AcademyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

data class NavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun MainAppContent(viewModel: AcademyViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isBiometricModalVisible by viewModel.isBiometricModalVisible.collectAsStateWithLifecycle()
    val isPaymentModalVisible by viewModel.isPaymentModalVisible.collectAsStateWithLifecycle()
    val selectedTopic by viewModel.selectedTopicForReading.collectAsStateWithLifecycle()
    val activeTestState by viewModel.testTakingState.collectAsStateWithLifecycle()
    val dailyQuizState by viewModel.dailyQuizState.collectAsStateWithLifecycle()

    val topics by viewModel.topics.collectAsStateWithLifecycle()
    val mockTests by viewModel.mockTests.collectAsStateWithLifecycle()
    val recentResults by viewModel.recentResults.collectAsStateWithLifecycle()
    val forumPosts by viewModel.forumPosts.collectAsStateWithLifecycle()
    val doubtSessions by viewModel.doubtSessions.collectAsStateWithLifecycle()
    val pyqAnalyses by viewModel.pyqAnalyses.collectAsStateWithLifecycle()

    val plannerGoal by viewModel.plannerGoal.collectAsStateWithLifecycle()
    val aiPlan by viewModel.aiPlan.collectAsStateWithLifecycle()
    val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsStateWithLifecycle()

    // Handle Back Button gracefully
    BackHandler(enabled = selectedTopic != null || activeTestState != null || currentScreen != Screen.Dashboard) {
        if (selectedTopic != null) {
            viewModel.closeTopic()
        } else if (activeTestState != null) {
            viewModel.exitTest()
        } else if (currentScreen == Screen.DailyQuiz) {
            viewModel.exitDailyQuiz()
        } else if (currentScreen != Screen.Dashboard) {
            viewModel.navigateTo(Screen.Dashboard)
        }
    }

    val navItems = listOf(
        NavItem(Screen.Dashboard, "Home", Icons.Default.Home, "nav_dashboard"),
        NavItem(Screen.StudyMaterials, "Notes", Icons.Default.MenuBook, "nav_notes"),
        NavItem(Screen.AiPlanner, "AI Planner", Icons.Default.AutoAwesome, "nav_ai_planner"),
        NavItem(Screen.MockTests, "Mocks", Icons.Default.Quiz, "nav_mocks"),
        NavItem(Screen.LiveDoubtAndForum, "Doubts", Icons.Default.LiveTv, "nav_doubts")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (activeTestState == null && selectedTopic == null && currentScreen != Screen.DailyQuiz) {
                AcademyTopBar(
                    currentLanguage = currentLanguage,
                    isPremiumUnlocked = userProfile.isPremiumUnlocked,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onUnlockPremiumClick = { viewModel.showPaymentModal() },
                    onProfileClick = { viewModel.navigateTo(Screen.Profile) }
                )
            }
        },
        bottomBar = {
            if (activeTestState == null && selectedTopic == null && currentScreen != Screen.DailyQuiz) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val selected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.navigateTo(item.screen) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NavyDark,
                                selectedTextColor = NavyPrimary,
                                indicatorColor = GoldAccent.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.testTag(item.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Dashboard -> {
                    DashboardScreen(
                        userProfile = userProfile,
                        currentLanguage = currentLanguage,
                        aiPlan = aiPlan,
                        recentResults = recentResults,
                        onNavigate = { viewModel.navigateTo(it) },
                        onUnlockPremiumClick = { viewModel.showPaymentModal() },
                        onStartDailyQuiz = { viewModel.startDailyQuiz() }
                    )
                }
                is Screen.AiPlanner -> {
                    AiPlannerScreen(
                        currentLanguage = currentLanguage,
                        goal = plannerGoal,
                        aiPlan = aiPlan,
                        isGenerating = isGeneratingPlan,
                        topics = topics,
                        mockTests = mockTests,
                        onUpdateGoal = { exam, days, score, hours ->
                            viewModel.updatePlannerGoal(exam, days, score, hours)
                        },
                        onRegenerateClick = { viewModel.generateAiPlan() },
                        onToggleTaskCompleted = { viewModel.toggleTaskCompletion(it) },
                        onOpenTopic = {
                            viewModel.openTopic(it)
                            viewModel.navigateTo(Screen.StudyMaterials)
                        },
                        onStartTest = {
                            viewModel.startTest(it)
                            viewModel.navigateTo(Screen.MockTests)
                        }
                    )
                }
                is Screen.StudyMaterials -> {
                    StudyMaterialsScreen(
                        currentLanguage = currentLanguage,
                        topics = topics,
                        selectedTopic = selectedTopic,
                        onSelectTopic = { viewModel.openTopic(it) },
                        onCloseTopic = { viewModel.closeTopic() },
                        onToggleBookmark = { viewModel.toggleBookmark(it) },
                        onMarkCompleted = { viewModel.markTopicDone(it) },
                        onToggleLanguage = { viewModel.toggleLanguage() }
                    )
                }
                is Screen.MockTests -> {
                    MockTestsScreen(
                        currentLanguage = currentLanguage,
                        mockTests = mockTests,
                        activeTestState = activeTestState,
                        onStartTest = { viewModel.startTest(it) },
                        onSelectOption = { qIdx, oIdx -> viewModel.selectTestOption(qIdx, oIdx) },
                        onNextQuestion = { viewModel.nextQuestion() },
                        onPrevQuestion = { viewModel.prevQuestion() },
                        onJumpQuestion = { viewModel.jumpToQuestion(it) },
                        onSubmitTest = { viewModel.submitTest() },
                        onExitTest = { viewModel.exitTest() },
                        onOpenAiPlanner = {
                            viewModel.exitTest()
                            viewModel.navigateTo(Screen.AiPlanner)
                        },
                        onStartDailyQuiz = { viewModel.startDailyQuiz() }
                    )
                }
                is Screen.PyqAnalysis -> {
                    PyqAnalysisScreen(
                        currentLanguage = currentLanguage,
                        pyqAnalyses = pyqAnalyses
                    )
                }
                is Screen.LiveDoubtAndForum -> {
                    LiveDoubtAndForumScreen(
                        currentLanguage = currentLanguage,
                        doubtSessions = doubtSessions,
                        forumPosts = forumPosts,
                        onUpvotePost = { viewModel.togglePostUpvote(it) },
                        onAddComment = { pId, text -> viewModel.addComment(pId, text) },
                        onPostQuestion = { title, content, exam ->
                            viewModel.postForumQuestion(title, content, exam)
                        }
                    )
                }
                is Screen.Profile -> {
                    ProfileScreen(
                        userProfile = userProfile,
                        currentLanguage = currentLanguage,
                        onUpdateProfile = { name, exam, year ->
                            viewModel.updateProfile(name, exam, year)
                        },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onUnlockPremiumClick = { viewModel.showPaymentModal() }
                    )
                }
                is Screen.DailyQuiz -> {
                    DailyQuizScreen(
                        quizState = dailyQuizState,
                        userProfile = userProfile,
                        currentLanguage = currentLanguage,
                        onSelectOption = { viewModel.selectDailyQuizOption(it) },
                        onNextQuestion = { viewModel.nextDailyQuizQuestion() },
                        onRestartQuiz = { viewModel.startDailyQuiz() },
                        onExitQuiz = { viewModel.exitDailyQuiz() },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onOpenAiPlanner = {
                            viewModel.exitDailyQuiz()
                            viewModel.navigateTo(Screen.AiPlanner)
                        }
                    )
                }
            }
        }
    }

    // Biometric Pop-Up Login on Launch
    if (isBiometricModalVisible) {
        BiometricAuthModal(
            userName = userProfile.name,
            targetExam = userProfile.targetExam,
            onAuthenticated = { name, exam ->
                viewModel.updateProfile(name, exam, userProfile.targetYear)
                viewModel.dismissBiometricModal()
            }
        )
    }

    // One-Time Payment Bottom Sheet (Rs. 99/-)
    if (isPaymentModalVisible) {
        PaymentBottomSheet(
            onDismiss = { viewModel.dismissPaymentModal() },
            onPaymentSuccess = { txId ->
                viewModel.unlockPremium(txId)
            }
        )
    }
}
