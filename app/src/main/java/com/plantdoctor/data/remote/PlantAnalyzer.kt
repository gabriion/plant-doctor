package com.plantdoctor.data.remote

import com.plantdoctor.data.remote.model.PlantDiagnosis

interface PlantAnalyzer {
    suspend fun analyze(imageBytes: ByteArray): PlantDiagnosis
}
