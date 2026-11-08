package com.example.data.api

import com.example.data.model.CandleData
import com.example.data.model.MarketIndex
import com.example.data.model.StockQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object LiveMarketApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/"

    // Maps Indian ticker symbols to Yahoo Finance symbols
    fun mapToApiSymbol(symbol: String): String = when (symbol) {
        "NIFTY 50" -> "%5ENSEI"
        "BANK NIFTY" -> "%5ENSEBANK"
        "SENSEX" -> "%5EBSESN"
        "FINNIFTY" -> "NIFTY_FIN_SERVICE.NS"
        "NIFTY IT" -> "%5ECNXIT"
        "MIDCAP SELECT" -> "NIFTY_MID_SELECT.NS"
        "RELIANCE" -> "RELIANCE.NS"
        "HDFCBANK" -> "HDFCBANK.NS"
        "ICICIBANK" -> "ICICIBANK.NS"
        "INFY" -> "INFY.NS"
        "TCS" -> "TCS.NS"
        "SBIN" -> "SBIN.NS"
        "TATAMOTORS" -> "TATAMOTORS.NS"
        "BHARTIARTL" -> "BHARTIARTL.NS"
        "ITC" -> "ITC.NS"
        "LT" -> "LT.NS"
        "BAJFINANCE" -> "BAJFINANCE.NS"
        "MARUTI" -> "MARUTI.NS"
        "TATASTEEL" -> "TATASTEEL.NS"
        "SUNPHARMA" -> "SUNPHARMA.NS"
        "ZOMATO" -> "ZOMATO.NS"
        else -> if (symbol.contains(".NS") || symbol.contains("%5E")) symbol else "$symbol.NS"
    }

    suspend fun fetchLiveIndex(symbol: String, fallback: MarketIndex): MarketIndex = withContext(Dispatchers.IO) {
        val apiSymbol = mapToApiSymbol(symbol)
        val url = "$BASE_URL$apiSymbol?interval=1d&range=1d"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext fallback

                val bodyStr = response.body?.string() ?: return@withContext fallback
                val root = JSONObject(bodyStr)
                val resultArr = root.getJSONObject("chart").getJSONArray("result")
                if (resultArr.length() == 0) return@withContext fallback

                val resultObj = resultArr.getJSONObject(0)
                val meta = resultObj.getJSONObject("meta")

                val ltp = meta.optDouble("regularMarketPrice", fallback.value)
                val prevClose = if (meta.has("previousClose")) meta.optDouble("previousClose")
                else meta.optDouble("chartPreviousClose", fallback.prevClose)

                val dayHigh = meta.optDouble("regularMarketDayHigh", if (ltp > fallback.high) ltp else fallback.high)
                val dayLow = meta.optDouble("regularMarketDayLow", if (ltp < fallback.low) ltp else fallback.low)
                val yearHigh = meta.optDouble("fiftyTwoWeekHigh", fallback.yearHigh)
                val yearLow = meta.optDouble("fiftyTwoWeekLow", fallback.yearLow)

                val change = ltp - prevClose
                val changePct = if (prevClose > 0) (change / prevClose) * 100.0 else 0.0

                fallback.copy(
                    value = Math.round(ltp * 100.0) / 100.0,
                    change = Math.round(change * 100.0) / 100.0,
                    changePercent = Math.round(changePct * 100.0) / 100.0,
                    high = Math.round(dayHigh * 100.0) / 100.0,
                    low = Math.round(dayLow * 100.0) / 100.0,
                    prevClose = Math.round(prevClose * 100.0) / 100.0,
                    yearHigh = Math.round(yearHigh * 100.0) / 100.0,
                    yearLow = Math.round(yearLow * 100.0) / 100.0
                )
            }
        } catch (e: Exception) {
            fallback
        }
    }

    suspend fun fetchLiveStock(symbol: String, fallback: StockQuote): StockQuote = withContext(Dispatchers.IO) {
        val apiSymbol = mapToApiSymbol(symbol)
        val url = "$BASE_URL$apiSymbol?interval=1d&range=1d"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext fallback

                val bodyStr = response.body?.string() ?: return@withContext fallback
                val root = JSONObject(bodyStr)
                val resultArr = root.getJSONObject("chart").getJSONArray("result")
                if (resultArr.length() == 0) return@withContext fallback

                val resultObj = resultArr.getJSONObject(0)
                val meta = resultObj.getJSONObject("meta")

                val ltp = meta.optDouble("regularMarketPrice", fallback.ltp)
                val prevClose = if (meta.has("previousClose")) meta.optDouble("previousClose")
                else meta.optDouble("chartPreviousClose", fallback.prevClose)

                val dayHigh = meta.optDouble("regularMarketDayHigh", if (ltp > fallback.high) ltp else fallback.high)
                val dayLow = meta.optDouble("regularMarketDayLow", if (ltp < fallback.low) ltp else fallback.low)
                val yearHigh = meta.optDouble("fiftyTwoWeekHigh", fallback.yearHigh)
                val yearLow = meta.optDouble("fiftyTwoWeekLow", fallback.yearLow)
                val volume = meta.optLong("regularMarketVolume", fallback.volume)

                val change = ltp - prevClose
                val changePct = if (prevClose > 0) (change / prevClose) * 100.0 else 0.0

                fallback.copy(
                    ltp = Math.round(ltp * 100.0) / 100.0,
                    change = Math.round(change * 100.0) / 100.0,
                    changePercent = Math.round(changePct * 100.0) / 100.0,
                    high = Math.round(dayHigh * 100.0) / 100.0,
                    low = Math.round(dayLow * 100.0) / 100.0,
                    prevClose = Math.round(prevClose * 100.0) / 100.0,
                    yearHigh = Math.round(yearHigh * 100.0) / 100.0,
                    yearLow = Math.round(yearLow * 100.0) / 100.0,
                    volume = if (volume > 0) volume else fallback.volume
                )
            }
        } catch (e: Exception) {
            fallback
        }
    }

    suspend fun fetchLiveCandles(symbol: String, timeframe: String): List<CandleData> = withContext(Dispatchers.IO) {
        val apiSymbol = mapToApiSymbol(symbol)
        val (interval, range) = when (timeframe) {
            "1D" -> Pair("5m", "1d")
            "1W" -> Pair("15m", "5d")
            "1M" -> Pair("1d", "1mo")
            "1Y" -> Pair("1wk", "1y")
            else -> Pair("5m", "1d")
        }

        val url = "$BASE_URL$apiSymbol?interval=$interval&range=$range"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()

                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val root = JSONObject(bodyStr)
                val resultArr = root.getJSONObject("chart").getJSONArray("result")
                if (resultArr.length() == 0) return@withContext emptyList()

                val resultObj = resultArr.getJSONObject(0)
                if (!resultObj.has("timestamp")) return@withContext emptyList()

                val timestamps = resultObj.getJSONArray("timestamp")
                val indicators = resultObj.getJSONObject("indicators")
                val quotesArr = indicators.getJSONArray("quote")
                if (quotesArr.length() == 0) return@withContext emptyList()

                val quote = quotesArr.getJSONObject(0)
                val opens = quote.optJSONArray("open")
                val highs = quote.optJSONArray("high")
                val lows = quote.optJSONArray("low")
                val closes = quote.optJSONArray("close")
                val volumes = quote.optJSONArray("volume")

                val timeFmt = SimpleDateFormat(if (timeframe == "1D" || timeframe == "1W") "HH:mm" else "dd MMM", Locale.US)
                val candles = mutableListOf<CandleData>()

                val len = timestamps.length()
                for (i in 0 until len) {
                    val ts = timestamps.optLong(i) * 1000L
                    val c = closes?.optDouble(i) ?: Double.NaN
                    val o = opens?.optDouble(i) ?: c
                    val h = highs?.optDouble(i) ?: c
                    val l = lows?.optDouble(i) ?: c
                    val v = volumes?.optLong(i) ?: 1000L

                    if (!c.isNaN() && !o.isNaN() && !h.isNaN() && !l.isNaN()) {
                        candles.add(
                            CandleData(
                                timestamp = ts,
                                timeLabel = timeFmt.format(Date(ts)),
                                open = o.toFloat(),
                                high = h.toFloat(),
                                low = l.toFloat(),
                                close = c.toFloat(),
                                volume = v
                            )
                        )
                    }
                }
                candles
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
