package com.gdghufs.nabi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gdghufs.nabi.ui.account.AccountScreen
import com.gdghufs.nabi.ui.chat.ChatScreen
import com.gdghufs.nabi.ui.history.HistoryScreen
import com.gdghufs.nabi.ui.home.HomeScreen
import com.gdghufs.nabi.ui.today.TodayScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Today.route) {
            TodayScreen()
        }
        composable(Screen.Chat.route) {
            ChatScreen()
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(Screen.Account.route) {
            AccountScreen()
        }
    }
}