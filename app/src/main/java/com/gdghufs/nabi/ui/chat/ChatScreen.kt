package com.gdghufs.nabi.ui.chat

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.gdghufs.nabi.R // Ensure you have a placeholder icon, e.g., R.drawable.ic_selfie_placeholder
import com.gdghufs.nabi.data.model.ChatMessage
import com.gdghufs.nabi.data.model.MessageType
import com.gdghufs.nabi.data.model.SenderType
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily
import com.gdghufs.nabi.utils.ActualCameraFeed
import com.gdghufs.nabi.utils.SelfieCameraView

@Preview
@Composable
fun ChatScreenPreview() {
    val mockMessages = listOf(
        ChatMessage(
            messageId = "1",
            sessionId = "prev_session",
            sender = SenderType.AI.value,
            message = "Hello! I am Nabi AI.",
            type = MessageType.TEXT.value,
            time = System.currentTimeMillis() - 20000
        ),
        ChatMessage(
            messageId = "2",
            sessionId = "prev_session",
            sender = SenderType.PATIENT.value,
            message = "Yes, hello!",
            type = MessageType.TEXT.value,
            time = System.currentTimeMillis() - 10000
        ),
        ChatMessage(
            messageId = "3",
            sessionId = "prev_session",
            sender = SenderType.AI.value,
            message = "How are you feeling today?",
            type = MessageType.SELECT.value,
            choices = listOf("Good", "So-so", "Not good"),
            time = System.currentTimeMillis()
        )
    )
    NabiTheme {
        ChatScreenContent(
            messages = mockMessages,
            inputText = "Message input preview",
            onInputTextChanged = {},
            onSendMessage = {},
            onChoiceSelected = { _, _ -> },
            isAiSpeaking = false,
            isVoiceModeEnabled = true,
            onToggleVoiceMode = {},
            isSttListening = true
        )
    }
}

@Composable
fun Header(isAiSpeaking: Boolean) { // Passed based on ViewModel's ttsPlaybackState
    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(44.dp)
                .background(color = Color(0xffFFFBE0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.symbol),
                contentDescription = "Nabi Symbol",
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "Nabi",
                color = Color(0xff202325),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(
                            color = if (isAiSpeaking) Color(0xFFFF6B6B) else Color(0xff7DDE86), // Change color when speaking
                            CircleShape
                        )
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (isAiSpeaking) "Speaking..." else "Always active", // Change status text
                    color = Color(0xff72777A),
                    fontFamily = RobotoPretendardFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class) // For FlowRow
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val ttsPlaybackState by viewModel.ttsPlaybackState.collectAsState()
    val isVoiceModeEnabled by viewModel.isVoiceModeEnabled.collectAsState()

    val isAiSpeaking =
        ttsPlaybackState == TtsPlaybackState.PLAYING || ttsPlaybackState == TtsPlaybackState.LOADING

    var isSttListening by remember { mutableStateOf(false) }
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val startSttNow: () -> Unit = {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            // Condition: Voice mode enabled, AI not speaking, STT not already listening
            if (isVoiceModeEnabled && !isAiSpeaking && !isSttListening) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // Korean
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak...")
                }
                try {
                    speechRecognizer.startListening(intent)
                    // isSttListening = true; // Set in onReadyForSpeech
                    Log.d(
                        "ChatScreen",
                        "STT startListening called (Condition: isVoiceModeEnabled=$isVoiceModeEnabled, !isAiSpeaking=${!isAiSpeaking}, !isSttListening=${!isSttListening})"
                    )
                } catch (e: Exception) {
                    Log.e("ChatScreen", "STT startListening exception: ${e.message}", e)
                    isSttListening = false // Set to false definitively if an exception occurs
                }
            } else {
                Log.d(
                    "ChatScreen",
                    "STT startListening skipped (Condition: isVoiceModeEnabled=$isVoiceModeEnabled, !isAiSpeaking=${!isAiSpeaking}, !isSttListening=${!isSttListening})"
                )
            }
        } else {
            Log.e("ChatScreen", "Speech recognition not available on this device.")
            if (isVoiceModeEnabled) viewModel.toggleVoiceMode() // Disable mode if speech recognition is not available
        }
    }


    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                Log.d("ChatScreen", "RECORD_AUDIO permission granted")
                // Condition to start STT after permission grant: Voice mode enabled, AI not speaking, STT not listening
                if (isVoiceModeEnabled && !isAiSpeaking && !isSttListening) {
                    Log.d("ChatScreen", "Permission granted. Attempting to start STT.")
                    startSttNow()
                } else {
                    Log.d(
                        "ChatScreen",
                        "Permission granted, but STT not started. Conditions: isVoiceModeEnabled=$isVoiceModeEnabled, !isAiSpeaking=${!isAiSpeaking}, !isSttListening=${!isSttListening}"
                    )
                }
            } else {
                Log.w("ChatScreen", "RECORD_AUDIO permission denied")
                if (isVoiceModeEnabled) viewModel.toggleVoiceMode() // Disable voice mode if permission is denied
                // Notify user of permission necessity (e.g., Snackbar)
            }
        }
    )


    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isSttListening = true // Set to true here
                inputText = "" // Clear text field when voice input starts (optional)
                Log.d("ChatScreen", "onReadyForSpeech: isSttListening set to true")
            }

            override fun onBeginningOfSpeech() {
                Log.d("ChatScreen", "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                // isSttListening = false; // Finally process as false in onError or onResults
                Log.d("ChatScreen", "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                isSttListening = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error" // Usually happens if startListening is called too frequently
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No matching voice"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timeout"
                    else -> "Unknown speech recognition error"
                }
                Log.e(
                    "ChatScreen",
                    "STT Error: $errorMsg (code: $error). isSttListening set to false."
                )

                // If voice mode is on, AI is not speaking, and specific errors (NO_MATCH, SPEECH_TIMEOUT) occur, listen again automatically
                if (isVoiceModeEnabled && !isAiSpeaking && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                    Log.d(
                        "ChatScreen",
                        "STT No match or Timeout, and voice mode is ON. Trying to listen again."
                    )
                    startSttNow()
                } else if (error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    Log.w(
                        "ChatScreen",
                        "STT Recognizer was busy or client error. Will not auto-restart immediately."
                    )
                    // Consider a small delay before attempting to restart if needed, or let user re-initiate.
                }
            }

            override fun onResults(results: Bundle?) {
                isSttListening = false // Set to false here
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    Log.d(
                        "ChatScreen",
                        "STT Results: \"$recognizedText\". isSttListening set to false."
                    )
                    if (recognizedText.isNotBlank()) {
                        viewModel.sendTextMessage(recognizedText, true)
                        // After AI response, TTS plays, and if isVoiceModeEnabled, STT starts again (LaunchedEffect below)
                    } else {
                        // If the recognized text is blank, can try to listen again under the condition (isVoiceModeEnabled && !isAiSpeaking)
                        if (isVoiceModeEnabled && !isAiSpeaking) {
                            Log.d(
                                "ChatScreen",
                                "STT Result is blank. Attempting to listen again if conditions met."
                            )
                            startSttNow()
                        }
                    }
                } else {
                    // If results are null or empty
                    Log.d(
                        "ChatScreen",
                        "STT Results are null or empty. isSttListening set to false."
                    )
                    if (isVoiceModeEnabled && !isAiSpeaking) {
                        Log.d(
                            "ChatScreen",
                            "STT No result. Attempting to listen again if conditions met."
                        )
                        startSttNow()
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches =
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                    inputText = matches[0] // Display partial results (optional)
                    Log.d("ChatScreen", "STT PartialResults: ${matches[0]}")
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    // Set up SpeechRecognizer listener and manage lifecycle
    DisposableEffect(Unit) {
        Log.d("ChatScreen", "Setting up SpeechRecognizer listener.")
        speechRecognizer.setRecognitionListener(recognitionListener)
        onDispose {
            Log.d("ChatScreen", "Destroying SpeechRecognizer in DisposableEffect.")
            speechRecognizer.stopListening()
            speechRecognizer.destroy()
        }
    }

    // Release SpeechRecognizer based on Lifecycle events
    DisposableEffect(lifecycleOwner, speechRecognizer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                // The onDispose of this DisposableEffect block might be called, so it could be redundant,
                // but explicitly handling it at the Activity/Fragment's ON_DESTROY point can be safer.
                // speechRecognizer.destroy() // Handled in the onDispose of the DisposableEffect(Unit) above
                Log.d("ChatScreen", "Lifecycle.Event.ON_DESTROY observed.")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // speechRecognizer.destroy() // Handled in the onDispose of the DisposableEffect(Unit) above
            Log.d("ChatScreen", "Removing lifecycle observer for SpeechRecognizer.")
        }
    }


    // After AI's TTS ends, if voice mode is active, AI is not speaking, and STT is not listening, then start STT
    LaunchedEffect(ttsPlaybackState, isVoiceModeEnabled, isAiSpeaking, isSttListening) {
        if (ttsPlaybackState == TtsPlaybackState.IDLE && isVoiceModeEnabled && !isAiSpeaking && !isSttListening) {
            Log.d(
                "ChatScreen",
                "AI TTS likely ended, voice mode ON, AI not speaking, STT not listening. Attempting to start STT."
            )
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startSttNow()
            } else {
                Log.d(
                    "ChatScreen",
                    "Cannot start STT automatically post-TTS: RECORD_AUDIO permission not granted."
                )
                // At this point, permission is not requested automatically. Guide the user to activate it by pressing the microphone button.
            }
        }
    }

    // If AI starts speaking, stop STT
    LaunchedEffect(
        isAiSpeaking,
        isSttListening
    ) { // Add isSttListening as a key as well, to re-evaluate when STT state changes
        if (isAiSpeaking && isSttListening) {
            Log.d("ChatScreen", "AI started speaking, stopping STT.")
            speechRecognizer.stopListening() // Calling stopListening() will trigger the onEndOfSpeech -> onResults/onError flow
            // Using cancel() can stop immediately and not call callbacks (choose based on situation)
            isSttListening =
                false // Immediate state change (does not wait for stopListening callback)
        }
    }


    // Auto-scroll message list
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ChatScreenContent(
        messages = messages,
        inputText = inputText,
        onInputTextChanged = { newText ->
            if (!isAiSpeaking) inputText = newText // Prevent input while AI is speaking (optional)
        },
        onSendMessage = {
            if (inputText.isNotBlank() && !isAiSpeaking) {
                viewModel.sendTextMessage(inputText)
                inputText = ""
                if (isSttListening) {
                    Log.d("ChatScreen", "Message sent via text input, stopping STT.")
                    speechRecognizer.stopListening() // Stop STT when message is sent
                    isSttListening = false
                }
            }
        },
        onChoiceSelected = { originalMsgText, choice ->
            if (!isAiSpeaking) {
                viewModel.onChoiceSelected(originalMsgText, choice)
                if (isSttListening) {
                    Log.d("ChatScreen", "Choice selected, stopping STT.")
                    speechRecognizer.stopListening() // Stop STT when a choice is selected
                    isSttListening = false
                }
            }
        },
        listState = listState,
        errorMessage = errorMessage,
        isAiSpeaking = isAiSpeaking,
        isVoiceModeEnabled = isVoiceModeEnabled,
        onToggleVoiceMode = {
            if (isAiSpeaking) {
                Log.d("ChatScreen", "Cannot toggle voice mode: AI is speaking.")
                return@ChatScreenContent // Cannot toggle while AI is speaking
            }

            val newVoiceModeState = !isVoiceModeEnabled
            viewModel.toggleVoiceMode() // Change ViewModel state first (UI subscribes to this state)

            if (newVoiceModeState) { // When trying to turn on voice mode
                Log.d("ChatScreen", "Toggling voice mode ON by user action.")
                if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    if (!isSttListening) { // The condition that AI is not speaking is already checked above; if STT is not on, start it
                        Log.d("ChatScreen", "Voice mode ON, permission granted. Starting STT.")
                        startSttNow()
                    } else {
                        Log.d(
                            "ChatScreen",
                            "Voice mode ON, permission granted, but STT already listening."
                        )
                    }
                } else {
                    Log.d("ChatScreen", "Voice mode ON, requesting RECORD_AUDIO permission.")
                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            } else { // When trying to turn off voice mode
                Log.d("ChatScreen", "Toggling voice mode OFF by user action.")
                if (isSttListening) {
                    Log.d("ChatScreen", "Voice mode OFF, stopping STT.")
                    speechRecognizer.stopListening()
                    isSttListening = false // Reflect state immediately
                }
            }
        },
        isSttListening = isSttListening
    )
}

@Composable
fun ChatScreenContent(
    messages: List<ChatMessage>,
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onChoiceSelected: (originalMessageText: String, choice: String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    errorMessage: String? = null,
    isAiSpeaking: Boolean,
    isVoiceModeEnabled: Boolean,
    onToggleVoiceMode: () -> Unit,
    isSttListening: Boolean
) {
    Box( // Use Box to allow overlaying the selfie camera
        Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column(Modifier.fillMaxSize()) { // This Column contains your original chat content
            Header(isAiSpeaking = isAiSpeaking)

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.messageId }) { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    when (message.sender) {
                        SenderType.AI.value -> {
                            if (message.type == MessageType.SELECT.value && message.choices != null) {
                                SelectMessage(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    text = message.message,
                                    choices = message.choices,
                                    enabled = !isAiSpeaking, // Cannot select while AI is speaking
                                    onChoiceClicked = { choice ->
                                        // onChoiceSelected already checks isAiSpeaking in ChatScreen
                                        onChoiceSelected(message.message, choice)
                                    }
                                )
                            } else {
                                NabiMessage(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    text = message.message
                                )
                            }
                        }

                        SenderType.PATIENT.value -> {
                            UserMessage(
                                modifier = Modifier.fillMaxWidth(),
                                text = message.message
                            )
                        }

                        else -> {
                            Log.w("ChatScreen", "Unknown sender type: ${message.sender}")
                        }
                    }
                }
            }

            Row( // Input bar
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 12.dp
                    ), // Adjust padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                        .border(
                            (1.5).dp,
                            color = if (isAiSpeaking && !isSttListening) Color.LightGray else Color(
                                0xff72777A
                            ), // LightGray only when AI is speaking & not STT
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .background(
                            if (isAiSpeaking && !isSttListening) Color(0xFFF0F0F0) else Color.Transparent, // Background color only when AI is speaking & not STT
                            RoundedCornerShape(22.dp)
                        ),
                    value = inputText,
                    onValueChange = onInputTextChanged,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = RobotoPretendardFamily,
                        color = if (isAiSpeaking && !isSttListening) Color.Gray else Color(
                            0xff000000
                        )
                    ),
                    enabled = !isAiSpeaking || isSttListening, // Enabled when AI is not speaking, or when STT is listening (for displaying partial results)
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (inputText.isEmpty()) {
                                BasicText(
                                    text = when {
                                        isAiSpeaking && !isSttListening -> "AI is speaking..." // If STT is on, "Listening..." might appear instead
                                        isSttListening -> "Listening..."
                                        isVoiceModeEnabled -> "Tap and speak, or type"
                                        else -> "Enter a message..."
                                    },
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontFamily = RobotoPretendardFamily,
                                        color = Color(0xff72777A)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(Modifier.width(8.dp))

                // Voice mode toggle button
                IconButton(
                    onClick = onToggleVoiceMode,
                    enabled = !isAiSpeaking, // Disable button while AI is speaking (also checked inside onToggleVoiceMode, but for UI feedback)
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (isVoiceModeEnabled) R.drawable.mic_24px else R.drawable.mic_off_24px),
                        contentDescription = if (isVoiceModeEnabled) "Turn off voice mode" else "Turn on voice mode",
                        tint = when {
                            isAiSpeaking -> Color.LightGray // Disabled look while AI is speaking
                            isSttListening -> Color.Red // When STT is listening
                            isVoiceModeEnabled -> Color(0xff0070F0) // When voice mode is on
                            else -> Color.Gray // When voice mode is off
                        }
                    )
                }

                Spacer(Modifier.width(4.dp))

                Box(
                    Modifier
                        .size(44.dp)
                        .background(
                            color = if (isAiSpeaking || inputText.isBlank()) Color.LightGray else Color(
                                0xff0070F0
                            ),
                            shape = CircleShape
                        )
                        .clickable(
                            enabled = !isAiSpeaking && inputText.isNotBlank(), // Clickable only when AI is not speaking and text is present
                            onClick = onSendMessage // isAiSpeaking is also checked inside onSendMessage, but for UI feedback
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(19.dp),
                        painter = painterResource(R.drawable.send),
                        contentDescription = "Send message",
                        contentScale = ContentScale.Fit,
                        alpha = if (isAiSpeaking || inputText.isBlank()) 0.5f else 1.0f
                    )
                }
            }
        }

        SelfieCameraView(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // Adjust padding so it doesn't overlap the input bar too much.
                // The input bar has bottom=12.dp. Height of input bar is approx 44.dp + 8.dp top padding.
                // So, bottom padding for selfie view should be at least 12 + 44 + some_spacing.
                .padding(
                    start = 16.dp,
                    bottom = 76.dp
                ) // e.g. 12(input_bar_bottom_padding) + 44(input_bar_height) + 20(spacing)
        )
    }
}


@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun NabiMessagePreview() {
    NabiTheme {
        NabiMessage(text = "How was your energy level today?")
    }
}

@Composable
fun NabiMessage(modifier: Modifier = Modifier, text: String) {
    Row(modifier.padding(end = 40.dp)) {
        Box(
            Modifier
                .size(44.dp)
                .background(color = Color(0xffFFFBE0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.symbol),
                contentDescription = "Nabi Symbol",
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .background(
                    color = Color(0xffF2F4F5),
                    shape = RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = RobotoPretendardFamily,
                color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class) // For FlowRow
@Composable
fun SelectMessage(
    modifier: Modifier = Modifier,
    text: String,
    choices: List<String>,
    enabled: Boolean, // Added to disable during AI speech
    onChoiceClicked: (String) -> Unit
) {
    Row(modifier.padding(end = 40.dp)) {
        Box(
            Modifier
                .size(44.dp)
                .background(color = Color(0xffFFFBE0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.symbol),
                contentDescription = "Nabi Symbol",
            )
        }
        Spacer(Modifier.width(8.dp))

        Column(
            modifier = modifier // Apply modifier directly to Column
                .background(
                    color = Color(0xffF2F8FF),
                    shape = RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp)
                )
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = RobotoPretendardFamily,
                color = Color(0xff006be5),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp)) // Slightly increase spacing

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Horizontal spacing between choices
                verticalArrangement = Arrangement.spacedBy(8.dp)   // Vertical spacing between choices (if multiple lines)
            ) {
                choices.forEach { choice ->
                    Box(
                        Modifier
                            .background(
                                color = if (enabled) Color.White else Color(0xFFE0E0E0), // Color when disabled
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (enabled) Color(0xffeeeeee) else Color(0xFFBDBDBD),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = enabled) { // Determine clickability based on enabled state
                                if (enabled) onChoiceClicked(choice)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = choice,
                            fontSize = 14.sp,
                            fontFamily = RobotoPretendardFamily,
                            color = if (enabled) Color(0xff006be5) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SelectMessagePreview() {
    NabiTheme {
        SelectMessage(
            Modifier.width(300.dp),
            "Select Message From AI",
            listOf(
                "Choice 1 is quite long",
                "Choice 2",
                "Short 3",
                "Another Choice 4",
                "Choice 5",
                "Choice 6"
            ),
            enabled = true,
            onChoiceClicked = {}
        )
    }
}


@Composable
fun UserMessage(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier
            .padding(start = 40.dp)
            .fillMaxWidth(), // Make Row occupy full width and align Box to the right
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            Modifier
                .background(
                    color = Color(0xff0070F0),
                    shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 20.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = RobotoPretendardFamily,
                color = Color.White
            )
        }
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun UserMessagePreview() {
    NabiTheme {
        UserMessage(text = "I had a great day today!")
    }
}