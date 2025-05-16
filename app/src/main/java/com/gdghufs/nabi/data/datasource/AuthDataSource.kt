package com.gdghufs.nabi.data.datasource

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.gdghufs.nabi.utils.NabiResult

interface AuthDataSource {
    suspend fun signInWithEmailPassword(email: String, password: String): NabiResult<FirebaseUser>
    suspend fun signUpWithEmailPassword(email: String, password: String): NabiResult<FirebaseUser>
    suspend fun signInWithGoogleCredential(credential: AuthCredential): NabiResult<FirebaseUser>
    fun getCurrentUser(): FirebaseUser?
    fun signOut()
}