package com.aicc.coldcall.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aicc.coldcall.core.model.DealStage
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getById(id: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE deal_stage = :dealStage ORDER BY name ASC")
    fun filterByDealStage(dealStage: DealStage): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE city = :city ORDER BY name ASC")
    fun filterByCity(city: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE industry = :industry ORDER BY name ASC")
    fun filterByIndustry(industry: String): Flow<List<ContactEntity>>
}
