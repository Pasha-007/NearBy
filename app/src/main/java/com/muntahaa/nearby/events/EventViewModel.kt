package com.muntahaa.nearby.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class EventListUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository
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
}
