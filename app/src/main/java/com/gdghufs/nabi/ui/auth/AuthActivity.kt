package com.gdghufs.nabi.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // viewModels 임포트
import androidx.compose.runtime.*
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.gdghufs.nabi.BuildConfig
import com.google.firebase.auth.GoogleAuthProvider
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import dagger.hilt.android.AndroidEntryPoint // AndroidEntryPoint 임포트
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

            NabiTheme {
                OnboardingScreen(onGoogleSignUpClick = {
                    coroutineScope.launch {
                        try {
                            val result = credentialManager.getCredential(
                                request = request,
                                context = activityContext
                            )

                            processGetCredentialResponse(result)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting credential in Activity", e)
                        }
                    }
                }, onEmailSignUpClick = {

                }, onSignInClick = {

                })
            }
        }
    }

    private fun processGetCredentialResponse(result: GetCredentialResponse) {
        val credential = result.credential

        when (credential) {
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
                        Log.e(TAG, "GoogleIdTokenParsingException in Activity", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating Firebase credential in Activity", e)
                    }
                } else {
                    Log.w(TAG, "Unexpected custom credential type in Activity: ${credential.type}")
                }
            }

            else -> {
                Log.w(TAG, "Unhandled credential type in Activity: ${credential.type}")
            }
        }
    }
}