package com.aicc.coldcall.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecordingDao {

    @Insert
    suspend fun insert(recording: RecordingEntity): Long

    @Query("SELECT * FROM recordings WHERE is_uploaded = 0")
    suspend fun getPendingUploads(): List<RecordingEntity>

    @Query("UPDATE recordings SET is_uploaded = 1, remote_url = :url WHERE id = :id")
    suspend fun markUploaded(id: Long, url: String)

    @Query("SELECT * FROM recordings WHERE contact_id = :contactId ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestForContact(contactId: String): RecordingEntity?

    @Query("DELETE FROM recordings WHERE is_uploaded = 1 AND created_at < :threshold")
    suspend fun deleteUploadedOlderThan(threshold: Long): Int
}
