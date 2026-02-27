package com.aicc.coldcall.feature.settings

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val serverConfigStore = mockk<ServerConfigStore>(relaxUnitFun = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads saved backend URL on init`() = runTest {
        coEvery { serverConfigStore.getBaseUrl() } returns "https://api.example.com/"
        coEvery { serverConfigStore.getToken() } returns "token-123"

        val vm = SettingsViewModel(serverConfigStore)
        advanceUntilIdle()

        assertEquals("https://api.example.com/", vm.uiState.value.backendUrl)
        assertTrue(vm.uiState.value.isSignedIn)
    }

    @Test
    fun `isSignedIn is false when no token`() = runTest {
        coEvery { serverConfigStore.getBaseUrl() } returns "http://10.0.2.2:8000/"
        coEvery { serverConfigStore.getToken() } returns null

        val vm = SettingsViewModel(serverConfigStore)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSignedIn)
    }

    @Test
    fun `saveSettings persists URL and token`() = runTest {
        coEvery { serverConfigStore.getBaseUrl() } returns "http://10.0.2.2:8000/"
        coEvery { serverConfigStore.getToken() } returns null

        val vm = SettingsViewModel(serverConfigStore)
        advanceUntilIdle()

        vm.saveSettings("https://new.api.com/", "new-token")
        advanceUntilIdle()

        coVerify { serverConfigStore.saveBackendUrl("https://new.api.com/") }
        coVerify { serverConfigStore.saveToken("new-token") }
    }

    @Test
    fun `signOut clears token`() = runTest {
        coEvery { serverConfigStore.getBaseUrl() } returns "http://10.0.2.2:8000/"
        coEvery { serverConfigStore.getToken() } returns "token-123"

        val vm = SettingsViewModel(serverConfigStore)
        advanceUntilIdle()

        vm.signOut()
        advanceUntilIdle()

        coVerify { serverConfigStore.clearToken() }
        assertFalse(vm.uiState.value.isSignedIn)
    }
}
