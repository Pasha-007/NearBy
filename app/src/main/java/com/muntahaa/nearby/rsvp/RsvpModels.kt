package com.muntahaa.nearby.rsvp

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Rsvp(
    @DocumentId
    val rsvpId: String = "",
    val eventId: String = "",
    val userId: String = "",
    val status: String = RsvpStatus.GOING,
    @ServerTimestamp
    val respondedAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

object RsvpStatus {
    const val GOING = "going"
    const val MAYBE = "maybe"
    const val DECLINED = "declined"
    const val WAITLISTED = "waitlisted"
}
