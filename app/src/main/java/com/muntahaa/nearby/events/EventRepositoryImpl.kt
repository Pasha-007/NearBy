package com.muntahaa.nearby.events

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val EVENTS_COLLECTION = "events"

class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : EventRepository {

    override fun observeEvents(): Flow<List<Event>> = callbackFlow {
        val registration = firestore.collection(EVENTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    runCatching { doc.toObject(Event::class.java) }.getOrNull()
                }
                trySend(events)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createEvent(event: Event): Result<String> = runCatching {
        val hostId = firebaseAuth.currentUser?.uid
            ?: error("Must be signed in to create an event")
        val newEvent = event.copy(
            hostId = hostId,
            rsvpCount = 0,
            createdAt = null,
            updatedAt = null
        )
        firestore.collection(EVENTS_COLLECTION).add(newEvent).await().id
    }

    override suspend fun updateEvent(eventId: String, event: Event): Result<Unit> = runCatching {
        val updates = mapOf(
            "title" to event.title,
            "description" to event.description,
            "locationName" to event.locationName,
            "location" to event.location,
            "startTime" to event.startTime,
            "endTime" to event.endTime,
            "capacity" to event.capacity,
            "visibility" to event.visibility,
            "status" to event.status,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection(EVENTS_COLLECTION).document(eventId).update(updates).await()
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        firestore.collection(EVENTS_COLLECTION).document(eventId).delete().await()
    }
}
