package com.gdghufs.nabi.data.datasource

import com.gdghufs.nabi.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatDataSource {
    fun getMessages(sessionId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: ChatMessage)
    suspend fun createChatSession(userId: String, chatAppointmentId: String?): String
}