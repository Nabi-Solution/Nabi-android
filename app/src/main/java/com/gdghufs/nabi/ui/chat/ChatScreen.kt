package com.gdghufs.nabi.ui.chat

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdghufs.nabi.R
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {} // 뒤로가기 네비게이션 콜백
) {
    val messages by viewModel.messages.collectAsState()
    val streamingNabiResponse by viewModel.streamingNabiResponse.collectAsState()
    val isNabiTyping by viewModel.isNabiTyping.collectAsState()
    val errorEvent by viewModel.errorEvents.collectAsState()

    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

//    오디오 녹음 권한 요청
//    val recordAudioPermissionState = rememberPermissionState(
//        Manifest.permission.RECORD_AUDIO
//    )
//
//    LaunchedEffect(Unit) {
//        if (!recordAudioPermissionState.status.isGranted) {
//            recordAudioPermissionState.launchPermissionRequest()
//        }
//    }
//
//    // ViewModel의 startConversation은 ViewModel의 init에서 호출되므로 여기서는 권한 체크 후 필요시 재시도 가능
//    LaunchedEffect(recordAudioPermissionState.status) {
//        if (recordAudioPermissionState.status.isGranted) {
//            // 권한이 부여되면 대화 시작 또는 재시작 (이미 init에서 시작했다면 중복 호출 방지 로직 필요할 수 있음)
//            // viewModel.startConversation() // ViewModel의 init에서 이미 호출됨
//        } else {
//            Toast.makeText(context, "마이크 권한이 필요합니다.", Toast.LENGTH_LONG).show()
//        }
//    }


    // 에러 메시지 표시
    LaunchedEffect(errorEvent) {
        errorEvent?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeErrorEvent() // 이벤트 소비
        }
    }

    // 새 메시지가 추가되거나, 나비가 타이핑 중일 때 스크롤
    LaunchedEffect(messages.size, streamingNabiResponse) {
        if (messages.isNotEmpty() || streamingNabiResponse.isNotEmpty()) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(messages.size.coerceAtLeast(0))
            }
        }
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            // LiveAPI의 startAudioConversation() 사용 시, 별도 입력창은 선택사항
            // 여기서는 음성 입력이 주가 되므로, 입력창을 생략하거나 간단한 상태 표시만 할 수 있습니다.
            // 만약 텍스트 입력도 지원하려면 아래 주석 해제
            // MessageInput(
            //     onSendMessage = { text -> viewModel.sendTextMessage(text) },
            //     isNabiTyping = isNabiTyping
            // )
            if (isNabiTyping && streamingNabiResponse.isBlank()) { // 응답 생성 중이지만 아직 텍스트가 없을 때 (로딩)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nabi가 듣고 있어요...")
                }
            }
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .background(color = Color.White)
                .padding(paddingValues) // Scaffold의 content padding 적용
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    if (message.isUserMessage) {
                        UserMessage(text = message.text)
                    } else {
                        NabiMessage(text = message.text)
                    }
                }
                // 나비가 현재 스트리밍 중인 메시지 표시
                if (streamingNabiResponse.isNotBlank()) {
                    item {
                        NabiMessage(text = streamingNabiResponse)
                    }
                }
            }
            // 만약 텍스트 입력이 필요하다면 하단에 배치
            // TextMessageInput(onSendMessage = { viewModel.sendTextMessage(it) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp) // 크기 살짝 줄임
                        .background(color = Color(0xffFFFBE0), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(24.dp), // 크기 살짝 줄임
                        painter = painterResource(R.drawable.symbol), // 실제 리소스 ID로 변경
                        contentDescription = "Nabi Symbol",
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Nabi",
                        color = Color(0xff202325),
                        fontSize = 16.sp, // 크기 살짝 키움
                        fontWeight = FontWeight.Bold // 강조
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
                            fontSize = 12.sp, // 크기 살짝 줄임
                            color = Color(0xff72777A),
                            fontFamily = RobotoPretendardFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, // Material 아이콘 사용
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}


@Composable
fun NabiMessage(text: String) {
    Row(
        Modifier
            .fillMaxWidth() // 너비 채우도록
            .padding(horizontal = 8.dp, vertical = 4.dp), // 패딩 추가
        horizontalArrangement = Arrangement.Start // 왼쪽 정렬
    ) {
        Box(
            Modifier
                .size(36.dp) // 크기 조정
                .background(color = Color(0xffFFFBE0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(22.dp), // 크기 조정
                painter = painterResource(R.drawable.symbol), // 실제 리소스 ID로 변경
                contentDescription = "Nabi Symbol",
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .background(
                    color = Color(0xffF2F4F5),
                    shape = RoundedCornerShape(topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp) // 모서리 한쪽만 뾰족하게
                )
                .padding(16.dp)
                .weight(1f, fill = false) // 내용에 맞게 너비 조절, 최대 너비는 Row에 의해 제한
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = RobotoPretendardFamily,
                color = Color.Black // 텍스트 색상 명시
            )
        }
        Spacer(Modifier.width(40.dp)) // 오른쪽 여백으로 사용자 메시지와 구분
    }
}

@Composable
fun UserMessage(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End // 오른쪽 정렬
    ) {
        Spacer(Modifier.width(40.dp)) // 왼쪽 여백으로 나비 메시지와 구분
        Box(
            Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary, // 테마 색상 사용
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp) // 반대쪽 모서리
                )
                .padding(16.dp)
                .weight(1f, fill = false)
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = RobotoPretendardFamily,
                color = MaterialTheme.colorScheme.onPrimary // 테마에 맞는 텍스트 색상
            )
        }
    }
}

// (선택 사항) 텍스트 입력 필드
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextMessageInput(
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("메시지를 입력하세요...") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = {
            if (text.isNotBlank()) {
                onSendMessage(text)
                text = ""
            }
        }) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Send Message",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Preview
@Composable
fun ChatScreenPreview() {
    NabiTheme {
//        // 미리보기용 ViewModel (실제로는 Hilt가 주입)
//        val previewViewModel = ChatViewModel()
        // 미리보기용 메시지 추가
        LaunchedEffect(Unit) {
            // previewViewModel.addMessage("미리보기 나비 메시지입니다.", false)
            // previewViewModel.addMessage("미리보기 사용자 메시지입니다.", true)
        }
    }
}//        ChatScreen(viewModel = previewViewModel)


@Preview(showBackground = true)
@Composable
fun NabiMessagePreview() {
    NabiTheme {
        NabiMessage("오늘 기분은 어떠셨나요?")
    }
}

@Preview(showBackground = true)
@Composable
fun UserMessagePreview() {
    NabiTheme {
        UserMessage("음... 그냥 그랬어요.")
    }
}