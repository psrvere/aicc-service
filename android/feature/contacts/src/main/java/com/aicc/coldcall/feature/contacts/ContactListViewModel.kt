package com.aicc.coldcall.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.data.api.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactListUiState(
    val allContacts: List<Contact> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    val dealStageFilter: DealStage? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactListUiState())
    val uiState: StateFlow<ContactListUiState> = _uiState.asStateFlow()

    private var contactsJob: Job? = null

    init {
        loadContacts()
    }

    private fun loadContacts() {
        contactsJob?.cancel()
        contactsJob = viewModelScope.launch {
            contactRepository.getContacts()
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = e.message)
                    }
                }
                .collect { contacts ->
                    _uiState.update { state ->
                        state.copy(
                            allContacts = contacts,
                            contacts = applyFilters(contacts, state.searchQuery, state.dealStageFilter),
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                contacts = applyFilters(state.allContacts, query, state.dealStageFilter),
            )
        }
    }

    fun onDealStageFilterChanged(dealStage: DealStage?) {
        _uiState.update { state ->
            state.copy(
                dealStageFilter = dealStage,
                contacts = applyFilters(state.allContacts, state.searchQuery, dealStage),
            )
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadContacts()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun applyFilters(
        contacts: List<Contact>,
        searchQuery: String,
        dealStageFilter: DealStage?,
    ): List<Contact> {
        var filtered = contacts
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true) ||
                    it.business?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        if (dealStageFilter != null) {
            filtered = filtered.filter { it.dealStage == dealStageFilter }
        }
        return filtered
    }
}
