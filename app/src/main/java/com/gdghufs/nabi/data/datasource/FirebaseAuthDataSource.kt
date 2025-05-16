package com.gdghufs.nabi.data.datasource

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.gdghufs.nabi.utils.NabiResult

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthDataSource {

    override suspend fun signInWithEmailPassword(email: String, password: String): NabiResult<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            NabiResult.Success(authResult.user!!)
        } catch (e: Exception) {
            NabiResult.Error(e)
        }
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String): NabiResult<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            NabiResult.Success(authResult.user!!)
        } catch (e: Exception) {
            NabiResult.Error(e)
        }
    }

    override suspend fun signInWithGoogleCredential(credential: AuthCredential): NabiResult<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            NabiResult.Success(authResult.user!!)
        } catch (e: Exception) {
            NabiResult.Error(e)
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

}