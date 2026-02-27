package com.aicc.coldcall.feature.contacts

import androidx.lifecycle.SavedStateHandle
import com.aicc.coldcall.core.model.CallLog
import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition
import com.aicc.coldcall.data.api.CallLogRepository
import com.aicc.coldcall.data.api.ContactRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactDetailViewModelTest {

    private val contactRepository = mockk<ContactRepository>()
    private val callLogRepository = mockk<CallLogRepository>()
    private val testDispatcher = StandardTestDispatcher()

    private val sampleContact = Contact(
        id = "1",
        name = "Alice Smith",
        phone = "111-222-3333",
        business = "Acme Corp",
        city = "NYC",
        industry = "Tech",
        dealStage = DealStage.Qualified,
        callCount = 3,
        lastCallSummary = "Discussed pricing",
        notes = "Interested in premium plan",
    )

    private val sampleCallLogs = listOf(
        CallLog(id = "cl1", contactId = "1", timestamp = "2024-01-15T10:00:00", durationSeconds = 120, disposition = Disposition.Connected, summary = "Discussed pricing", dealStage = DealStage.Qualified),
        CallLog(id = "cl2", contactId = "1", timestamp = "2024-01-10T14:30:00", durationSeconds = 60, disposition = Disposition.NoAnswer, dealStage = DealStage.Contacted),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(contactId: String = "1"): ContactDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("contactId" to contactId))
        return ContactDetailViewModel(savedStateHandle, contactRepository, callLogRepository)
    }

    @Test
    fun `loads contact by ID`() = runTest {
        coEvery { contactRepository.getContact("1") } returns sampleContact
        every { callLogRepository.getByContactId("1") } returns flowOf(sampleCallLogs)

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(sampleContact, vm.uiState.value.contact)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `loads call history for contact`() = runTest {
        coEvery { contactRepository.getContact("1") } returns sampleContact
        every { callLogRepository.getByContactId("1") } returns flowOf(sampleCallLogs)

        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.callHistory.size)
        assertEquals("cl1", vm.uiState.value.callHistory[0].id)
    }

    @Test
    fun `delete triggers repository delete`() = runTest {
        coEvery { contactRepository.getContact("1") } returns sampleContact
        every { callLogRepository.getByContactId("1") } returns flowOf(emptyList())
        coEvery { contactRepository.deleteContact("1") } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.deleteContact()
        advanceUntilIdle()

        coVerify { contactRepository.deleteContact("1") }
        assertTrue(vm.uiState.value.isDeleted)
    }

    @Test
    fun `error state when contact not found`() = runTest {
        coEvery { contactRepository.getContact("999") } throws RuntimeException("Not found")
        every { callLogRepository.getByContactId("999") } returns flowOf(emptyList())

        val vm = createViewModel("999")
        advanceUntilIdle()

        assertNull(vm.uiState.value.contact)
        assertEquals("Not found", vm.uiState.value.error)
    }
}
