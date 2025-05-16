package com.gdghufs.nabi.data.repository

import com.gdghufs.nabi.data.datasource.TextToSpeechDataSource
import com.gdghufs.nabi.utils.TextToSpeechCallback
import javax.inject.Inject

interface TextToSpeechRepository {
    suspend fun getSpeechAudio(text: String, languageCode: String = "ko-KR", voiceName: String? = "ko-KR-Chirp3-HD-Achernar"): Result<ByteArray>
}

class TextToSpeechRepositoryImpl @Inject constructor(
    private val remoteDataSource: TextToSpeechDataSource
) : TextToSpeechRepository {
    override suspend fun getSpeechAudio(text: String, languageCode: String, voiceName: String?): Result<ByteArray> {
        return remoteDataSource.synthesizeText(text, languageCode, voiceName)
    }
}