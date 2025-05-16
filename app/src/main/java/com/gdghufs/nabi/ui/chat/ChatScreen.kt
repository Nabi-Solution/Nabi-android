package com.gdghufs.nabi.ui.chat // 적절한 패키지명으로 변경

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text // Material 3 Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gdghufs.nabi.R
import com.gdghufs.nabi.data.model.ChatMessage
import com.gdghufs.nabi.data.model.MessageType
import com.gdghufs.nabi.data.model.SenderType
import com.gdghufs.nabi.data.repository.ChatRepository
import com.gdghufs.nabi.data.repository.ChatRepositoryImpl
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily

@Preview
@Composable
fun ChatScreenPreview() {
    // Preview에서는 ViewModel을 직접 생성하거나 Mock 데이터를 사용
    val mockMessages = listOf(
        ChatMessage(messageId="1", sessionId = "prev_session", sender = SenderType.AI.value, message = "안녕하세요! 나비 AI 입니다.", type = MessageType.TEXT.value, time = System.currentTimeMillis() - 20000),
        ChatMessage(messageId="2", sessionId = "prev_session", sender = SenderType.PATIENT.value, message = "네, 안녕하세요!", type = MessageType.TEXT.value, time = System.currentTimeMillis() - 10000),
        ChatMessage(messageId="3", sessionId = "prev_session", sender = SenderType.AI.value, message = "오늘 기분은 어떠신가요?", type = MessageType.SELECT.value, choices = listOf("좋아요", "그냥 그래요", "안 좋아요"), time = System.currentTimeMillis())
    )
    NabiTheme {
        ChatScreenContent(
            messages = mockMessages,
            inputText = "메시지 입력 미리보기",
            onInputTextChanged = {},
            onSendMessage = {},
            onChoiceSelected = { _, _ -> }
        )
    }
}

@Composable
fun Header() {
    Row(
        Modifier
            .fillMaxWidth() // 전체 너비 채우도록 수정
            .padding(12.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(44.dp)
                .background(color = Color(0xffFFFBE0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.symbol), // 실제 drawable 리소스 사용
                contentDescription = "Nabi Symbol",
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "Nabi",
                color = Color(0xff202325),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(color = Color(0xff7DDE86), CircleShape)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Always active",
                    color = Color(0xff72777A),
                    fontFamily = RobotoPretendardFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp // 크기 명시
                )
            }
        }
    }
}

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState() // 에러 메시지 수집

    var inputText by remember { mutableStateOf("") } // TextField 상태는 UI 내부에서 관리

    // 메시지 목록 자동 스크롤
    val listState = rememberLazyListState()
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ChatScreenContent(
        messages = messages,
        inputText = inputText,
        onInputTextChanged = { newText -> inputText = newText },
        onSendMessage = {
            if (inputText.isNotBlank()) {
                viewModel.sendTextMessage(inputText)
                inputText = "" // 메시지 전송 후 입력 필드 초기화
            }
        },
        onChoiceSelected = { originalMsgText, choice ->
            viewModel.onChoiceSelected(originalMsgText, choice)
        },
        listState = listState,
        errorMessage = errorMessage
    )
}


@Composable
fun ChatScreenContent(
    messages: List<ChatMessage>,
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onChoiceSelected: (originalMessageText: String, choice: String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    errorMessage: String? = null
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Header()

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth() // 너비 채우기
                .weight(1f)
                .padding(horizontal = 12.dp), // 좌우 패딩 추가
            contentPadding = PaddingValues(vertical = 8.dp) // 아이템 상하 간격
        ) {
            items(messages, key = { it.messageId }) { message ->
                // 메시지 간 간격
                Spacer(modifier = Modifier.height(8.dp))

                when (message.sender) {
                    SenderType.AI.value -> {
                        if (message.type == MessageType.SELECT.value && message.choices != null) {
                            SelectMessage(
                                modifier = Modifier.fillMaxWidth(0.85f), // AI 메시지 최대 너비 제한
                                text = message.message,
                                choices = message.choices,
                                onChoiceClicked = { choice ->
                                    onChoiceSelected(message.message, choice)
                                }
                            )
                        } else {
                            NabiMessage(
                                modifier = Modifier.fillMaxWidth(0.85f), // AI 메시지 최대 너비 제한
                                text = message.message
                            )
                        }
                    }
                    SenderType.PATIENT.value -> {
                        UserMessage(
                            modifier = Modifier.fillMaxWidth(), // UserMessage는 Row 내부에서 정렬되므로 전체너비
                            text = message.message
                        )
                    }
                    else -> {
                        // 알 수 없는 발신자 처리 (예: 로그)
                        Log.w("ChatScreen", "Unknown sender type: ${message.sender}")
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically // 요소들 수직 중앙 정렬
        ) {
            BasicTextField(
                modifier = Modifier
                    .weight(1f) // 남은 공간 모두 차지
                    .defaultMinSize(minHeight = 44.dp) // 최소 높이 보장
                    .border(
                        (1.5).dp,
                        color = Color(0xff72777A),
                        shape = RoundedCornerShape(22.dp) // 더 둥글게
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp), // 패딩 조정
                value = inputText,
                onValueChange = onInputTextChanged,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = RobotoPretendardFamily,
                    color = Color(0xff000000)
                ),
                decorationBox = { innerTextField -> // Hint Text 구현
                    Box(contentAlignment = Alignment.CenterStart) { // 정렬 변경
                        if (inputText.isEmpty()) {
                            BasicText(
                                text = "Type a message...",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = RobotoPretendardFamily,
                                    color = Color(0xff72777A)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(Modifier.width(8.dp)) // 간격 줄임

            Box(
                Modifier
                    .size(44.dp)
                    .background(color = Color(0xff0070F0), shape = CircleShape)
                    .clickable(onClick = onSendMessage), // 클릭 이벤트 추가
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.size(19.dp),
                    painter = painterResource(R.drawable.send), // 실제 drawable 리소스
                    contentDescription = "Send message",
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}


@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun NabiMessagePreview() {
    NabiTheme {
        NabiMessage(text = "How was your energy level today?")
    }
}

@Composable
fun NabiMessage(modifier: Modifier = Modifier, text: String) {
    Row(modifier.padding(end = 40.dp)) { // 오른쪽 여백을 주어 사용자 메시지와 겹치지 않게
        Box(
            Modifier
                .size(44.dp)
                .background(color = Color(0xffFFFBE0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.symbol),
                contentDescription = "Nabi Symbol",
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .background(
                    color = Color(0xffF2F4F5),
                    shape = RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp)
                )
                .padding(16.dp)
        ) {
            Text(text = text, fontSize = 16.sp, fontFamily = RobotoPretendardFamily, color = Color.Black)
        }
    }
}


@Composable
fun SelectMessage(
    modifier: Modifier = Modifier,
    text: String,
    choices: List<String>,
    onChoiceClicked: (String) -> Unit
) {
    // NabiMessage와 유사한 레이아웃으로 감싸줌
    Row(modifier.padding(end = 40.dp)) {
        Box(
            Modifier
                .size(44.dp)
                .background(color = Color(0xffFFFBE0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.symbol),
                contentDescription = "Nabi Symbol",
            )
        }
        Spacer(Modifier.width(8.dp))

        // 기존 SelectMessage 내용을 여기에 배치
        Column(
            modifier
                .background(color = Color(0xffF2F8FF), shape = RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp)) // NabiMessage와 유사한 모양
                .padding(14.dp),
            horizontalAlignment = Alignment.Start // AI 메시지이므로 시작점 정렬
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = RobotoPretendardFamily,
                color = Color(0xff006be5),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                choices.forEach { choice ->
                    Box(
                        Modifier
                            .background(
                                color = Color(0xffffffff),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, Color(0xffeeeeee), RoundedCornerShape(16.dp))
                            .clickable { onChoiceClicked(choice) } // 선택지 클릭 이벤트
                            .padding(12.dp, 8.dp)
                    ) {
                        Text(
                            text = choice,
                            fontSize = 14.sp,
                            fontFamily = RobotoPretendardFamily,
                            color = Color(0xff006be5) // 선택지 텍스트 색상 변경
                        )
                    }
                    // Spacer(Modifier.width(8.dp)) // FlowRow가 간격 처리
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SelectMessagePreview() {
    NabiTheme {
        SelectMessage(
            Modifier.width(300.dp),
            "Select Message From AI",
            listOf("Choice 1 is quite long", "Choice 2", "Short 3", "Another Choice 4"),
            onChoiceClicked = {}
        )
    }
}


@Composable
fun UserMessage(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier
            // .fillMaxSize() // 전체 크기를 채우면 안됨. 메시지 크기만큼만.
            .padding(start = 40.dp), // 왼쪽 여백을 주어 AI 메시지와 겹치지 않게
        horizontalArrangement = Arrangement.End
    ) {
        // Spacer(Modifier.weight(1f)) // 오른쪽 정렬을 위해 Spacer 추가 (Row가 End 정렬하므로 필요 없을 수도)
        Box(
            Modifier
                .background(
                    color = Color(0xff0070F0),
                    shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 20.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = RobotoPretendardFamily,
                color = Color.White
            )
        }
        // User 아이콘을 원한다면 여기에 추가
        // Spacer(Modifier.width(8.dp))
        // Image(...)
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun UserMessagePreview() {
    NabiTheme {
        UserMessage(text = "I had a great day today!")
    }
}