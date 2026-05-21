package com.plantdoctor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.plantdoctor.data.local.dao.DiagnosisDao
import com.plantdoctor.data.local.dao.PlantDao
import com.plantdoctor.data.local.dao.SeasonalTipDao
import com.plantdoctor.data.local.entity.DiagnosisEntity
import com.plantdoctor.data.local.entity.PlantEntity
import com.plantdoctor.data.local.entity.SeasonalTipEntity

@Database(
    entities = [PlantEntity::class, DiagnosisEntity::class, SeasonalTipEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PlantDoctorDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun diagnosisDao(): DiagnosisDao
    abstract fun seasonalTipDao(): SeasonalTipDao
}
