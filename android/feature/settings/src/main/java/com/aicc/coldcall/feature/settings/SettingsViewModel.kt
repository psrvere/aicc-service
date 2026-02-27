package com.aicc.coldcall.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val backendUrl: String = "",
    val isSignedIn: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val serverConfigStore: ServerConfigStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val url = serverConfigStore.getBaseUrl()
            val token = serverConfigStore.getToken()
            _uiState.update {
                it.copy(
                    backendUrl = url,
                    isSignedIn = token != null,
                )
            }
        }
    }

    fun saveSettings(url: String, token: String) {
        viewModelScope.launch {
            serverConfigStore.saveBackendUrl(url)
            if (token.isNotBlank()) {
                serverConfigStore.saveToken(token)
            }
            val savedToken = serverConfigStore.getToken()
            _uiState.update {
                it.copy(
                    backendUrl = url,
                    isSignedIn = savedToken != null,
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            serverConfigStore.clearToken()
            _uiState.update { it.copy(isSignedIn = false) }
        }
    }
}
