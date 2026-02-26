package com.aicc.coldcall.core.network

import io.mockk.coEvery
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private val tokenProvider = mockk<TokenProvider>()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `adds Bearer token header when token is available`() {
        coEvery { tokenProvider.getToken() } returns "test-token-123"

        val interceptor = AuthInterceptor(tokenProvider)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        client.newCall(
            Request.Builder().url(mockWebServer.url("/api/contacts")).build()
        ).execute().use { /* consume and close response */ }

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer test-token-123", recorded.getHeader("Authorization"))
    }

    @Test
    fun `does not add header when token is null`() {
        coEvery { tokenProvider.getToken() } returns null

        val interceptor = AuthInterceptor(tokenProvider)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        client.newCall(
            Request.Builder().url(mockWebServer.url("/api/contacts")).build()
        ).execute().use { /* consume and close response */ }

        val recorded = mockWebServer.takeRequest()
        assertNull(recorded.getHeader("Authorization"))
    }
}
