package com.aicc.coldcall.feature.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicc.coldcall.core.model.CallLog
import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.data.api.CallLogRepository
import com.aicc.coldcall.data.api.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactDetailUiState(
    val contact: Contact? = null,
    val callHistory: List<CallLog> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
) : ViewModel() {

    private val contactId: String = checkNotNull(savedStateHandle["contactId"])

    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    init {
        loadContact()
        loadCallHistory()
    }

    private fun loadContact() {
        viewModelScope.launch {
            try {
                val contact = contactRepository.getContact(contactId)
                _uiState.update { it.copy(contact = contact, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadCallHistory() {
        viewModelScope.launch {
            callLogRepository.getByContactId(contactId)
                .catch { /* ignore — call history is supplementary */ }
                .collect { logs ->
                    _uiState.update { it.copy(callHistory = logs) }
                }
        }
    }

    fun deleteContact() {
        viewModelScope.launch {
            try {
                contactRepository.deleteContact(contactId)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
