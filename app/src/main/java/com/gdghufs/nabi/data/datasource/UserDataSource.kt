package com.gdghufs.nabi.data.datasource

import com.gdghufs.nabi.data.model.UserProfileDto

interface UserDataSource {
    suspend fun getUserProfile(uid: String): com.gdghufs.nabi.utils.NabiResult<UserProfileDto?>
    suspend fun saveUserProfile(
        uid: String,
        userProfile: UserProfileDto
    ): com.gdghufs.nabi.utils.NabiResult<Unit>

    suspend fun updateUserProfile(
        uid: String,
        userProfile: UserProfileDto
    ): com.gdghufs.nabi.utils.NabiResult<Unit>
}