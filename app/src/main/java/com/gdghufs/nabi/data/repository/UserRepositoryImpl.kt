package com.gdghufs.nabi.data.repository

import android.util.Log
import com.gdghufs.nabi.data.datasource.ChatAppointmentDataSource
import com.gdghufs.nabi.data.datasource.AuthDataSource
import com.gdghufs.nabi.data.datasource.UserDataSource
import com.gdghufs.nabi.data.model.UserProfileDto
import com.gdghufs.nabi.domain.model.User
import com.google.firebase.auth.FirebaseUser
import jakarta.inject.Inject
import com.gdghufs.nabi.utils.NabiResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.UserProfileChangeRequest
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


class UserRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: AuthDataSource,
    private val userDataSource: UserDataSource,
    private val chatAppointmentDataSource: ChatAppointmentDataSource
) : UserRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override suspend fun signInWithEmailPassword(email: String, password: String): NabiResult<User> {
        val authResult = firebaseAuthDataSource.signInWithEmailPassword(email, password)
        return when (authResult) {
            is NabiResult.Success -> {
                val firebaseUser = authResult.data
                when (val profileResult = userDataSource.getUserProfile(firebaseUser.uid)) {
                    is NabiResult.Success -> {
                        val userProfileDto = profileResult.data
                        val user = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = userProfileDto?.name ?: firebaseUser.displayName,
                            isEmailVerified = firebaseUser.isEmailVerified,
                            role = userProfileDto?.role
                        )
                        NabiResult.Success(user)
                    }

                    is NabiResult.Error -> {
                        NabiResult.Error(profileResult.exception)
                    }

                    NabiResult.Loading -> {
                        NabiResult.Loading
                    }
                }
            }

            is NabiResult.Error -> authResult
            is NabiResult.Loading -> NabiResult.Loading
        }
    }

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String,
        name: String,
        role: String
    ): NabiResult<User> {
        val authResult = firebaseAuthDataSource.signUpWithEmailPassword(email, password)
        return when (authResult) {
            is NabiResult.Success -> {
                val firebaseUser = authResult.data
                try {
                    val profileUpdates =
                        UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    firebaseUser.updateProfile(profileUpdates).await()

                    val userProfileDto = UserProfileDto(
                        email = firebaseUser.email,
                        name = name,
                        role = role
                    )
                    when (val saveResult =
                        userDataSource.saveUserProfile(firebaseUser.uid, userProfileDto)) {
                        is NabiResult.Success -> {
                            val createdUser = User( // 생성된 정보로 User 도메인 모델 생성
                                uid = firebaseUser.uid,
                                email = firebaseUser.email,
                                displayName = name,
                                isEmailVerified = firebaseUser.isEmailVerified,
                                role = role
                            )
                            NabiResult.Success(createdUser)
                        }

                        is NabiResult.Error -> {
                            Log.e(
                                TAG,
                                "Error saving user profile to Firestore.",
                                saveResult.exception
                            )
                            NabiResult.Error(saveResult.exception)
                        }

                        NabiResult.Loading -> {
                            NabiResult.Loading
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting display name or preparing Firestore save.", e)
                    NabiResult.Error(e)
                }
            }

            is NabiResult.Error -> authResult
            is NabiResult.Loading -> NabiResult.Loading
        }
    }

    override suspend fun signInWithGoogleCredential(
        credential: AuthCredential,
        defaultRole: String
    ): NabiResult<User> {
        val authResult = firebaseAuthDataSource.signInWithGoogleCredential(credential)
        return when (authResult) {
            is NabiResult.Success -> {
                val firebaseUser = authResult.data
                val googleName = firebaseUser.displayName ?: "사용자"
                val googleEmail = firebaseUser.email

                when (val profileResult = userDataSource.getUserProfile(firebaseUser.uid)) {
                    is NabiResult.Success -> {
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
                                when (val updateResult = userDataSource.updateUserProfile(
                                    firebaseUser.uid,
                                    updatedDto
                                )) {
                                    is NabiResult.Error -> Log.w(
                                        TAG,
                                        "Failed to update user profile for Google user.",
                                        updateResult.exception
                                    )

                                    else -> {}
                                }
                            }
                            NabiResult.Success(
                                User(
                                    firebaseUser.uid,
                                    googleEmail,
                                    googleName,
                                    firebaseUser.isEmailVerified,
                                    finalRole
                                )
                            )
                        } else {
                            val newUserProfileDto = UserProfileDto(
                                email = googleEmail,
                                name = googleName,
                                role = defaultRole
                            )
                            when (val saveResult = userDataSource.saveUserProfile(
                                firebaseUser.uid,
                                newUserProfileDto
                            )) {
                                is NabiResult.Success -> NabiResult.Success(
                                    User(
                                        firebaseUser.uid,
                                        googleEmail,
                                        googleName,
                                        firebaseUser.isEmailVerified,
                                        defaultRole
                                    )
                                )

                                is NabiResult.Error -> {
                                    Log.e(
                                        TAG,
                                        "Error saving new Google user profile to Firestore.",
                                        saveResult.exception
                                    )
                                    NabiResult.Error(saveResult.exception)
                                }

                                NabiResult.Loading -> {
                                    Log.w(
                                        TAG,
                                        "Loading while saving new Google user profile to Firestore."
                                    )
                                    NabiResult.Loading
                                }
                            }
                        }
                    }

                    is NabiResult.Error -> {
                        Log.e(
                            TAG,
                            "Error fetching Google user profile from Firestore.",
                            profileResult.exception
                        )
                        NabiResult.Error(profileResult.exception)
                    }

                    NabiResult.Loading -> {
                        Log.w(TAG, "Loading while fetching Google user profile from Firestore.")
                        NabiResult.Loading
                    }
                }
            }

            is NabiResult.Error -> authResult
            is NabiResult.Loading -> NabiResult.Loading
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuthDataSource.getCurrentUser() ?: return null

        return when (val profileResult = userDataSource.getUserProfile(firebaseUser.uid)) {
            is NabiResult.Success -> {
                val userProfileDto = profileResult.data
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = userProfileDto?.name ?: firebaseUser.displayName,
                    isEmailVerified = firebaseUser.isEmailVerified,
                    role = userProfileDto?.role,
                    disease = userProfileDto?.disease,
                    summary = userProfileDto?.summary
                )
            }

            is NabiResult.Error -> {
                Log.e(
                    TAG,
                    "getCurrentUser: Error fetching user profile from Firestore.",
                    profileResult.exception
                )
                User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    isEmailVerified = firebaseUser.isEmailVerified,
                    role = null,
                    disease = null,
                    summary = null
                )
            }

            NabiResult.Loading -> {
                Log.w(TAG, "getCurrentUser: Loading user profile from Firestore.")
                null
            }
        }
    }


    override fun signOut() {
        firebaseAuthDataSource.signOut()
    }

    override suspend fun updateUserDisease(uid: String, disease: String): NabiResult<Unit> {
        return when (val result = userDataSource.getUserProfile(uid)) {
            is NabiResult.Success -> {
                val userProfileDto = result.data
                if (userProfileDto != null) {
                    val updatedProfile = userProfileDto.copy(disease = disease)
                    userDataSource.updateUserProfile(uid, updatedProfile)
                } else {
                    NabiResult.Error(Exception("User profile not found"))
                }
            }

            is NabiResult.Error -> {
                NabiResult.Error(result.exception)
            }

            NabiResult.Loading -> {
                NabiResult.Loading
            }
        }
    }
}