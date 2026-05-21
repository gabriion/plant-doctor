package com.plantdoctor.ui.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantdoctor.data.local.entity.DiagnosisEntity
import com.plantdoctor.data.local.entity.PlantEntity
import com.plantdoctor.data.repository.DiagnosisRepository
import com.plantdoctor.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PlantUiState {
    data object Loading : PlantUiState()
    data class Success(val plants: List<PlantEntity>) : PlantUiState()
    data class Error(val message: String) : PlantUiState()
}

data class PlantDetailUiState(
    val plant: PlantEntity? = null,
    val diagnoses: List<DiagnosisEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PlantViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val diagnosisRepository: DiagnosisRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _plantsState = MutableStateFlow<PlantUiState>(PlantUiState.Loading)
    val plantsState: StateFlow<PlantUiState> = _plantsState.asStateFlow()

    private val _detailState = MutableStateFlow(PlantDetailUiState())
    val detailState: StateFlow<PlantDetailUiState> = _detailState.asStateFlow()

    init {
        loadPlants()
    }

    fun loadPlants() {
        viewModelScope.launch {
            plantRepository.getAllPlants()
                .catch { e -> _plantsState.value = PlantUiState.Error(e.message ?: "Failed to load plants") }
                .collect { plants -> _plantsState.value = PlantUiState.Success(plants) }
        }
    }

    fun loadPlantDetail(plantId: Long) {
        viewModelScope.launch {
            _detailState.value = PlantDetailUiState(isLoading = true)
            try {
                val plant = plantRepository.getPlantById(plantId)
                if (plant != null) {
                    diagnosisRepository.getDiagnosesForPlant(plantId).collect { diagnoses ->
                        _detailState.value = PlantDetailUiState(
                            plant = plant,
                            diagnoses = diagnoses,
                            isLoading = false
                        )
                    }
                } else {
                    _detailState.value = PlantDetailUiState(isLoading = false)
                }
            } catch (e: Exception) {
                _detailState.value = PlantDetailUiState(isLoading = false)
            }
        }
    }

    fun addPlant(plant: PlantEntity) {
        viewModelScope.launch {
            plantRepository.insertPlant(plant)
        }
    }

    fun updatePlant(plant: PlantEntity) {
        viewModelScope.launch {
            plantRepository.updatePlant(plant)
        }
    }

    fun deletePlant(plant: PlantEntity) {
        viewModelScope.launch {
            plantRepository.deletePlant(plant)
        }
    }

    suspend fun getPlantById(plantId: Long): PlantEntity? {
        return plantRepository.getPlantById(plantId)
    }
}
