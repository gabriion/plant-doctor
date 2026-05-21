package com.plantdoctor.domain.model

import com.plantdoctor.data.remote.model.Confidence
import com.plantdoctor.data.remote.model.Severity
import com.plantdoctor.data.remote.model.Treatment

data class Diagnosis(
    val id: Long = 0,
    val plantId: Long? = null,
    val imageUri: String,
    val identification: String,
    val diagnosis: String,
    val severity: Severity,
    val confidence: Confidence,
    val treatment: Treatment,
    val createdAt: Long = System.currentTimeMillis()
)
