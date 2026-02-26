package com.aicc.coldcall.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callLog: CallLogEntity)

    @Query("SELECT * FROM call_logs WHERE contact_id = :contactId ORDER BY timestamp DESC")
    fun getByContactId(contactId: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE timestamp LIKE :datePrefix || '%' ORDER BY timestamp DESC")
    fun getByDate(datePrefix: String): Flow<List<CallLogEntity>>
}
