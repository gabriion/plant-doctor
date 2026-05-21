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
                id = 1, plantId = null, imageUri = "content://image/1",
                identification = "Tomato", diagnosis = "Early Blight",
                severity = "MODERATE", confidence = "HIGH",
                immediateActions = "[\"Remove affected leaves\"]",
                products = "[\"Copper fungicide\"]",
                prevention = "[\"Rotate crops yearly\"]"
            ),
            DiagnosisEntity(
                id = 2, plantId = null, imageUri = "content://image/2",
                identification = "Rose", diagnosis = "Black Spot",
                severity = "MILD", confidence = "HIGH",
                immediateActions = "[\"Prune infected leaves\"]",
                products = "[\"Neem oil\"]",
                prevention = "[\"Good air circulation\"]"
            )
        )
        coEvery { diagnosisDao.getAllDiagnoses() } returns flowOf(diagnoses)

        val result = repository.getAllDiagnoses().first()

        assertEquals(2, result.size)
        assertEquals("Early Blight", result[0].diagnosis)
        assertEquals("Black Spot", result[1].diagnosis)
    }

    @Test
    fun `getRecentDiagnoses returns limited diagnoses from dao`() = runTest {
        val diagnoses = listOf(
            DiagnosisEntity(
                id = 1, plantId = null, imageUri = "content://image/3",
                identification = "Basil", diagnosis = "Downy Mildew",
                severity = "SEVERE", confidence = "MEDIUM",
                immediateActions = "[\"Remove affected plants\"]",
                products = "[\"Phosphorous acid fungicide\"]",
                prevention = "[\"Avoid overhead watering\"]"
            )
        )
        coEvery { diagnosisDao.getRecentDiagnoses(5) } returns flowOf(diagnoses)

        val result = repository.getRecentDiagnoses(5).first()

        assertEquals(1, result.size)
        assertEquals("Downy Mildew", result[0].diagnosis)
        assertEquals("SEVERE", result[0].severity)
    }

    @Test
    fun `insertDiagnosis calls dao insert`() = runTest {
        val diagnosis = DiagnosisEntity(
            id = 1, plantId = 10L, imageUri = "content://image/4",
            identification = "Orchid", diagnosis = "Root Rot",
            severity = "SEVERE", confidence = "HIGH",
            immediateActions = "[\"Repot in fresh medium\"]",
            products = "[\"Hydrogen peroxide 3%\"]",
            prevention = "[\"Allow medium to dry between waterings\"]"
        )
        coEvery { diagnosisDao.insertDiagnosis(diagnosis) } returns 1L

        repository.insertDiagnosis(diagnosis)

        coVerify(exactly = 1) { diagnosisDao.insertDiagnosis(diagnosis) }
    }
}
