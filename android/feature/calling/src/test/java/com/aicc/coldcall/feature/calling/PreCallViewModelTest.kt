package com.aicc.coldcall.feature.calling

import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.data.api.ContactRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreCallViewModelTest {

    private val contactRepository = mockk<ContactRepository>()
    private val testDispatcher = StandardTestDispatcher()

    private val sampleContact = Contact(
        id = "c1",
        name = "Alice Johnson",
        phone = "+1234567890",
        business = "Acme Corp",
        dealStage = DealStage.Contacted,
        lastCallSummary = "Discussed pricing options",
        notes = "Prefers morning calls",
        callCount = 2,
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
    fun `loads contact by ID from repository`() = runTest {
        coEvery { contactRepository.getContact("c1") } returns sampleContact

        val vm = PreCallViewModel(contactRepository, "c1")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNotNull(state.contact)
        assertEquals("Alice Johnson", state.contact?.name)
        assertEquals("+1234567890", state.contact?.phone)
        assertFalse(state.isLoading)
    }

    @Test
    fun `exposes contact details`() = runTest {
        coEvery { contactRepository.getContact("c1") } returns sampleContact

        val vm = PreCallViewModel(contactRepository, "c1")
        advanceUntilIdle()

        val contact = vm.uiState.value.contact!!
        assertEquals("Acme Corp", contact.business)
        assertEquals(DealStage.Contacted, contact.dealStage)
        assertEquals("Discussed pricing options", contact.lastCallSummary)
        assertEquals("Prefers morning calls", contact.notes)
    }

    @Test
    fun `error state when contact not found`() = runTest {
        coEvery { contactRepository.getContact("bad-id") } throws RuntimeException("Contact not found")

        val vm = PreCallViewModel(contactRepository, "bad-id")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.contact)
        assertEquals("Contact not found", state.error)
        assertFalse(state.isLoading)
    }
}
