package com.aicc.coldcall.feature.calling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicc.coldcall.core.model.AISummary
import com.aicc.coldcall.core.model.Disposition
import com.aicc.coldcall.core.network.dto.CallLogCreateDto
import com.aicc.coldcall.data.api.CallLogRepository
import com.aicc.coldcall.data.recording.AiPipelineRepository
import com.aicc.coldcall.data.recording.RecordingUploader
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

enum class AiPipelineStatus {
    UPLOADING,
    TRANSCRIBING,
    SUMMARIZING,
    COMPLETED,
    ERROR,
}

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
    val aiPipelineStatus: AiPipelineStatus? = null,
    val aiSummary: AISummary? = null,
    val recordingUrl: String? = null,
    val transcript: String? = null,
    val aiError: String? = null,
)

@HiltViewModel(assistedFactory = PostCallViewModel.Factory::class)
class PostCallViewModel @AssistedInject constructor(
    private val callLogRepository: CallLogRepository,
    private val aiPipelineRepository: AiPipelineRepository,
    private val recordingUploader: RecordingUploader,
    @Assisted("contactId") private val contactId: String,
    @Assisted("contactName") private val contactName: String,
    @Assisted private val clock: () -> LocalDate,
    @Assisted("recordingFilePath") private val recordingFilePath: String?,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("contactId") contactId: String,
            @Assisted("contactName") contactName: String,
            clock: () -> LocalDate,
            @Assisted("recordingFilePath") recordingFilePath: String?,
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

                if (disposition == Disposition.Connected && recordingFilePath != null) {
                    runAiPipeline(recordingFilePath)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.message)
                }
            }
        }
    }

    private fun runAiPipeline(filePath: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(aiPipelineStatus = AiPipelineStatus.UPLOADING, aiError = null) }

                val url = recordingUploader.uploadFile(filePath)
                _uiState.update { it.copy(recordingUrl = url, aiPipelineStatus = AiPipelineStatus.TRANSCRIBING) }

                val transcript = aiPipelineRepository.transcribe(url)
                _uiState.update { it.copy(transcript = transcript, aiPipelineStatus = AiPipelineStatus.SUMMARIZING) }

                val summary = aiPipelineRepository.summarize(contactId, transcript)
                _uiState.update {
                    it.copy(
                        aiSummary = summary,
                        aiPipelineStatus = AiPipelineStatus.COMPLETED,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(aiPipelineStatus = AiPipelineStatus.ERROR, aiError = e.message)
                }
            }
        }
    }

    fun updateAiSummary(text: String) {
        val current = _uiState.value.aiSummary ?: return
        _uiState.update {
            it.copy(aiSummary = current.copy(summary = text))
        }
    }

    fun confirmAiSummary() {
        val currentState = _uiState.value
        val aiSummary = currentState.aiSummary ?: return
        val disposition = currentState.disposition ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val dto = CallLogCreateDto(
                    contactId = currentState.contactId,
                    durationSeconds = currentState.durationSeconds,
                    disposition = disposition.name,
                    summary = aiSummary.summary,
                    dealStage = aiSummary.recommendedDealStage.name,
                    recordingUrl = currentState.recordingUrl,
                    transcript = currentState.transcript,
                    nextFollowUp = currentState.followUpDate,
                )
                callLogRepository.logCall(dto)
                _uiState.update { it.copy(isSaving = false, aiPipelineStatus = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, aiError = e.message) }
            }
        }
    }

    fun regenerateSummary() {
        val transcript = _uiState.value.transcript ?: return
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(aiPipelineStatus = AiPipelineStatus.SUMMARIZING, aiError = null) }
                val summary = aiPipelineRepository.summarize(contactId, transcript)
                _uiState.update {
                    it.copy(aiSummary = summary, aiPipelineStatus = AiPipelineStatus.COMPLETED)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(aiPipelineStatus = AiPipelineStatus.ERROR, aiError = e.message)
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
