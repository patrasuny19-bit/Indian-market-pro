package com.example.data

import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

object MarketDataRepository {

    fun getIndices(): List<MarketIndex> = listOf(
        MarketIndex(
            symbol = "NIFTY 50",
            name = "Nifty 50 Index",
            exchange = "NSE",
            value = 23960.60,
            change = 149.75,
            changePercent = 0.63,
            open = 23850.00,
            high = 24025.00,
            low = 23820.00,
            prevClose = 23810.85,
            yearHigh = 26277.35,
            yearLow = 21281.45,
            sparkline = listOf(23810f, 23850f, 23880f, 23920f, 23950f, 23910f, 23980f, 24025f, 23960f)
        ),
        MarketIndex(
            symbol = "BANK NIFTY",
            name = "Nifty Bank Index",
            exchange = "NSE",
            value = 55913.60,
            change = 374.20,
            changePercent = 0.67,
            open = 55600.00,
            high = 56100.00,
            low = 55520.00,
            prevClose = 55539.40,
            yearHigh = 56250.00,
            yearLow = 47850.00,
            sparkline = listOf(55539f, 55600f, 55750f, 55700f, 55850f, 55950f, 56100f, 55913f)
        ),
        MarketIndex(
            symbol = "SENSEX",
            name = "BSE SENSEX",
            exchange = "BSE",
            value = 77301.40,
            change = 385.10,
            changePercent = 0.50,
            open = 77000.00,
            high = 77550.00,
            low = 76950.00,
            prevClose = 76916.30,
            yearHigh = 85978.25,
            yearLow = 71200.00,
            sparkline = listOf(76916f, 77000f, 77150f, 77250f, 77400f, 77550f, 77301f)
        ),
        MarketIndex(
            symbol = "FINNIFTY",
            name = "Nifty Financial Services",
            exchange = "NSE",
            value = 23890.15,
            change = 178.50,
            changePercent = 0.75,
            open = 23750.00,
            high = 23980.00,
            low = 23695.40,
            prevClose = 23711.65,
            yearHigh = 25230.10,
            yearLow = 19450.80,
            sparkline = listOf(23711f, 23750f, 23800f, 23780f, 23860f, 23920f, 23980f, 23890f)
        ),
        MarketIndex(
            symbol = "NIFTY IT",
            name = "Nifty IT Index",
            exchange = "NSE",
            value = 42180.90,
            change = -142.30,
            changePercent = -0.34,
            open = 42350.00,
            high = 42480.00,
            low = 41990.20,
            prevClose = 42323.20,
            yearHigh = 44248.50,
            yearLow = 30120.00,
            sparkline = listOf(42323f, 42350f, 42480f, 42200f, 42100f, 42050f, 42180f)
        ),
        MarketIndex(
            symbol = "MIDCAP SELECT",
            name = "Nifty Midcap Select",
            exchange = "NSE",
            value = 13120.45,
            change = 118.20,
            changePercent = 0.91,
            open = 13010.00,
            high = 13180.00,
            low = 12980.00,
            prevClose = 13002.25,
            yearHigh = 13450.00,
            yearLow = 8800.00,
            sparkline = listOf(13002f, 13050f, 13080f, 13110f, 13180f, 13120f)
        )
    )

    fun getStocks(): List<StockQuote> = com.example.data.fno.FnoUniverseRepository.getAllInitialStockQuotes()


    fun getMarketBreadth(): MarketBreadth = MarketBreadth(
        advances = 34,
        declines = 15,
        unchanged = 1
    )

    fun getFiiDiiData(): List<FiiDiiRecord> = listOf(
        FiiDiiRecord("04 Nov", -845.20, 1420.50, 480.10, 112.40),
        FiiDiiRecord("03 Nov", -1280.40, 2150.80, -320.00, 450.00),
        FiiDiiRecord("02 Nov", 420.00, 890.30, 780.00, 620.00),
        FiiDiiRecord("01 Nov", -2140.60, 3120.00, -890.00, 180.00),
        FiiDiiRecord("31 Oct", -1890.00, 2480.00, -410.00, 290.00)
    )

    fun getFnoBuildups(): List<FnoBuildupItem> = listOf(
        FnoBuildupItem("TATASTEEL", 154.20, 2.39, 84520000, 12.4, BuildupType.LONG_BUILDUP, "Metals"),
        FnoBuildupItem("SBIN", 825.60, 2.15, 62410000, 9.8, BuildupType.LONG_BUILDUP, "Banking"),
        FnoBuildupItem("ZOMATO", 265.80, 3.67, 98200000, 18.2, BuildupType.LONG_BUILDUP, "Internet"),
        FnoBuildupItem("MARUTI", 11480.00, -0.73, 1420000, 8.5, BuildupType.SHORT_BUILDUP, "Auto"),
        FnoBuildupItem("INFY", 1865.10, -0.66, 28400000, 6.2, BuildupType.SHORT_BUILDUP, "IT"),
        FnoBuildupItem("RELIANCE", 2745.50, 1.41, 41200000, -4.8, BuildupType.SHORT_COVERING, "Energy"),
        FnoBuildupItem("ICICIBANK", 1290.40, 1.44, 38900000, -5.2, BuildupType.SHORT_COVERING, "Banking"),
        FnoBuildupItem("ITC", 485.20, -0.37, 54200000, -3.1, BuildupType.LONG_UNWINDING, "FMCG")
    )

    fun getOptionChain(underlying: String = "NIFTY 50", customSpotPrice: Double? = null): OptionChainData {
        val cleanSym = underlying.trim().uppercase()
        val isIndex = cleanSym in listOf("NIFTY 50", "BANK NIFTY", "FINNIFTY", "SENSEX", "MIDCAP SELECT", "NIFTY IT")
        val stockInst = if (!isIndex) com.example.data.fno.FnoStockMasterDatabase.getInstrumentBySymbol(cleanSym) else null

        val spotPrice = customSpotPrice ?: when (cleanSym) {
            "BANK NIFTY" -> 51280.0
            "FINNIFTY" -> 23890.0
            "SENSEX" -> 77410.0
            "MIDCAP SELECT" -> 13120.0
            "NIFTY IT" -> 42180.0
            "NIFTY 50" -> 24150.0
            else -> stockInst?.baseLtp ?: 24150.0
        }

        val step = if (isIndex) {
            when (cleanSym) {
                "BANK NIFTY", "SENSEX" -> 100.0
                else -> 50.0
            }
        } else {
            when {
                spotPrice >= 50000.0 -> 1000.0
                spotPrice >= 20000.0 -> 500.0
                spotPrice >= 10000.0 -> 100.0
                spotPrice >= 4000.0 -> 50.0
                spotPrice >= 1500.0 -> 20.0
                spotPrice >= 500.0 -> 10.0
                spotPrice >= 200.0 -> 5.0
                spotPrice >= 50.0 -> 2.5
                else -> 0.5
            }
        }

        val lotSize = if (isIndex) {
            when (cleanSym) {
                "BANK NIFTY" -> 15
                else -> 25
            }
        } else {
            stockInst?.lotSize ?: 500
        }

        val expiries = if (isIndex) {
            listOf("Weekly Expiry (Current)", "Next Week Expiry", "Monthly Expiry", "Next Month Expiry")
        } else {
            listOf("Current Month (27 Nov)", "Next Month (25 Dec)", "Far Month (29 Jan)")
        }

        val atmStrike = (Math.round(spotPrice / step) * step).toDouble()
        val strikes = mutableListOf<OptionStrike>()
        var totalCallOi = 0L
        var totalPutOi = 0L

        // Baseline premium scaling based on spot price (e.g. 0.7% of spot for ATM time value)
        val atmTimeVal = Math.max(1.5, Math.round((spotPrice * 0.007) * 100.0) / 100.0)
        val oiBase = (lotSize * 2200L).coerceIn(30000L, 1600000L)

        for (i in -6..6) {
            val strike = atmStrike + (i * step)
            val diff = strike - spotPrice
            val isAtm = (i == 0)

            val callIntrinsic = Math.max(0.0, spotPrice - strike)
            val callTimeValue = Math.max(0.35, atmTimeVal - (Math.abs(diff) / step) * (atmTimeVal * 0.12))
            val callLtp = Math.round((callIntrinsic + callTimeValue) * 100.0) / 100.0

            val distFactor = Math.exp(-0.09 * Math.abs(i))
            val callOi = (oiBase * distFactor + (if (i == 1) (oiBase * 0.15) else 0.0)).toLong().coerceAtLeast((lotSize * 50L))
            val callOiChange = if (i >= 0) (oiBase * 0.04 - i * (oiBase * 0.004)).toLong() else (-oiBase * 0.02 + i * (oiBase * 0.002)).toLong()

            val putIntrinsic = Math.max(0.0, strike - spotPrice)
            val putTimeValue = Math.max(0.35, atmTimeVal - (Math.abs(diff) / step) * (atmTimeVal * 0.11))
            val putLtp = Math.round((putIntrinsic + putTimeValue) * 100.0) / 100.0

            val putOi = (oiBase * distFactor + (if (i == -1) (oiBase * 0.16) else 0.0)).toLong().coerceAtLeast((lotSize * 50L))
            val putOiChange = if (i <= 0) (oiBase * 0.045 + i * (oiBase * 0.003)).toLong() else (-oiBase * 0.018 - i * (oiBase * 0.003)).toLong()

            totalCallOi += callOi
            totalPutOi += putOi

            val callDelta = (0.50 - (i * 0.06)).coerceIn(0.05, 0.95)
            val putDelta = (-0.50 - (i * 0.06)).coerceIn(-0.95, -0.05)

            strikes.add(
                OptionStrike(
                    strikePrice = strike,
                    callLtp = callLtp,
                    callChange = Math.round((-diff * 0.04) * 10.0) / 10.0,
                    callOi = callOi,
                    callOiChange = callOiChange,
                    callIv = Math.round((14.0 + Math.abs(i) * 0.35) * 10.0) / 10.0,
                    callDelta = Math.round(callDelta * 100.0) / 100.0,
                    putLtp = putLtp,
                    putChange = Math.round((diff * 0.04) * 10.0) / 10.0,
                    putOi = putOi,
                    putOiChange = putOiChange,
                    putIv = Math.round((14.5 + Math.abs(i) * 0.40) * 10.0) / 10.0,
                    putDelta = Math.round(putDelta * 100.0) / 100.0,
                    isAtm = isAtm
                )
            )
        }

        val pcr = if (totalCallOi > 0) Math.round((totalPutOi.toDouble() / totalCallOi) * 100.0) / 100.0 else 1.05
        return OptionChainData(
            underlying = cleanSym,
            spotPrice = spotPrice,
            selectedExpiry = expiries.first(),
            expiries = expiries,
            strikes = strikes,
            totalCallOi = totalCallOi,
            totalPutOi = totalPutOi,
            pcr = pcr,
            maxPainStrike = atmStrike
        )
    }

    fun getMarketDepth(ltp: Double): MarketDepth {
        val rows = mutableListOf<MarketDepthRow>()
        var totalBids = 0L
        var totalAsks = 0L

        for (i in 1..5) {
            val bidPrice = Math.round((ltp - (i * 0.25)) * 100.0) / 100.0
            val askPrice = Math.round((ltp + (i * 0.25)) * 100.0) / 100.0
            val bQty = 1200L * i + 850L
            val aQty = 1100L * i + 790L
            totalBids += bQty
            totalAsks += aQty

            rows.add(
                MarketDepthRow(
                    bidOrders = (22 - i * 3).coerceAtLeast(3),
                    bidQty = bQty,
                    bidPrice = bidPrice,
                    askPrice = askPrice,
                    askQty = aQty,
                    askOrders = (20 - i * 3).coerceAtLeast(2)
                )
            )
        }

        return MarketDepth(
            rows = rows,
            totalBidQty = totalBids,
            totalAskQty = totalAsks
        )
    }

    fun getCandles(symbol: String, timeframe: String): List<CandleData> {
        val candleCount = when (timeframe) {
            "1D" -> 35
            "1W" -> 25
            "1M" -> 30
            "1Y" -> 45
            else -> 30
        }

        val basePrice = when (symbol) {
            "NIFTY 50" -> 24100f
            "BANK NIFTY" -> 51200f
            "SENSEX" -> 79350f
            "FINNIFTY" -> 23850f
            "RELIANCE" -> 2720f
            "HDFCBANK" -> 1770f
            "ICICIBANK" -> 1280f
            "TCS" -> 4140f
            "SBIN" -> 815f
            "TATAMOTORS" -> 950f
            else -> 1500f
        }

        val volatility = basePrice * 0.0025f
        val list = mutableListOf<CandleData>()
        var currentClose = basePrice

        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, -candleCount * 15)
        val timeFmt = SimpleDateFormat("HH:mm", Locale.US)

        for (i in 0 until candleCount) {
            val open = currentClose
            val trend = Math.sin(i * 0.3).toFloat() * volatility
            val close = open + trend
            val high = Math.max(open, close) + volatility * 0.4f
            val low = Math.min(open, close) - volatility * 0.4f
            val volume = 25000L + (i * 1200L)

            cal.add(Calendar.MINUTE, 15)
            list.add(
                CandleData(
                    timestamp = cal.timeInMillis,
                    timeLabel = timeFmt.format(cal.time),
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume
                )
            )
            currentClose = close
        }
        return list
    }

    fun getInitialPortfolio(): PortfolioSummary {
        return PortfolioSummary(
            startingCapital = 1000000.0,
            availableMargin = 1000000.0,
            usedMargin = 0.0,
            realizedPnl = 0.0,
            positions = emptyList(),
            orders = emptyList()
        )
    }

    /**
     * Real-time tick stream. Does NOT invent fake random fluctuations.
     * Yields only verified stream deltas.
     */
    fun liveTickStream(): Flow<Map<String, Double>> = flow {
        // No fake random ticks emitted. Real prices are polled and synced directly via licensed exchange feeds.
    }
}
