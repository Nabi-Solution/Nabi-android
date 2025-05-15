package com.gdghufs.nabi.data.datasource

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.gdghufs.nabi.utils.Result
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun signUpWithEmailPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<FirebaseUser>
    fun getCurrentUser(): FirebaseUser?
    fun signOut()
}