package com.gdghufs.nabi.ui.auth

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.gdghufs.nabi.R // 실제 리소스 경로
import com.gdghufs.nabi.ui.theme.AccentYellow

@Composable
fun SignUpScreen(
    isLoading: Boolean,
    onSignUpClick: (email: String, password: String, name: String) -> Unit,
    onSwitchToSignInClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val isPasswordValid = password.length >= 6
    val doPasswordsMatch = password == confirmPassword && password.isNotEmpty()
    val isNameValid = name.isNotBlank()

    val isSignUpEnabled =
        isNameValid && isEmailValid(email) && isPasswordValid && doPasswordsMatch && !isLoading

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
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
                    painter = painterResource(R.drawable.back), // 실제 리소스 확인
                    contentDescription = "Back"
                )
            }
        }

        Spacer(Modifier.height(60.dp))

        Text(
            text = "Sign up with Email",
            style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        )

        Spacer(Modifier.height(18.dp))

        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            color = Color(0xff797C7B),
            text = "Get chatting with friends and family today by signing up for our chat app!", // 문구 수정
            style = TextStyle(fontSize = 14.sp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(60.dp))

        // 입력 필드들
        SignField(
            label = "Your name",
            value = name,
            onValueChange = { name = it },
            isError = name.isBlank() && name.isNotEmpty(), // 간단한 유효성 예시
            enabled = !isLoading
        )
        Spacer(Modifier.height(24.dp))

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
            isError = !isPasswordValid && password.isNotEmpty(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image =
                    if (passwordVisible) painterResource(R.drawable.visibility_24px) else painterResource(
                        R.drawable.visibility_off_24px
                    )
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, if (passwordVisible) "Hide password" else "Show password")
                }
            },
            enabled = !isLoading
        )
        if (!isPasswordValid && password.isNotEmpty()) {
            Text(
                "Password must be at least 6 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))

        SignField(
            label = "Confirm Password",
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            keyboardType = KeyboardType.Password,
            isError = !doPasswordsMatch && confirmPassword.isNotEmpty(),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image =
                    if (passwordVisible) painterResource(R.drawable.visibility_24px) else painterResource(
                        R.drawable.visibility_off_24px
                    )
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(image, if (confirmPasswordVisible) "Hide password" else "Show password")
                }
            },
            enabled = !isLoading
        )
        if (!doPasswordsMatch && confirmPassword.isNotEmpty()) {
            Text(
                "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(60.dp)) // 간격 조정

        // 가입 버튼 또는 로딩 인디케이터
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
                    onClick = {
                        onSignUpClick(email, password, name)
                    },
                    modifier = Modifier.fillMaxSize(),
                    enabled = isSignUpEnabled,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xffA02121),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xffF3F6F6),
                        disabledContentColor = Color(0xff797C7B)
                    )
                ) {
                    Text(
                        "Create Account",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        TextButton(
            onClick = { if (!isLoading) onSwitchToSignInClick() },
            enabled = !isLoading
        ) {
            Text("Already have an account? Log In", color = AccentYellow)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        isLoading = false,
        onSignUpClick = { _, _, _ -> },
        onSwitchToSignInClick = { },
        onBackClick = { }
    )
}

@Preview(showBackground = true, name = "SignUp Loading State")
@Composable
fun SignUpScreenLoadingPreview() {
    SignUpScreen(
        isLoading = true,
        onSignUpClick = { _, _, _ -> },
        onSwitchToSignInClick = { },
        onBackClick = { }
    )
}