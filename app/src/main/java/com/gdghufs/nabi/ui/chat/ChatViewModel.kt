package com.gdghufs.nabi.ui.chat

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdghufs.nabi.data.model.ChatMessage
import com.gdghufs.nabi.data.model.MessageType
import com.gdghufs.nabi.data.model.SenderType
import com.gdghufs.nabi.data.repository.ChatRepository
import com.gdghufs.nabi.data.repository.TextToSpeechRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayDeque // 명시적 큐 사용
import javax.inject.Inject

enum class TtsPlaybackState { // 이전과 동일
    IDLE,
    LOADING,
    PLAYING,
    ERROR
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val textToSpeechRepository: TextToSpeechRepository,
    private val application: Application
) : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var lastSpokenAiMessageId: String? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _ttsPlaybackState = MutableStateFlow(TtsPlaybackState.IDLE)
    val ttsPlaybackState: StateFlow<TtsPlaybackState> = _ttsPlaybackState.asStateFlow()

    private val _ttsErrorMessage = MutableStateFlow<String?>(null)
    val ttsErrorMessage: StateFlow<String?> = _ttsErrorMessage.asStateFlow()

    private var currentSessionId: String = "test_session_123"

    private val ttsRequestQueue = ArrayDeque<String>()
    private val ttsQueueMutex = Mutex()
    private var isProcessingTtsJob = false
    private var currentTtsJob: Job? = null


    init {
        loadMessages(currentSessionId)
        viewModelScope.launch {
            val aiGreeting = ChatMessage(
                sessionId = currentSessionId,
                sender = SenderType.AI.value,
                message = "안녕하세요! 나비입니다. 오늘 기분은 어떠신가요?",
                type = MessageType.TEXT.value
            )
            repository.sendTextMessage(
                currentSessionId,
                aiGreeting.message,
                SenderType.AI,
                MessageType.TEXT
            )


            kotlinx.coroutines.delay(1500)
            val aiSelectMessage = ChatMessage(
                sessionId = currentSessionId,
                sender = SenderType.AI.value,
                message = "오늘 당신의 에너지 레벨은 어떠셨나요?",
                type = MessageType.SELECT.value,
                choices = listOf("매우 좋음", "좋음", "보통", "나쁨", "매우 나쁨")
            )
            repository.sendTextMessage(
                currentSessionId,
                aiSelectMessage.message,
                SenderType.AI,
                MessageType.SELECT,
                aiSelectMessage.choices
            )
        }
    }

    fun setSessionId(sessionId: String) {
        currentSessionId = sessionId
        lastSpokenAiMessageId = null
        _messages.value = emptyList()
        stopSpeaking()
        loadMessages(sessionId)
    }

    private fun loadMessages(sessionId: String) {
        viewModelScope.launch {
            repository.getMessages(sessionId)
                .catch { e ->
                    _errorMessage.value = "메시지 로딩 실패: ${e.message}"
                    Log.e("ChatViewModel", "Error loading messages", e)
                }
                .collect { currentMessageList ->
                    val previousLastMessage = _messages.value.lastOrNull()
                    _messages.value = currentMessageList
                    val newLastMessage = currentMessageList.lastOrNull()

                    if (newLastMessage != null &&
                        newLastMessage.sender == SenderType.AI.value &&
                        newLastMessage.type == MessageType.TEXT.value &&
                        newLastMessage.messageId != lastSpokenAiMessageId &&
                        (previousLastMessage?.messageId != newLastMessage.messageId || (_messages.value.size == 1 && previousLastMessage == null))
                    ) {
                        Log.d(
                            "ChatViewModel",
                            "New AI TEXT message for TTS: ${newLastMessage.message}"
                        )
                        requestSpeech(newLastMessage.message)
                        lastSpokenAiMessageId = newLastMessage.messageId
                    } else if (newLastMessage != null && newLastMessage.sender == SenderType.AI.value && newLastMessage.type == MessageType.TEXT.value && newLastMessage.messageId == lastSpokenAiMessageId) {
                        Log.d(
                            "ChatViewModel",
                            "AI message already spoken or in queue: ${newLastMessage.message}"
                        )
                    }
                }
        }
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank() || currentSessionId.isBlank()) return
        viewModelScope.launch {
            try {
                repository.sendTextMessage(
                    currentSessionId,
                    text,
                    SenderType.PATIENT,
                    MessageType.TEXT
                )
            } catch (e: Exception) {
                _errorMessage.value = "메시지 전송 실패: ${e.message}"
                Log.e("ChatViewModel", "Error sending text message", e)
            }
        }
    }

    fun onChoiceSelected(originalMessageText: String, choice: String) {
        if (currentSessionId.isBlank()) return
        viewModelScope.launch {
            try {
                repository.sendSelectResponseMessage(currentSessionId, originalMessageText, choice)
            } catch (e: Exception) {
                _errorMessage.value = "선택 응답 전송 실패: ${e.message}"
                Log.e("ChatViewModel", "Error sending choice response", e)
            }
        }
    }

    private fun requestSpeech(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            ttsQueueMutex.withLock {
                ttsRequestQueue.addLast(text)
                Log.d(
                    "ChatViewModel",
                    "Added to TTS queue: \"$text\". Queue size: ${ttsRequestQueue.size}"
                )
            }
            tryProcessNextInQueue()
        }
    }

    // 수정된 tryProcessNextInQueue 함수
    private fun tryProcessNextInQueue() {
        viewModelScope.launch { // 이 launch는 큐 체크 및 작업 시작 '트리거' 역할
            val textToSpeak: String // Lock 외부에서 사용할 변수

            ttsQueueMutex.withLock { // Lock 범위는 큐 조작 및 상태 플래그 업데이트로 최소화
                if (isProcessingTtsJob || ttsRequestQueue.isEmpty()) {
                    if (isProcessingTtsJob) Log.d("ChatViewModel", "TTS job already in progress.")
                    if (ttsRequestQueue.isEmpty() && !isProcessingTtsJob) Log.d(
                        "ChatViewModel",
                        "TTS queue is empty, nothing to process."
                    )
                    return@launch // 현재 launch (큐 체크 트리거) 종료
                }
                isProcessingTtsJob = true
                textToSpeak = ttsRequestQueue.removeFirst()
                Log.d(
                    "ChatViewModel",
                    "Dequeued for TTS: \"$textToSpeak\". Remaining in queue: ${ttsRequestQueue.size}"
                )
            } // Mutex 락 여기서 해제

            currentTtsJob = viewModelScope.launch {
                executeTTSJob(textToSpeak)
            }
        }
    }

    private suspend fun executeTTSJob(text: String) {
        _ttsPlaybackState.value = TtsPlaybackState.LOADING
        _ttsErrorMessage.value = null
        Log.d("ChatViewModel", "Requesting audio for TTS job: \"$text\"")

        textToSpeechRepository.getSpeechAudio(text).fold(
            onSuccess = { audioBytes ->
                Log.d("ChatViewModel", "Audio received for TTS job: \"$text\"")
                playAudio(audioBytes, text)
            },
            onFailure = { exception ->
                Log.e("ChatViewModel", "TTS API Error for job: \"$text\"", exception)
                _ttsPlaybackState.value = TtsPlaybackState.ERROR
                _ttsErrorMessage.value = "TTS API Error: ${exception.message}"
                signalTtsJobCompletionAndProcessNext()
            }
        )
    }

    private fun playAudio(audioBytes: ByteArray, originalText: String) {
        var tempAudioFile: File? = null
        try {
            tempAudioFile = createTempAudioFile(audioBytes)
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(application, Uri.fromFile(tempAudioFile))
                prepareAsync()

                setOnPreparedListener {
                    Log.d(
                        "ChatViewModel",
                        "MediaPlayer prepared, starting TTS for: \"$originalText\""
                    )
                    it.start()
                    _ttsPlaybackState.value = TtsPlaybackState.PLAYING
                }

                setOnCompletionListener {
                    Log.d("ChatViewModel", "TTS playback completed for: \"$originalText\"")
                    _ttsPlaybackState.value = TtsPlaybackState.IDLE
                    it.release()
                    if (mediaPlayer == it) mediaPlayer = null
                    tempAudioFile?.delete()
                    signalTtsJobCompletionAndProcessNext()
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(
                        "ChatViewModel",
                        "MediaPlayer Error (what:$what, extra:$extra) for: \"$originalText\""
                    )
                    _ttsPlaybackState.value = TtsPlaybackState.ERROR
                    _ttsErrorMessage.value = "MediaPlayer Error for: \"$originalText\""
                    mp.release()
                    if (mediaPlayer == mp) mediaPlayer = null
                    tempAudioFile?.delete()
                    signalTtsJobCompletionAndProcessNext()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Playback setup Error for: \"$originalText\"", e)
            _ttsPlaybackState.value = TtsPlaybackState.ERROR
            _ttsErrorMessage.value = "Playback setup Error: ${e.message}"
            mediaPlayer?.release()
            mediaPlayer = null
            tempAudioFile?.delete()
            signalTtsJobCompletionAndProcessNext()
        }
    }

    private fun signalTtsJobCompletionAndProcessNext() {
        viewModelScope.launch {
            ttsQueueMutex.withLock {
                isProcessingTtsJob = false
                Log.d("ChatViewModel", "TTS job completed. isProcessingTtsJob set to false.")
            }
            tryProcessNextInQueue()
        }
    }

    private fun createTempAudioFile(audioBytes: ByteArray): File {
        val outputDir = application.cacheDir
        val fileName = "tts_audio_${System.currentTimeMillis()}"
        val outputFile = File.createTempFile(fileName, ".mp3", outputDir)
        FileOutputStream(outputFile).use { fos ->
            fos.write(audioBytes)
        }
        Log.d("ChatViewModel", "Temp audio file created: ${outputFile.absolutePath}")
        return outputFile
    }

    fun stopSpeaking() {
        viewModelScope.launch {
            ttsQueueMutex.withLock {
                ttsRequestQueue.clear()
                currentTtsJob?.cancel()
                isProcessingTtsJob = false
                Log.d("ChatViewModel", "TTS queue cleared and processing stopped.")
            }
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            _ttsPlaybackState.value = TtsPlaybackState.IDLE
        }
    }

    private fun clearTemporaryAudioFiles() {
        val outputDir = application.cacheDir
        outputDir.listFiles { file ->
            file.name.startsWith("tts_audio_") && file.name.endsWith(".mp3")
        }?.forEach { file ->
            if (file.delete()) {
                Log.d("ChatViewModel", "Deleted temp audio file: ${file.name}")
            } else {
                Log.w("ChatViewModel", "Failed to delete temp audio file: ${file.name}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ChatViewModel", "onCleared called.")
        stopSpeaking()
        clearTemporaryAudioFiles()
        Log.d("ChatViewModel", "ChatViewModel cleared.")
    }
}