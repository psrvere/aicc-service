package com.aicc.coldcall.feature.contacts

import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.data.api.ContactRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactListViewModelTest {

    private val contactRepository = mockk<ContactRepository>()
    private val testDispatcher = StandardTestDispatcher()

    private val sampleContacts = listOf(
        Contact(id = "1", name = "Alice Smith", phone = "111", business = "Acme", city = "NYC", industry = "Tech", dealStage = DealStage.New),
        Contact(id = "2", name = "Bob Jones", phone = "222", business = "Beta", city = "LA", industry = "Finance", dealStage = DealStage.Contacted),
        Contact(id = "3", name = "Carol White", phone = "333", business = "Gamma", city = "NYC", industry = "Tech", dealStage = DealStage.Qualified),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads contacts from repository`() = runTest {
        every { contactRepository.getContacts() } returns flowOf(sampleContacts)

        val vm = ContactListViewModel(contactRepository)
        advanceUntilIdle()

        assertEquals(sampleContacts, vm.uiState.value.contacts)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `search filters contacts locally`() = runTest {
        every { contactRepository.getContacts() } returns flowOf(sampleContacts)

        val vm = ContactListViewModel(contactRepository)
        advanceUntilIdle()

        vm.onSearchQueryChanged("alice")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.contacts.size)
        assertEquals("Alice Smith", vm.uiState.value.contacts[0].name)
    }

    @Test
    fun `filter by deal stage`() = runTest {
        every { contactRepository.getContacts() } returns flowOf(sampleContacts)

        val vm = ContactListViewModel(contactRepository)
        advanceUntilIdle()

        vm.onDealStageFilterChanged(DealStage.New)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.contacts.size)
        assertEquals(DealStage.New, vm.uiState.value.contacts[0].dealStage)
    }

    @Test
    fun `clear deal stage filter shows all contacts`() = runTest {
        every { contactRepository.getContacts() } returns flowOf(sampleContacts)

        val vm = ContactListViewModel(contactRepository)
        advanceUntilIdle()

        vm.onDealStageFilterChanged(DealStage.New)
        advanceUntilIdle()
        vm.onDealStageFilterChanged(null)
        advanceUntilIdle()

        assertEquals(3, vm.uiState.value.contacts.size)
    }

    @Test
    fun `pull to refresh fetches fresh data`() = runTest {
        every { contactRepository.getContacts() } returns flowOf(sampleContacts)

        val vm = ContactListViewModel(contactRepository)
        advanceUntilIdle()

        vm.refresh()
        advanceUntilIdle()

        assertEquals(sampleContacts, vm.uiState.value.contacts)
        assertFalse(vm.uiState.value.isRefreshing)
    }

    @Test
    fun `error state on repository failure`() = runTest {
        every { contactRepository.getContacts() } returns flow { throw RuntimeException("Network error") }

        val vm = ContactListViewModel(contactRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isRefreshing)
        assertEquals("Network error", vm.uiState.value.error)
    }

    @Test
    fun `search query is preserved in state`() = runTest {
        every { contactRepository.getContacts() } returns flowOf(sampleContacts)

        val vm = ContactListViewModel(contactRepository)
        advanceUntilIdle()

        vm.onSearchQueryChanged("bob")
        advanceUntilIdle()

        assertEquals("bob", vm.uiState.value.searchQuery)
    }

    @Test
    fun `deal stage filter is preserved in state`() = runTest {
        every { contactRepository.getContacts() } returns flowOf(sampleContacts)

        val vm = ContactListViewModel(contactRepository)
        advanceUntilIdle()

        vm.onDealStageFilterChanged(DealStage.Contacted)
        advanceUntilIdle()

        assertEquals(DealStage.Contacted, vm.uiState.value.dealStageFilter)
    }
}
