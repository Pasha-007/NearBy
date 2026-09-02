package com.muntahaa.nearby.events.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muntahaa.nearby.events.Event
import com.muntahaa.nearby.events.EventViewModel
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventViewModel,
    onSignOut: () -> Unit,
    onCreateEvent: () -> Unit,
    onEditEvent: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUid = viewModel.currentUid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby") },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text("Sign out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateEvent) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                uiState.errorMessage != null -> Text(
                    text = uiState.errorMessage!!,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = MaterialTheme.colorScheme.error
                )

                uiState.events.isEmpty() -> Text(
                    text = "No events yet. Check back soon!",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.events, key = { it.eventId }) { event ->
                        // Next step: onClick = { onEventClick(event.eventId) } for event-detail navigation
                        EventCard(
                            event = event,
                            isOwnedByCurrentUser = event.hostId == currentUid,
                            onEditClick = { onEditEvent(event.eventId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    isOwnedByCurrentUser: Boolean,
    onEditClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(event.title, style = MaterialTheme.typography.titleMedium)
                if (isOwnedByCurrentUser) {
                    TextButton(onClick = onEditClick) {
                        Text("Edit")
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(event.locationName, style = MaterialTheme.typography.bodyMedium)
            event.startTime?.let { startTime ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(startTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (event.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
            Spacer(Modifier.height(8.dp))
            val capacity = event.capacity
            Text(
                text = if (capacity != null) "${event.rsvpCount}/$capacity going" else "${event.rsvpCount} going",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
