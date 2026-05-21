package com.plantdoctor.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantdoctor.data.local.entity.DiagnosisEntity
import com.plantdoctor.data.repository.DiagnosisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val diagnoses: List<DiagnosisEntity> = emptyList(),
    val filteredDiagnoses: List<DiagnosisEntity> = emptyList(),
    val selectedFilter: SeverityFilter = SeverityFilter.ALL,
    val isLoading: Boolean = true
)

enum class SeverityFilter {
    ALL, MILD, MODERATE, SEVERE
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val diagnosisRepository: DiagnosisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadDiagnoses()
    }

    private fun loadDiagnoses() {
        viewModelScope.launch {
            diagnosisRepository.getAllDiagnoses().collect { diagnoses ->
                val currentFilter = _uiState.value.selectedFilter
                _uiState.value = HistoryUiState(
                    diagnoses = diagnoses,
                    filteredDiagnoses = applyFilter(diagnoses, currentFilter),
                    selectedFilter = currentFilter,
                    isLoading = false
                )
            }
        }
    }

    fun setFilter(filter: SeverityFilter) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            selectedFilter = filter,
            filteredDiagnoses = applyFilter(currentState.diagnoses, filter)
        )
    }

    private fun applyFilter(
        diagnoses: List<DiagnosisEntity>,
        filter: SeverityFilter
    ): List<DiagnosisEntity> {
        return when (filter) {
            SeverityFilter.ALL -> diagnoses
            else -> diagnoses.filter { it.severity.equals(filter.name, ignoreCase = true) }
        }
    }
}
