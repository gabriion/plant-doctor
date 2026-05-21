package com.plantdoctor.ui.diagnosis

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantdoctor.data.local.entity.DiagnosisEntity
import com.plantdoctor.data.local.entity.PlantEntity
import com.plantdoctor.data.remote.PlantAnalyzer
import com.plantdoctor.data.remote.model.PlantDiagnosis
import com.plantdoctor.data.repository.DiagnosisRepository
import com.plantdoctor.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DiagnosisUiState {
    data object Idle : DiagnosisUiState()
    data object Analyzing : DiagnosisUiState()
    data class Success(val diagnosis: PlantDiagnosis, val imageUri: String) : DiagnosisUiState()
    data class Error(val message: String) : DiagnosisUiState()
}

@HiltViewModel
class DiagnosisViewModel @Inject constructor(
    private val plantAnalyzer: PlantAnalyzer,
    private val diagnosisRepository: DiagnosisRepository,
    private val plantRepository: PlantRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Idle)
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    val plants: StateFlow<List<PlantEntity>> = plantRepository.getAllPlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val imageUri: String? = savedStateHandle.get<String>("imageUri")

    init {
        imageUri?.let { analyzeImage(it) }
    }

    fun analyzeImage(uriString: String) {
        viewModelScope.launch {
            _uiState.value = DiagnosisUiState.Analyzing
            try {
                val uri = Uri.parse(uriString)
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw Exception("Could not read image")
                val result = plantAnalyzer.analyze(bytes)
                _uiState.value = DiagnosisUiState.Success(result, uriString)
            } catch (e: Exception) {
                _uiState.value = DiagnosisUiState.Error(e.message ?: "Analysis failed")
            }
        }
    }

    fun saveDiagnosis(plantId: Long? = null) {
        val state = _uiState.value
        if (state !is DiagnosisUiState.Success) return
        viewModelScope.launch {
            val d = state.diagnosis
            val entity = DiagnosisEntity(
                plantId = plantId,
                imageUri = state.imageUri,
                identification = d.identification,
                diagnosis = d.diagnosis,
                severity = d.severity.name,
                confidence = d.confidence.name,
                immediateActions = com.google.gson.Gson().toJson(d.treatment.immediate),
                products = com.google.gson.Gson().toJson(d.treatment.products),
                prevention = com.google.gson.Gson().toJson(d.treatment.prevention)
            )
            diagnosisRepository.insertDiagnosis(entity)
        }
    }

    fun retry() {
        imageUri?.let { analyzeImage(it) }
    }
}
