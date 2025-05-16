package com.gdghufs.nabi.ui.auth

sealed class AuthScreen(val route: String) {
    data object Onboarding : AuthScreen("onboarding_screen")
    data object Login : AuthScreen("login_screen")
    data object SignUp : AuthScreen("signup_screen")
}