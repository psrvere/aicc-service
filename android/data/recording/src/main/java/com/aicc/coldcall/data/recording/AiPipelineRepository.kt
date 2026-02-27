package com.aicc.coldcall.data.recording

import com.aicc.coldcall.core.model.AISummary
import com.aicc.coldcall.core.network.AiccApiService
import com.aicc.coldcall.core.network.dto.SummarizeRequestDto
import com.aicc.coldcall.core.network.dto.TranscribeRequestDto
import com.aicc.coldcall.core.network.dto.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiPipelineRepository @Inject constructor(
    private val api: AiccApiService,
) {

    suspend fun transcribe(recordingUrl: String): String {
        val response = api.transcribe(TranscribeRequestDto(recordingUrl))
        return response.text
    }

    suspend fun summarize(contactId: String, transcript: String): AISummary {
        val response = api.summarize(SummarizeRequestDto(contactId, transcript))
        return response.toDomain()
    }
}
