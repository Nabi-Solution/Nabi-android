package com.gdghufs.nabi.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var selectedDisease by remember {
        mutableStateOf("")
    }

    if (showDiseaseDialog) {
        DiseaseSelectionDialog(
            onConfirm = {
                selectedDisease = it
                homeViewModel.updateUserDisease(it)
            }
        )
    }

    LaunchedEffect(isLoading, currentUser) {
        if (!isLoading && currentUser == null) {
            val intent = Intent(context, AuthActivity::class.java).apply {

            }
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
fun MainAppUI(homeViewModel: HomeViewModel) { // ViewModel을 AppNavHost 내부 화면에서 사용하기 위해 전달
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


// Data class to hold disease information for easier management
data class DiseaseInfo(
    val id: String, // Internal ID, e.g., "asthma"
    val displayName: String // Display name, e.g., "Asthma"
)

@Composable
fun DiseaseSelectionDialog(
    onConfirm: (String) -> Unit = {}, // Callback with the internal ID of the selected disease
    onDismiss: () -> Unit = {}       // Callback for when the dialog is dismissed
) {
    var selectedDiseaseId by remember {
        mutableStateOf("") // Stores the internal ID of the selected disease
    }

    val diseases = listOf(
        DiseaseInfo("asthma", "Asthma"),
        DiseaseInfo("hypertension", "Hypertension"),
        DiseaseInfo("depression", "Depression")
    )

    AlertDialog(
        onDismissRequest = onDismiss, // Call onDismiss when the user tries to dismiss (e.g., back button, click outside)
        title = { Text("Select Disease") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Please select the disease you want to manage.")
                Spacer(modifier = Modifier.height(8.dp))

                diseases.forEach { diseaseInfo ->
                    val isSelected = diseaseInfo.id == selectedDiseaseId
                    OutlinedButton(
                        onClick = { selectedDiseaseId = diseaseInfo.id },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp, // Thicker border for selected item
                            color = if (isSelected) Color.Red else Color.Gray
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isSelected) Color.Red else MaterialTheme.colorScheme.onSurface // Text color
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
                    // Typically, onConfirm would also trigger onDismiss or the logic to hide the dialog
                },
                enabled = selectedDiseaseId.isNotBlank() // Enable confirm only if a disease is selected
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { // Added a dismiss button for better UX
                Text("Cancel")
            }
        }
    )
}

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
            },
            onDismiss = {
                showDialog = false // Hide dialog on dismiss
                println("Dismissed")
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