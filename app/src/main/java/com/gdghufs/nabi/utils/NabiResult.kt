package com.gdghufs.nabi.utils

sealed class NabiResult<out T> {
    data class Success<out T>(val data: T) : NabiResult<T>()
    data class Error(val exception: Exception) : NabiResult<Nothing>()
    data object Loading : NabiResult<Nothing>()
}