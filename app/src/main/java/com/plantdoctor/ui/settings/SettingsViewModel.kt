package com.plantdoctor.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AiProvider(val displayName: String) {
    OPENAI("OpenAI"),
    GEMINI("Gemini")
}

data class SettingsUiState(
    val provider: AiProvider = AiProvider.GEMINI,
    val apiKey: String = "",
    val isTestingConnection: Boolean = false,
    val connectionTestResult: ConnectionTestResult? = null
)

sealed class ConnectionTestResult {
    data object Success : ConnectionTestResult()
    data class Failure(val message: String) : ConnectionTestResult()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "plant_doctor_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val providerName = encryptedPrefs.getString("ai_provider", AiProvider.GEMINI.name)
        val provider = try {
            AiProvider.valueOf(providerName ?: AiProvider.GEMINI.name)
        } catch (_: Exception) {
            AiProvider.GEMINI
        }

        val apiKey = encryptedPrefs.getString("api_key_${provider.name}", "") ?: ""

        _uiState.value = SettingsUiState(
            provider = provider,
            apiKey = apiKey
        )
    }

    fun setProvider(provider: AiProvider) {
        encryptedPrefs.edit().putString("ai_provider", provider.name).apply()
        val apiKey = encryptedPrefs.getString("api_key_${provider.name}", "") ?: ""
        _uiState.value = _uiState.value.copy(
            provider = provider,
            apiKey = apiKey,
            connectionTestResult = null
        )
    }

    fun setApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(apiKey = apiKey)
    }

    fun saveApiKey() {
        val state = _uiState.value
        encryptedPrefs.edit()
            .putString("api_key_${state.provider.name}", state.apiKey)
            .apply()
    }

    fun getApiKey(): String {
        val provider = _uiState.value.provider
        return encryptedPrefs.getString("api_key_${provider.name}", "") ?: ""
    }

    fun clearApiKey() {
        val provider = _uiState.value.provider
        encryptedPrefs.edit().remove("api_key_${provider.name}").apply()
        _uiState.value = _uiState.value.copy(apiKey = "", connectionTestResult = null)
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isTestingConnection = true,
                connectionTestResult = null
            )
            try {
                val apiKey = _uiState.value.apiKey
                if (apiKey.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isTestingConnection = false,
                        connectionTestResult = ConnectionTestResult.Failure("API key is empty")
                    )
                    return@launch
                }

                // Simple validation - check key format
                val isValid = when (_uiState.value.provider) {
                    AiProvider.OPENAI -> apiKey.startsWith("sk-")
                    AiProvider.GEMINI -> apiKey.length > 10
                }

                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionTestResult = if (isValid) {
                        ConnectionTestResult.Success
                    } else {
                        ConnectionTestResult.Failure("Invalid API key format")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    connectionTestResult = ConnectionTestResult.Failure(
                        e.message ?: "Connection test failed"
                    )
                )
            }
        }
    }

    fun clearAllData() {
        encryptedPrefs.edit().clear().apply()
        _uiState.value = SettingsUiState()
    }
}
