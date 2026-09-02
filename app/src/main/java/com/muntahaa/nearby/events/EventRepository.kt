package com.muntahaa.nearby.events

import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeEvents(): Flow<List<Event>>
    suspend fun createEvent(event: Event): Result<String>
    suspend fun updateEvent(eventId: String, event: Event): Result<Unit>
    suspend fun deleteEvent(eventId: String): Result<Unit>
}
