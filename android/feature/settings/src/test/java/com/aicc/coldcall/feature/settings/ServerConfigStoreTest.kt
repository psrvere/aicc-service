package com.aicc.coldcall.feature.settings

import android.content.SharedPreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.mockk.every
import io.mockk.mockk

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class ServerConfigStoreTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())
    private lateinit var store: ServerConfigStore
    private lateinit var encryptedPrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        val tokenStore = mutableMapOf<String, String>()
        editor = mockk(relaxed = true) {
            every { putString(any(), any()) } answers {
                tokenStore[firstArg()] = secondArg()
                this@mockk
            }
            every { remove(any()) } answers {
                tokenStore.remove(firstArg<String>())
                this@mockk
            }
        }
        encryptedPrefs = mockk {
            every { getString(any(), any()) } answers { tokenStore[firstArg()] }
            every { edit() } returns editor
        }
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_settings.preferences_pb") }
        )
        store = ServerConfigStore(dataStore, encryptedPrefs, testScope)
    }

    @Test
    fun `getBaseUrl returns default when nothing saved`() = runTest {
        val url = store.getBaseUrl()
        assertEquals("http://10.0.2.2:8000/", url)
    }

    @Test
    fun `getToken returns null when nothing saved`() = runTest {
        val token = store.getToken()
        assertNull(token)
    }

    @Test
    fun `saveBackendUrl persists and retrieves URL`() = runTest {
        store.saveBackendUrl("https://api.example.com/")
        val url = store.getBaseUrl()
        assertEquals("https://api.example.com/", url)
    }

    @Test
    fun `saveToken persists and retrieves token`() = runTest {
        store.saveToken("my-token")
        val token = store.getToken()
        assertEquals("my-token", token)
    }

    @Test
    fun `clearToken removes token`() = runTest {
        store.saveToken("my-token")
        assertEquals("my-token", store.getToken())

        store.clearToken()
        assertNull(store.getToken())
    }
}
