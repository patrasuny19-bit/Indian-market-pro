package com.example.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private const val TAG = "YahooCrumbManager"

object YahooCrumbManager {

    const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"

    @Volatile
    var cachedCrumb: String? = null
        private set

    val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            synchronized(cookieStore) {
                val list = cookieStore.getOrPut(url.host) { mutableListOf() }
                for (cookie in cookies) {
                    list.removeAll { it.name == cookie.name }
                    list.add(cookie)
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            synchronized(cookieStore) {
                return cookieStore.entries
                    .filter { url.host.endsWith(it.key.removePrefix(".")) || it.key.contains("yahoo.com") }
                    .flatMap { it.value }
            }
        }
    }

    private val authClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun getCrumb(): String? = withContext(Dispatchers.IO) {
        val existing = cachedCrumb
        if (!existing.isNullOrBlank()) {
            return@withContext existing
        }
        refreshCrumb()
    }

    suspend fun refreshCrumb(): String? = withContext(Dispatchers.IO) {
        try {
            // Step 1: Query fc.yahoo.com to acquire session cookies
            val cookieReq = Request.Builder()
                .url("https://fc.yahoo.com")
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()

            try {
                authClient.newCall(cookieReq).execute().use { response ->
                    Log.d(TAG, "Cookie handshake status: ${response.code}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "fc.yahoo.com response handled: ${e.message}")
            }

            // Step 2: Fetch crumb from test endpoint
            val crumbReq = Request.Builder()
                .url("https://query2.finance.yahoo.com/v1/test/getcrumb")
                .header("User-Agent", USER_AGENT)
                .header("Accept", "*/*")
                .header("Referer", "https://finance.yahoo.com/")
                .build()

            authClient.newCall(crumbReq).execute().use { response ->
                val body = response.body?.string()?.trim()
                if (response.isSuccessful && !body.isNullOrBlank() && !body.contains("<html", ignoreCase = true)) {
                    cachedCrumb = body
                    Log.i(TAG, "Successfully acquired live market crumb: $body")
                    return@withContext body
                } else {
                    Log.w(TAG, "Crumb endpoint returned code ${response.code}, body: $body")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh Yahoo crumb", e)
        }
        cachedCrumb
    }
}
