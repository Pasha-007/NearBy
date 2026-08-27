package com.muntahaa.nearby.events

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Event(
    @DocumentId
    val eventId: String = "",
    val hostId: String = "",
    val title: String = "",
    val description: String = "",
    val location: GeoPoint? = null,
    val locationName: String = "",
    val startTime: Date? = null,
    val endTime: Date? = null,
    val capacity: Long? = null,
    val rsvpCount: Long = 0,
    val visibility: String = "public",
    val status: String = "published",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
