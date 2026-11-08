package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

class AndroidChartBridge(
    private val onTickReceived: (symbol: String, price: Double, change: Double, changePercent: Double) -> Unit
) {
    @JavascriptInterface
    fun onRealtimeTick(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val symbol = json.optString("symbol", "")
            val price = json.optDouble("price", 0.0)
            val change = json.optDouble("change", 0.0)
            val changePercent = json.optDouble("changePercent", 0.0)
            onTickReceived(symbol, price, change, changePercent)
        } catch (e: Exception) {
            Log.e("ChartBridge", "Error parsing tick", e)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChartWebView(
    modifier: Modifier = Modifier,
    onTick: (symbol: String, price: Double, change: Double, changePercent: Double) -> Unit,
    onWebViewCreated: (WebView) -> Unit = {}
) {
    val context = LocalContext.current

    val webView = remember(context) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#131722"))
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setSupportZoom(false)
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d("ChartEngineJS", "${consoleMessage?.message()} [line ${consoleMessage?.lineNumber()}]")
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("ChartWebView", "Page loaded: $url")
                }
            }

            addJavascriptInterface(AndroidChartBridge(onTick), "AndroidBridge")
            loadUrl("file:///android_asset/chart-engine/index.html")
            onWebViewCreated(this)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}
