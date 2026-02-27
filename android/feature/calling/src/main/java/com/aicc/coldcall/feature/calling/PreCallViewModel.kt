package com.aicc.coldcall.feature.calling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.data.api.ContactRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreCallUiState(
    val contact: Contact? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel(assistedFactory = PreCallViewModel.Factory::class)
class PreCallViewModel @AssistedInject constructor(
    private val contactRepository: ContactRepository,
    @Assisted private val contactId: String,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(contactId: String): PreCallViewModel
    }

    private val _uiState = MutableStateFlow(PreCallUiState())
    val uiState: StateFlow<PreCallUiState> = _uiState.asStateFlow()

    init {
        loadContact()
    }

    private fun loadContact() {
        viewModelScope.launch {
            try {
                val contact = contactRepository.getContact(contactId)
                _uiState.update {
                    it.copy(contact = contact, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
}
