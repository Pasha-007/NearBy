package com.muntahaa.nearby.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muntahaa.nearby.auth.AuthViewModel
import com.muntahaa.nearby.auth.ui.LoginScreen
import com.muntahaa.nearby.auth.ui.SignUpScreen
import com.muntahaa.nearby.events.EventViewModel
import com.muntahaa.nearby.events.ui.EventListScreen

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
            EventListScreen(viewModel = eventViewModel, onSignOut = onSignOut)
            // Next step: pass onEventClick = { id -> navController.navigate(NearbyDestination.EventDetail(id)) }
        }
    }
}
