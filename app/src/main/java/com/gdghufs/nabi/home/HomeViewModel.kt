package com.gdghufs.nabi.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdghufs.nabi.data.repository.UserRepository
import com.gdghufs.nabi.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showDiseaseDialog = MutableStateFlow(false)
    val showDiseaseDialog: StateFlow<Boolean> = _showDiseaseDialog.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            val user = userRepository.getCurrentUser()
            _currentUser.value = user

            _isLoading.value = false

            _showDiseaseDialog.value = user?.disease == null
        }
    }

    fun updateUserDisease(disease: String) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                userRepository.updateUserDisease(user.uid, disease)
                _showDiseaseDialog.value = false
            }
        }
    }

    fun signOutUser() {
        viewModelScope.launch {
            userRepository.signOut()
            _currentUser.value = null
        }
    }

    fun refreshUserState() {
        checkCurrentUser()
    }
}