package com.gdghufs.nabi.data.repository

import android.util.Log
import com.gdghufs.nabi.data.datasource.AuthDataSource
import com.gdghufs.nabi.data.datasource.UserDataSource
import com.gdghufs.nabi.data.model.UserProfileDto
import com.gdghufs.nabi.domain.model.User
import com.google.firebase.auth.FirebaseUser
import jakarta.inject.Inject
import com.gdghufs.nabi.utils.Result
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

fun FirebaseUser.toDomainUser(role: String?): User {
    return User(
        uid = this.uid,
        email = this.email,
        displayName = this.displayName,
        isEmailVerified = this.isEmailVerified,
        role = role
    )
}


class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: AuthDataSource,
    private val userDataSource: UserDataSource
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override suspend fun signInWithEmailPassword(email: String, password: String): Result<User> {
        val authResult = firebaseAuthDataSource.signInWithEmailPassword(email, password)
        return when (authResult) {
            is Result.Success -> {
                val firebaseUser = authResult.data
                when (val profileResult = userDataSource.getUserProfile(firebaseUser.uid)) {
                    is Result.Success -> {
                        val userProfileDto = profileResult.data
                        val user = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = userProfileDto?.name ?: firebaseUser.displayName,
                            isEmailVerified = firebaseUser.isEmailVerified,
                            role = userProfileDto?.role
                        )
                        Result.Success(user)
                    }
                    is Result.Error -> {
                        Result.Error(profileResult.exception)
                    }
                    Result.Loading -> {
                        Result.Loading
                    }
                }
            }
            is Result.Error -> authResult
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun signUpWithEmailPassword(email: String, password: String, name: String, role: String): Result<User> {
        val authResult = firebaseAuthDataSource.signUpWithEmailPassword(email, password)
        return when (authResult) {
            is Result.Success -> {
                val firebaseUser = authResult.data
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    firebaseUser.updateProfile(profileUpdates).await()

                    val userProfileDto = UserProfileDto(
                        email = firebaseUser.email,
                        name = name,
                        role = role
                    )
                    when (val saveResult = userDataSource.saveUserProfile(firebaseUser.uid, userProfileDto)) {
                        is Result.Success -> {
                            val createdUser = User( // 생성된 정보로 User 도메인 모델 생성
                                uid = firebaseUser.uid,
                                email = firebaseUser.email,
                                displayName = name,
                                isEmailVerified = firebaseUser.isEmailVerified,
                                role = role
                            )
                            Result.Success(createdUser)
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Error saving user profile to Firestore.", saveResult.exception)
                            Result.Error(saveResult.exception)
                        }

                        Result.Loading -> {
                            Result.Loading
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting display name or preparing Firestore save.", e)
                    Result.Error(e)
                }
            }
            is Result.Error -> authResult
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun signInWithGoogleCredential(credential: AuthCredential, defaultRole: String): Result<User> {
        val authResult = firebaseAuthDataSource.signInWithGoogleCredential(credential)
        return when (authResult) {
            is Result.Success -> {
                val firebaseUser = authResult.data
                val googleName = firebaseUser.displayName ?: "사용자"
                val googleEmail = firebaseUser.email

                when (val profileResult = userDataSource.getUserProfile(firebaseUser.uid)) {
                    is Result.Success -> {
                        val existingProfileDto = profileResult.data
                        if (existingProfileDto != null) {
                            var updatedDto = existingProfileDto.copy()
                            var needsUpdate = false

                            if (existingProfileDto.name != googleName) {
                                updatedDto = updatedDto.copy(name = googleName)
                                needsUpdate = true
                            }
                            if (existingProfileDto.email != googleEmail) {
                                updatedDto = updatedDto.copy(email = googleEmail)
                                needsUpdate = true
                            }

                            val finalRole = existingProfileDto.role

                            if (needsUpdate) {
                                when(val updateResult = userDataSource.updateUserProfile(firebaseUser.uid, updatedDto)){
                                    is Result.Error -> Log.w(TAG, "Failed to update user profile for Google user.", updateResult.exception)
                                    else -> {}
                                }
                            }
                            Result.Success(User(firebaseUser.uid, googleEmail, googleName, firebaseUser.isEmailVerified, finalRole))
                        } else {
                            val newUserProfileDto = UserProfileDto(
                                email = googleEmail,
                                name = googleName,
                                role = defaultRole
                            )
                            when (val saveResult = userDataSource.saveUserProfile(firebaseUser.uid, newUserProfileDto)) {
                                is Result.Success -> Result.Success(User(firebaseUser.uid, googleEmail, googleName, firebaseUser.isEmailVerified, defaultRole))
                                is Result.Error -> {
                                    Log.e(TAG, "Error saving new Google user profile to Firestore.", saveResult.exception)
                                    Result.Error(saveResult.exception)
                                }

                                Result.Loading -> {
                                    Log.w(TAG, "Loading while saving new Google user profile to Firestore.")
                                    Result.Loading
                                }
                            }
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Error fetching Google user profile from Firestore.", profileResult.exception)
                        Result.Error(profileResult.exception)
                    }

                    Result.Loading -> {
                        Log.w(TAG, "Loading while fetching Google user profile from Firestore.")
                        Result.Loading
                    }
                }
            }
            is Result.Error -> authResult
            is Result.Loading -> Result.Loading
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuthDataSource.getCurrentUser() ?: return null

        return when (val profileResult = userDataSource.getUserProfile(firebaseUser.uid)) {
            is Result.Success -> {
                val userProfileDto = profileResult.data
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = userProfileDto?.name ?: firebaseUser.displayName,
                    isEmailVerified = firebaseUser.isEmailVerified,
                    role = userProfileDto?.role
                )
            }
            is Result.Error -> {
                Log.e(TAG, "getCurrentUser: Error fetching user profile from Firestore.", profileResult.exception)
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    isEmailVerified = firebaseUser.isEmailVerified,
                    role = null
                )
            }
            Result.Loading -> {
                Log.w(TAG, "getCurrentUser: Loading user profile from Firestore.")
                null
            }
        }
    }


    override fun signOut() {
        firebaseAuthDataSource.signOut()
    }
}