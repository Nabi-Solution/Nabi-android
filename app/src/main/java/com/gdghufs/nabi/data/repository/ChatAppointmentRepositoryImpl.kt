package com.gdghufs.nabi.data.repository

import com.gdghufs.nabi.data.datasource.ChatAppointmentDataSource
import com.gdghufs.nabi.data.model.ChatAppointment
import com.gdghufs.nabi.utils.NabiResult
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class ChatAppointmentRepositoryImpl @Inject constructor(
    private val dataSource: ChatAppointmentDataSource
) : ChatAppointmentRepository {

    override fun getChatAppointments(userUid: String): Flow<NabiResult<List<ChatAppointment>>> {
        return dataSource.getChatAppointments(userUid)
    }

    override suspend fun addChatAppointment(userUid: String, appointment: ChatAppointment): NabiResult<Unit> {
        return dataSource.addChatAppointment(userUid, appointment)
    }

    override suspend fun updateChatAppointment(userUid: String, appointment: ChatAppointment): NabiResult<Unit> {
        val appointmentId = appointment.id ?: return NabiResult.Error(IllegalArgumentException("Appointment ID cannot be null for update")) // 변경됨 (NabiResult)
        return dataSource.updateChatAppointment(userUid, appointmentId, appointment)
    }

    override suspend fun markAsCheckedToday(userUid: String, appointmentId: String): NabiResult<Unit> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        return dataSource.updateCheckedDate(userUid, appointmentId, todayStr, true)
    }

    override suspend fun updateCheckedStatusForDate(
        userUid: String,
        appointmentId: String,
        date: String,
        isChecked: Boolean
    ): NabiResult<Unit> {
        return dataSource.updateCheckedDate(userUid, appointmentId, date, isChecked)
    }
}