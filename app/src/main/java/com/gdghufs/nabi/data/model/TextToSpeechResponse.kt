package com.gdghufs.nabi.data.model

data class TextToSpeechResponse(
    val audioContent: String? // Base64 인코딩된 오디오 데이터
)