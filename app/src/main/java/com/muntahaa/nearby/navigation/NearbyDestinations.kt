package com.muntahaa.nearby.navigation

import kotlinx.serialization.Serializable

sealed interface NearbyDestination {
    @Serializable
    data object Login : NearbyDestination

    @Serializable
    data object SignUp : NearbyDestination

    @Serializable
    data object EventList : NearbyDestination

    @Serializable
    data object CreateEvent : NearbyDestination

    @Serializable
    data class EditEvent(val eventId: String) : NearbyDestination

    // Next step: @Serializable data class EventDetail(val eventId: String) : NearbyDestination
}
