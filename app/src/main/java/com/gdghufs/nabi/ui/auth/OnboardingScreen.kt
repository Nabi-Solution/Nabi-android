package com.gdghufs.nabi.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdghufs.nabi.R
import com.gdghufs.nabi.ui.theme.AmiriFamily
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily

@Composable
@Preview
fun OnboardingScreenPreview() {
    NabiTheme {
        OnboardingScreen()
    }
}

@Composable
fun OnboardingScreen(
    onGoogleSignUpClick: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(color = Color(0xffFFFBE0))
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Image(
                modifier = Modifier.size(29.dp, 26.dp),
                painter = painterResource(R.drawable.symbol),
                contentDescription = null
            )
            Text(
                text = "Nabi",
                fontFamily = RobotoPretendardFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(42.dp))

        Text(
            "Easily reach\nthose who care\n— anytime you\nneed",
            fontFamily = AmiriFamily,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            lineHeight = 78.sp
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Stay connected, feel supported,\n" +
                    "and keep moving forward.",
            fontFamily = AmiriFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 26.sp,
            color = Color(0xffADBDB7)
        )

        Spacer(Modifier.height(48.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(color = Color.White, shape = CircleShape)
                    .border(1.dp, Color(0xff010101), CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        onGoogleSignUpClick()
                    }
            ) {
                Image(
                    painter = painterResource(R.drawable.google),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.height(30.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(color = Color(0xffD6E4E0))
            )
            Spacer(Modifier.width(16.dp))
            val orText = buildAnnotatedString {
                append("OR")
                addStyle(
                    SpanStyle(
                        color = Color(0xFF88918E),
                        fontSize = 14.sp,
                        drawStyle = Stroke(
                            width = 2f,
                        )
                    ),
                    0,
                    length
                )
            }

            Text(text = orText, fontFamily = RobotoPretendardFamily)
            Spacer(Modifier.width(16.dp))
            Spacer(
                Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(color = Color(0xffD6E4E0))
            )
        }

        Spacer(Modifier.height(30.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .background(color = Color(0xffF3F6F6), shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigateToSignUp() }
                .padding(16.dp)
        ) {
            Text(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center),
                text = "Sign up with Email",
                color = Color(0xff797C7B),
                fontFamily = RobotoPretendardFamily
            )
        }

        Spacer(Modifier.height(46.dp))

        Row(
            Modifier
                .align(Alignment.CenterHorizontally)
                .clickable {
                    onNavigateToLogin()
                }) {
            Text(
                "Existing account?",
                fontFamily = AmiriFamily,
                fontSize = 14.sp,
                color = Color(0xff88918E)
            )

            Spacer(Modifier.width(4.dp))

            Text(
                "Log in",
                fontFamily = AmiriFamily,
                fontSize = 14.sp,
                color = Color(0xffA02121)
            )
        }
    }
}