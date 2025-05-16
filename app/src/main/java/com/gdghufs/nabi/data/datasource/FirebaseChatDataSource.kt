package com.gdghufs.nabi.data.datasource

import com.gdghufs.nabi.data.model.ChatMessage
import com.gdghufs.nabi.data.model.ChatSession
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class FirebaseChatDataSource @Inject constructor(
    private val database: FirebaseDatabase
) : ChatDataSource {

    private val sessionsRef = database.getReference("chatSessions")

    override fun getMessages(sessionId: String): Flow<List<ChatMessage>> = callbackFlow {
        val sessionMessagesRef = sessionsRef.child(sessionId).child("chatMessages")

        // If you have a timestamp field in ChatMessage (e.g., "timestamp")
        // and want Firebase to order them, you can use:
        // val query = sessionMessagesRef.orderByChild("timestamp")
        // Otherwise, listening directly and sorting in code (as originally done) is also an option.
        val query = sessionMessagesRef // Listen directly to the messages node for this session

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(ChatMessage::class.java)?.apply {
                        // messageId is the key of the message under chatSessions/{sessionId}/chatMessages/{messageId}
                        this.messageId = dataSnapshot.key ?: ""
                        // The sessionId field in ChatMessage object will be populated if it's saved in the DB.
                        // If it's not saved in the message object itself anymore (because path implies it),
                        // you might want to set it here:
                        // this.sessionId = sessionId // if ChatMessage model has sessionId field
                    }
                }.sortedBy { it.getTimestampLong() } // Assumes ChatMessage has getTimestampLong()
                trySend(messages).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(valueEventListener)

        awaitClose { query.removeEventListener(valueEventListener) }
    }

    override suspend fun sendMessage(message: ChatMessage) {
        if (message.sessionId.isBlank()) {
            throw IllegalArgumentException("ChatMessage must have a valid sessionId to be saved.")
        }

        val sessionMessagesRef = sessionsRef.child(message.sessionId).child("chatMessages")

        val messageId = sessionMessagesRef.push().key
            ?: throw IllegalStateException("Couldn't get push key for messages in session ${message.sessionId}")

        sessionMessagesRef.child(messageId).setValue(message).await()
    }

    override suspend fun createChatSession(userId: String, chatAppointmentId: String?): String =
        suspendCoroutine { continuation ->
            val sessionId = sessionsRef.push().key
            if (sessionId == null) {
                continuation.resumeWithException(IllegalStateException("Could not create session ID"))
                return@suspendCoroutine
            }
            val newSession = ChatSession(
                userId = userId,
                chatAppointmentId = chatAppointmentId
            )
            sessionsRef.child(sessionId).setValue(newSession)
                .addOnSuccessListener { continuation.resume(sessionId) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
}