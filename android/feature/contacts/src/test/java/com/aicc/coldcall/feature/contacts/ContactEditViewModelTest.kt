package com.aicc.coldcall.feature.contacts

import androidx.lifecycle.SavedStateHandle
import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.core.model.ContactCreate
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.data.api.ContactRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactEditViewModelTest {

    private val contactRepository = mockk<ContactRepository>()
    private val testDispatcher = StandardTestDispatcher()

    private val existingContact = Contact(
        id = "1",
        name = "Alice Smith",
        phone = "111-222-3333",
        business = "Acme Corp",
        city = "NYC",
        industry = "Tech",
        dealStage = DealStage.Qualified,
        notes = "VIP customer",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(contactId: String? = null): ContactEditViewModel {
        val savedStateHandle = SavedStateHandle(
            if (contactId != null) mapOf("contactId" to contactId) else emptyMap()
        )
        return ContactEditViewModel(savedStateHandle, contactRepository)
    }

    @Test
    fun `save new contact calls createContact`() = runTest {
        val created = existingContact.copy(id = "new-1")
        coEvery { contactRepository.createContact(any()) } returns created

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onNameChanged("Alice Smith")
        vm.onPhoneChanged("111-222-3333")
        vm.onBusinessChanged("Acme Corp")
        vm.onCityChanged("NYC")
        vm.onIndustryChanged("Tech")
        vm.save()
        advanceUntilIdle()

        coVerify {
            contactRepository.createContact(
                ContactCreate(
                    name = "Alice Smith",
                    phone = "111-222-3333",
                    business = "Acme Corp",
                    city = "NYC",
                    industry = "Tech",
                    dealStage = DealStage.New,
                    notes = null,
                    nextFollowUp = null,
                )
            )
        }
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun `save existing contact calls updateContact`() = runTest {
        coEvery { contactRepository.getContact("1") } returns existingContact
        coEvery { contactRepository.updateContact(any(), any()) } returns existingContact.copy(name = "Alice Updated")

        val vm = createViewModel("1")
        advanceUntilIdle()

        vm.onNameChanged("Alice Updated")
        vm.save()
        advanceUntilIdle()

        coVerify { contactRepository.updateContact("1", any()) }
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun `validation name required`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onPhoneChanged("111-222-3333")
        vm.save()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSaved)
        assertNotNull(vm.uiState.value.nameError)
    }

    @Test
    fun `validation phone required`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onNameChanged("Alice Smith")
        vm.save()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSaved)
        assertNotNull(vm.uiState.value.phoneError)
    }

    @Test
    fun `loads existing contact fields for editing`() = runTest {
        coEvery { contactRepository.getContact("1") } returns existingContact

        val vm = createViewModel("1")
        advanceUntilIdle()

        assertEquals("Alice Smith", vm.uiState.value.name)
        assertEquals("111-222-3333", vm.uiState.value.phone)
        assertEquals("Acme Corp", vm.uiState.value.business)
        assertEquals("NYC", vm.uiState.value.city)
        assertEquals("Tech", vm.uiState.value.industry)
        assertEquals(DealStage.Qualified, vm.uiState.value.dealStage)
    }

    @Test
    fun `isEditMode is true when contactId is provided`() = runTest {
        coEvery { contactRepository.getContact("1") } returns existingContact

        val vm = createViewModel("1")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isEditMode)
    }

    @Test
    fun `isEditMode is false for new contact`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isEditMode)
    }
}
