package com.aicc.coldcall.core.network

import com.aicc.coldcall.core.network.dto.CallLogCreateDto
import com.aicc.coldcall.core.network.dto.ContactCreateDto
import com.aicc.coldcall.core.network.dto.ContactUpdateDto
import com.aicc.coldcall.core.network.dto.SummarizeRequestDto
import com.aicc.coldcall.core.network.dto.TranscribeRequestDto
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AiccApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: AiccApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val moshi = MoshiFactory.create()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AiccApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // --- Contacts ---

    @Test
    fun `GET contacts hits correct path`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        api.getContacts()

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/contacts", request.path)
    }

    @Test
    fun `GET contacts with filters passes query params`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        api.getContacts(dealStage = "New", city = "Mumbai", industry = "Tech")

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path!!.contains("deal_stage=New"))
        assertTrue(request.path!!.contains("city=Mumbai"))
        assertTrue(request.path!!.contains("industry=Tech"))
    }

    @Test
    fun `GET contact by id hits correct path`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(CONTACT_JSON)
        )

        api.getContact("uuid-123")

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/contacts/uuid-123", request.path)
    }

    @Test
    fun `POST contact sends correct body`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(CONTACT_JSON)
        )

        api.createContact(ContactCreateDto(name = "John", phone = "+123"))

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/contacts", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"name\":\"John\""))
        assertTrue(body.contains("\"phone\":\"+123\""))
    }

    @Test
    fun `PUT contact sends correct path and body`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(CONTACT_JSON)
        )

        api.updateContact("uuid-123", ContactUpdateDto(name = "Jane"))

        val request = mockWebServer.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/contacts/uuid-123", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"name\":\"Jane\""))
    }

    @Test
    fun `DELETE contact hits correct path`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        api.deleteContact("uuid-123")

        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/contacts/uuid-123", request.path)
    }

    // --- Call Plan ---

    @Test
    fun `GET call plan hits correct path`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        api.getTodayCallPlan()

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/callplan/today", request.path)
    }

    // --- Calls ---

    @Test
    fun `POST call log sends correct body`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody(CALL_LOG_JSON)
        )

        api.logCall(
            CallLogCreateDto(
                contactId = "c-1",
                durationSeconds = 120,
                disposition = "Connected"
            )
        )

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/calls/log", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"contact_id\":\"c-1\""))
        assertTrue(body.contains("\"duration_seconds\":120"))
        assertTrue(body.contains("\"disposition\":\"Connected\""))
    }

    // --- Recordings ---

    @Test
    fun `POST recording upload uses multipart`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"url":"https://storage.example.com/file.m4a"}""")
        )

        val filePart = MultipartBody.Part.createFormData(
            "file", "test.m4a", "fake-audio-data".toRequestBody()
        )
        api.uploadRecording(filePart)

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/recordings/upload", request.path)
        assertTrue(request.getHeader("Content-Type")!!.contains("multipart"))
    }

    @Test
    fun `POST transcribe sends recording url`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"text":"Hello world"}""")
        )

        api.transcribe(TranscribeRequestDto(recordingUrl = "https://storage.example.com/file.m4a"))

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/recordings/transcribe", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"recording_url\":\"https://storage.example.com/file.m4a\""))
    }

    @Test
    fun `POST summarize sends contact id and transcript`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(AI_SUMMARY_JSON)
        )

        api.summarize(SummarizeRequestDto(contactId = "c-1", transcript = "Hello"))

        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/recordings/summarize", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"contact_id\":\"c-1\""))
        assertTrue(body.contains("\"transcript\":\"Hello\""))
    }

    // --- Dashboard ---

    @Test
    fun `GET dashboard stats hits correct path`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(DASHBOARD_STATS_JSON)
        )

        api.getDashboardStats()

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/dashboard/stats", request.path)
    }

    // --- Response Deserialization ---

    @Test
    fun `deserializes contact response correctly`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(CONTACT_JSON)
        )

        val contact = api.getContact("uuid-123")

        assertEquals("uuid-123", contact.id)
        assertEquals("John Doe", contact.name)
        assertEquals("+1234567890", contact.phone)
        assertEquals("Acme Corp", contact.business)
        assertEquals("New", contact.dealStage)
        assertEquals(0, contact.callCount)
    }

    @Test
    fun `deserializes call plan list correctly`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""[$CALL_PLAN_ITEM_JSON]""")
        )

        val plan = api.getTodayCallPlan()

        assertEquals(1, plan.size)
        assertEquals("item-1", plan[0].id)
        assertEquals("New contact", plan[0].reason)
    }

    @Test
    fun `deserializes dashboard stats correctly`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(DASHBOARD_STATS_JSON)
        )

        val stats = api.getDashboardStats()

        assertEquals(15, stats.callsToday)
        assertEquals(8, stats.connectedToday)
        assertEquals(0.53, stats.conversionRate, 0.001)
        assertEquals(7, stats.streak)
        assertEquals(10, stats.pipeline["New"])
    }

    companion object {
        private val CONTACT_JSON = """
            {
                "id": "uuid-123",
                "name": "John Doe",
                "phone": "+1234567890",
                "business": "Acme Corp",
                "city": "Mumbai",
                "industry": "Tech",
                "deal_stage": "New",
                "last_called": null,
                "call_count": 0,
                "last_call_summary": null,
                "recording_link": null,
                "next_follow_up": null,
                "notes": null,
                "created_at": "2026-01-15T09:00:00"
            }
        """.trimIndent()

        private val CALL_LOG_JSON = """
            {
                "id": "log-1",
                "contact_id": "c-1",
                "timestamp": "2026-02-25T14:30:00",
                "duration_seconds": 120,
                "disposition": "Connected",
                "summary": null,
                "deal_stage": "New",
                "recording_url": null,
                "transcript": null
            }
        """.trimIndent()

        private val CALL_PLAN_ITEM_JSON = """
            {
                "id": "item-1",
                "name": "New Lead",
                "phone": "+1111111111",
                "business": "StartupCo",
                "deal_stage": "New",
                "next_follow_up": null,
                "call_count": 0,
                "last_call_summary": null,
                "reason": "New contact"
            }
        """.trimIndent()

        private val AI_SUMMARY_JSON = """
            {
                "summary": "Good call",
                "recommended_deal_stage": "Proposal",
                "next_action": "Send proposal"
            }
        """.trimIndent()

        private val DASHBOARD_STATS_JSON = """
            {
                "calls_today": 15,
                "connected_today": 8,
                "conversion_rate": 0.53,
                "streak": 7,
                "pipeline": {"New": 10, "Contacted": 5, "Won": 4}
            }
        """.trimIndent()
    }
}
