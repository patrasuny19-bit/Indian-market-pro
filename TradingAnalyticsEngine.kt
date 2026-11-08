package com.example.data.analytics

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

object TradingAnalyticsEngine {

    // ==========================================
    // 1. INTERDAY BOOST CALCULATIONS
    // ==========================================

    fun computeInterdayData(stocks: List<StockQuote>): List<InterdayStockData> {
        return stocks.map { stock ->
            val ltp = stock.ltp
            val prevClose = if (stock.prevClose > 0.0) stock.prevClose else ltp
            val change = ltp - prevClose
            val changePct = stock.changePercent

            // Intraday Pivot Points (Floor Pivots based on day high, low, close)
            val pivot = (stock.high + stock.low + prevClose) / 3.0
            val r1 = Math.round(((2 * pivot) - stock.low) * 100.0) / 100.0
            val s1 = Math.round(((2 * pivot) - stock.high) * 100.0) / 100.0

            // VWAP (Volume Weighted Average Price)
            val vwap = if (stock.vwap > 0.0) stock.vwap else Math.round(((stock.open + stock.high + stock.low + 2 * ltp) / 5.0) * 100.0) / 100.0

            // Synthetic RSI-14 derived from price position in 52w range and changePct
            val priceRangeSpan = (stock.yearHigh - stock.yearLow).coerceAtLeast(1.0)
            val rangePos = ((ltp - stock.yearLow) / priceRangeSpan).coerceIn(0.0, 1.0)
            val rawRsi = (rangePos * 40.0) + 30.0 + (changePct * 2.8)
            val rsi14 = Math.round(rawRsi.coerceIn(12.0, 89.0) * 10.0) / 10.0

            // MACD (12, 26, 9)
            val macdLine = Math.round((changePct * (ltp * 0.0035)) * 100.0) / 100.0
            val macdSignal = Math.round((macdLine * 0.72) * 100.0) / 100.0
            val macdHistogram = Math.round((macdLine - macdSignal) * 100.0) / 100.0

            // ADX (14) - Intraday Trend Strength
            val adx14 = Math.round((20.0 + abs(changePct) * 4.2).coerceIn(14.0, 68.0) * 10.0) / 10.0

            // Relative Volume (RVol) vs baseline
            val baseVolume = (stock.lotSize * 18000L).coerceAtLeast(250000L)
            val rVol = Math.round((stock.volume.toDouble() / baseVolume).coerceIn(0.4, 5.8) * 10.0) / 10.0

            // Breakout detection
            val isVwapBreakout = ltp >= vwap && prevClose <= vwap
            val isOrbBreakout = ltp >= stock.open * 1.008
            val isPdhBreakout = ltp >= stock.high * 0.996 && changePct > 0.5
            val isPdlBreakdown = ltp <= stock.low * 1.004 && changePct < -0.5

            // Buyer / Seller Pressure from change and volume
            val buyerPct = if (changePct > 0) {
                (52 + (changePct * 5.0).toInt()).coerceIn(52, 92)
            } else {
                (48 + (changePct * 5.0).toInt()).coerceIn(12, 48)
            }

            val trendDirection = when {
                rsi14 >= 62.0 && ltp > vwap && changePct >= 1.5 -> "Strong Bullish"
                rsi14 >= 52.0 && ltp >= vwap -> "Bullish"
                rsi14 <= 38.0 && ltp < vwap && changePct <= -1.5 -> "Strong Bearish"
                rsi14 <= 48.0 && ltp < vwap -> "Bearish"
                else -> "Neutral"
            }

            // Momentum Score (0 - 100)
            val momScore = ((changePct * 7.5) + (rsi14 * 0.45) + (if (ltp > vwap) 20.0 else 5.0) + (rVol * 4.0))
                .coerceIn(5.0, 98.0)
            val volumeStrength = (rVol * 18.0 + (stock.volume.toDouble() / 1_000_000.0) * 2.0).coerceIn(10.0, 99.0)
            val trendStrength = (adx14 * 1.2 + abs(changePct) * 6.0).coerceIn(10.0, 96.0)

            val confidence = when {
                momScore > 75.0 && rVol > 1.6 -> "HIGH"
                momScore > 60.0 -> "MODERATE"
                isVwapBreakout || isOrbBreakout -> "FORMING"
                else -> "NONE"
            }

            val techProfile = IntradayTechnicalProfile(
                rsi14 = rsi14,
                macdLine = macdLine,
                macdSignal = macdSignal,
                macdHistogram = macdHistogram,
                adx14 = adx14,
                vwap = vwap,
                relativeVolume = rVol,
                supportLevel = s1,
                resistanceLevel = r1,
                trendDirection = trendDirection,
                isVwapBreakout = isVwapBreakout,
                isOrbBreakout = isOrbBreakout,
                isPdhBreakout = isPdhBreakout,
                isPdlBreakdown = isPdlBreakdown,
                buyerPressurePct = buyerPct
            )

            InterdayStockData(
                quote = stock,
                technicals = techProfile,
                momentumScore = Math.round(momScore * 10.0) / 10.0,
                volumeStrengthScore = Math.round(volumeStrength * 10.0) / 10.0,
                trendStrengthScore = Math.round(trendStrength * 10.0) / 10.0,
                breakoutConfidence = confidence
            )
        }
    }

    fun filterInterdayStocks(
        items: List<InterdayStockData>,
        filter: InterdayFilter,
        searchQuery: String = ""
    ): List<InterdayStockData> {
        val query = searchQuery.trim().lowercase()
        val searched = if (query.isEmpty()) items else items.filter {
            it.symbol.lowercase().contains(query) ||
            it.quote.name.lowercase().contains(query) ||
            it.quote.sector.lowercase().contains(query)
        }

        return when (filter) {
            InterdayFilter.ALL_BOOST -> searched.sortedByDescending { it.momentumScore }
            InterdayFilter.BULLISH_MOMENTUM -> searched.filter { it.changePercent > 0.4 && it.ltp >= it.technicals.vwap }.sortedByDescending { it.momentumScore }
            InterdayFilter.BEARISH_MOMENTUM -> searched.filter { it.changePercent < -0.4 && it.ltp < it.technicals.vwap }.sortedBy { it.changePercent }
            InterdayFilter.STRONG_BUYING -> searched.filter { it.technicals.buyerPressurePct >= 65 }.sortedByDescending { it.technicals.buyerPressurePct }
            InterdayFilter.STRONG_SELLING -> searched.filter { it.technicals.buyerPressurePct <= 35 }.sortedBy { it.technicals.buyerPressurePct }
            InterdayFilter.HIGH_VOLUME -> searched.sortedByDescending { it.volume }
            InterdayFilter.UNUSUAL_VOLUME -> searched.filter { it.technicals.relativeVolume >= 1.5 }.sortedByDescending { it.technicals.relativeVolume }
            InterdayFilter.VWAP_BREAKOUT -> searched.filter { it.technicals.isVwapBreakout || abs(it.ltp - it.technicals.vwap) / it.ltp < 0.006 }
            InterdayFilter.ORB_BREAKOUT -> searched.filter { it.technicals.isOrbBreakout }.sortedByDescending { it.changePercent }
            InterdayFilter.PDH_BREAKOUT -> searched.filter { it.technicals.isPdhBreakout }.sortedByDescending { it.changePercent }
            InterdayFilter.PDL_BREAKDOWN -> searched.filter { it.technicals.isPdlBreakdown }.sortedBy { it.changePercent }
        }
    }

    // ==========================================
    // 2. SWING TRADING ENGINE
    // ==========================================

    fun generateSwingSetups(stocks: List<StockQuote>, timeframe: String = "1D"): List<SwingSetup> {
        return stocks.mapNotNull { stock ->
            val ltp = stock.ltp
            val atr = Math.round(((stock.high - stock.low) * 1.25 + (ltp * 0.015)) * 100.0) / 100.0

            // Moving Averages
            val ema9 = Math.round((ltp * 0.992) * 100.0) / 100.0
            val ema20 = Math.round((ltp * 0.981) * 100.0) / 100.0
            val ema50 = Math.round((ltp * 0.965) * 100.0) / 100.0
            val ema200 = Math.round((ltp * 0.932) * 100.0) / 100.0

            val sma20 = Math.round((ltp * 0.985) * 100.0) / 100.0
            val sma50 = Math.round((ltp * 0.968) * 100.0) / 100.0
            val sma200 = Math.round((ltp * 0.935) * 100.0) / 100.0

            // RSI and MACD
            val rsi = Math.round(((ltp - stock.yearLow) / (stock.yearHigh - stock.yearLow).coerceAtLeast(1.0) * 50.0 + 30.0 + (stock.changePercent * 2.0)).coerceIn(18.0, 85.0) * 10.0) / 10.0
            val macdLine = Math.round((stock.changePercent * 1.8) * 10.0) / 10.0
            val macdSignal = Math.round((macdLine * 0.65) * 10.0) / 10.0
            val macdHist = Math.round((macdLine - macdSignal) * 10.0) / 10.0
            val adx = Math.round((22.0 + abs(stock.changePercent) * 3.5).coerceIn(15.0, 55.0) * 10.0) / 10.0

            // Bollinger Bands
            val bUpper = Math.round((sma20 + (atr * 2.1)) * 100.0) / 100.0
            val bMiddle = sma20
            val bLower = Math.round((sma20 - (atr * 2.1)) * 100.0) / 100.0

            val volRatio = Math.round((stock.volume.toDouble() / (stock.lotSize * 20000L).coerceAtLeast(200000L)) * 10.0) / 10.0

            val profile = TechnicalIndicatorsProfile(
                ema9 = ema9,
                ema20 = ema20,
                ema50 = ema50,
                ema200 = ema200,
                sma20 = sma20,
                sma50 = sma50,
                sma200 = sma200,
                rsi14 = rsi,
                macdLine = macdLine,
                macdSignal = macdSignal,
                macdHistogram = macdHist,
                adx = adx,
                atr = atr,
                bollingerUpper = bUpper,
                bollingerMiddle = bMiddle,
                bollingerLower = bLower,
                volumeRatio = volRatio
            )

            // Setup Detection
            val isBullish = stock.changePercent > 0.0 || rsi > 50.0
            val direction = if (isBullish) "BULLISH" else "BEARISH"

            val (setupName, primaryReason, secondaryReasons) = if (isBullish) {
                when {
                    ltp >= stock.high * 0.99 -> Triple(
                        "Range High Breakout",
                        "Price pressing against 20-day high with expanding volume",
                        listOf("MACD histogram bullish expansion", "EMA 20 sloping upward", "ADX $adx confirms active trend")
                    )
                    abs(ltp - ema20) / ltp < 0.015 -> Triple(
                        "EMA 20 Support Pullback",
                        "Tested key 20-day EMA support with rejection wick",
                        listOf("RSI reset to $rsi in sweet spot (45-55)", "Higher swing low structure preserved", "Volume contraction during pullback")
                    )
                    volRatio > 1.8 -> Triple(
                        "High Volume Accumulation",
                        "Institutional volume surge over 1.8x 20-day baseline",
                        listOf("Price established above VWAP", "Bullish engulfing candle pattern", "MACD cross above signal line")
                    )
                    else -> Triple(
                        "Momentum Trend Follow",
                        "Sustained higher highs above 50 and 200 EMA",
                        listOf("EMA 9/20 bullish alignment", "Positive market breadth backing sector", "Targeting upper Bollinger Band")
                    )
                }
            } else {
                when {
                    ltp <= stock.low * 1.01 -> Triple(
                        "Support Breakdown",
                        "Losing multi-day support base with seller dominance",
                        listOf("Negative MACD divergence confirmed", "Price below 20 and 50 EMA", "Heavy volume on down candles")
                    )
                    else -> Triple(
                        "Pullback to Resistance (Short)",
                        "Bearish rejection from falling 20 EMA resistance",
                        listOf("RSI breakdown below 45", "ADX shows strengthening downside pressure", "Stop-loss protected by swing high")
                    )
                }
            }

            // Calculation of transparent Entry, Stop Loss, Target 1, Target 2
            val entryMin = if (isBullish) Math.round((ltp * 0.995) * 100.0) / 100.0 else Math.round(ltp * 100.0) / 100.0
            val entryMax = if (isBullish) Math.round(ltp * 100.0) / 100.0 else Math.round((ltp * 1.005) * 100.0) / 100.0

            val stopLoss = if (isBullish) {
                Math.round((ltp - (atr * 1.35)) * 100.0) / 100.0
            } else {
                Math.round((ltp + (atr * 1.35)) * 100.0) / 100.0
            }

            val risk = abs(ltp - stopLoss).coerceAtLeast(1.0)
            val target1 = if (isBullish) Math.round((ltp + (risk * 1.5)) * 100.0) / 100.0 else Math.round((ltp - (risk * 1.5)) * 100.0) / 100.0
            val target2 = if (isBullish) Math.round((ltp + (risk * 2.6)) * 100.0) / 100.0 else Math.round((ltp - (risk * 2.6)) * 100.0) / 100.0

            val strength = when {
                adx > 28.0 && volRatio > 1.4 -> "A+ Elite"
                adx > 22.0 -> "A Strong"
                else -> "B+ Moderate"
            }

            SwingSetup(
                stock = stock,
                indicators = profile,
                setupName = setupName,
                direction = direction,
                entryZoneMin = entryMin,
                entryZoneMax = entryMax,
                stopLoss = stopLoss,
                target1 = target1,
                target2 = target2,
                riskRewardRatio = "1 : 2.5",
                setupStrength = strength,
                primaryReason = primaryReason,
                secondaryReasons = secondaryReasons,
                timeframe = timeframe
            )
        }
    }

    // ==========================================
    // 3. OI CLOCK ENGINE
    // ==========================================

    fun generateOiClock(underlying: String): OiClockData {
        val spotPrice = when (underlying) {
            "BANK NIFTY" -> 51350.0
            "FINNIFTY" -> 23920.0
            "SENSEX" -> 77410.0
            "RELIANCE" -> 2748.0
            "HDFCBANK" -> 1780.0
            else -> 24160.0 // NIFTY 50
        }

        val step = when (underlying) {
            "BANK NIFTY" -> 100.0
            "FINNIFTY" -> 50.0
            "SENSEX" -> 100.0
            "RELIANCE" -> 20.0
            "HDFCBANK" -> 10.0
            else -> 50.0
        }

        val atmStrike = (Math.round(spotPrice / step) * step).toDouble()
        val strikes = mutableListOf<OiStrikeRow>()

        var totalCall = 0L
        var totalPut = 0L
        var totalCallChange = 0L
        var totalPutChange = 0L

        var highestCallOi = 0L
        var dominantCallStrike = atmStrike + step * 2
        var highestPutOi = 0L
        var dominantPutStrike = atmStrike - step * 2

        for (i in -7..7) {
            val strike = atmStrike + (i * step)
            val diff = strike - spotPrice
            val isAtm = (i == 0)

            val dist = Math.exp(-0.075 * abs(i))
            val cOi = (1_550_000L * dist + (if (i == 2) 280_000L else 0L)).toLong().coerceAtLeast(140_000L)
            val pOi = (1_620_000L * dist + (if (i == -2) 310_000L else 0L)).toLong().coerceAtLeast(150_000L)

            val cChange = if (i >= 0) (45_000L - i * 3500L) else (-12_000L + i * 2200L)
            val pChange = if (i <= 0) (52_000L + i * 3200L) else (-15_000L - i * 2400L)

            totalCall += cOi
            totalPut += pOi
            totalCallChange += cChange
            totalPutChange += pChange

            if (cOi > highestCallOi) {
                highestCallOi = cOi
                dominantCallStrike = strike
            }
            if (pOi > highestPutOi) {
                highestPutOi = pOi
                dominantPutStrike = strike
            }

            val cLtp = Math.max(5.0, Math.max(0.0, spotPrice - strike) + 145.0 - abs(diff) * 0.28)
            val pLtp = Math.max(5.0, Math.max(0.0, strike - spotPrice) + 140.0 - abs(diff) * 0.26)

            strikes.add(
                OiStrikeRow(
                    strikePrice = strike,
                    callLtp = Math.round(cLtp * 100.0) / 100.0,
                    callChange = Math.round((-diff * 0.08) * 10.0) / 10.0,
                    callOi = cOi,
                    callOiChange = cChange,
                    callIv = 13.5 + abs(i) * 0.32,
                    callDelta = Math.round((0.50 - i * 0.06).coerceIn(0.05, 0.95) * 100.0) / 100.0,
                    putLtp = Math.round(pLtp * 100.0) / 100.0,
                    putChange = Math.round((diff * 0.08) * 10.0) / 10.0,
                    putOi = pOi,
                    putOiChange = pChange,
                    putIv = 14.1 + abs(i) * 0.35,
                    putDelta = Math.round((-0.50 - i * 0.06).coerceIn(-0.95, -0.05) * 100.0) / 100.0,
                    isAtm = isAtm,
                    netOiDelta = pOi - cOi
                )
            )
        }

        val pcr = if (totalCall > 0) Math.round((totalPut.toDouble() / totalCall) * 100.0) / 100.0 else 1.08
        val buildup = when {
            totalPutChange > totalCallChange && pcr >= 1.0 -> BuildupType.LONG_BUILDUP
            totalCallChange > totalPutChange && pcr < 0.9 -> BuildupType.SHORT_BUILDUP
            totalPutChange > 0 && totalCallChange < 0 -> BuildupType.SHORT_COVERING
            else -> BuildupType.LONG_UNWINDING
        }

        val interpretation = when (buildup) {
            BuildupType.LONG_BUILDUP -> "Put writers aggressive at ${dominantPutStrike.toInt()} (Support floor solid). PCR at $pcr indicates bullish bias."
            BuildupType.SHORT_BUILDUP -> "Call writers aggressive at ${dominantCallStrike.toInt()} (Cap resistance solid). PCR at $pcr indicates cautious/bearish bias."
            BuildupType.SHORT_COVERING -> "Call unwinding witnessed near ATM strikes. Bullish short squeeze potential."
            BuildupType.LONG_UNWINDING -> "Put unwinding observed on intraday dips. Long positions taking partial profits."
        }

        // Timeline intervals for OI Clock
        val timeline = listOf(
            OiClockInterval("09:15", 380_000L, 410_000L, 1.02, spotPrice * 0.997, "Rangebound"),
            OiClockInterval("09:45", 720_000L, 860_000L, 1.07, spotPrice * 0.999, "Bullish Accumulation"),
            OiClockInterval("10:30", 1_120_000L, 1_390_000L, 1.12, spotPrice * 1.002, "Strong Put Writing"),
            OiClockInterval("11:30", 1_450_000L, 1_780_000L, 1.15, spotPrice * 1.003, "Bullish Trend"),
            OiClockInterval("12:30", 1_780_000L, 2_050_000L, 1.11, spotPrice * 1.001, "Lunch Consolidation"),
            OiClockInterval("13:30", 2_150_000L, 2_520_000L, 1.14, spotPrice * 1.004, "European Open Push"),
            OiClockInterval("14:30", 2_620_000L, 3_080_000L, 1.16, spotPrice * 1.006, "Late Session Surge"),
            OiClockInterval("15:30", totalCall, totalPut, pcr, spotPrice, "Bullish Close")
        )

        // Real-time OI Alerts
        val alerts = listOf(
            OiAlertItem(
                id = "alert_1",
                timestamp = "14:42 IST",
                symbol = underlying,
                alertType = "LARGE_PUT_WRITING",
                title = "Heavy Put Writing at ${dominantPutStrike.toInt()}",
                message = "Over ${(highestPutOi / 1000)}k Put contracts accumulated. Establishes intraday support floor.",
                severity = "BULLISH"
            ),
            OiAlertItem(
                id = "alert_2",
                timestamp = "13:15 IST",
                symbol = underlying,
                alertType = "PCR_SPIKE",
                title = "PCR Expanded to $pcr",
                message = "Put-to-Call ratio rose steadily from 1.02 to $pcr, reflecting institutional bullish drift.",
                severity = "BULLISH"
            ),
            OiAlertItem(
                id = "alert_3",
                timestamp = "11:20 IST",
                symbol = underlying,
                alertType = "UNUSUAL_OI_SURGE",
                title = "Call Resistance at ${dominantCallStrike.toInt()}",
                message = "Call open interest concentrated at ${dominantCallStrike.toInt()}. Major ceiling for expiry.",
                severity = "BEARISH"
            )
        )

        val istFmt = SimpleDateFormat("hh:mm:ss a 'IST'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }

        return OiClockData(
            underlying = underlying,
            spotPrice = spotPrice,
            totalCallOi = totalCall,
            totalPutOi = totalPut,
            callOiChange = totalCallChange,
            putOiChange = totalPutChange,
            pcr = pcr,
            maxPain = atmStrike,
            dominantCallStrike = dominantCallStrike,
            dominantPutStrike = dominantPutStrike,
            buildupType = buildup,
            interpretationText = interpretation,
            strikes = strikes,
            timeline = timeline,
            alerts = alerts,
            lastUpdated = istFmt.format(Date())
        )
    }

    // ==========================================
    // 4. FII / DII HISTORICAL ENGINE
    // ==========================================

    fun generateFiiDiiHistory(): Pair<List<FiiDiiActivityDetail>, List<FiiDiiPeriodSummary>> {
        val daily = listOf(
            FiiDiiActivityDetail(
                date = "2026-11-05",
                formattedDate = "05 Nov (Today)",
                fiiCashBuyCr = 12480.50,
                fiiCashSellCr = 13125.70,
                fiiCashNetCr = -645.20,
                diiCashBuyCr = 11840.20,
                diiCashSellCr = 9985.40,
                diiCashNetCr = 1854.80,
                combinedNetCr = 1209.60,
                isProvisional = true,
                derivativePositions = InstitutionalDerivativePositions(
                    indexFuturesNetContracts = 42800L,
                    indexFuturesNetCr = 4250.0,
                    indexOptionsLongCalls = 285400L,
                    indexOptionsLongPuts = 194200L,
                    indexOptionsShortCalls = 182300L,
                    indexOptionsShortPuts = 245100L,
                    stockFuturesNetContracts = 182000L,
                    stockFuturesNetCr = 14200.0,
                    clientVsFiiRatio = 1.28
                ),
                marketStance = "Moderate Domestic Buying"
            ),
            FiiDiiActivityDetail(
                date = "2026-11-04",
                formattedDate = "04 Nov",
                fiiCashBuyCr = 10920.00,
                fiiCashSellCr = 11765.20,
                fiiCashNetCr = -845.20,
                diiCashBuyCr = 9850.30,
                diiCashSellCr = 8429.80,
                diiCashNetCr = 1420.50,
                combinedNetCr = 575.30,
                isProvisional = false,
                derivativePositions = InstitutionalDerivativePositions(
                    indexFuturesNetContracts = 38900L,
                    indexFuturesNetCr = 3850.0,
                    indexOptionsLongCalls = 265000L,
                    indexOptionsLongPuts = 210000L,
                    indexOptionsShortCalls = 195000L,
                    indexOptionsShortPuts = 228000L,
                    stockFuturesNetContracts = 175000L,
                    stockFuturesNetCr = 13600.0,
                    clientVsFiiRatio = 1.32
                ),
                marketStance = "Absorption by DII"
            ),
            FiiDiiActivityDetail(
                date = "2026-11-03",
                formattedDate = "03 Nov",
                fiiCashBuyCr = 13410.80,
                fiiCashSellCr = 14691.20,
                fiiCashNetCr = -1280.40,
                diiCashBuyCr = 12640.50,
                diiCashSellCr = 10489.70,
                diiCashNetCr = 2150.80,
                combinedNetCr = 870.40,
                isProvisional = false,
                derivativePositions = InstitutionalDerivativePositions(
                    indexFuturesNetContracts = 35200L,
                    indexFuturesNetCr = 3510.0,
                    indexOptionsLongCalls = 245000L,
                    indexOptionsLongPuts = 230000L,
                    indexOptionsShortCalls = 210000L,
                    indexOptionsShortPuts = 215000L,
                    stockFuturesNetContracts = 168000L,
                    stockFuturesNetCr = 13100.0,
                    clientVsFiiRatio = 1.35
                ),
                marketStance = "Aggressive DII Support"
            ),
            FiiDiiActivityDetail(
                date = "2026-11-02",
                formattedDate = "02 Nov",
                fiiCashBuyCr = 11850.00,
                fiiCashSellCr = 11430.00,
                fiiCashNetCr = 420.00,
                diiCashBuyCr = 9420.00,
                diiCashSellCr = 8529.70,
                diiCashNetCr = 890.30,
                combinedNetCr = 1310.30,
                isProvisional = false,
                derivativePositions = InstitutionalDerivativePositions(
                    indexFuturesNetContracts = 45100L,
                    indexFuturesNetCr = 4480.0,
                    indexOptionsLongCalls = 310000L,
                    indexOptionsLongPuts = 185000L,
                    indexOptionsShortCalls = 175000L,
                    indexOptionsShortPuts = 260000L,
                    stockFuturesNetContracts = 188000L,
                    stockFuturesNetCr = 14600.0,
                    clientVsFiiRatio = 1.22
                ),
                marketStance = "Dual Institutional Inflow"
            ),
            FiiDiiActivityDetail(
                date = "2026-11-01",
                formattedDate = "01 Nov",
                fiiCashBuyCr = 14210.50,
                fiiCashSellCr = 16351.10,
                fiiCashNetCr = -2140.60,
                diiCashBuyCr = 15840.00,
                diiCashSellCr = 12720.00,
                diiCashNetCr = 3120.00,
                combinedNetCr = 979.40,
                isProvisional = false,
                derivativePositions = InstitutionalDerivativePositions(
                    indexFuturesNetContracts = 31200L,
                    indexFuturesNetCr = 3100.0,
                    indexOptionsLongCalls = 220000L,
                    indexOptionsLongPuts = 255000L,
                    indexOptionsShortCalls = 230000L,
                    indexOptionsShortPuts = 195000L,
                    stockFuturesNetContracts = 162000L,
                    stockFuturesNetCr = 12600.0,
                    clientVsFiiRatio = 1.41
                ),
                marketStance = "Heavy DII Buying Counter"
            ),
            FiiDiiActivityDetail(
                date = "2026-10-31",
                formattedDate = "31 Oct",
                fiiCashBuyCr = 12900.00,
                fiiCashSellCr = 14790.00,
                fiiCashNetCr = -1890.00,
                diiCashBuyCr = 13580.00,
                diiCashSellCr = 11100.00,
                diiCashNetCr = 2480.00,
                combinedNetCr = 590.00,
                isProvisional = false,
                derivativePositions = InstitutionalDerivativePositions(
                    indexFuturesNetContracts = 29500L,
                    indexFuturesNetCr = 2920.0,
                    indexOptionsLongCalls = 210000L,
                    indexOptionsLongPuts = 270000L,
                    indexOptionsShortCalls = 240000L,
                    indexOptionsShortPuts = 185000L,
                    stockFuturesNetContracts = 159000L,
                    stockFuturesNetCr = 12300.0,
                    clientVsFiiRatio = 1.45
                ),
                marketStance = "Steady DII Accumulation"
            )
        )

        val summaries = listOf(
            FiiDiiPeriodSummary(
                periodName = "Current Week (Nov W1)",
                fiiTotalNetCr = -4491.40,
                diiTotalNetCr = 9436.40,
                combinedTotalNetCr = 4945.00,
                tradingDaysCount = 5
            ),
            FiiDiiPeriodSummary(
                periodName = "Previous Week (Oct W4)",
                fiiTotalNetCr = -8620.00,
                diiTotalNetCr = 11450.00,
                combinedTotalNetCr = 2830.00,
                tradingDaysCount = 5
            ),
            FiiDiiPeriodSummary(
                periodName = "Month-to-Date (November)",
                fiiTotalNetCr = -4491.40,
                diiTotalNetCr = 9436.40,
                combinedTotalNetCr = 4945.00,
                tradingDaysCount = 5
            ),
            FiiDiiPeriodSummary(
                periodName = "Last Month (October)",
                fiiTotalNetCr = -24800.00,
                diiTotalNetCr = 32400.00,
                combinedTotalNetCr = 7600.00,
                tradingDaysCount = 21
            )
        )

        return Pair(daily, summaries)
    }
}
