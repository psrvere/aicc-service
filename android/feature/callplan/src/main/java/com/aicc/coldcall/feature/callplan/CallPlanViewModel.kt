package com.aicc.coldcall.feature.callplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicc.coldcall.core.model.CallPlanItem
import com.aicc.coldcall.data.api.CallPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallPlanUiState(
    val planItems: List<CallPlanItem> = emptyList(),
    val completedIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val followUpCount: Int = 0,
    val newContactCount: Int = 0,
)

@HiltViewModel
class CallPlanViewModel @Inject constructor(
    private val callPlanRepository: CallPlanRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallPlanUiState())
    val uiState: StateFlow<CallPlanUiState> = _uiState.asStateFlow()

    init {
        loadPlan()
    }

    private fun loadPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = callPlanRepository.getTodayPlan()
                val followUps = items.count { it.reason.contains("Follow-up", ignoreCase = true) }
                _uiState.update {
                    it.copy(
                        planItems = items,
                        isLoading = false,
                        followUpCount = followUps,
                        newContactCount = items.size - followUps,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun markAsCompleted(contactId: String) {
        _uiState.update {
            it.copy(completedIds = it.completedIds + contactId)
        }
    }

    fun refresh() {
        loadPlan()
    }
}
