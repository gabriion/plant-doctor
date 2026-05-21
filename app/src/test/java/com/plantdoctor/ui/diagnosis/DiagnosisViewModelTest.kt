package com.plantdoctor.ui.diagnosis

import android.content.Context
import app.cash.turbine.test
import com.plantdoctor.data.local.entity.DiagnosisEntity
import com.plantdoctor.data.remote.PlantAnalyzer
import com.plantdoctor.data.repository.DiagnosisRepository
import com.plantdoctor.data.repository.PlantRepository
import com.plantdoctor.domain.model.DiagnosisResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiagnosisViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var plantAnalyzer: PlantAnalyzer

    @MockK
    lateinit var diagnosisRepository: DiagnosisRepository

    @MockK
    lateinit var plantRepository: PlantRepository

    @MockK
    lateinit var context: Context

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DiagnosisViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DiagnosisViewModel(plantAnalyzer, diagnosisRepository, plantRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DiagnosisUiState.Idle)
        }
    }

    @Test
    fun `analyzeImage sets Analyzing then Success on successful analysis`() = runTest {
        val imageBytes = byteArrayOf(1, 2, 3)
        val diagnosisResult = DiagnosisResult(
            plantName = "Tomato",
            diseaseName = "Early Blight",
            severity = "moderate",
            confidence = 0.85f,
            description = "Fungal disease causing brown spots on leaves",
            immediateActions = listOf("Remove affected leaves", "Apply fungicide"),
            recommendedProducts = listOf("Copper fungicide spray"),
            preventionTips = listOf("Rotate crops yearly", "Water at base of plant")
        )
        coEvery { plantAnalyzer.analyze(imageBytes, any()) } returns diagnosisResult

        viewModel.uiState.test {
            assertEquals(DiagnosisUiState.Idle, awaitItem())

            viewModel.analyzeImage(imageBytes, context)

            val analyzingState = awaitItem()
            assertTrue(analyzingState is DiagnosisUiState.Analyzing)

            val successState = awaitItem()
            assertTrue(successState is DiagnosisUiState.Success)
            assertEquals(diagnosisResult, (successState as DiagnosisUiState.Success).result)
        }
    }

    @Test
    fun `analyzeImage sets Error on failure`() = runTest {
        val imageBytes = byteArrayOf(1, 2, 3)
        val errorMessage = "Network error occurred"
        coEvery { plantAnalyzer.analyze(imageBytes, any()) } throws RuntimeException(errorMessage)

        viewModel.uiState.test {
            assertEquals(DiagnosisUiState.Idle, awaitItem())

            viewModel.analyzeImage(imageBytes, context)

            val analyzingState = awaitItem()
            assertTrue(analyzingState is DiagnosisUiState.Analyzing)

            val errorState = awaitItem()
            assertTrue(errorState is DiagnosisUiState.Error)
            assertEquals(errorMessage, (errorState as DiagnosisUiState.Error).message)
        }
    }

    @Test
    fun `saveDiagnosis calls repository insert`() = runTest {
        val diagnosis = DiagnosisEntity(
            id = "test-id",
            plantId = null,
            plantName = "Rose",
            diseaseName = "Black Spot",
            severity = "mild",
            confidence = 0.9f,
            description = "Fungal disease",
            immediateActions = "Prune infected leaves",
            recommendedProducts = "Neem oil",
            preventionTips = "Good air circulation",
            imageUri = "content://image/1",
            createdAt = System.currentTimeMillis()
        )
        coEvery { diagnosisRepository.insertDiagnosis(diagnosis) } returns Unit

        viewModel.saveDiagnosis(diagnosis)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { diagnosisRepository.insertDiagnosis(diagnosis) }
    }
}
