package com.aicc.coldcall.feature.calling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicc.coldcall.core.model.Disposition
import com.aicc.coldcall.core.network.dto.CallLogCreateDto
import com.aicc.coldcall.data.api.CallLogRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PostCallUiState(
    val contactId: String = "",
    val contactName: String = "",
    val disposition: Disposition? = null,
    val followUpDate: String? = null,
    val summary: String = "",
    val durationSeconds: Int = 0,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel(assistedFactory = PostCallViewModel.Factory::class)
class PostCallViewModel @AssistedInject constructor(
    private val callLogRepository: CallLogRepository,
    @Assisted("contactId") private val contactId: String,
    @Assisted("contactName") private val contactName: String,
    @Assisted private val clock: () -> LocalDate,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("contactId") contactId: String,
            @Assisted("contactName") contactName: String,
            clock: () -> LocalDate,
        ): PostCallViewModel
    }

    private val _uiState = MutableStateFlow(
        PostCallUiState(contactId = contactId, contactName = contactName)
    )
    val uiState: StateFlow<PostCallUiState> = _uiState.asStateFlow()

    fun selectDisposition(disposition: Disposition) {
        val followUp = calculateFollowUp(disposition)
        _uiState.update {
            it.copy(disposition = disposition, followUpDate = followUp)
        }
    }

    fun updateSummary(summary: String) {
        _uiState.update { it.copy(summary = summary) }
    }

    fun updateFollowUpDate(date: String?) {
        _uiState.update { it.copy(followUpDate = date) }
    }

    fun saveCall() {
        val currentState = _uiState.value
        val disposition = currentState.disposition ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val dto = CallLogCreateDto(
                    contactId = currentState.contactId,
                    durationSeconds = currentState.durationSeconds,
                    disposition = disposition.name,
                    summary = currentState.summary.ifBlank { null },
                    nextFollowUp = currentState.followUpDate,
                )
                callLogRepository.logCall(dto)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.message)
                }
            }
        }
    }

    private fun calculateFollowUp(disposition: Disposition): String? {
        val today = clock()
        return when (disposition) {
            Disposition.Callback -> today.plusDays(1).toString()
            Disposition.NoAnswer -> today.plusDays(3).toString()
            Disposition.Voicemail -> today.plusDays(7).toString()
            else -> null
        }
    }
}
