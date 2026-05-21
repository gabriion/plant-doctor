package com.plantdoctor.data.repository

import com.plantdoctor.data.local.dao.PlantDao
import com.plantdoctor.data.local.entity.PlantEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlantRepositoryTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var plantDao: PlantDao

    private lateinit var repository: PlantRepository

    @Before
    fun setup() {
        repository = PlantRepository(plantDao)
    }

    @Test
    fun `getAllPlants returns flow of plants from dao`() = runTest {
        val plants = listOf(
            PlantEntity(
                id = "1",
                name = "Monstera",
                species = "Monstera deliciosa",
                location = "Living Room",
                notes = "Needs indirect light",
                photoUri = null,
                createdAt = System.currentTimeMillis()
            ),
            PlantEntity(
                id = "2",
                name = "Snake Plant",
                species = "Sansevieria trifasciata",
                location = "Bedroom",
                notes = "Very low maintenance",
                photoUri = null,
                createdAt = System.currentTimeMillis()
            )
        )
        coEvery { plantDao.getAllPlants() } returns flowOf(plants)

        val result = repository.getAllPlants().first()

        assertEquals(2, result.size)
        assertEquals("Monstera", result[0].name)
        assertEquals("Snake Plant", result[1].name)
    }

    @Test
    fun `insertPlant calls dao insert`() = runTest {
        val plant = PlantEntity(
            id = "1",
            name = "Fern",
            species = "Nephrolepis exaltata",
            location = "Bathroom",
            notes = "Loves humidity",
            photoUri = null,
            createdAt = System.currentTimeMillis()
        )
        coEvery { plantDao.insertPlant(plant) } returns Unit

        repository.insertPlant(plant)

        coVerify(exactly = 1) { plantDao.insertPlant(plant) }
    }

    @Test
    fun `getPlantById returns plant from dao`() = runTest {
        val plant = PlantEntity(
            id = "1",
            name = "Pothos",
            species = "Epipremnum aureum",
            location = "Kitchen",
            notes = "Trailing vine",
            photoUri = null,
            createdAt = System.currentTimeMillis()
        )
        coEvery { plantDao.getPlantById("1") } returns plant

        val result = repository.getPlantById("1")

        assertNotNull(result)
        assertEquals("Pothos", result?.name)
        assertEquals("1", result?.id)
    }

    @Test
    fun `deletePlant calls dao delete`() = runTest {
        val plant = PlantEntity(
            id = "1",
            name = "Cactus",
            species = "Cactaceae",
            location = "Window",
            notes = "Minimal water",
            photoUri = null,
            createdAt = System.currentTimeMillis()
        )
        coEvery { plantDao.deletePlant(plant) } returns Unit

        repository.deletePlant(plant)

        coVerify(exactly = 1) { plantDao.deletePlant(plant) }
    }
}
