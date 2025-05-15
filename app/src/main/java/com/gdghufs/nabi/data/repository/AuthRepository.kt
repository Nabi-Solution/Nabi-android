package com.gdghufs.nabi.data.repository

import com.gdghufs.nabi.domain.model.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.gdghufs.nabi.utils.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithEmailPassword(email: String, password: String): Result<User>
    suspend fun signUpWithEmailPassword(email: String, password: String, name : String, role : String): Result<User>
    suspend fun signInWithGoogleCredential(credential: AuthCredential, defaultRole: String = "patient"): Result<User>

    suspend fun getCurrentUser(): User?
    fun signOut()
}