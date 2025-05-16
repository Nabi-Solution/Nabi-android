package com.gdghufs.nabi.data.datasource

import android.util.Base64
import com.gdghufs.nabi.BuildConfig
import com.gdghufs.nabi.data.model.AudioConfig
import com.gdghufs.nabi.data.model.SynthesisInput
import com.gdghufs.nabi.data.model.TextToSpeechRequest
import com.gdghufs.nabi.data.model.VoiceSelectionParams
import com.gdghufs.nabi.data.remote.GoogleTextToSpeechApiService
import javax.inject.Inject

interface TextToSpeechDataSource {
    suspend fun synthesizeText(
        text: String,
        languageCode: String,
        voiceName: String?
    ): Result<ByteArray>
}

class TextToSpeechRemoteDataSource @Inject constructor(
    private val apiService: GoogleTextToSpeechApiService
) : TextToSpeechDataSource {

    private val apiKey = BuildConfig.GOOGLE_API_KEY

    override suspend fun synthesizeText(
        text: String,
        languageCode: String,
        voiceName: String?
    ): Result<ByteArray> {
        val request = TextToSpeechRequest(
            input = SynthesisInput(text),
            voice = VoiceSelectionParams(languageCode = languageCode, name = voiceName),
            audioConfig = AudioConfig(audioEncoding = "MP3") // MP3로 요청
        )
        return try {
            val response = apiService.synthesizeSpeech(request, apiKey)
            if (response.isSuccessful && response.body()?.audioContent != null) {
                val audioBytes = Base64.decode(
                    response.body()!!.audioContent,
                    Base64.DEFAULT
                ) // Google API는 RFC 2045 호환 Base64 사용
                Result.success(audioBytes)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}