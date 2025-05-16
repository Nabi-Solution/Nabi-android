package com.gdghufs.nabi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gdghufs.nabi.ui.account.AccountRoute
import com.gdghufs.nabi.ui.chat.ChatScreen
import com.gdghufs.nabi.ui.history.HistoryScreen
import com.gdghufs.nabi.ui.history.ReportWebViewScreen
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
            HomeScreen(navigateToChat = {
                navController.navigate(Screen.Chat.route) {
                    popUpTo(Screen.Home.route) {
                        inclusive = true
                    }
                }
            })
        }
        composable(Screen.Today.route) {
            TodayScreen()
        }
        composable(Screen.Chat.route) {
            ChatScreen()
        }
        composable(Screen.History.route) {
            HistoryScreen(navController)
        }
        composable(Screen.Account.route) {
            AccountRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${ScreenRoutes.REPORT_WEBVIEW_SCREEN}/{htmlContent}",
            arguments = listOf(navArgument("htmlContent") {
                type = NavType.StringType
                nullable = true // Allow null if content might not pass
            })
        ) { backStackEntry ->
            val htmlContent = backStackEntry.arguments?.getString("htmlContent")
            ReportWebViewScreen(
                navController = navController,
                encodedHtmlContent = htmlContent
            )
        }
    }
}