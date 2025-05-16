package com.gdghufs.nabi.ui.account

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdghufs.nabi.R
import com.gdghufs.nabi.ui.auth.AuthActivity
import com.gdghufs.nabi.ui.theme.AmiriFamily
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily

@Composable
@Preview
fun AccountScreenPreview() {
    NabiTheme {
        AccountScreen(onBackClick = {})
    }
}

@Composable
fun AccountRoute(
    onBackClick: () -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel()
) {
    AccountScreen(
        onBackClick = onBackClick,
        accountViewModel = accountViewModel
    )
}

@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    accountViewModel: AccountViewModel = hiltViewModel()
) {
    val currentUser by accountViewModel.currentUser.collectAsState()
    val isLoading by accountViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser, isLoading) {
        if (!isLoading && currentUser == null) {
            val intent = Intent(context, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        accountViewModel.signOut()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onBackClick.invoke() }) {
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(R.drawable.back_arrow),
                    contentDescription = null
                )
            }
            Spacer(Modifier.width(18.dp))
            Text(
                "Account",
                fontFamily = AmiriFamily,
                color = Color(0xff740D0D),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }

        Spacer(Modifier.height(40.dp))

        Box(
            Modifier
                .size(120.dp)
                .background(color = Color(0xffEDECF4), shape = CircleShape)
        ) {
            Image(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center),
                painter = painterResource(R.drawable.symbol),
                contentDescription = null
            )
        }

        Spacer(Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            currentUser?.let { user ->
        Text(
                    user.displayName ?: "Unknown",
            fontSize = 24.sp,
            fontFamily = RobotoPretendardFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
                    user.email ?: "Unknown",
            fontSize = 12.sp,
            fontFamily = AmiriFamily,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.primary
        )
            }
        }

        Spacer(Modifier.height(40.dp))

        Column {
            MenuItem("Sign out") {
                showLogoutDialog = true
            }
        }
    }
}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                onClick.invoke()
            }
            .padding(20.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.logout_24px),
            modifier = Modifier.size(20.dp),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = Color.Black)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            text = text,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}