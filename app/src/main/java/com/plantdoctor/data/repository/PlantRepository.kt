package com.plantdoctor.data.repository

import com.plantdoctor.data.local.dao.PlantDao
import com.plantdoctor.data.local.entity.PlantEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantRepository @Inject constructor(
    private val plantDao: PlantDao
) {
    fun getAllPlants(): Flow<List<PlantEntity>> = plantDao.getAllPlants()

    suspend fun getPlantById(id: Long): PlantEntity? = plantDao.getPlantById(id)

    suspend fun insertPlant(plant: PlantEntity): Long = plantDao.insertPlant(plant)

    suspend fun updatePlant(plant: PlantEntity) = plantDao.updatePlant(plant)

    suspend fun deletePlant(plant: PlantEntity) = plantDao.deletePlant(plant)

    fun getPlantCount(): Flow<Int> = plantDao.getPlantCount()
}
