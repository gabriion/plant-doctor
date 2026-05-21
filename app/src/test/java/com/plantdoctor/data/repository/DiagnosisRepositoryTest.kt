package com.plantdoctor.data.repository

import com.plantdoctor.data.local.dao.DiagnosisDao
import com.plantdoctor.data.local.entity.DiagnosisEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DiagnosisRepositoryTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var diagnosisDao: DiagnosisDao

    private lateinit var repository: DiagnosisRepository

    @Before
    fun setup() {
        repository = DiagnosisRepository(diagnosisDao)
    }

    @Test
    fun `getAllDiagnoses returns flow of diagnoses from dao`() = runTest {
        val diagnoses = listOf(
            DiagnosisEntity(
                id = "1",
                plantId = null,
                plantName = "Tomato",
                diseaseName = "Early Blight",
                severity = "moderate",
                confidence = 0.85f,
                description = "Fungal disease causing brown spots",
                immediateActions = "Remove affected leaves, apply fungicide",
                recommendedProducts = "Copper fungicide spray",
                preventionTips = "Rotate crops yearly, water at base",
                imageUri = "content://image/1",
                createdAt = System.currentTimeMillis()
            ),
            DiagnosisEntity(
                id = "2",
                plantId = null,
                plantName = "Rose",
                diseaseName = "Black Spot",
                severity = "mild",
                confidence = 0.92f,
                description = "Fungal disease with black circular spots",
                immediateActions = "Prune infected leaves",
                recommendedProducts = "Neem oil",
                preventionTips = "Ensure good air circulation",
                imageUri = "content://image/2",
                createdAt = System.currentTimeMillis()
            )
        )
        coEvery { diagnosisDao.getAllDiagnoses() } returns flowOf(diagnoses)

        val result = repository.getAllDiagnoses().first()

        assertEquals(2, result.size)
        assertEquals("Early Blight", result[0].diseaseName)
        assertEquals("Black Spot", result[1].diseaseName)
    }

    @Test
    fun `getRecentDiagnoses returns limited diagnoses from dao`() = runTest {
        val diagnoses = listOf(
            DiagnosisEntity(
                id = "1",
                plantId = null,
                plantName = "Basil",
                diseaseName = "Downy Mildew",
                severity = "severe",
                confidence = 0.78f,
                description = "Yellowing leaves with fuzzy gray growth",
                immediateActions = "Remove and destroy affected plants",
                recommendedProducts = "Phosphorous acid fungicide",
                preventionTips = "Avoid overhead watering, space plants",
                imageUri = "content://image/3",
                createdAt = System.currentTimeMillis()
            )
        )
        coEvery { diagnosisDao.getRecentDiagnoses(5) } returns flowOf(diagnoses)

        val result = repository.getRecentDiagnoses(5).first()

        assertEquals(1, result.size)
        assertEquals("Downy Mildew", result[0].diseaseName)
        assertEquals("severe", result[0].severity)
    }

    @Test
    fun `insertDiagnosis calls dao insert`() = runTest {
        val diagnosis = DiagnosisEntity(
            id = "1",
            plantId = "plant-1",
            plantName = "Orchid",
            diseaseName = "Root Rot",
            severity = "severe",
            confidence = 0.88f,
            description = "Roots are mushy and brown",
            immediateActions = "Repot in fresh medium, trim dead roots",
            recommendedProducts = "Hydrogen peroxide 3% solution",
            preventionTips = "Allow medium to dry between waterings",
            imageUri = "content://image/4",
            createdAt = System.currentTimeMillis()
        )
        coEvery { diagnosisDao.insertDiagnosis(diagnosis) } returns Unit

        repository.insertDiagnosis(diagnosis)

        coVerify(exactly = 1) { diagnosisDao.insertDiagnosis(diagnosis) }
    }
}
