package com.gdghufs.nabi.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // items(List) 사용을 위해 추가
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator // 로딩 인디케이터
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdghufs.nabi.R
import com.gdghufs.nabi.data.model.ChatAppointment
import com.gdghufs.nabi.domain.model.User
import com.gdghufs.nabi.ui.dashboard.DashboardUiState // DashboardUiState 경로
import com.gdghufs.nabi.ui.dashboard.DashboardViewModel // DashboardViewModel 경로
import com.gdghufs.nabi.ui.theme.NabiTheme
import java.text.SimpleDateFormat // 시간 포맷팅
import java.util.Locale         // 시간 포맷팅

// Preview는 ViewModel 없이 UI의 정적 모습을 보기 위함이므로,
// ViewModel과 연결된 HomeScreen을 직접 Preview하기보다
// HomeScreenContent와 같은 내부 UI 함수를 만들어 Preview하는 것이 좋습니다.
@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    NabiTheme {
        // Preview용 가짜 데이터로 HomeScreenContent를 호출
        val previewState = DashboardUiState(
            user = User(uid="1", displayName = "John Doe", summary = "Feeling good today. Slept well.", email = "", isEmailVerified = false, role = ""),
            appointments = listOf(
                ChatAppointment(id="a1", name = "Morning Self-Check", hour = 9, minute = 0),
                ChatAppointment(id="a2", name = "Evening Reflection", hour = 21, minute = 30)
            ),
            daysUsingNavi = "15",
            isLoading = false
        )
        HomeScreenContent(uiState = previewState)
    }
}

// ViewModel과 연결되는 메인 HomeScreen
@Composable
fun HomeScreen(
    viewModel: DashboardViewModel = hiltViewModel() // Hilt ViewModel 주입
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(uiState = uiState)
}

// UI 로직만 담당하는 Composable (ViewModel 의존성 없음)
@Composable
fun HomeScreenContent(uiState: DashboardUiState) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background_day),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // 그라데이션 오버레이 (디자인 유지)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 1.0f), // 약간의 투명도 조절
                            Color.White.copy(alpha = 0f)
                        ),
                    )
                )
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.errorMessage != null) {
            // 에러 메시지 표시 (간단한 Text 또는 좀 더 정교한 UI 가능)
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error: ${uiState.errorMessage}",
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // 메인 컨텐츠 표시
            Column(
                Modifier
                    .fillMaxSize()
                    // 패딩 조정 (기존 48.dp 유지 또는 화면 크기에 따라 조절)
                    .padding(horizontal = 32.dp, vertical = 48.dp)
            ) {
                // 상단 사용자 정보
                Text(
                    text = "Hi, ${uiState.user?.displayName ?: "User"}", // ViewModel 데이터 사용
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "how's it going?",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(Modifier.height(16.dp))
                Row( // 구분선 (디자인 유지)
                    Modifier
                        .size(42.dp, 2.dp)
                        .background(color = Color.Black) // 또는 MaterialTheme.colorScheme.primary
                ) {}
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Been using Navi for ${uiState.daysUsingNavi} days", // ViewModel 데이터 사용
                    fontSize = 10.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                )

                Spacer(Modifier.height(80.dp)) // 간격 조정 (기존 80.dp)

                Text(
                    "Reservations",
                    fontSize = 18.sp, // 약간 조정
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(16.dp)) // 약간 조정

                // 약속 목록 (LazyColumn)
                if (uiState.appointments.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No reservations scheduled.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        // 하단 Summary 영역이 고정될 수 있도록 weight(1f)를 주거나,
                        // LazyColumn의 높이를 제한해야 합니다.
                        // 여기서는 LazyColumn이 남은 공간을 모두 차지하도록 하고,
                        // 하단 Summary가 그 위에 오도록 Box 내에서 Column 정렬을 이용합니다.
                        // 또는 LazyColumn의 높이를 명시적으로 지정할 수도 있습니다.
                        // Modifier.heightIn(max = 300.dp) // 예시: 최대 높이 지정
                        modifier = Modifier.weight(1f), // 이 Column 내에서 남은 세로 공간 차지
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.appointments) { appointment -> // ViewModel 데이터 사용
                            ReservationItem(appointment = appointment)
                        }
                    }
                }
            }

            // 하단 Summary (디자인 유지)
            Column(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Color.White,
                        RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    ) // 위쪽 모서리만 둥글게
                    .padding(horizontal = 32.dp, vertical = 32.dp) // 패딩 조정 (기존 48.dp, 32.dp)
            ) {
                Text(
                    "Summary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(20.dp)) // 간격 조정

                // "status", "log" 대신 ViewModel의 summary 사용
                Text(
                    text = uiState.user?.summary ?: "No summary available.", // ViewModel 데이터 사용
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (uiState.user?.summary.isNullOrEmpty()) Color.Gray else Color.DarkGray,
                    lineHeight = 20.sp // 줄 간격 추가
                )

                Spacer(Modifier.height(28.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Disclaimer: Information based on user input.",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xffa0a0a0)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationItemPreview() {
    NabiTheme {
        // ChatAppointment 객체로 Preview 수정
        ReservationItem(
            appointment = ChatAppointment(name = "Doctor's Visit", hour = 14, minute = 30)
        )
    }
}

@Composable
fun ReservationItem(appointment: ChatAppointment) { // ChatAppointment 객체를 받도록 수정
    // 시간 포맷 (예: "02:30 PM")
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val calendar = remember { java.util.Calendar.getInstance() }
    calendar.set(java.util.Calendar.HOUR_OF_DAY, appointment.hour)
    calendar.set(java.util.Calendar.MINUTE, appointment.minute)
    val displayTime = timeFormatter.format(calendar.time)

    Row(
        Modifier
            .fillMaxWidth()
            .background(color = Color.White.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp)) // 약간의 투명도와 모서리 둥글기 조절
            .padding(horizontal = 16.dp, vertical = 12.dp), // 패딩 약간 조절
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘 추가 (선택 사항)
        // Image(painter = painterResource(id = R.drawable.ic_event_note_24), contentDescription = "Reservation icon", modifier = Modifier.size(24.dp))
        // Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = appointment.name, // ViewModel 데이터 사용
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = "at $displayTime", // 포맷된 시간 사용
                color = Color.DarkGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}