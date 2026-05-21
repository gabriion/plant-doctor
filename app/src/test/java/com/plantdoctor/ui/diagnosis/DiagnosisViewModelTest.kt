package com.plantdoctor.ui.diagnosis

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.plantdoctor.data.local.entity.PlantEntity
import com.plantdoctor.data.remote.PlantAnalyzer
import com.plantdoctor.data.remote.model.Confidence
import com.plantdoctor.data.remote.model.PlantDiagnosis
import com.plantdoctor.data.remote.model.Severity
import com.plantdoctor.data.remote.model.Treatment
import com.plantdoctor.data.repository.DiagnosisRepository
import com.plantdoctor.data.repository.PlantRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class DiagnosisViewModelTest {

    private val plantAnalyzer: PlantAnalyzer = mockk()
    private val diagnosisRepository: DiagnosisRepository = mockk(relaxed = true)
    private val plantRepository: PlantRepository = mockk()
    private val context: Context = mockk()
    private val contentResolver: ContentResolver = mockk()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DiagnosisViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { context.contentResolver } returns contentResolver
        every { plantRepository.getAllPlants() } returns flowOf(emptyList<PlantEntity>())

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Uri::class)
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): DiagnosisViewModel {
        return DiagnosisViewModel(plantAnalyzer, diagnosisRepository, plantRepository, context, savedStateHandle)
    }

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel = createViewModel()
        assertEquals(DiagnosisUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `analyzeImage transitions to Success on successful analysis`() = runTest {
        val imageBytes = byteArrayOf(1, 2, 3)
        val diagnosis = PlantDiagnosis(
            identification = "Tomato",
            diagnosis = "Early Blight",
            severity = Severity.MODERATE,
            treatment = Treatment(
                immediate = listOf("Remove affected leaves"),
                products = listOf("Copper fungicide"),
                prevention = listOf("Rotate crops yearly")
            ),
            confidence = Confidence.HIGH
        )

        every { contentResolver.openInputStream(any()) } returns ByteArrayInputStream(imageBytes)
        coEvery { plantAnalyzer.analyze(any()) } returns diagnosis

        viewModel = createViewModel()
        viewModel.analyzeImage("content://image/1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DiagnosisUiState.Success)
        assertEquals(diagnosis, (state as DiagnosisUiState.Success).diagnosis)
        assertEquals("content://image/1", state.imageUri)
    }

    @Test
    fun `analyzeImage transitions to Error on failure`() = runTest {
        every { contentResolver.openInputStream(any()) } returns ByteArrayInputStream(byteArrayOf(1))
        coEvery { plantAnalyzer.analyze(any()) } throws RuntimeException("Network error")

        viewModel = createViewModel()
        viewModel.analyzeImage("content://image/1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DiagnosisUiState.Error)
        assertEquals("Network error", (state as DiagnosisUiState.Error).message)
    }
}
