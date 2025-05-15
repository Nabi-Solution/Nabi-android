package com.gdghufs.nabi.data.datasource

import com.gdghufs.nabi.data.model.UserProfileDto

interface UserDataSource {
    suspend fun getUserProfile(uid: String): com.gdghufs.nabi.utils.Result<UserProfileDto?>
    suspend fun saveUserProfile(
        uid: String,
        userProfile: UserProfileDto
    ): com.gdghufs.nabi.utils.Result<Unit>

    suspend fun updateUserProfile(
        uid: String,
        userProfile: UserProfileDto
    ): com.gdghufs.nabi.utils.Result<Unit>
}