package com.aicc.coldcall.feature.settings

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aicc.coldcall.core.network.BaseUrlProvider
import com.aicc.coldcall.core.network.TokenProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ServerConfigStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @Named("encrypted") private val encryptedPrefs: SharedPreferences,
) : TokenProvider, BaseUrlProvider {

    private companion object {
        val BACKEND_URL_KEY = stringPreferencesKey("backend_url")
        const val AUTH_TOKEN_KEY = "auth_token"
        const val DEFAULT_URL = "http://10.0.2.2:8000/"
    }

    override suspend fun getBaseUrl(): String =
        dataStore.data.map { prefs -> prefs[BACKEND_URL_KEY] ?: DEFAULT_URL }.first()

    override suspend fun getToken(): String? =
        encryptedPrefs.getString(AUTH_TOKEN_KEY, null)

    suspend fun saveBackendUrl(url: String) {
        dataStore.edit { prefs -> prefs[BACKEND_URL_KEY] = url }
    }

    suspend fun saveToken(token: String) {
        encryptedPrefs.edit().putString(AUTH_TOKEN_KEY, token).apply()
    }

    suspend fun clearToken() {
        encryptedPrefs.edit().remove(AUTH_TOKEN_KEY).apply()
    }
}
