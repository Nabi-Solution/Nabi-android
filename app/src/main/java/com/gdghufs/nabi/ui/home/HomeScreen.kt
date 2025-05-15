package com.gdghufs.nabi.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil3.Image
import com.gdghufs.nabi.R
import com.gdghufs.nabi.ui.theme.NabiTheme
import java.util.Date

@Composable
@Preview(showBackground = true)
fun HomeScreenPreview() {
    NabiTheme {
        HomeScreen("John Doe")
    }
}

@Composable
fun HomeScreen(displayName: String) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background_day),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 1f),
                            Color.White.copy(alpha = 0f)
                        )
                    )
                )
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            Text(
                text = "Hi, $displayName",
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

            Row(
                Modifier
                    .size(42.dp, 2.dp)
                    .background(color = Color.Black)
            ) {

            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Been using Navi for N days",
                fontSize = 10.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(80.dp))

            Text("Reservations")

            Spacer(Modifier.height(20.dp))

            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(2) {
                    ReservationItem("with Nabi", Date())
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White, RoundedCornerShape(32.dp, 32.dp, 0.dp, 0.dp))
                .padding(48.dp, 32.dp)
        ) {
            Text("Summary", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(28.dp))

            Text("status", fontSize = 14.sp, fontWeight = FontWeight.Normal)

            Spacer(Modifier.height(8.dp))

            Text("log", fontSize = 14.sp, fontWeight = FontWeight.Normal)

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

@Preview
@Composable
fun ReservationItemPreview() {
    NabiTheme {
        ReservationItem("John Doe", Date().apply {

        })
    }
}

@Composable
fun ReservationItem(name: String, time: Date) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(color = Color(0x99FFFFFF), shape = RoundedCornerShape(4.dp))
            .padding(20.dp, 16.dp)
    ) {
        Column(Modifier.padding()) {
            Text(
                text = name,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "at $time",
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}