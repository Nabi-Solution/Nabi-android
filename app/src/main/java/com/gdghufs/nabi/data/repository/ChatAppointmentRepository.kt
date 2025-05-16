package com.gdghufs.nabi.data.repository

import com.gdghufs.nabi.data.model.ChatAppointment
import com.gdghufs.nabi.utils.NabiResult
import kotlinx.coroutines.flow.Flow

interface ChatAppointmentRepository {
    fun getChatAppointments(userUid: String): Flow<NabiResult<List<ChatAppointment>>> // 변경됨 (NabiResult)
    suspend fun addChatAppointment(userUid: String, appointment: ChatAppointment): NabiResult<Unit> // 변경됨 (NabiResult)
    suspend fun updateChatAppointment(userUid: String, appointment: ChatAppointment): NabiResult<Unit> // 변경됨 (NabiResult)
    suspend fun markAsCheckedToday(userUid: String, appointmentId: String): NabiResult<Unit> // 변경됨 (NabiResult)
    suspend fun updateCheckedStatusForDate(userUid: String, appointmentId: String, date: String, isChecked: Boolean): NabiResult<Unit> // 변경됨 (NabiResult)
}