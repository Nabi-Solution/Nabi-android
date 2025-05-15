package com.gdghufs.nabi.ui.today

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdghufs.nabi.R
import com.gdghufs.nabi.ui.common.TimeTag
import com.gdghufs.nabi.ui.common.TimeTagType
import com.gdghufs.nabi.ui.theme.AmiriFamily
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily


@Preview(showBackground = true)
@Composable
fun TodayScreenPreview() {
    TodayScreen()
}

@Composable
fun TodayScreen() {
    val listState = rememberLazyListState()
    val showShadow by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Today",
                    fontFamily = AmiriFamily,
                    color = Color(0xff740D0D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            if (showShadow) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x1A000000),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    Text(
                        "17 May",
                        fontFamily = AmiriFamily,
                        color = Color(0xffE2B2B2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(24.dp))

                    Text(
                        "To-Do",
                        fontFamily = AmiriFamily,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            items(3) { index ->
                TodoItem()
                if (index < 2) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = Color(0x1A000000)
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))

                Text(
                    "Medication",
                    fontFamily = AmiriFamily,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(16.dp))
            }

            items(3) { index ->
                TodoItem()
                if (index < 2) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = Color(0x1A000000)
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))

                Text(
                    "Suggestions",
                    fontFamily = AmiriFamily,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(16.dp))
            }

            items(3) { index ->
                SuggestionItem()

                if (index < 2) {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoItemPreview() {
    TodoItem()
}

@Composable
fun TodoItem() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier
                .fillMaxWidth()
                .weight(0.6f)
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .background(color = Color(0xfff2f2f2), shape = CircleShape)
            )

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Todo Item",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
                Text("Note", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .weight(0.4f)
        ) {

            TimeTag(TimeTagType.AnyTime, modifier = Modifier.align(Alignment.CenterStart))

            Box(
                Modifier
                    .size(28.dp)
                    .background(color = Color(0xffD9D9D9), shape = RoundedCornerShape(5.dp))
                    .align(Alignment.CenterEnd)
            ) {

            }
        }
    }
}

@Composable
fun SuggestionItem() {
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(4.dp),
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .background(color = Color(0xffFCFFD9), shape = RoundedCornerShape(4.dp))
            .padding(20.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text("Suggestion Text", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text("Suggestion Description", fontSize = 10.sp, fontWeight = FontWeight.Normal)
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable {

                }) {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                painter = painterResource(R.drawable.add_circle_24px),
                colorFilter = ColorFilter.tint(Color.Black),
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
fun SuggestionItemPreview() {
    NabiTheme {
        SuggestionItem()
    }
}