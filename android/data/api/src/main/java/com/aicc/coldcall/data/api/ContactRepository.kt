package com.aicc.coldcall.data.api

import com.aicc.coldcall.core.database.ContactDao
import com.aicc.coldcall.core.database.toEntity
import com.aicc.coldcall.core.database.toDomain
import com.aicc.coldcall.core.model.Contact
import com.aicc.coldcall.core.model.ContactCreate
import com.aicc.coldcall.core.model.ContactUpdate
import com.aicc.coldcall.core.network.AiccApiService
import com.aicc.coldcall.core.network.dto.ContactCreateDto
import com.aicc.coldcall.core.network.dto.ContactUpdateDto
import com.aicc.coldcall.core.network.dto.toDomain
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val api: AiccApiService,
    private val contactDao: ContactDao,
) {
    fun getContacts(): Flow<List<Contact>> =
        contactDao.getAll()
            .map { entities -> entities.map { it.toDomain() } }
            .onStart {
                CoroutineScope(currentCoroutineContext() + Job()).launch {
                    refreshFromApi()
                }
            }

    private suspend fun refreshFromApi() {
        try {
            val dtos = api.getContacts()
            val entities = dtos.map { it.toDomain().toEntity() }
            contactDao.insertAll(entities)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Offline — fall back to cached data
        }
    }

    suspend fun getContact(id: String): Contact =
        api.getContact(id).toDomain()

    suspend fun createContact(create: ContactCreate): Contact {
        val dto = ContactCreateDto(
            name = create.name,
            phone = create.phone,
            business = create.business,
            city = create.city,
            industry = create.industry,
            dealStage = create.dealStage?.name,
            notes = create.notes,
            nextFollowUp = create.nextFollowUp,
        )
        val contact = api.createContact(dto).toDomain()
        contactDao.insert(contact.toEntity())
        return contact
    }

    suspend fun updateContact(id: String, update: ContactUpdate): Contact {
        val dto = ContactUpdateDto(
            name = update.name,
            phone = update.phone,
            business = update.business,
            city = update.city,
            industry = update.industry,
            dealStage = update.dealStage?.name,
            notes = update.notes,
            nextFollowUp = update.nextFollowUp,
            lastCallSummary = update.lastCallSummary,
            recordingLink = update.recordingLink,
        )
        val contact = api.updateContact(id, dto).toDomain()
        contactDao.insert(contact.toEntity())
        return contact
    }

    suspend fun deleteContact(id: String) {
        api.deleteContact(id)
        contactDao.deleteById(id)
    }

    fun searchContacts(query: String): Flow<List<Contact>> =
        contactDao.searchByName(query).map { entities -> entities.map { it.toDomain() } }
}
