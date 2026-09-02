package com.muntahaa.nearby.rsvp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RsvpUiState(
    val status: String? = null,
    val isUpdating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RsvpViewModel @Inject constructor(
    private val rsvpRepository: RsvpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RsvpUiState())
    val uiState: StateFlow<RsvpUiState> = _uiState.asStateFlow()

    private var observedEventId: String? = null

    fun observeRsvp(eventId: String) {
        if (observedEventId == eventId) return
        observedEventId = eventId
        viewModelScope.launch {
            rsvpRepository.observeUserRsvp(eventId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(error = throwable.localizedMessage ?: "Failed to load RSVP status.")
                    }
                }
                .collect { rsvp ->
                    _uiState.update { it.copy(status = rsvp?.status) }
                }
        }
    }

    fun setStatus(eventId: String, status: String) {
        _uiState.update { it.copy(isUpdating = true, error = null) }
        viewModelScope.launch {
            rsvpRepository.setRsvpStatus(eventId, status)
                .onSuccess {
                    _uiState.update { it.copy(isUpdating = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isUpdating = false, error = throwable.localizedMessage ?: "Failed to update RSVP.")
                    }
                }
        }
    }
}
