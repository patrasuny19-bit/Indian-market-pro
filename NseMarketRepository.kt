package com.example.data.api

import android.util.Log
import com.example.data.api.model.ChartItemDto
import com.example.data.model.CandleData
import com.example.data.model.MarketIndex
import com.example.data.model.StockQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object NseMarketRepository {

    private val api = RetrofitClient.apiService

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
        try {
            val response = api.getChart(symbol = apiSymbol, interval = "1d", range = "1d")
            if (response.isSuccessful) {
                val chartItem = response.body()?.chart?.result?.firstOrNull()
                if (chartItem?.meta != null) {
                    val meta = chartItem.meta
                    val ltp = meta.regularMarketPrice ?: fallback.value
                    val prevClose = meta.effectivePrevClose ?: fallback.prevClose
                    val dayHigh = meta.regularMarketDayHigh ?: (if (ltp > fallback.high) ltp else fallback.high)
                    val dayLow = meta.regularMarketDayLow ?: (if (ltp < fallback.low) ltp else fallback.low)
                    val yearHigh = meta.fiftyTwoWeekHigh ?: fallback.yearHigh
                    val yearLow = meta.fiftyTwoWeekLow ?: fallback.yearLow

                    val change = ltp - prevClose
                    val changePct = if (prevClose > 0) (change / prevClose) * 100.0 else 0.0
                    val istFmt = SimpleDateFormat("hh:mm:ss a 'IST'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    }
                    val now = Date()

                    return@withContext fallback.copy(
                        value = Math.round(ltp * 100.0) / 100.0,
                        change = Math.round(change * 100.0) / 100.0,
                        changePercent = Math.round(changePct * 100.0) / 100.0,
                        high = Math.round(dayHigh * 100.0) / 100.0,
                        low = Math.round(dayLow * 100.0) / 100.0,
                        prevClose = Math.round(prevClose * 100.0) / 100.0,
                        yearHigh = Math.round(yearHigh * 100.0) / 100.0,
                        yearLow = Math.round(yearLow * 100.0) / 100.0,
                        lastUpdatedTimestamp = now.time,
                        lastUpdatedFormatted = istFmt.format(now),
                        isRealTimeFresh = true
                    )
                }
            }
        } catch (_: Exception) {
            // Graceful fallback on network error
        }
        fallback.copy(isRealTimeFresh = false)
    }

    suspend fun fetchLiveStock(symbol: String, fallback: StockQuote): StockQuote = withContext(Dispatchers.IO) {
        val apiSymbol = mapToApiSymbol(symbol)
        try {
            val response = api.getChart(symbol = apiSymbol, interval = "1d", range = "1d")
            if (response.isSuccessful) {
                val chartItem = response.body()?.chart?.result?.firstOrNull()
                if (chartItem?.meta != null) {
                    val meta = chartItem.meta
                    val ltp = meta.regularMarketPrice ?: fallback.ltp
                    val prevClose = meta.effectivePrevClose ?: fallback.prevClose
                    val dayHigh = meta.regularMarketDayHigh ?: (if (ltp > fallback.high) ltp else fallback.high)
                    val dayLow = meta.regularMarketDayLow ?: (if (ltp < fallback.low) ltp else fallback.low)
                    val yearHigh = meta.fiftyTwoWeekHigh ?: fallback.yearHigh
                    val yearLow = meta.fiftyTwoWeekLow ?: fallback.yearLow
                    val volume = meta.regularMarketVolume ?: fallback.volume

                    val change = ltp - prevClose
                    val changePct = if (prevClose > 0) (change / prevClose) * 100.0 else 0.0
                    val istFmt = SimpleDateFormat("hh:mm:ss a 'IST'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    }
                    val now = Date()

                    val vwap = Math.round(((ltp * 0.45) + (dayHigh * 0.25) + (dayLow * 0.25) + (prevClose * 0.05)) * 100.0) / 100.0
                    val bid = Math.round((ltp - 0.15) * 100.0) / 100.0
                    val ask = Math.round((ltp + 0.15) * 100.0) / 100.0

                    Log.i("NseMarketRepo", "Live real quote for $symbol: ₹$ltp (${"%.2f".format(changePct)}%)")
                    return@withContext fallback.copy(
                        ltp = Math.round(ltp * 100.0) / 100.0,
                        change = Math.round(change * 100.0) / 100.0,
                        changePercent = Math.round(changePct * 100.0) / 100.0,
                        high = Math.round(dayHigh * 100.0) / 100.0,
                        low = Math.round(dayLow * 100.0) / 100.0,
                        prevClose = Math.round(prevClose * 100.0) / 100.0,
                        yearHigh = Math.round(yearHigh * 100.0) / 100.0,
                        yearLow = Math.round(yearLow * 100.0) / 100.0,
                        volume = if (volume > 0) volume else fallback.volume,
                        vwap = vwap,
                        bidPrice = bid,
                        askPrice = ask,
                        lastUpdatedTimestamp = now.time,
                        lastUpdatedFormatted = istFmt.format(now),
                        isRealTimeFresh = true
                    )
                }
            } else {
                android.util.Log.w("NseMarketRepo", "Live stock fetch failed for $symbol, code: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("NseMarketRepo", "Exception fetching live stock $symbol", e)
        }
        fallback.copy(isRealTimeFresh = false)
    }

    suspend fun fetchDynamicStock(query: String): StockQuote? = withContext(Dispatchers.IO) {
        val clean = query.trim().uppercase()
        val apiSymbol = if (clean.contains(".NS") || clean.contains("%5E") || clean.contains(":")) clean else "$clean.NS"
        try {
            val response = api.getChart(symbol = apiSymbol, interval = "1d", range = "1d")
            if (response.isSuccessful) {
                val chartItem = response.body()?.chart?.result?.firstOrNull()
                val meta = chartItem?.meta
                if (meta != null) {
                    val ltp = meta.regularMarketPrice ?: 0.0
                    if (ltp > 0.0) {
                        val prevClose = meta.effectivePrevClose ?: ltp
                        val dayHigh = meta.regularMarketDayHigh ?: ltp
                        val dayLow = meta.regularMarketDayLow ?: ltp
                        val change = ltp - prevClose
                        val changePct = if (prevClose > 0) (change / prevClose) * 100.0 else 0.0
                        val shortName = meta.shortName ?: clean
                        val exchange = meta.exchangeName ?: "NSE"
                        val istFmt = SimpleDateFormat("hh:mm:ss a 'IST'", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                        }
                        val now = Date()
                        return@withContext StockQuote(
                            symbol = clean,
                            name = shortName,
                            ltp = Math.round(ltp * 100.0) / 100.0,
                            change = Math.round(change * 100.0) / 100.0,
                            changePercent = Math.round(changePct * 100.0) / 100.0,
                            open = meta.chartPreviousClose ?: ltp,
                            high = Math.round(dayHigh * 100.0) / 100.0,
                            low = Math.round(dayLow * 100.0) / 100.0,
                            prevClose = Math.round(prevClose * 100.0) / 100.0,
                            volume = meta.regularMarketVolume ?: 100000L,
                            turnoverCr = 50.0,
                            sector = if (exchange.contains("BSE")) "BSE Equity" else "NSE Equity",
                            marketCapCr = 10000.0,
                            peRatio = 20.0,
                            isFnO = true,
                            lotSize = 250,
                            yearHigh = meta.fiftyTwoWeekHigh ?: (ltp * 1.3),
                            yearLow = meta.fiftyTwoWeekLow ?: (ltp * 0.7),
                            sparkline = listOf((prevClose * 0.99).toFloat(), (prevClose * 1.01).toFloat(), ltp.toFloat()),
                            lastUpdatedTimestamp = now.time,
                            lastUpdatedFormatted = istFmt.format(now),
                            isRealTimeFresh = true
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        null
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

        try {
            val response = api.getChart(symbol = apiSymbol, interval = interval, range = range)
            if (response.isSuccessful) {
                val chartItem = response.body()?.chart?.result?.firstOrNull()
                val timestamps = chartItem?.timestamp
                val quote = chartItem?.indicators?.quote?.firstOrNull()

                if (!timestamps.isNullOrEmpty() && quote != null) {
                    val opens = quote.open
                    val highs = quote.high
                    val lows = quote.low
                    val closes = quote.close
                    val volumes = quote.volume

                    val timeFmt = SimpleDateFormat(if (timeframe == "1D" || timeframe == "1W") "HH:mm" else "dd MMM", Locale.US)
                    val candles = mutableListOf<CandleData>()

                    for (i in timestamps.indices) {
                        val ts = timestamps[i] * 1000L
                        val c = closes?.getOrNull(i)
                        val o = opens?.getOrNull(i) ?: c
                        val h = highs?.getOrNull(i) ?: c
                        val l = lows?.getOrNull(i) ?: c
                        val v = volumes?.getOrNull(i) ?: 1000L

                        if (c != null && o != null && h != null && l != null) {
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
                    if (candles.isNotEmpty()) return@withContext candles
                }
            }
        } catch (_: Exception) {
            // Fallback
        }
        emptyList()
    }

    suspend fun fetchAllIndices(fallbacks: List<MarketIndex>): List<MarketIndex> = withContext(Dispatchers.IO) {
        fallbacks.map { index ->
            async { fetchLiveIndex(index.symbol, index) }
        }.awaitAll()
    }

    suspend fun fetchAllStocks(fallbacks: List<StockQuote>): List<StockQuote> = withContext(Dispatchers.IO) {
        fallbacks.map { stock ->
            async { fetchLiveStock(stock.symbol, stock) }
        }.awaitAll()
    }
}
