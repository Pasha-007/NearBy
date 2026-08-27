package com.muntahaa.nearby.events

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

private const val EVENTS_COLLECTION = "events"

class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
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
}
