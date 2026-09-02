package com.muntahaa.nearby.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val authStatus: Flow<AuthStatus> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            trySend(
                if (user != null) {
                    AuthStatus.Authenticated(uid = user.uid, email = user.email)
                } else {
                    AuthStatus.Unauthenticated
                }
            )
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
