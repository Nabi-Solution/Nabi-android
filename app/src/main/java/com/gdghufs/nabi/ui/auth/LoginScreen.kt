package com.gdghufs.nabi.ui.auth

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdghufs.nabi.R
import com.gdghufs.nabi.ui.theme.AccentYellow
import com.gdghufs.nabi.ui.theme.NabiTheme

@Composable
fun LoginScreen(
    isLoading: Boolean,
    onLoginClick: (String, String) -> Unit,
    onGoogleLoginClick: () -> Unit,
    onSwitchToSignUpClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val isLoginEnabled = isEmailValid(email) && password.isNotEmpty() && !isLoading

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(45.dp))

        Box(
            Modifier
                .size(24.dp)
                .align(Alignment.Start)
                .clip(CircleShape)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(24.dp)) {
                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Back"
                )
            }
        }

        Spacer(Modifier.height(45.dp))

        Text(
            text = "Log in to Nabi!",
            style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        )

        Spacer(Modifier.height(38.dp))

        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            color = Color(0xff797C7B),
            text = "Welcome back! Sign in using your social account or email to continue us",
            style = TextStyle(fontSize = 14.sp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(30.dp))

        IconButton(
            onClick = { if (!isLoading) onGoogleLoginClick() },
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, Color(0xffCDD1D0), CircleShape),
            enabled = !isLoading
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.google), // 실제 리소스 확인
                contentDescription = "Google Login"
            )
        }

        Spacer(Modifier.height(60.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(color = Color(0xffcdd1d0))
            )
            Spacer(Modifier.width(16.dp))
            Text(text = "OR", style = TextStyle(fontSize = 14.sp, color = Color(0xff797C7B)))
            Spacer(Modifier.width(16.dp))
            Spacer(
                Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(color = Color(0xffcdd1d0))
            )
        }

        Spacer(Modifier.height(45.dp))

        Column(Modifier.fillMaxWidth()) {
            SignField(
                label = "Your email",
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email,
                isError = !isEmailValid(email) && email.isNotEmpty(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(24.dp))

            SignField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image =
                        if (passwordVisible) painterResource(R.drawable.visibility_24px) else painterResource(
                            R.drawable.visibility_off_24px
                        )
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Image(
                            painter = image,
                            contentDescription = description,
                            colorFilter = ColorFilter.tint(Color(0xff797C7B))
                        )
                    }
                },
                enabled = !isLoading
            )
        }

        Spacer(Modifier.height(45.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else {
                Button(
                    onClick = { onLoginClick(email, password) },
                    modifier = Modifier.fillMaxSize(),
                    enabled = isLoginEnabled,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentYellow,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xffF3F6F6),
                        disabledContentColor = Color(0xff797C7B)
                    )
                ) {
                    Text(
                        "Log in",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(
            onClick = { if (!isLoading) onSwitchToSignUpClick() },
            enabled = !isLoading
        ) {
            Text("Don't have an account? Sign Up", color = Color(0xffFEC62F))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    NabiTheme {
        LoginScreen(
            isLoading = false,
            onLoginClick = { _, _ -> },
            onGoogleLoginClick = { },
            onSwitchToSignUpClick = { },
            onBackClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Login Loading State")
@Composable
fun LoginScreenLoadingPreview() {
    NabiTheme {
        LoginScreen(
            isLoading = true,
            onLoginClick = { _, _ -> },
            onGoogleLoginClick = { },
            onSwitchToSignUpClick = { },
            onBackClick = { }
        )
    }
}