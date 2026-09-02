package com.muntahaa.nearby.rsvp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val RSVPS_COLLECTION = "rsvps"
private const val EVENTS_COLLECTION = "events"

class RsvpRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : RsvpRepository {

    override fun observeUserRsvp(eventId: String): Flow<Rsvp?> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(RSVPS_COLLECTION)
            .document(rsvpDocId(eventId, userId))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rsvp = if (snapshot != null && snapshot.exists()) {
                    runCatching { snapshot.toObject(Rsvp::class.java) }.getOrNull()
                } else {
                    null
                }
                trySend(rsvp)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun setRsvpStatus(eventId: String, status: String): Result<Unit> = runCatching {
        val userId = firebaseAuth.currentUser?.uid
            ?: error("Must be signed in to RSVP")
        val rsvpRef = firestore.collection(RSVPS_COLLECTION).document(rsvpDocId(eventId, userId))
        val eventRef = firestore.collection(EVENTS_COLLECTION).document(eventId)

        firestore.runTransaction { transaction ->
            val existing = transaction.get(rsvpRef)
            val previousStatus = if (existing.exists()) existing.getString("status") else null

            val data = hashMapOf(
                "eventId" to eventId,
                "userId" to userId,
                "status" to status,
                "respondedAt" to (if (existing.exists()) existing.get("respondedAt") else FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            transaction.set(rsvpRef, data)

            val wasGoing = previousStatus == RsvpStatus.GOING
            val isGoing = status == RsvpStatus.GOING
            if (wasGoing != isGoing) {
                val delta = if (isGoing) 1L else -1L
                transaction.update(eventRef, "rsvpCount", FieldValue.increment(delta))
            }
            Unit
        }.await()
    }

    private fun rsvpDocId(eventId: String, userId: String) = "${eventId}_$userId"
}
