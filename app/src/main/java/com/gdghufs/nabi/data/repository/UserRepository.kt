package com.gdghufs.nabi.data.repository

import com.gdghufs.nabi.domain.model.User
import com.google.firebase.auth.AuthCredential
import com.gdghufs.nabi.utils.NabiResult

interface UserRepository {
    suspend fun signInWithEmailPassword(email: String, password: String): NabiResult<User>
    suspend fun signUpWithEmailPassword(email: String, password: String, name : String, role : String): NabiResult<User>
    suspend fun signInWithGoogleCredential(credential: AuthCredential, defaultRole: String = "patient"): NabiResult<User>

    suspend fun getCurrentUser(): User?
    fun signOut()

    suspend fun updateUserDisease(uid: String, disease: String): NabiResult<Unit>
}