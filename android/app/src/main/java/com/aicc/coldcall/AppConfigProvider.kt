package com.aicc.coldcall

import com.aicc.coldcall.core.network.BaseUrlProvider
import com.aicc.coldcall.core.network.TokenProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigProvider @Inject constructor() : TokenProvider, BaseUrlProvider {

    override suspend fun getBaseUrl(): String = BuildConfig.BASE_URL

    override suspend fun getToken(): String? =
        BuildConfig.API_KEY.ifBlank { null }
}
