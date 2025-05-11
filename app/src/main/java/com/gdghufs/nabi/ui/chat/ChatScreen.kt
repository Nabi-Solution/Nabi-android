package com.gdghufs.nabi.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdghufs.nabi.R
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily

@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen()
}

@Composable
fun ChatScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Row(
            Modifier
                .padding(12.dp)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.back_arrow),
                    contentDescription = null
                )
            }

            Spacer(Modifier.width(8.dp))

            Box(
                Modifier
                    .size(44.dp)
                    .background(color = Color(0xffFFFBE0), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.size(26.dp),
                    painter = painterResource(R.drawable.symbol),
                    contentDescription = null,
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
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(backgroundColor = android.graphics.Color.WHITE.toLong(), showBackground = true)
@Composable
fun NabiMessagePreview() {
    NabiTheme {
        NabiMessage("How was your energy level today?")
    }
}

@Composable
fun NabiMessage(text: String) {
    Row(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .size(44.dp)
                .background(color = Color(0xffFFFBE0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.drawable.symbol),
                contentDescription = null,
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(Modifier
            .background(color = Color(0xffF2F4F5), shape = RoundedCornerShape(0.dp, 20.dp, 20.dp, 20.dp))
            .padding(16.dp)
        ) {
            Text(text = text, fontSize = 16.sp, fontFamily = RobotoPretendardFamily)
        }
    }
}