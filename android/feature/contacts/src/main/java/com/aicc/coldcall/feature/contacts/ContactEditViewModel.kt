package com.aicc.coldcall.feature.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicc.coldcall.core.model.ContactCreate
import com.aicc.coldcall.core.model.ContactUpdate
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.data.api.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject

data class ContactEditUiState(
    val name: String = "",
    val phone: String = "",
    val business: String = "",
    val city: String = "",
    val industry: String = "",
    val dealStage: DealStage = DealStage.New,
    val notes: String = "",
    val nextFollowUp: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null,
    val phoneError: String? = null,
    val error: String? = null,
)

@HiltViewModel
class ContactEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
) : ViewModel() {

    private val contactId: String? = savedStateHandle["contactId"]

    private val isSubmitting = AtomicBoolean(false)
    private val _uiState = MutableStateFlow(ContactEditUiState())
    val uiState: StateFlow<ContactEditUiState> = _uiState.asStateFlow()

    init {
        if (contactId != null) {
            loadContact(contactId)
        }
    }

    private fun loadContact(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val contact = contactRepository.getContact(id)
                _uiState.update {
                    it.copy(
                        name = contact.name,
                        phone = contact.phone,
                        business = contact.business ?: "",
                        city = contact.city ?: "",
                        industry = contact.industry ?: "",
                        dealStage = contact.dealStage,
                        notes = contact.notes ?: "",
                        nextFollowUp = contact.nextFollowUp ?: "",
                        isEditMode = true,
                        isLoading = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = null) }
    }

    fun onBusinessChanged(business: String) {
        _uiState.update { it.copy(business = business) }
    }

    fun onCityChanged(city: String) {
        _uiState.update { it.copy(city = city) }
    }

    fun onIndustryChanged(industry: String) {
        _uiState.update { it.copy(industry = industry) }
    }

    fun onDealStageChanged(dealStage: DealStage) {
        _uiState.update { it.copy(dealStage = dealStage) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onNextFollowUpChanged(nextFollowUp: String) {
        _uiState.update { it.copy(nextFollowUp = nextFollowUp) }
    }

    fun save() {
        if (!isSubmitting.compareAndSet(false, true)) return

        val state = _uiState.value
        var hasError = false

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(phoneError = "Phone is required") }
            hasError = true
        }
        if (hasError) {
            isSubmitting.set(false)
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                if (contactId != null) {
                    contactRepository.updateContact(
                        contactId,
                        ContactUpdate(
                            name = state.name,
                            phone = state.phone,
                            business = state.business.ifBlank { null },
                            city = state.city.ifBlank { null },
                            industry = state.industry.ifBlank { null },
                            dealStage = state.dealStage,
                            notes = state.notes.ifBlank { null },
                            nextFollowUp = state.nextFollowUp.ifBlank { null },
                        )
                    )
                } else {
                    contactRepository.createContact(
                        ContactCreate(
                            name = state.name,
                            phone = state.phone,
                            business = state.business.ifBlank { null },
                            city = state.city.ifBlank { null },
                            industry = state.industry.ifBlank { null },
                            dealStage = state.dealStage,
                            notes = state.notes.ifBlank { null },
                            nextFollowUp = state.nextFollowUp.ifBlank { null },
                        )
                    )
                }
                _uiState.update { it.copy(isLoading = false, isSaved = true, error = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            } finally {
                isSubmitting.set(false)
            }
        }
    }
}
