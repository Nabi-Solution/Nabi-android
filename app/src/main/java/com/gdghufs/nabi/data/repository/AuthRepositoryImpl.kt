package com.gdghufs.nabi.data.repository

import com.gdghufs.nabi.data.datasource.AuthDataSource
import com.gdghufs.nabi.domain.model.User
import com.google.firebase.auth.FirebaseUser
import jakarta.inject.Inject
import com.gdghufs.nabi.utils.Result
import com.google.firebase.auth.AuthCredential

fun FirebaseUser.toDomainUser(): User {
    return User(
        uid = this.uid,
        email = this.email,
        displayName = this.displayName,
        isEmailVerified = this.isEmailVerified
    )
}

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource
) : AuthRepository {

    override suspend fun signInWithEmailPassword(email: String, password: String): Result<User> {
        return when (val result = authDataSource.signInWithEmailPassword(email, password)) {
            is Result.Success -> Result.Success(result.data.toDomainUser())
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String): Result<User> {
        return when (val result = authDataSource.signUpWithEmailPassword(email, password)) {
            is Result.Success -> Result.Success(result.data.toDomainUser())
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<User> {
        return when (val result = authDataSource.signInWithGoogleCredential(credential)) {
            is Result.Success -> Result.Success(result.data.toDomainUser())
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    override fun getCurrentUser(): User? {
        return authDataSource.getCurrentUser()?.toDomainUser()
    }

    override fun signOut() {
        authDataSource.signOut()
    }
}