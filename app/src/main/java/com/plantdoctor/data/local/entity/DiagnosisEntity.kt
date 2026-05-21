package com.plantdoctor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnoses")
data class DiagnosisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long? = null,
    val imageUri: String,
    val identification: String,
    val diagnosis: String,
    val severity: String, // MILD, MODERATE, SEVERE
    val confidence: String, // LOW, MEDIUM, HIGH
    val immediateActions: String, // JSON list
    val products: String, // JSON list
    val prevention: String, // JSON list
    val createdAt: Long = System.currentTimeMillis()
)
