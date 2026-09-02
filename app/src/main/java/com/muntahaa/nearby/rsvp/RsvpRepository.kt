package com.muntahaa.nearby.rsvp

import kotlinx.coroutines.flow.Flow

interface RsvpRepository {
    fun observeUserRsvp(eventId: String): Flow<Rsvp?>
    suspend fun setRsvpStatus(eventId: String, status: String): Result<Unit>
}
