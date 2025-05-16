package com.gdghufs.nabi.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ChatAppointment(
    val id: String? = null,
    val userId: String = "",           // 사용자 UID
    val name: String = "",              // 약속 이름 (예: "자가진단 알림")
    val hour: Int = 0,                  // 알림 시간 (0-23)
    val minute: Int = 0,                // 알림 분 (0-59)
    val orderWeight: Int = 0,           // 정렬 가중치 (hour * 60 + minute)
    val checkedDates: Map<String, Boolean> = emptyMap() // 날짜별 자가진단 완료 여부 ("yyyy-MM-dd" : true/false)
)