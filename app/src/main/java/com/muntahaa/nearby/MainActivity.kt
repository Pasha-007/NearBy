package com.muntahaa.nearby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.muntahaa.nearby.auth.AuthStatus
import com.muntahaa.nearby.auth.AuthViewModel
import com.muntahaa.nearby.navigation.NearbyDestination
import com.muntahaa.nearby.navigation.NearbyNavHost
import com.muntahaa.nearby.ui.theme.NearbyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NearbyTheme {
                NearbyApp()
            }
        }
    }
}

@Composable
fun NearbyApp(authViewModel: AuthViewModel = hiltViewModel()) {
    val authStatus by authViewModel.authStatus.collectAsState()

    when (val status = authStatus) {
        AuthStatus.Loading -> LoadingScreen()
        AuthStatus.Unauthenticated -> {
            key(status) {
                NearbyNavHost(
                    startDestination = NearbyDestination.Login,
                    onSignOut = authViewModel::signOut
                )
            }
        }
        is AuthStatus.Authenticated -> {
            key(status.uid) {
                NearbyNavHost(
                    startDestination = NearbyDestination.EventList,
                    onSignOut = authViewModel::signOut
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
