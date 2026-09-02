package com.muntahaa.nearby.events.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muntahaa.nearby.events.Event
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event?,
    isLoading: Boolean,
    errorMessage: String?,
    isOwner: Boolean,
    onEditClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Event") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    if (isOwner && event != null) {
                        TextButton(onClick = onEditClick) { Text("Edit") }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            errorMessage != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            event == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Event not found",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (event.status == "cancelled") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "This event has been cancelled",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Text(event.title, style = MaterialTheme.typography.headlineSmall)
                Text(event.locationName, style = MaterialTheme.typography.bodyLarge)

                val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                val startTime = event.startTime
                val endTime = event.endTime
                if (startTime != null) {
                    val timeText = if (endTime != null) {
                        "${dateFormat.format(startTime)} – ${dateFormat.format(endTime)}"
                    } else {
                        dateFormat.format(startTime)
                    }
                    Text(timeText, style = MaterialTheme.typography.bodyMedium)
                }

                if (event.description.isNotBlank()) {
                    Text(event.description, style = MaterialTheme.typography.bodyMedium)
                }

                val capacity = event.capacity
                Text(
                    text = if (capacity != null) "${event.rsvpCount}/$capacity going" else "${event.rsvpCount} going",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
