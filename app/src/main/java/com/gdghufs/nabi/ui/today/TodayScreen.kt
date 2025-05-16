package com.gdghufs.nabi.ui.today

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdghufs.nabi.R
import com.gdghufs.nabi.data.model.Habit
import com.gdghufs.nabi.data.model.Medication
import com.gdghufs.nabi.data.model.TimeOfDay
import com.gdghufs.nabi.data.repository.HabitRepository
import com.gdghufs.nabi.data.repository.MedicationRepository
import com.gdghufs.nabi.data.repository.UserRepository
import com.gdghufs.nabi.domain.model.User
import com.gdghufs.nabi.ui.common.TimeTag
import com.gdghufs.nabi.ui.theme.AmiriFamily
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.utils.DateUtil
import com.gdghufs.nabi.utils.NabiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Placeholder for R.drawable.add_circle_24px
// Create a dummy drawable in res/drawable for preview, e.g., a simple circle vector.
// Or replace with an actual Material Icon if available.
// For now, I'll assume it exists. If not, previews might crash or show errors.
// Example: object R { object drawable { const val add_circle_24px = 0 } }


@Composable
fun TodayScreen(viewModel: TodayViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
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
                    fontFamily = AmiriFamily, // Adjusted
                    color = Color(0xff740D0D),
                    fontWeight = FontWeight.Bold, // Adjusted
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

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Error: ${uiState.error}",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
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
                            .background(Color.White) // Ensure background for the date text
                            .padding(top = 12.dp, bottom = 8.dp, start = 12.dp) // Adjust padding
                    ) {
                        Text(
                            uiState.displayDateString, // Dynamic date
                            fontFamily = AmiriFamily, // Adjusted
                            color = Color(0xffE2B2B2),
                            fontWeight = FontWeight.Bold, // Adjusted
                            fontSize = 36.sp
                        )
                    }
                }

                if (uiState.todoItems.isNotEmpty()) {
                    item {
                        ListSectionHeader("To-Do")
                    }
                    itemsIndexed(
                        uiState.todoItems,
                        key = { _, item -> "habit-${item.id}" }) { index, item ->
                        TodoItem(
                            item = item,
                            isChecked = item.histories[uiState.currentDateString] ?: false,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleCompletion(item, isChecked)
                            }
                        )
                        if (index < uiState.todoItems.size - 1) {
                            ListDivider()
                        }
                    }
                }

                if (uiState.medicationItems.isNotEmpty()) {
                    item {
                        ListSectionHeader("Medication")
                    }
                    itemsIndexed(
                        uiState.medicationItems,
                        key = { _, item -> "med-${item.id}" }) { index, item ->
                        TodoItem(
                            item = item,
                            isChecked = item.histories[uiState.currentDateString] ?: false,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleCompletion(item, isChecked)
                            }
                        )
                        if (index < uiState.medicationItems.size - 1) {
                            ListDivider()
                        }
                    }
                }


                // Suggestions (static for now as per original code)
                item {
                    ListSectionHeader("Suggestions")
                }
                items(3, key = { it }) { index -> // Using index as key for static items
                    SuggestionItem() // Assuming this is static or will get its own data source
                    if (index < 2) {
                        Spacer(Modifier.height(16.dp))
                    }
                }
                item { Spacer(Modifier.height(24.dp)) } // Bottom padding
            }
        }
    }
}

@Composable
fun ListSectionHeader(title: String) {
    Column(modifier = Modifier.padding(start = 12.dp)) { // Match date padding
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            fontFamily = AmiriFamily, // Adjusted
            color = Color.Black,
            fontWeight = FontWeight.Bold, // Adjusted
            fontSize = 18.sp
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun ListDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        thickness = 1.dp,
        color = Color(0x1A000000)
    )
}


@Composable
fun TodoItem(
    item: TodoListItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp), // Add slight horizontal padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .weight(0.6f)
                .padding(end = 8.dp), // Add padding to prevent text overlap
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .background(color = Color(0xfff2f2f2), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (item) {
                        is TodoListItem.HabitItem -> when (item.source.lowercase()) {
                            "doctor" -> "👨‍⚕️" // Doctor emoji
                            "patient" -> "🙋" // Patient emoji (or similar)
                            "ai" -> "🤖"     // AI emoji
                            else -> "📝"      // Default habit icon
                        }

                        is TodoListItem.MedicationItem -> "💊" // Pill emoji
                    },
                    fontSize = 18.sp // Adjust size as needed
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
                // You can add a note if your model supports it
                // Text("Note if available", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
            }
        }
        Box(
            Modifier
                .weight(0.4f)
                .fillMaxWidth()
        ) {
            TimeTag(
                type = item.timeOfDay.toTimeTagType(),
                modifier = Modifier.align(Alignment.CenterStart)
            )

            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .size(28.dp) // Checkbox itself might be larger due to padding
                    .align(Alignment.CenterEnd),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = Color(0xffD9D9D9),
                    checkmarkColor = Color.White
                )
            )
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
                .clickable { /* TODO: Handle suggestion click */ }
        ) {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                painter = painterResource(R.drawable.add_circle_24px), // Ensure this drawable exists
                colorFilter = ColorFilter.tint(Color.Black),
                contentDescription = "Add suggestion"
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun TodayScreenPreview() {
    NabiTheme { // Replace with your actual theme
        // Mock ViewModel for preview
        val mockViewModel = TodayViewModel(
            habitRepository = object : HabitRepository { // Mock implementation
                override fun getHabits(userId: String): Flow<NabiResult<List<Habit>>> = flowOf(
                    NabiResult.Success(
                        listOf(
                            Habit(
                                id = "1",
                                name = "Morning Walk",
                                source = "doctor",
                                timeOfDay = TimeOfDay.MORNING,
                                orderWeight = 10,
                                histories = mapOf(DateUtil.getCurrentDateString() to true)
                            ),
                            Habit(
                                id = "2",
                                name = "Read a book",
                                source = "patient",
                                timeOfDay = TimeOfDay.EVENING,
                                orderWeight = 5
                            )
                        )
                    )
                )

                override suspend fun updateHabitCompletion(
                    habitId: String,
                    date: String,
                    isCompleted: Boolean
                ): NabiResult<Unit> = NabiResult.Success(Unit)
            },
            medicationRepository = object : MedicationRepository { // Mock implementation
                override fun getMedications(userId: String): Flow<NabiResult<List<Medication>>> =
                    flowOf(
                        NabiResult.Success(
                            listOf(
                                Medication(
                                    id = "med1",
                                    name = "Vitamin D",
                                    timeOfDay = TimeOfDay.ANYTIME,
                                    orderWeight = 8,
                                    histories = mapOf(
                                        DateUtil.getCurrentDateString() to false
                                    )
                                )
                            )
                        )
                    )

                override suspend fun updateMedicationCompletion(
                    medicationId: String,
                    date: String,
                    isCompleted: Boolean
                ): NabiResult<Unit> = NabiResult.Success(Unit)
            },
            userRepository = object : UserRepository { // Mock UserRepository
                override suspend fun signInWithEmailPassword(
                    email: String,
                    password: String
                ): NabiResult<User> = NabiResult.Error(
                    Exception()
                )

                override suspend fun signUpWithEmailPassword(
                    email: String,
                    password: String,
                    name: String,
                    role: String
                ): NabiResult<User> = NabiResult.Error(
                    Exception()
                )

                override suspend fun signInWithGoogleCredential(
                    credential: com.google.firebase.auth.AuthCredential,
                    defaultRole: String
                ): NabiResult<User> = NabiResult.Error(
                    java.lang.Exception()
                )

                override suspend fun getCurrentUser(): User? =
                    User("preview_user", "a@b.com", "Preview User", true, "patient")

                override fun signOut() {}
                override suspend fun updateUserDisease(
                    uid: String,
                    disease: String
                ): NabiResult<Unit> = NabiResult.Success(Unit)
            }
        )
        TodayScreen(viewModel = mockViewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun TodoItemPreview() {
    NabiTheme {
        val habit = Habit(
            id = "prev_habit",
            name = "Drink Water",
            source = "ai",
            timeOfDay = TimeOfDay.AFTERNOON,
            histories = mapOf(DateUtil.getCurrentDateString() to true)
        )
        TodoItem(
            item = TodoListItem.HabitItem(habit),
            isChecked = true,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationItemPreview() {
    NabiTheme {
        val medication =
            Medication(id = "prev_med", name = "Painkiller", timeOfDay = TimeOfDay.EVENING)
        TodoItem(
            item = TodoListItem.MedicationItem(medication),
            isChecked = false,
            onCheckedChange = {}
        )
    }
}


@Preview
@Composable
fun SuggestionItemPreview() {
    NabiTheme {
        SuggestionItem()
    }
}

// Dummy NabiTheme for preview if not already defined
// @Composable
// fun NabiTheme(content: @Composable () -> Unit) {
//    MaterialTheme { // Or your custom theme
//        content()
//    }
// }