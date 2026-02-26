package com.aicc.coldcall.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallLogDaoTest {

    private lateinit var database: AiccDatabase
    private lateinit var callLogDao: CallLogDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AiccDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        callLogDao = database.callLogDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createCallLog(
        id: String = "log1",
        contactId: String = "c1",
        timestamp: String = "2025-01-15T10:30:00",
        durationSeconds: Int = 180,
        disposition: Disposition = Disposition.Connected,
        summary: String? = null,
        dealStage: DealStage = DealStage.Qualified
    ) = CallLogEntity(
        id = id,
        contactId = contactId,
        timestamp = timestamp,
        durationSeconds = durationSeconds,
        disposition = disposition,
        summary = summary,
        dealStage = dealStage,
        recordingUrl = null,
        transcript = null
    )

    @Test
    fun `insert and query call log by contact id`() = runTest {
        callLogDao.insert(createCallLog(id = "log1", contactId = "c1"))
        callLogDao.insert(createCallLog(id = "log2", contactId = "c1"))
        callLogDao.insert(createCallLog(id = "log3", contactId = "c2"))

        val results = callLogDao.getByContactId("c1").first()

        assertEquals(2, results.size)
        results.forEach { assertEquals("c1", it.contactId) }
    }

    @Test
    fun `query by contact id returns empty for no logs`() = runTest {
        val results = callLogDao.getByContactId("nonexistent").first()
        assertEquals(0, results.size)
    }

    @Test
    fun `query by contact id returns logs ordered by timestamp descending`() = runTest {
        callLogDao.insert(createCallLog(id = "log1", contactId = "c1", timestamp = "2025-01-10T10:00:00"))
        callLogDao.insert(createCallLog(id = "log2", contactId = "c1", timestamp = "2025-01-15T10:00:00"))
        callLogDao.insert(createCallLog(id = "log3", contactId = "c1", timestamp = "2025-01-12T10:00:00"))

        val results = callLogDao.getByContactId("c1").first()

        assertEquals("log2", results[0].id) // Jan 15 (most recent)
        assertEquals("log3", results[1].id) // Jan 12
        assertEquals("log1", results[2].id) // Jan 10 (oldest)
    }

    @Test
    fun `query today's call logs by date prefix`() = runTest {
        callLogDao.insert(createCallLog(id = "log1", timestamp = "2025-01-15T10:00:00"))
        callLogDao.insert(createCallLog(id = "log2", timestamp = "2025-01-15T14:30:00"))
        callLogDao.insert(createCallLog(id = "log3", timestamp = "2025-01-16T09:00:00"))

        val results = callLogDao.getByDate("2025-01-15").first()

        assertEquals(2, results.size)
        results.forEach { assert(it.timestamp.startsWith("2025-01-15")) }
    }

    @Test
    fun `query by date returns empty when no logs match`() = runTest {
        callLogDao.insert(createCallLog(id = "log1", timestamp = "2025-01-15T10:00:00"))

        val results = callLogDao.getByDate("2025-01-20").first()

        assertEquals(0, results.size)
    }

    @Test
    fun `insert with same id replaces existing log`() = runTest {
        callLogDao.insert(createCallLog(id = "log1", durationSeconds = 100))
        callLogDao.insert(createCallLog(id = "log1", durationSeconds = 200))

        val results = callLogDao.getByContactId("c1").first()

        assertEquals(1, results.size)
        assertEquals(200, results[0].durationSeconds)
    }

    @Test
    fun `all dispositions persist correctly`() = runTest {
        Disposition.entries.forEachIndexed { index, disposition ->
            callLogDao.insert(
                createCallLog(
                    id = "log$index",
                    disposition = disposition,
                    contactId = "dispositions"
                )
            )
        }

        val results = callLogDao.getByContactId("dispositions").first()

        assertEquals(Disposition.entries.size, results.size)
        val storedDispositions = results.map { it.disposition }.toSet()
        assertEquals(Disposition.entries.toSet(), storedDispositions)
    }
}
