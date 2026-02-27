package com.aicc.coldcall.core.network

interface BaseUrlProvider {
    suspend fun getBaseUrl(): String
}
