package com.gdghufs.nabi.data.repository

import com.gdghufs.nabi.data.datasource.ChatDataSource
import com.gdghufs.nabi.data.model.ChatMessage
import com.gdghufs.nabi.data.model.MessageType
import com.gdghufs.nabi.data.model.SenderType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val dataSource: ChatDataSource
) : ChatRepository {

    override fun getMessages(sessionId: String): Flow<List<ChatMessage>> {
        return dataSource.getMessages(sessionId)
    }

    override suspend fun sendTextMessage(
        sessionId: String,
        text: String,
        sender: SenderType,
        messageType: MessageType,
        choices: List<String>?
    ) {
        val message = ChatMessage(
            sessionId = sessionId,
            sender = sender.value,
            message = text,
            type = MessageType.TEXT.value
        )
        dataSource.sendMessage(message)
    }

    override suspend fun sendSelectResponseMessage(sessionId: String, originalMessageText: String, selectedChoice: String) {
        val userMessage = ChatMessage(
            sessionId = sessionId,
            sender = SenderType.PATIENT.value,
            message = selectedChoice, // 사용자가 선택한 내용을 메시지로
            type = MessageType.TEXT.value // 일반 텍스트 메시지로 처리
        )
        dataSource.sendMessage(userMessage)
    }
}