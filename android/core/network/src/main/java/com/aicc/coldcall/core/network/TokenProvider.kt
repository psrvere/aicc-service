package com.aicc.coldcall.core.network

interface TokenProvider {
    suspend fun getToken(): String?
}
