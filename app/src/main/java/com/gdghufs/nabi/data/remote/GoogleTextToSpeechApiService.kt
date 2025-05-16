package com.gdghufs.nabi.data.remote

import com.gdghufs.nabi.data.model.TextToSpeechRequest
import com.gdghufs.nabi.data.model.TextToSpeechResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleTextToSpeechApiService {
    @POST("v1/text:synthesize")
    suspend fun synthesizeSpeech(
        @Body request: TextToSpeechRequest,
        @Query("key") apiKey: String
    ): Response<TextToSpeechResponse>
}