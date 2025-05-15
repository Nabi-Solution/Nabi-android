package com.gdghufs.nabi.data.datasource

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.gdghufs.nabi.utils.Result
import kotlinx.coroutines.flow.Flow

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthDataSource {

    override suspend fun signInWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.Success(authResult.user!!)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.Success(authResult.user!!)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            Result.Success(authResult.user!!)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

}