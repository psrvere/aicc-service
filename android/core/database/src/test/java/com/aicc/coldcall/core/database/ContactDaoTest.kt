package com.aicc.coldcall.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aicc.coldcall.core.model.DealStage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContactDaoTest {

    private lateinit var database: AiccDatabase
    private lateinit var contactDao: ContactDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AiccDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        contactDao = database.contactDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createContact(
        id: String = "c1",
        name: String = "Alice Smith",
        phone: String = "5551234567",
        business: String? = null,
        city: String? = null,
        industry: String? = null,
        dealStage: DealStage = DealStage.New,
        callCount: Int = 0
    ) = ContactEntity(
        id = id,
        name = name,
        phone = phone,
        business = business,
        city = city,
        industry = industry,
        dealStage = dealStage,
        lastCalled = null,
        callCount = callCount,
        lastCallSummary = null,
        recordingLink = null,
        nextFollowUp = null,
        notes = null,
        createdAt = null
    )

    @Test
    fun `insert and query all contacts`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Alice"))
        contactDao.insert(createContact(id = "c2", name = "Bob"))

        val contacts = contactDao.getAll().first()

        assertEquals(2, contacts.size)
    }

    @Test
    fun `query all returns contacts ordered by name`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Charlie"))
        contactDao.insert(createContact(id = "c2", name = "Alice"))
        contactDao.insert(createContact(id = "c3", name = "Bob"))

        val contacts = contactDao.getAll().first()

        assertEquals("Alice", contacts[0].name)
        assertEquals("Bob", contacts[1].name)
        assertEquals("Charlie", contacts[2].name)
    }

    @Test
    fun `query by id returns correct contact`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Alice"))
        contactDao.insert(createContact(id = "c2", name = "Bob"))

        val contact = contactDao.getById("c1")

        assertNotNull(contact)
        assertEquals("Alice", contact!!.name)
    }

    @Test
    fun `query by id returns null for missing contact`() = runTest {
        val contact = contactDao.getById("nonexistent")
        assertNull(contact)
    }

    @Test
    fun `update contact persists changes`() = runTest {
        val original = createContact(id = "c1", name = "Alice", phone = "5551111111")
        contactDao.insert(original)

        val updated = original.copy(name = "Alice Updated", phone = "5552222222")
        contactDao.update(updated)

        val result = contactDao.getById("c1")
        assertEquals("Alice Updated", result!!.name)
        assertEquals("5552222222", result.phone)
    }

    @Test
    fun `delete contact removes it`() = runTest {
        val contact = createContact(id = "c1", name = "Alice")
        contactDao.insert(contact)

        contactDao.delete(contact)

        val result = contactDao.getById("c1")
        assertNull(result)
    }

    @Test
    fun `delete by id removes contact`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Alice"))

        contactDao.deleteById("c1")

        val result = contactDao.getById("c1")
        assertNull(result)
    }

    @Test
    fun `search by name matches partial name`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Alice Smith"))
        contactDao.insert(createContact(id = "c2", name = "Bob Jones"))
        contactDao.insert(createContact(id = "c3", name = "Alice Cooper"))

        val results = contactDao.searchByName("Alice").first()

        assertEquals(2, results.size)
        assertEquals("Alice Cooper", results[0].name)
        assertEquals("Alice Smith", results[1].name)
    }

    @Test
    fun `search by name returns empty for no match`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Alice"))

        val results = contactDao.searchByName("Zara").first()

        assertEquals(0, results.size)
    }

    @Test
    fun `filter by deal stage returns matching contacts`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Alice", dealStage = DealStage.New))
        contactDao.insert(createContact(id = "c2", name = "Bob", dealStage = DealStage.Qualified))
        contactDao.insert(createContact(id = "c3", name = "Carol", dealStage = DealStage.New))

        val results = contactDao.filterByDealStage(DealStage.New).first()

        assertEquals(2, results.size)
    }

    @Test
    fun `filter by city returns matching contacts`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Alice", city = "Austin"))
        contactDao.insert(createContact(id = "c2", name = "Bob", city = "Denver"))
        contactDao.insert(createContact(id = "c3", name = "Carol", city = "Austin"))

        val results = contactDao.filterByCity("Austin").first()

        assertEquals(2, results.size)
    }

    @Test
    fun `filter by industry returns matching contacts`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Alice", industry = "Tech"))
        contactDao.insert(createContact(id = "c2", name = "Bob", industry = "Finance"))
        contactDao.insert(createContact(id = "c3", name = "Carol", industry = "Tech"))

        val results = contactDao.filterByIndustry("Tech").first()

        assertEquals(2, results.size)
    }

    @Test
    fun `insertAll inserts multiple contacts`() = runTest {
        val contacts = listOf(
            createContact(id = "c1", name = "Alice"),
            createContact(id = "c2", name = "Bob"),
            createContact(id = "c3", name = "Carol")
        )

        contactDao.insertAll(contacts)

        val results = contactDao.getAll().first()
        assertEquals(3, results.size)
    }

    @Test
    fun `insert with same id replaces existing contact`() = runTest {
        contactDao.insert(createContact(id = "c1", name = "Alice"))
        contactDao.insert(createContact(id = "c1", name = "Alice Updated"))

        val result = contactDao.getById("c1")
        assertEquals("Alice Updated", result!!.name)

        val all = contactDao.getAll().first()
        assertEquals(1, all.size)
    }
}
