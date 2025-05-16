package com.gdghufs.nabi.data.datasource

import com.gdghufs.nabi.data.model.UserProfileDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.gdghufs.nabi.utils.NabiResult
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject

class FirestoreUserDataSourceImpl @Inject constructor(private val firestore: FirebaseFirestore)  : UserDataSource {
    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun getUserProfile(uid: String): NabiResult<UserProfileDto?> {
        return try {
            val documentSnapshot =
                firestore.collection(USERS_COLLECTION).document(uid).get().await()
            if (documentSnapshot.exists()) {
                val userProfileDto = documentSnapshot.toObject(UserProfileDto::class.java)
                NabiResult.Success(userProfileDto)
            } else {
                NabiResult.Success(null)
            }
        } catch (e: Exception) {
            NabiResult.Error(e)
        }
    }

    override suspend fun saveUserProfile(uid: String, userProfile: UserProfileDto): NabiResult<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION).document(uid).set(userProfile).await()
            NabiResult.Success(Unit)
        } catch (e: Exception) {
            NabiResult.Error(e)
        }
    }

    override suspend fun updateUserProfile(uid: String, userProfile: UserProfileDto): NabiResult<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION).document(uid)
                .set(userProfile, SetOptions.merge()).await()
            NabiResult.Success(Unit)
        } catch (e: Exception) {
            NabiResult.Error(e)
        }
    }
}