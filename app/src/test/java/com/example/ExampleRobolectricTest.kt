package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ExamCategory
import com.example.data.model.Language
import com.example.data.repository.AcademyRepository
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BrainstormQ", appName)
    }

    @Test
    fun `test repository loads bilingual mock tests and subjects`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = AcademyRepository(context)

        val topics = repository.topics.value
        assertTrue("Repository must have study topics", topics.isNotEmpty())

        val mockTests = repository.mockTests.value
        assertTrue("Repository must have mock tests", mockTests.isNotEmpty())

        val pyq = repository.pyqAnalyses.value
        assertTrue("Repository must have PYQ analyses", pyq.isNotEmpty())
    }

    @Test
    fun `test daily quiz generation with 5 questions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = AcademyRepository(context)

        val quiz = repository.generateDailyQuiz(ExamCategory.APSC)
        assertNotNull("Daily quiz should be created", quiz)
        assertEquals("Daily quiz must have 5 questions", 5, quiz.questions.size)
        assertFalse("Quiz should start incomplete", quiz.isCompleted)
    }
}
