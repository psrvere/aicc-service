package com.aicc.coldcall.data.api

import app.cash.turbine.test
import com.aicc.coldcall.core.database.ContactDao
import com.aicc.coldcall.core.database.ContactEntity
import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.core.model.ContactCreate
import com.aicc.coldcall.core.model.ContactUpdate
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.network.AiccApiService
import com.aicc.coldcall.core.network.dto.ContactCreateDto
import com.aicc.coldcall.core.network.dto.ContactDto
import com.aicc.coldcall.core.network.dto.ContactUpdateDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ContactRepositoryTest {

    private val api = mockk<AiccApiService>()
    private val contactDao = mockk<ContactDao>(relaxUnitFun = true)
    private lateinit var repository: ContactRepository

    private val contactDto = ContactDto(
        id = "1",
        name = "Alice",
        phone = "555-0001",
        business = "Acme",
        city = "NYC",
        industry = "Tech",
        dealStage = "Qualified",
        callCount = 3,
    )

    private val contactEntity = ContactEntity(
        id = "1",
        name = "Alice",
        phone = "555-0001",
        business = "Acme",
        city = "NYC",
        industry = "Tech",
        dealStage = DealStage.Qualified,
        lastCalled = null,
        callCount = 3,
        lastCallSummary = null,
        recordingLink = null,
        nextFollowUp = null,
        notes = null,
        createdAt = null,
    )

    @Before
    fun setUp() {
        repository = ContactRepository(api, contactDao)
    }

    @Test
    fun `getContacts fetches from API and caches in Room`() = runTest {
        coEvery { api.getContacts() } returns listOf(contactDto)
        every { contactDao.getAll() } returns flowOf(listOf(contactEntity))

        repository.getContacts().test {
            val contacts = awaitItem()
            assertEquals(1, contacts.size)
            assertEquals("Alice", contacts[0].name)
            assertEquals(DealStage.Qualified, contacts[0].dealStage)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { api.getContacts() }
        coVerify { contactDao.insertAll(any()) }
    }

    @Test
    fun `getContacts returns cached data when API fails`() = runTest {
        coEvery { api.getContacts() } throws RuntimeException("Network error")
        every { contactDao.getAll() } returns flowOf(listOf(contactEntity))

        repository.getContacts().test {
            val contacts = awaitItem()
            assertEquals(1, contacts.size)
            assertEquals("Alice", contacts[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getContact fetches single contact by ID`() = runTest {
        coEvery { api.getContact("1") } returns contactDto

        val contact = repository.getContact("1")
        assertEquals("Alice", contact.name)
        assertEquals(DealStage.Qualified, contact.dealStage)
    }

    @Test
    fun `createContact calls API and inserts in cache`() = runTest {
        val create = ContactCreate(name = "Bob", phone = "555-0002")
        val responseDto = contactDto.copy(id = "2", name = "Bob", phone = "555-0002")
        coEvery { api.createContact(any()) } returns responseDto

        val contact = repository.createContact(create)
        assertEquals("Bob", contact.name)

        coVerify { contactDao.insert(any()) }
    }

    @Test
    fun `updateContact calls API and updates cache`() = runTest {
        val update = ContactUpdate(name = "Alice Updated")
        val updatedDto = contactDto.copy(name = "Alice Updated")
        coEvery { api.updateContact(eq("1"), any()) } returns updatedDto

        val contact = repository.updateContact("1", update)
        assertEquals("Alice Updated", contact.name)

        coVerify { contactDao.insert(any()) }
    }

    @Test
    fun `deleteContact calls API and removes from cache`() = runTest {
        coEvery { api.deleteContact("1") } returns Unit

        repository.deleteContact("1")

        coVerify { api.deleteContact("1") }
        coVerify { contactDao.deleteById("1") }
    }

    @Test
    fun `searchContacts searches local cache`() = runTest {
        every { contactDao.searchByName("Ali") } returns flowOf(listOf(contactEntity))

        repository.searchContacts("Ali").test {
            val contacts = awaitItem()
            assertEquals(1, contacts.size)
            assertEquals("Alice", contacts[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
