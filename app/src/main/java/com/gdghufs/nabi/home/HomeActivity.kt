package com.gdghufs.nabi.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gdghufs.nabi.navigation.AppNavHost
import com.gdghufs.nabi.navigation.BottomNavItem
import com.gdghufs.nabi.ui.auth.AuthActivity
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            NabiTheme {
                MainHomeScreen()
            }
        }
    }
}

@Composable
fun MainHomeScreen(homeViewModel: HomeViewModel = hiltViewModel()) {
    val currentUser by homeViewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by homeViewModel.isLoading.collectAsStateWithLifecycle()
    val showDiseaseDialog by homeViewModel.showDiseaseDialog.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(isLoading, currentUser) {
        if (!isLoading && currentUser == null) {
            val intent = Intent(context, AuthActivity::class.java)
            context.startActivity(intent)
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
        }

        currentUser != null -> {
            MainAppUI(homeViewModel)
            
            if (showDiseaseDialog) {
                DiseaseSelectionDialog(
                    onConfirm = { disease ->
                        homeViewModel.updateUserDisease(disease)
                    }
                )
            }
        }

        else -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("User not found")
            }
        }
    }
}

@Composable
fun MainAppUI(homeViewModel: HomeViewModel) {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Today,
        BottomNavItem.Chat,
        BottomNavItem.History,
        BottomNavItem.Account
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            Row(
                Modifier
                    .fillMaxWidth()
                    .shadow(1.dp)
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp)
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentDestination?.route == screen.route
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                onClick = {
                                    if (!isSelected) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(screen.icon),
                            contentDescription = screen.title,
                            colorFilter = ColorFilter.tint(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = screen.title,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = RobotoPretendardFamily,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun DiseaseSelectionDialog(
    onConfirm: (String) -> Unit
) {
    var selectedDiseaseId by remember { mutableStateOf("") }

    val diseases = listOf(
        DiseaseInfo("asthma", "천식"),
        DiseaseInfo("hypertension", "고혈압"),
        DiseaseInfo("depression", "우울증")
    )

    AlertDialog(
        onDismissRequest = { },
        title = { Text("질병 선택") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("관리하고자 하는 질병을 선택해주세요.")
                Spacer(modifier = Modifier.height(8.dp))

                diseases.forEach { diseaseInfo ->
                    val isSelected = diseaseInfo.id == selectedDiseaseId
                    OutlinedButton(
                        onClick = { selectedDiseaseId = diseaseInfo.id },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(diseaseInfo.displayName)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedDiseaseId.isNotBlank()) {
                        onConfirm(selectedDiseaseId)
                    }
                },
                enabled = selectedDiseaseId.isNotBlank()
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = { }) {
                Text("취소")
            }
        }
    )
}

data class DiseaseInfo(
    val id: String,
    val displayName: String
)

@Preview(showBackground = true)
@Composable
fun DiseaseSelectionDialogPreview() {
    // State to control dialog visibility for preview
    var showDialog by remember { mutableStateOf(true) }
    var confirmedDisease by remember { mutableStateOf<String?>(null) }

    if (showDialog) {
        DiseaseSelectionDialog(
            onConfirm = { diseaseId ->
                confirmedDisease = diseaseId
                showDialog = false // Hide dialog on confirm
                println("Confirmed: $diseaseId")
            }
        )
    }

    // Display the confirmed disease below (for preview purposes)
    Column {
        Button(onClick = { showDialog = true }) {
            Text("Show Disease Selection Dialog")
        }
        if (confirmedDisease != null) {
            Text("Selected disease: $confirmedDisease", style = MaterialTheme.typography.bodyLarge)
        }
    }
}