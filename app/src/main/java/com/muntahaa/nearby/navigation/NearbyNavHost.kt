package com.muntahaa.nearby.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.muntahaa.nearby.auth.AuthViewModel
import com.muntahaa.nearby.auth.ui.LoginScreen
import com.muntahaa.nearby.auth.ui.SignUpScreen
import com.muntahaa.nearby.events.EventFormEvent
import com.muntahaa.nearby.events.EventViewModel
import com.muntahaa.nearby.events.ui.EventDetailScreen
import com.muntahaa.nearby.events.ui.EventFormScreen
import com.muntahaa.nearby.events.ui.EventListScreen
import com.muntahaa.nearby.rsvp.RsvpViewModel

@Composable
fun NearbyNavHost(
    startDestination: NearbyDestination,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<NearbyDestination.Login> {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate(NearbyDestination.SignUp) {
                        popUpTo(NearbyDestination.Login) { inclusive = true }
                    }
                }
            )
        }
        composable<NearbyDestination.SignUp> {
            val authViewModel: AuthViewModel = hiltViewModel()
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(NearbyDestination.Login) {
                        popUpTo(NearbyDestination.SignUp) { inclusive = true }
                    }
                }
            )
        }
        composable<NearbyDestination.EventList> {
            val eventViewModel: EventViewModel = hiltViewModel()
            EventListScreen(
                viewModel = eventViewModel,
                onSignOut = onSignOut,
                onCreateEvent = { navController.navigate(NearbyDestination.CreateEvent) },
                onEditEvent = { eventId -> navController.navigate(NearbyDestination.EditEvent(eventId)) },
                onEventClick = { eventId -> navController.navigate(NearbyDestination.EventDetail(eventId)) }
            )
        }
        composable<NearbyDestination.CreateEvent> {
            val eventViewModel: EventViewModel = hiltViewModel()
            EventFormScreen(
                viewModel = eventViewModel,
                eventId = null,
                isOwner = true,
                isLoadingEvent = false,
                loadError = null,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable<NearbyDestination.EditEvent> { backStackEntry ->
            val route: NearbyDestination.EditEvent = backStackEntry.toRoute()
            val eventViewModel: EventViewModel = hiltViewModel()
            val listUiState by eventViewModel.uiState.collectAsState()
            var hasSeeded by rememberSaveable(route.eventId) { mutableStateOf(false) }
            val target = listUiState.events.find { it.eventId == route.eventId }

            LaunchedEffect(target, hasSeeded) {
                if (!hasSeeded && target != null) {
                    eventViewModel.onEvent(EventFormEvent.OnStartEditing(target))
                    hasSeeded = true
                }
            }

            EventFormScreen(
                viewModel = eventViewModel,
                eventId = route.eventId,
                isOwner = target?.hostId == eventViewModel.currentUid,
                isLoadingEvent = !hasSeeded && listUiState.isLoading,
                loadError = when {
                    hasSeeded -> null
                    listUiState.errorMessage != null -> listUiState.errorMessage
                    !listUiState.isLoading && target == null -> "Event not found"
                    else -> null
                },
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable<NearbyDestination.EventDetail> { backStackEntry ->
            val route: NearbyDestination.EventDetail = backStackEntry.toRoute()
            val eventViewModel: EventViewModel = hiltViewModel()
            val rsvpViewModel: RsvpViewModel = hiltViewModel()
            val listUiState by eventViewModel.uiState.collectAsState()
            val rsvpUiState by rsvpViewModel.uiState.collectAsState()
            val target = listUiState.events.find { it.eventId == route.eventId }

            LaunchedEffect(route.eventId) {
                rsvpViewModel.observeRsvp(route.eventId)
            }

            EventDetailScreen(
                event = target,
                isLoading = listUiState.isLoading,
                errorMessage = listUiState.errorMessage,
                isOwner = target?.hostId == eventViewModel.currentUid,
                rsvpStatus = rsvpUiState.status,
                isRsvpUpdating = rsvpUiState.isUpdating,
                rsvpError = rsvpUiState.error,
                onRsvpStatusSelected = { status -> rsvpViewModel.setStatus(route.eventId, status) },
                onEditClick = { navController.navigate(NearbyDestination.EditEvent(route.eventId)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
