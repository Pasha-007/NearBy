package com.muntahaa.nearby.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authStatus: StateFlow<AuthStatus> = authRepository.authStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthStatus.Loading)

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _signUpUiState = MutableStateFlow(SignUpUiState())
    val signUpUiState: StateFlow<SignUpUiState> = _signUpUiState.asStateFlow()

    fun onLoginEmailChange(value: String) {
        _loginUiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onLoginPasswordChange(value: String) {
        _loginUiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun login() {
        val state = _loginUiState.value
        val emailError = validateEmail(state.email)
        val passwordError = if (state.password.isBlank()) "Password is required" else null

        if (emailError != null || passwordError != null) {
            _loginUiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _loginUiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.login(state.email.trim(), state.password)
                .onSuccess {
                    _loginUiState.update { it.copy(isLoading = false) }
                }
                .onFailure { throwable ->
                    _loginUiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(throwable)) }
                }
        }
    }

    fun onSignUpEmailChange(value: String) {
        _signUpUiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onSignUpPasswordChange(value: String) {
        _signUpUiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onSignUpConfirmPasswordChange(value: String) {
        _signUpUiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null) }
    }

    fun signUp() {
        val state = _signUpUiState.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        val confirmPasswordError = if (state.confirmPassword != state.password) "Passwords do not match" else null

        if (emailError != null || passwordError != null || confirmPasswordError != null) {
            _signUpUiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        _signUpUiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.signUp(state.email.trim(), state.password)
                .onSuccess {
                    _signUpUiState.update { it.copy(isLoading = false) }
                }
                .onFailure { throwable ->
                    _signUpUiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(throwable)) }
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address"
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Password is required"
        password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }

    private fun mapAuthError(throwable: Throwable): String = when (throwable) {
        is FirebaseAuthUserCollisionException -> "An account already exists with this email."
        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
        is FirebaseAuthInvalidUserException -> "No account found with this email."
        is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 6 characters."
        else -> throwable.localizedMessage ?: "Something went wrong. Please try again."
    }
}
