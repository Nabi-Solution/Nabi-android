package com.gdghufs.nabi.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue

data class ChatSession(
    @get:Exclude var sessionId: String = "", // Firebase에서 자동 생성된 키를 사용하기 위해 Exclude
    val userId: String = "",
    val startedAt: Any = ServerValue.TIMESTAMP, // Long 대신 Any로 하여 ServerValue.TIMESTAMP 사용
    val chatAppointmentId: String? = null
)

data class ChatMessage(
    @get:Exclude var messageId: String = "", // Firebase에서 자동 생성된 키를 사용하기 위해 Exclude
    val sessionId: String = "",
    val sender: String = "patient", // "patient" | "ai"
    val message: String = "",
    val type: String = "text",      // "text" | "video" | "select" 등
    val time: Any = ServerValue.TIMESTAMP, // Long 대신 Any로 하여 ServerValue.TIMESTAMP 사용
    val choices: List<String>? = null // SelectMessage를 위한 필드
) {
    @Exclude
    fun getTimestampLong(): Long {
        return if (time is Long) time else 0L
    }
}

enum class SenderType(val value: String) {
    PATIENT("patient"),
    AI("ai")
}

enum class MessageType(val value: String) {
    TEXT("text"),
    VIDEO("video"),
    SELECT("select") // SelectMessage를 위한 타입 추가
}