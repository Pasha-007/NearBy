package com.muntahaa.nearby.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventListUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class EventFormState(
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val date: Date? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val titleError: String? = null,
    val locationNameError: String? = null
)

sealed interface EventFormEvent {
    data class OnTitleChanged(val value: String) : EventFormEvent
    data class OnDescriptionChanged(val value: String) : EventFormEvent
    data class OnLocationChanged(val value: String) : EventFormEvent
    data class OnDateChanged(val value: Date) : EventFormEvent
    data object OnCreateEvent : EventFormEvent
    data class OnUpdateEvent(val eventId: String) : EventFormEvent
    data class OnDeleteEvent(val eventId: String) : EventFormEvent
    data class OnStartEditing(val event: Event) : EventFormEvent
}

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    val uiState: StateFlow<EventListUiState> = eventRepository.observeEvents()
        .map { events -> EventListUiState(events = events, isLoading = false) }
        .catch { throwable ->
            emit(
                EventListUiState(
                    isLoading = false,
                    errorMessage = throwable.localizedMessage ?: "Failed to load events."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EventListUiState(isLoading = true)
        )

    private val _formState = MutableStateFlow(EventFormState())
    val formState: StateFlow<EventFormState> = _formState.asStateFlow()

    private val _saveCompleted = MutableSharedFlow<Unit>()
    val saveCompleted: SharedFlow<Unit> = _saveCompleted.asSharedFlow()

    val currentUid: String?
        get() = firebaseAuth.currentUser?.uid

    fun onEvent(event: EventFormEvent) {
        when (event) {
            is EventFormEvent.OnTitleChanged -> _formState.update { it.copy(title = event.value, titleError = null) }
            is EventFormEvent.OnDescriptionChanged -> _formState.update { it.copy(description = event.value) }
            is EventFormEvent.OnLocationChanged -> _formState.update { it.copy(locationName = event.value, locationNameError = null) }
            is EventFormEvent.OnDateChanged -> _formState.update { it.copy(date = event.value) }
            EventFormEvent.OnCreateEvent -> createEvent()
            is EventFormEvent.OnUpdateEvent -> updateEvent(event.eventId)
            is EventFormEvent.OnDeleteEvent -> deleteEvent(event.eventId)
            is EventFormEvent.OnStartEditing -> _formState.update {
                it.copy(
                    title = event.event.title,
                    description = event.event.description,
                    locationName = event.event.locationName,
                    date = event.event.startTime
                )
            }
        }
    }

    private fun validateRequiredFields(form: EventFormState): Boolean {
        val titleError = if (form.title.isBlank()) "Title is required" else null
        val locationNameError = if (form.locationName.isBlank()) "Location is required" else null
        if (titleError != null || locationNameError != null) {
            _formState.update { it.copy(titleError = titleError, locationNameError = locationNameError) }
            return false
        }
        return true
    }

    private fun createEvent() {
        val form = _formState.value
        if (!validateRequiredFields(form)) return
        val newEvent = Event(
            title = form.title,
            description = form.description,
            locationName = form.locationName,
            startTime = form.date
        )
        _formState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            eventRepository.createEvent(newEvent)
                .onSuccess {
                    _formState.value = EventFormState()
                    _saveCompleted.emit(Unit)
                }
                .onFailure { throwable ->
                    _formState.update {
                        it.copy(isSaving = false, error = throwable.localizedMessage ?: "Failed to create event.")
                    }
                }
        }
    }

    private fun updateEvent(eventId: String) {
        val form = _formState.value
        if (!validateRequiredFields(form)) return

        val original = uiState.value.events.find { it.eventId == eventId }
        val currentUid = firebaseAuth.currentUser?.uid
        if (original == null || original.hostId != currentUid) {
            _formState.update { it.copy(error = "You can only edit your own events") }
            return
        }

        val merged = original.copy(
            title = form.title,
            description = form.description,
            locationName = form.locationName,
            startTime = form.date
        )
        _formState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            eventRepository.updateEvent(eventId, merged)
                .onSuccess {
                    _formState.value = EventFormState()
                    _saveCompleted.emit(Unit)
                }
                .onFailure { throwable ->
                    _formState.update {
                        it.copy(isSaving = false, error = throwable.localizedMessage ?: "Failed to update event.")
                    }
                }
        }
    }

    private fun deleteEvent(eventId: String) {
        val original = uiState.value.events.find { it.eventId == eventId }
        val currentUid = firebaseAuth.currentUser?.uid
        if (original == null || original.hostId != currentUid) {
            _formState.update { it.copy(error = "You can only delete your own events") }
            return
        }

        _formState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            eventRepository.deleteEvent(eventId)
                .onSuccess {
                    _formState.value = EventFormState()
                    _saveCompleted.emit(Unit)
                }
                .onFailure { throwable ->
                    _formState.update {
                        it.copy(isSaving = false, error = throwable.localizedMessage ?: "Failed to delete event.")
                    }
                }
        }
    }
}
