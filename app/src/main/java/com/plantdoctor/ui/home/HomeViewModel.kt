package com.plantdoctor.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantdoctor.data.local.entity.DiagnosisEntity
import com.plantdoctor.data.local.entity.PlantEntity
import com.plantdoctor.data.local.entity.SeasonalTipEntity
import com.plantdoctor.data.local.dao.SeasonalTipDao
import com.plantdoctor.data.repository.DiagnosisRepository
import com.plantdoctor.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val recentDiagnoses: List<DiagnosisEntity> = emptyList(),
    val plants: List<PlantEntity> = emptyList(),
    val seasonalTips: List<SeasonalTipEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val diagnosisRepository: DiagnosisRepository,
    private val seasonalTipDao: SeasonalTipDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                diagnosisRepository.getRecentDiagnoses(5),
                plantRepository.getAllPlants(),
                seasonalTipDao.getTipsForMonth(Calendar.getInstance().get(Calendar.MONTH) + 1)
            ) { diagnoses, plants, tips ->
                HomeUiState(
                    recentDiagnoses = diagnoses,
                    plants = plants,
                    seasonalTips = tips,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }
}
