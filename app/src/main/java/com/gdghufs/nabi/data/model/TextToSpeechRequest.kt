package com.gdghufs.nabi.data.model

data class SynthesisInput(val text: String)

data class VoiceSelectionParams(
    val languageCode: String,
    val name: String? = null, // 특정 음성 모델 지정 (예: "ko-KR-Wavenet-A")
    val ssmlGender: String? = null // "FEMALE", "MALE", "NEUTRAL"
)

data class AudioConfig(
    val audioEncoding: String = "MP3" // "LINEAR16", "OGG_OPUS" 등
)

data class TextToSpeechRequest(
    val input: SynthesisInput,
    val voice: VoiceSelectionParams,
    val audioConfig: AudioConfig
)
