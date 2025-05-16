package com.gdghufs.nabi.data.datasource

import com.gdghufs.nabi.data.model.ChatAppointment
import com.gdghufs.nabi.utils.NabiResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import com.google.firebase.firestore.ktx.toObjects

class FirestoreChatAppointmentDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatAppointmentDataSource {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val APPOINTMENTS_SUBCOLLECTION = "appointments"
    }

    override fun getChatAppointments(userUid: String): Flow<NabiResult<List<ChatAppointment>>> =
        callbackFlow {
            trySend(NabiResult.Loading) // 변경됨 (NabiResult)

            val todayStr = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Calendar.getInstance().time)

            val listenerRegistration = firestore
                .collection(APPOINTMENTS_SUBCOLLECTION)
                .whereEqualTo("userId", userUid)
                .orderBy("orderWeight")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        trySend(NabiResult.Error(error)).isSuccess // 변경됨 (NabiResult)
                        close(error)
                        return@addSnapshotListener
                    }

                    val appointments = snapshots?.toObjects<ChatAppointment>()
                        ?.filter {
                            it.checkedDates[todayStr] != true
                        } ?: emptyList()
                    trySend(NabiResult.Success(appointments)).isSuccess // 변경됨 (NabiResult)
                }
            awaitClose { listenerRegistration.remove() }
        }

    override suspend fun addChatAppointment(
        userUid: String,
        appointment: ChatAppointment
    ): NabiResult<Unit> {
        return try {
            val newAppointmentRef = firestore
                .collection(USERS_COLLECTION)
                .document(userUid)
                .collection(APPOINTMENTS_SUBCOLLECTION)
                .document()

            val finalAppointment = appointment.copy(
                userId = userUid,
                orderWeight = appointment.hour * 60 + appointment.minute
            )
            newAppointmentRef.set(finalAppointment).await()
            NabiResult.Success(Unit) // 변경됨 (NabiResult)
        } catch (e: Exception) {
            NabiResult.Error(e) // 변경됨 (NabiResult)
        }
    }

    override suspend fun updateChatAppointment(
        userUid: String,
        appointmentId: String,
        appointment: ChatAppointment
    ): NabiResult<Unit> {
        return try {
            val finalAppointment = appointment.copy(
                userId = userUid,
                orderWeight = appointment.hour * 60 + appointment.minute
            )
            firestore
                .collection(USERS_COLLECTION)
                .document(userUid)
                .collection(APPOINTMENTS_SUBCOLLECTION)
                .document(appointmentId)
                .set(finalAppointment, SetOptions.merge())
                .await()
            NabiResult.Success(Unit) // 변경됨 (NabiResult)
        } catch (e: Exception) {
            NabiResult.Error(e) // 변경됨 (NabiResult)
        }
    }

    override suspend fun updateCheckedDate(
        userUid: String,
        appointmentId: String,
        date: String,
        isChecked: Boolean
    ): NabiResult<Unit> {
        return try {
            firestore
                .collection(USERS_COLLECTION)
                .document(userUid)
                .collection(APPOINTMENTS_SUBCOLLECTION)
                .document(appointmentId)
                .update("checkedDates.$date", isChecked)
                .await()
            NabiResult.Success(Unit) // 변경됨 (NabiResult)
        } catch (e: Exception) {
            NabiResult.Error(e) // 변경됨 (NabiResult)
        }
    }
}