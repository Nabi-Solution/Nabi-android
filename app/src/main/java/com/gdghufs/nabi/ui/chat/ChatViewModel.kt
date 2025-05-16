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
import com.gdghufs.nabi.data.repository.UserRepository
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
import java.util.ArrayDeque
import java.util.UUID
import javax.inject.Inject

enum class TtsPlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    ERROR
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val userRepository: UserRepository,
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

    // Voice mode state
    private val _isVoiceModeEnabled =
        MutableStateFlow(false) // Initial value can be kept as false or changed to true if needed
    val isVoiceModeEnabled: StateFlow<Boolean> = _isVoiceModeEnabled.asStateFlow()

    private var currentSessionId: String = UUID.randomUUID().toString()

    private val ttsRequestQueue = ArrayDeque<String>()
    private val ttsQueueMutex = Mutex()
    private var isProcessingTtsJob = false
    private var currentTtsJob: Job? = null

    init {
        // Start AI's first utterance
        viewModelScope.launch {
            currentSessionId = userRepository.getCurrentUser()?.uid + "_" + currentSessionId
            loadMessages(currentSessionId)
            // First text message (TTS target)
            val aiGreetingMessage = "Hello! This is Nabi. How are you feeling today?"
            repository.sendTextMessage(
                currentSessionId,
                aiGreetingMessage,
                SenderType.AI,
                MessageType.TEXT
            )

            // Choice message after a slight delay (not a TTS target)
            kotlinx.coroutines.delay(1500) // Delay can be adjusted considering TTS playback time
            val aiSelectMessageText = "How was your energy level today?"
            val choices = listOf("Very Good", "Good", "Average", "Bad", "Very Bad")
            repository.sendTextMessage(
                currentSessionId,
                aiSelectMessageText,
                SenderType.AI,
                MessageType.SELECT,
                choices
            )

            // Enable voice mode after AI's first utterance
            if (!_isVoiceModeEnabled.value) {
                _isVoiceModeEnabled.value = true
                Log.d("ChatViewModel", "Default voice mode enabled after initial AI message.")
            }
        }
    }

    fun setSessionId(sessionId: String) {
        currentSessionId = sessionId
        lastSpokenAiMessageId = null
        _messages.value = emptyList()
        stopSpeaking() // Stop TTS
        // Initialize voice mode related states as needed
        // e.g., If you want to reset voice mode to default (e.g., false) when a new session starts, handle it here
        // _isVoiceModeEnabled.value = false // or true, depending on policy
        loadMessages(sessionId)
    }

    private fun loadMessages(sessionId: String) {
        viewModelScope.launch {
            repository.getMessages(sessionId)
                .catch { e ->
                    _errorMessage.value = "Failed to load messages: ${e.message}"
                    Log.e("ChatViewModel", "Error loading messages", e)
                }
                .collect { currentMessageList ->
                    val previousLastMessage = _messages.value.lastOrNull()
                    _messages.value = currentMessageList
                    val newLastMessage = currentMessageList.lastOrNull()

                    if (newLastMessage != null &&
                        newLastMessage.sender == SenderType.AI.value &&
                        newLastMessage.type == MessageType.TEXT.value && // TTS for TEXT type messages only
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

    fun sendTextMessage(text: String, fromSTT: Boolean = false) {
        if (text.isBlank() || currentSessionId.isBlank()) return
        if (fromSTT && !_isVoiceModeEnabled.value) return
        viewModelScope.launch {
            try {
                repository.sendTextMessage(
                    currentSessionId,
                    text,
                    SenderType.PATIENT,
                    MessageType.TEXT
                )
                _isVoiceModeEnabled.value =
                    false // Consider if STT sending text should turn off voice mode
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send message: ${e.message}"
                Log.e("ChatViewModel", "Error sending text message", e)
            }
        }
    }

    fun onChoiceSelected(originalMessageText: String, choice: String) {
        if (currentSessionId.isBlank()) return
        viewModelScope.launch {
            try {
                // First, display the user's selection as a plain text message
                repository.sendTextMessage(
                    currentSessionId,
                    choice, // Selected content as message
                    SenderType.PATIENT,
                    MessageType.TEXT // Process as plain text
                )
                // Then, internally process the selection response (e.g., decide AI's next action)
                // This part might be handled by the server or AI logic. Here, only a local log is left as an example.
                Log.d(
                    "ChatViewModel",
                    "Choice selected: '$choice' for question '$originalMessageText'"
                )
                // If an immediate AI response message to the selection is needed, call repository.sendTextMessage(..., SenderType.AI, ...) here

            } catch (e: Exception) {
                _errorMessage.value = "Failed to send choice response: ${e.message}"
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

    private fun tryProcessNextInQueue() {
        viewModelScope.launch {
            val textToSpeak: String

            ttsQueueMutex.withLock {
                if (isProcessingTtsJob || ttsRequestQueue.isEmpty()) {
                    if (isProcessingTtsJob) Log.d("ChatViewModel", "TTS job already in progress.")
                    if (ttsRequestQueue.isEmpty() && !isProcessingTtsJob) Log.d(
                        "ChatViewModel",
                        "TTS queue is empty, nothing to process."
                    )
                    return@launch
                }
                isProcessingTtsJob = true
                textToSpeak = ttsRequestQueue.removeFirst()
                Log.d(
                    "ChatViewModel",
                    "Dequeued for TTS: \"$textToSpeak\". Remaining in queue: ${ttsRequestQueue.size}"
                )
            }

            currentTtsJob = viewModelScope.launch {
                executeTTSJob(textToSpeak)
            }
        }
    }

    private suspend fun executeTTSJob(text: String) {
        _ttsPlaybackState.value = TtsPlaybackState.LOADING
        _ttsErrorMessage.value = null
        Log.d("ChatViewModel", "Requesting audio for TTS job: \"$text\"")

        // _isVoiceModeEnabled.value = true // TTS starting does not necessarily mean voice mode is enabled by user. It's for AI speech.

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
            // Ensure current TTS Job is completed before attempting to process next TTS
            currentTtsJob = null // Remove current Job reference
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
                currentTtsJob?.cancel() // Cancel currently ongoing TTS Job
                currentTtsJob = null
                isProcessingTtsJob = false
                Log.d("ChatViewModel", "TTS queue cleared and current TTS job cancelled.")
            }
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release() // Release MediaPlayer resources
            }
            mediaPlayer = null
            _ttsPlaybackState.value = TtsPlaybackState.IDLE
            Log.d("ChatViewModel", "MediaPlayer stopped and released.")
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

    // Voice mode toggle function
    fun toggleVoiceMode() {
        _isVoiceModeEnabled.value = !_isVoiceModeEnabled.value
        Log.d("ChatViewModel", "Voice mode toggled. Enabled: ${isVoiceModeEnabled.value}")
        if (!_isVoiceModeEnabled.value) {
            // Additional tasks when voice mode is deactivated (e.g., notification to force stop STT - handled in UI)
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.d("ChatViewModel", "onCleared called.")
        stopSpeaking()
        clearTemporaryAudioFiles()
        mediaPlayer?.release() // Reliably release MediaPlayer resources when ViewModel is cleared
        mediaPlayer = null
        Log.d("ChatViewModel", "ChatViewModel cleared.")
    }
}