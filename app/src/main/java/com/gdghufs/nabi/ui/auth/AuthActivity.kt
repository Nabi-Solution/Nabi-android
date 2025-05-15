package com.gdghufs.nabi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gdghufs.nabi.BuildConfig
import com.gdghufs.nabi.home.HomeActivity
import com.google.firebase.auth.GoogleAuthProvider
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    val TAG = "AuthActivity"

    @Inject
    lateinit var credentialManager: CredentialManager

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()


        setContent {
            val activityContext = this
            val coroutineScope = rememberCoroutineScope()
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }

            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(key1 = uiState.error) {
                uiState.error?.let { errorMessage ->
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                    viewModel.clearError()
                }
            }

            LaunchedEffect(key1 = uiState.authSuccess) {
                if (uiState.authSuccess) {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    val message = when (currentRoute) {
                        AuthScreen.Login.route -> "Login successful!"
                        AuthScreen.SignUp.route -> "Registration successful!"
                        AuthScreen.Onboarding.route -> "Google login successful!"
                        else -> "Verification successful!"
                    }
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                    viewModel.resetAuthSuccess()

                    val intent = Intent(activityContext, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            NabiTheme {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = AuthScreen.Onboarding.route,
                    ) {
                        composable(AuthScreen.Onboarding.route) {
                            OnboardingScreen(
                                onGoogleSignUpClick = {
                                    coroutineScope.launch {
                                        try {
                                            val result = credentialManager.getCredential(
                                                request = request,
                                                context = activityContext
                                            )
                                            processGetCredentialResponse(result)
                                        } catch (e: Exception) {
                                            Log.e(
                                                TAG,
                                                "Google Sign-In failed (Onboarding): ${e.message}",
                                                e
                                            )
                                            snackbarHostState.showSnackbar("Google 로그인 중 오류가 발생했습니다: ${e.localizedMessage}")
                                        }
                                    }
                                },
                                onNavigateToSignUp = {
                                    navController.navigate(AuthScreen.SignUp.route) {
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate(AuthScreen.Login.route) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(AuthScreen.Login.route) {
                            LoginScreen(
                                isLoading = uiState.isLoading,
                                onLoginClick = { email, password ->
                                    viewModel.signInWithEmailPassword(email, password)
                                },
                                onGoogleLoginClick = {
                                    coroutineScope.launch {
                                        try {
                                            val result = credentialManager.getCredential(
                                                request = request,
                                                context = activityContext
                                            )
                                            processGetCredentialResponse(result)
                                        } catch (e: Exception) {
                                            Log.e(
                                                TAG,
                                                "Google Sign-In failed (Login): ${e.message}",
                                                e
                                            )
                                            snackbarHostState.showSnackbar("Google 로그인 중 오류가 발생했습니다: ${e.localizedMessage}")
                                        }
                                    }
                                },
                                onSwitchToSignUpClick = {
                                    navController.navigate(AuthScreen.SignUp.route) {
                                        launchSingleTop = true
                                    }
                                },
                                onBackClick = {
                                    navController.navigateUp()
                                }
                            )
                        }

                        composable(AuthScreen.SignUp.route) {
                            SignUpScreen(
                                isLoading = uiState.isLoading,
                                onSignUpClick = { email, password, name ->
                                    viewModel.signUpWithEmailPassword(
                                        email = email,
                                        password = password,
                                        name = name
                                    )
                                },
                                onSwitchToSignInClick = {
                                    navController.navigate(AuthScreen.Login.route) {
                                        launchSingleTop = true
                                    }
                                },
                                onBackClick = {
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun processGetCredentialResponse(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(
                                credential.data
                            )
                        val googleIdToken = googleIdTokenCredential.idToken

                        val firebaseCredential =
                            GoogleAuthProvider.getCredential(googleIdToken, null)
                        viewModel.signInWithGoogleCredential(firebaseCredential)
                    } catch (e: com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException) {
                        Log.e(TAG, "GoogleIdTokenParsingException: ${e.message}", e)
                        viewModel.viewModelScope.launch {
                            (viewModel.uiState as MutableStateFlow).update {
                                it.copy(isLoading = false, error = "Google 로그인 처리 중 오류 발생 (토큰 파싱)")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating Firebase credential: ${e.message}", e)
                        viewModel.viewModelScope.launch {
                            (viewModel.uiState as MutableStateFlow).update {
                                it.copy(
                                    isLoading = false,
                                    error = "Google 로그인 처리 중 오류 발생 (Firebase)"
                                )
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Unexpected custom credential type: ${credential.type}")
                    viewModel.viewModelScope.launch {
                        (viewModel.uiState as MutableStateFlow).update {
                            it.copy(isLoading = false, error = "알 수 없는 인증 유형입니다.")
                        }
                    }
                }
            }

            else -> {
                Log.w(TAG, "Unhandled credential type: ${credential.type}")
                viewModel.viewModelScope.launch {
                    (viewModel.uiState as MutableStateFlow).update {
                        it.copy(isLoading = false, error = "지원하지 않는 인증 방식입니다.")
                    }
                }
            }
        }
    }
}