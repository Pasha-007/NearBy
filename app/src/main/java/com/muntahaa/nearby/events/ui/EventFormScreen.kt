package com.muntahaa.nearby.events.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.muntahaa.nearby.events.EventFormEvent
import com.muntahaa.nearby.events.EventViewModel
import java.text.DateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    viewModel: EventViewModel,
    eventId: String?,
    isOwner: Boolean,
    isLoadingEvent: Boolean,
    loadError: String?,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val isEditing = eventId != null

    LaunchedEffect(Unit) {
        viewModel.saveCompleted.collect { onSaved() }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Event" else "Create Event") },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                },
                actions = {
                    if (isEditing && isOwner) {
                        TextButton(onClick = { showDeleteConfirm = true }) { Text("Delete") }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoadingEvent -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            loadError != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = loadError,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = MaterialTheme.colorScheme.error
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
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = { viewModel.onEvent(EventFormEvent.OnTitleChanged(it)) },
                    label = { Text("Title") },
                    singleLine = true,
                    isError = formState.titleError != null,
                    supportingText = {
                        formState.titleError?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { viewModel.onEvent(EventFormEvent.OnDescriptionChanged(it)) },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formState.locationName,
                    onValueChange = { viewModel.onEvent(EventFormEvent.OnLocationChanged(it)) },
                    label = { Text("Location") },
                    singleLine = true,
                    isError = formState.locationNameError != null,
                    supportingText = {
                        formState.locationNameError?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val date = formState.date
                    Text(
                        if (date != null) {
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date)
                        } else {
                            "Pick date & time"
                        }
                    )
                }

                val error = formState.error
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        val editingId = eventId
                        if (editingId != null) {
                            viewModel.onEvent(EventFormEvent.OnUpdateEvent(editingId))
                        } else {
                            viewModel.onEvent(EventFormEvent.OnCreateEvent)
                        }
                    },
                    enabled = !formState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (formState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (isEditing) "Save changes" else "Create event")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = formState.date?.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val initialCalendar = remember {
            Calendar.getInstance().apply {
                formState.date?.let { time = it }
            }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = initialCalendar.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val baseMillis = pendingDateMillis ?: formState.date?.time ?: System.currentTimeMillis()
                    val combined = Calendar.getInstance().apply {
                        timeInMillis = baseMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    viewModel.onEvent(EventFormEvent.OnDateChanged(combined.time))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    if (showDeleteConfirm && eventId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete event?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.onEvent(EventFormEvent.OnDeleteEvent(eventId))
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
