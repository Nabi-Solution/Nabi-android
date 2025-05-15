package com.gdghufs.nabi.ui.chat

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.LiveContentResponse.Status.Companion.TURN_COMPLETE
import com.google.firebase.vertexai.type.LiveSession
import com.google.firebase.vertexai.type.PublicPreviewAPI
import com.google.firebase.vertexai.type.ResponseModality
import com.google.firebase.vertexai.type.asTextOrNull
import com.google.firebase.vertexai.type.liveGenerationConfig
import com.google.firebase.vertexai.vertexAI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// 메시지 데이터 클래스
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUserMessage: Boolean
)

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    // LiveAPI 모델 설정
    @OptIn(PublicPreviewAPI::class)
    private val model = Firebase.vertexAI.liveModel(
        modelName = "gemini-2.0-flash-live-preview-04-09", // Live API 전용 모델
        generationConfig = liveGenerationConfig {
            responseModality = ResponseModality.TEXT // 모델 응답은 오디오로

        }
    )

    @OptIn(PublicPreviewAPI::class)
    private var liveSession: LiveSession? = null

    // 대화 메시지 목록 상태
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // 모델이 현재 응답 중인 텍스트 (스트리밍 효과용)
    private val _streamingNabiResponse = MutableStateFlow("")
    val streamingNabiResponse: StateFlow<String> = _streamingNabiResponse.asStateFlow()

    private val _isNabiTyping = MutableStateFlow(false)
    val isNabiTyping: StateFlow<Boolean> = _isNabiTyping.asStateFlow()

    private val _errorEvents = MutableStateFlow<String?>(null)
    val errorEvents: StateFlow<String?> = _errorEvents.asStateFlow()

    init {
        startConversation()
    }

    @OptIn(PublicPreviewAPI::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startConversation() {
        viewModelScope.launch @RequiresPermission(Manifest.permission.RECORD_AUDIO) {
            try {
                // 이전 세션이 있다면 닫기
                liveSession?.close()

                // 새 세션 시작
                liveSession = model.connect()
                Log.d("ChatViewModel", "LiveAPI session connected.")

//                 초기 메시지 (선택 사항)
                 addMessage("안녕하세요! 나비에게 무엇이든 물어보세요.", false)

                // startAudioConversation() 호출:
                // - 마이크를 통해 사용자 음성 입력을 자동으로 수신 시작
                // - 모델의 오디오 응답을 자동으로 재생
                liveSession?.startAudioConversation()
                Log.d("ChatViewModel", "Audio conversation started.")


                // 모델 응답 스트림 수신 및 처리
                liveSession?.receive()
                    ?.catch { e ->
                        Log.e("ChatViewModel", "Error collecting responses", e)
                        handleError("응답 수신 중 오류: ${e.message}")
                        _isNabiTyping.value = false
                    }
                    ?.onCompletion {
                        Log.d("ChatViewModel", "Response collection completed.")
                        _isNabiTyping.value = false
                        // 스트리밍 중이던 메시지가 있다면 확정
                        if (_streamingNabiResponse.value.isNotBlank()) {
                            addMessage(_streamingNabiResponse.value, false)
                            _streamingNabiResponse.value = ""
                        }
                    }
                    ?.collect { response ->
                        _isNabiTyping.value = true
                        Log.d("ChatViewModel", response.data?.parts?.getOrNull(0)?.asTextOrNull() ?: "")
                        response.text?.let { textChunk ->
                            _streamingNabiResponse.value += textChunk
                            Log.d("ChatViewModel", "Received chunk: $textChunk")
                        }
                        if (response.status == TURN_COMPLETE) {
                            Log.d("ChatViewModel", "End of turn received.")
                            if (_streamingNabiResponse.value.isNotBlank()) {
                                addMessage(_streamingNabiResponse.value, false)
                                _streamingNabiResponse.value = ""
                            }
                            _isNabiTyping.value = false
                            // 다음 사용자 입력을 위해 다시 오디오 대화 시작 (필요에 따라)
                            // liveSession?.startAudioConversation() // 이미 한번 시작했으면 계속 수신 대기 상태일 수 있음. 테스트 필요.
                        }
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error starting conversation", e)
                handleError("대화 시작 중 오류: ${e.localizedMessage}")
            }
        }
    }

    // 텍스트 메시지 전송 (음성 입력 대신 텍스트 입력을 사용할 경우)
    @OptIn(PublicPreviewAPI::class)
    fun sendTextMessage(userInput: String) {
        if (userInput.isBlank()) return

        addMessage(userInput, true) // 사용자 메시지 UI에 추가

        viewModelScope.launch {
            try {
                _isNabiTyping.value = true
                liveSession?.send(userInput)
                // sendMessage 후에도 session.response 스트림에서 응답을 받습니다.
                // startAudioConversation() 과 sendMessage() 를 함께 사용할 때의 동작은 API 문서를 면밀히 확인해야 합니다.
                // 일반적으로는 둘 중 하나의 입력 방식을 주로 사용합니다.
                // 만약 startAudioConversation()이 이미 활성화된 상태에서 sendMessage를 보내는 경우,
                // 음성 입력과 텍스트 입력이 어떻게 조율되는지 확인이 필요합니다.
                // 여기서는 startAudioConversation()을 주 입력으로 가정하고,
                // textMessage 전송은 보조적인 기능으로 간주하거나,
                // 또는 textMessage를 보낼 때는 startAudioConversation의 자동 음성 인식을 잠시 중단하는 로직이 필요할 수 있습니다.
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                handleError("메시지 전송 중 오류: ${e.message}")
                _isNabiTyping.value = false
            }
        }
    }


    private fun addMessage(text: String, isUser: Boolean) {
        val newMessage = ChatMessage(text = text, isUserMessage = isUser)
        _messages.value = _messages.value + newMessage
    }

    private fun handleError(errorMessage: String) {
        _errorEvents.value = errorMessage
        // UI에 오류 메시지를 표시하기 위해 _messages에 추가할 수도 있습니다.
        // addMessage("오류: $errorMessage", false)
    }

    fun consumeErrorEvent() {
        _errorEvents.value = null
    }

    @OptIn(PublicPreviewAPI::class)
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try {
                liveSession?.close()
                Log.d("ChatViewModel", "LiveAPI session closed.")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error closing session", e)
            }
        }
    }
}