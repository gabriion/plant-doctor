package com.plantdoctor.ui.camera

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

data class CameraUiState(
    val capturedImageUri: Uri? = null,
    val isPreviewMode: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onImageCaptured(uri: Uri) {
        _uiState.value = CameraUiState(
            capturedImageUri = uri,
            isPreviewMode = true
        )
    }

    fun onRetake() {
        _uiState.value = CameraUiState()
    }

    fun onError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun createImageFile(cacheDir: File): File {
        val timestamp = System.currentTimeMillis()
        return File(cacheDir, "plant_capture_$timestamp.jpg")
    }
}
