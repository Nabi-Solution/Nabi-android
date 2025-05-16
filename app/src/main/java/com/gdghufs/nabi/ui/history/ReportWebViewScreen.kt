package com.gdghufs.nabi.ui.history

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor // For WebView background
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled") // JavaScript is often useful in WebViews
@Composable
fun ReportWebViewScreen(
    navController: NavController,
    encodedHtmlContent: String?
) {
    val htmlContent = remember(encodedHtmlContent) { // Recalculate only if encodedHtmlContent changes
        encodedHtmlContent?.let {
            try {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) {
                // Log error e.printStackTrace()
                "<html><body><h1>Error</h1><p>Could not decode report content.</p><p>${e.message}</p></body></html>"
            }
        } ?: "<html><body><h1>Error</h1><p>No report content provided.</p></body></html>"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Log Detail") }, // More specific title
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to History"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Theme color
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = object : WebViewClient() {
                            // Optional: Override URL loading if you have links in HTML
                            // override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            //     return false // Load in this WebView
                            // }
                        }
                        settings.javaScriptEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.domStorageEnabled = true // Good for complex HTML/JS
                        setBackgroundColor(AndroidColor.TRANSPARENT) // Let HTML define background
                    }
                },
                update = { webView ->
                    // Load the HTML content
                    // Using a base URL of null is fine for self-contained HTML.
                    // If you had relative paths for images/CSS within the HTML, you'd set a base URL.
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}