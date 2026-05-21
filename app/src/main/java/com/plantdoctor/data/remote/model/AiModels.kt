package com.plantdoctor.data.remote.model

enum class Severity { MILD, MODERATE, SEVERE }

enum class Confidence { LOW, MEDIUM, HIGH }

data class Treatment(
    val immediate: List<String>,
    val products: List<String>,
    val prevention: List<String>
)

data class PlantDiagnosis(
    val identification: String,
    val diagnosis: String,
    val severity: Severity,
    val treatment: Treatment,
    val confidence: Confidence
)
