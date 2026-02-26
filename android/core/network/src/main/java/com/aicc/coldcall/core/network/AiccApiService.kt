package com.aicc.coldcall.core.network

import com.aicc.coldcall.core.network.dto.AISummaryDto
import com.aicc.coldcall.core.network.dto.CallLogCreateDto
import com.aicc.coldcall.core.network.dto.CallLogDto
import com.aicc.coldcall.core.network.dto.CallPlanItemDto
import com.aicc.coldcall.core.network.dto.ContactCreateDto
import com.aicc.coldcall.core.network.dto.ContactDto
import com.aicc.coldcall.core.network.dto.ContactUpdateDto
import com.aicc.coldcall.core.network.dto.DashboardStatsDto
import com.aicc.coldcall.core.network.dto.RecordingUploadResponseDto
import com.aicc.coldcall.core.network.dto.SummarizeRequestDto
import com.aicc.coldcall.core.network.dto.TranscribeRequestDto
import com.aicc.coldcall.core.network.dto.TranscriptionResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AiccApiService {

    // --- Contacts ---

    @GET("api/contacts")
    suspend fun getContacts(
        @Query("deal_stage") dealStage: String? = null,
        @Query("city") city: String? = null,
        @Query("industry") industry: String? = null
    ): List<ContactDto>

    @GET("api/contacts/{id}")
    suspend fun getContact(@Path("id") id: String): ContactDto

    @POST("api/contacts")
    suspend fun createContact(@Body contact: ContactCreateDto): ContactDto

    @PUT("api/contacts/{id}")
    suspend fun updateContact(
        @Path("id") id: String,
        @Body contact: ContactUpdateDto
    ): ContactDto

    @DELETE("api/contacts/{id}")
    suspend fun deleteContact(@Path("id") id: String)

    // --- Call Plan ---

    @GET("api/callplan/today")
    suspend fun getTodayCallPlan(): List<CallPlanItemDto>

    // --- Calls ---

    @POST("api/calls/log")
    suspend fun logCall(@Body callLog: CallLogCreateDto): CallLogDto

    // --- Recordings ---

    @Multipart
    @POST("api/recordings/upload")
    suspend fun uploadRecording(@Part file: MultipartBody.Part): RecordingUploadResponseDto

    @POST("api/recordings/transcribe")
    suspend fun transcribe(@Body request: TranscribeRequestDto): TranscriptionResponseDto

    @POST("api/recordings/summarize")
    suspend fun summarize(@Body request: SummarizeRequestDto): AISummaryDto

    // --- Dashboard ---

    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): DashboardStatsDto
}
