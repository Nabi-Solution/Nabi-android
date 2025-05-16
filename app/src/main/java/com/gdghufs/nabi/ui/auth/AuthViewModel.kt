package com.gdghufs.nabi.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdghufs.nabi.data.repository.UserRepository
import com.gdghufs.nabi.domain.model.User
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gdghufs.nabi.utils.NabiResult
import dagger.hilt.android.lifecycle.HiltViewModel


data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val authSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _googleSignInRequest = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val googleSignInRequest: SharedFlow<Unit> = _googleSignInRequest.asSharedFlow()

    fun signInWithEmailPassword(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, authSuccess = false) }
            when (val result = userRepository.signInWithEmailPassword(email, password)) {
                is NabiResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, currentUser = result.data, authSuccess = true)
                    }
                }

                is NabiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.exception.message ?: "로그인 실패")
                    }
                }

                NabiResult.Loading -> {
                }
            }
        }
    }

    fun signUpWithEmailPassword(email: String, password: String, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, authSuccess = false) }
            when (val result =
                userRepository.signUpWithEmailPassword(email, password, name, "patient")) {
                is NabiResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, currentUser = result.data, authSuccess = true)
                    }
                }

                is NabiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.exception.message ?: "회원가입 실패")
                    }
                }

                NabiResult.Loading -> {
                }
            }
        }
    }

    fun signInWithGoogleCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, authSuccess = false) }
            when (val result = userRepository.signInWithGoogleCredential(credential)) {
                is NabiResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, currentUser = result.data, authSuccess = true)
                    }
                }

                is NabiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Google 로그인 실패"
                        )
                    }
                }

                NabiResult.Loading -> {
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetAuthSuccess() {
        _uiState.update { it.copy(authSuccess = false) }
    }
}