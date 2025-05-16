package com.gdghufs.nabi.data.repository

import com.gdghufs.nabi.data.model.ChatMessage
import com.gdghufs.nabi.data.model.MessageType
import com.gdghufs.nabi.data.model.SenderType
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(sessionId: String): Flow<List<ChatMessage>>
    suspend fun sendTextMessage(sessionId: String, text: String, sender: SenderType, messageType: MessageType, choices: List<String>? = null)
    suspend fun sendSelectResponseMessage(sessionId: String, originalMessageText: String, selectedChoice: String)
}
