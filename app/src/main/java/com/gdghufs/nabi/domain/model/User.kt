package com.gdghufs.nabi.domain.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isEmailVerified: Boolean,
    val role: String?
)