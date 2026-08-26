package com.muntahaa.nearby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.muntahaa.nearby.auth.AuthStatus
import com.muntahaa.nearby.auth.AuthViewModel
import com.muntahaa.nearby.auth.ui.LoginScreen
import com.muntahaa.nearby.auth.ui.SignUpScreen
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

private enum class AuthScreen { LOGIN, SIGN_UP }

@Composable
fun NearbyApp(authViewModel: AuthViewModel = hiltViewModel()) {
    val authStatus by authViewModel.authStatus.collectAsState()

    when (val status = authStatus) {
        AuthStatus.Loading -> LoadingScreen()
        AuthStatus.Unauthenticated -> {
            var authScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
            when (authScreen) {
                AuthScreen.LOGIN -> LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToSignUp = { authScreen = AuthScreen.SIGN_UP }
                )
                AuthScreen.SIGN_UP -> SignUpScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { authScreen = AuthScreen.LOGIN }
                )
            }
        }
        is AuthStatus.Authenticated -> HomePlaceholderScreen(
            email = status.email,
            onSignOut = authViewModel::signOut
        )
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

@Composable
private fun HomePlaceholderScreen(email: String?, onSignOut: () -> Unit) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Signed in as ${email ?: "unknown"}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSignOut) {
                Text("Sign out")
            }
        }
    }
}
