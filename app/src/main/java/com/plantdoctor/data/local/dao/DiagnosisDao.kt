package com.plantdoctor.data.local.dao

import androidx.room.*
import com.plantdoctor.data.local.entity.DiagnosisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosisDao {
    @Query("SELECT * FROM diagnoses ORDER BY createdAt DESC")
    fun getAllDiagnoses(): Flow<List<DiagnosisEntity>>

    @Query("SELECT * FROM diagnoses WHERE plantId = :plantId ORDER BY createdAt DESC")
    fun getDiagnosesForPlant(plantId: Long): Flow<List<DiagnosisEntity>>

    @Query("SELECT * FROM diagnoses ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentDiagnoses(limit: Int = 5): Flow<List<DiagnosisEntity>>

    @Query("SELECT * FROM diagnoses WHERE id = :id")
    suspend fun getDiagnosisById(id: Long): DiagnosisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosis(diagnosis: DiagnosisEntity): Long

    @Update
    suspend fun updateDiagnosis(diagnosis: DiagnosisEntity)

    @Delete
    suspend fun deleteDiagnosis(diagnosis: DiagnosisEntity)

    @Query("SELECT * FROM diagnoses WHERE severity = :severity ORDER BY createdAt DESC")
    fun getDiagnosesBySeverity(severity: String): Flow<List<DiagnosisEntity>>
}
