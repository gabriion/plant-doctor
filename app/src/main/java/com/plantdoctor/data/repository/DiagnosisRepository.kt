package com.plantdoctor.data.repository

import com.plantdoctor.data.local.dao.DiagnosisDao
import com.plantdoctor.data.local.entity.DiagnosisEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosisRepository @Inject constructor(
    private val diagnosisDao: DiagnosisDao
) {
    fun getAllDiagnoses(): Flow<List<DiagnosisEntity>> = diagnosisDao.getAllDiagnoses()

    fun getDiagnosesForPlant(plantId: Long): Flow<List<DiagnosisEntity>> =
        diagnosisDao.getDiagnosesForPlant(plantId)

    fun getRecentDiagnoses(limit: Int = 5): Flow<List<DiagnosisEntity>> =
        diagnosisDao.getRecentDiagnoses(limit)

    suspend fun getDiagnosisById(id: Long): DiagnosisEntity? =
        diagnosisDao.getDiagnosisById(id)

    suspend fun insertDiagnosis(diagnosis: DiagnosisEntity): Long =
        diagnosisDao.insertDiagnosis(diagnosis)

    suspend fun updateDiagnosis(diagnosis: DiagnosisEntity) =
        diagnosisDao.updateDiagnosis(diagnosis)

    suspend fun deleteDiagnosis(diagnosis: DiagnosisEntity) =
        diagnosisDao.deleteDiagnosis(diagnosis)

    fun getDiagnosesBySeverity(severity: String): Flow<List<DiagnosisEntity>> =
        diagnosisDao.getDiagnosesBySeverity(severity)
}
