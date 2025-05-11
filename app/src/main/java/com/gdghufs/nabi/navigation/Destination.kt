package com.gdghufs.nabi.navigation

import com.gdghufs.nabi.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: Int,
) {
    data object Home : BottomNavItem(Screen.Home.route, "Home", R.drawable.navbar_home_24)
    data object Today : BottomNavItem(Screen.Today.route, "Today", R.drawable.navbar_today_24)
    data object Chat : BottomNavItem(Screen.Chat.route, "Chat", R.drawable.navbar_chat_24)
    data object History :
        BottomNavItem(Screen.History.route, "History", R.drawable.navbar_history_24)

    data object Account :
        BottomNavItem(Screen.Account.route, "Account", R.drawable.navbar_account_24)
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Today : Screen("today")
    data object Chat : Screen("chat")
    data object History : Screen("history")
    data object Account : Screen("account")
}