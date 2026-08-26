package com.muntahaa.nearby.auth

/** Current Firebase sign-in status, driven by [com.google.firebase.auth.FirebaseAuth]'s auth state listener. */
sealed interface AuthStatus {
    data object Loading : AuthStatus
    data class Authenticated(val uid: String, val email: String?) : AuthStatus
    data object Unauthenticated : AuthStatus
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
