package com.gdghufs.nabi.ui.dashboard // ViewModel 패키지 경로 (예시)

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdghufs.nabi.data.model.ChatAppointment
import com.gdghufs.nabi.data.repository.ChatAppointmentRepository
import com.gdghufs.nabi.data.repository.UserRepository
import com.gdghufs.nabi.domain.model.User
import com.gdghufs.nabi.utils.NabiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI 상태를 나타내는 데이터 클래스
data class DashboardUiState(
    val user: User? = null,
    val appointments: List<ChatAppointment> = emptyList(),
    val daysUsingNavi: String = "1",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appointmentRepository: ChatAppointmentRepository
) : ViewModel() {

    private val _userResult = MutableStateFlow<NabiResult<User?>>(NabiResult.Loading)
    private val _appointmentsResult = MutableStateFlow<NabiResult<List<ChatAppointment>>>(NabiResult.Loading)

    val uiState: StateFlow<DashboardUiState> = combine(
        _userResult,
        _appointmentsResult
    ) { userResult, appointmentsResult ->
        val currentUser = if (userResult is NabiResult.Success) userResult.data else null
        val currentAppointments = if (appointmentsResult is NabiResult.Success) appointmentsResult.data else emptyList()

        // 에러 처리: 둘 중 하나라도 에러면 에러 메시지 설정
        val errorMsg = when {
            userResult is NabiResult.Error -> "Failed to load user data: ${userResult.exception.message}"
            appointmentsResult is NabiResult.Error -> "Failed to load appointments: ${appointmentsResult.exception.message}"
            else -> null
        }

        // 로딩 상태: 둘 중 하나라도 로딩 중이면 전체 로딩
        val isLoading = userResult is NabiResult.Loading || appointmentsResult is NabiResult.Loading

        // "N일째 사용 중" (User 모델에 가입일 정보가 없으므로 임시 처리)
        // val days = currentUser?.joinedTimestamp?.let { calculateDays(it) } ?: "N"
        val days = "N" // 임시

        DashboardUiState(
            user = currentUser,
            appointments = currentAppointments,
            daysUsingNavi = days,
            isLoading = isLoading,
            errorMessage = errorMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DashboardUiState() // 초기값 (로딩중)
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // 1. 사용자 정보 가져오기
            _userResult.value = NabiResult.Loading
            try {
                val user = userRepository.getCurrentUser() // User? 반환
                if (user != null) {
                    _userResult.value = NabiResult.Success(user)
                    // 2. 사용자 정보 성공 시, 해당 사용자의 약속 목록 가져오기
                    loadAppointments(user.uid)
                } else {
                    _userResult.value = NabiResult.Error(Exception("Current user not found."))
                    _appointmentsResult.value = NabiResult.Success(emptyList()) // 사용자가 없으면 약속도 없다고 가정
                }
            } catch (e: Exception) {
                _userResult.value = NabiResult.Error(e)
                _appointmentsResult.value = NabiResult.Success(emptyList()) // 사용자 로드 실패시 약속도 비움
            }
        }
    }

    private fun loadAppointments(userUid: String) {
        viewModelScope.launch {
            appointmentRepository.getChatAppointments(userUid) // Flow<NabiResult<List<ChatAppointment>>> 반환 가정
                .collect { result ->
                    _appointmentsResult.value = result
                }
        }
    }

    // N일 계산 함수 (User 모델에 joinedTimestamp가 있다는 가정 하에 예시)
    // private fun calculateDays(joinedTimestamp: Long): String {
    //     val diff = System.currentTimeMillis() - joinedTimestamp
    //     return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toString()
    // }
}