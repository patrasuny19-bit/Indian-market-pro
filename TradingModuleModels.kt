package com.example.data.model

// ==========================================
// 1. INTERDAY BOOST MODELS
// ==========================================

enum class InterdayFilter(val label: String, val shortDesc: String) {
    ALL_BOOST("All Boost", "Intraday momentum universe"),
    BULLISH_MOMENTUM("Top Bullish", "High momentum above VWAP"),
    BEARISH_MOMENTUM("Top Bearish", "Heavy selling below VWAP"),
    STRONG_BUYING("Strong Buying", "Aggressive buyer depth & surge"),
    STRONG_SELLING("Strong Selling", "Aggressive seller depth & dump"),
    HIGH_VOLUME("High Volume", "Highest traded volume today"),
    UNUSUAL_VOLUME("Unusual Volume", "Relative Volume (RVol) > 1.8x"),
    VWAP_BREAKOUT("VWAP Breakout", "Crossing VWAP from below"),
    ORB_BREAKOUT("ORB Breakout", "Opening range breakout"),
    PDH_BREAKOUT("PDH Breakout", "Trading above Previous Day High"),
    PDL_BREAKDOWN("PDL Breakdown", "Trading below Previous Day Low")
}

data class IntradayTechnicalProfile(
    val rsi14: Double,
    val macdLine: Double,
    val macdSignal: Double,
    val macdHistogram: Double,
    val adx14: Double,
    val vwap: Double,
    val relativeVolume: Double, // RVol vs 10-day average
    val supportLevel: Double,
    val resistanceLevel: Double,
    val trendDirection: String, // "Strong Bullish", "Bullish", "Neutral", "Bearish", "Strong Bearish"
    val isVwapBreakout: Boolean,
    val isOrbBreakout: Boolean,
    val isPdhBreakout: Boolean,
    val isPdlBreakdown: Boolean,
    val buyerPressurePct: Int // 0 to 100
)

data class InterdayStockData(
    val quote: StockQuote,
    val technicals: IntradayTechnicalProfile,
    val momentumScore: Double, // 0.0 to 100.0
    val volumeStrengthScore: Double, // 0.0 to 100.0
    val trendStrengthScore: Double, // 0.0 to 100.0
    val breakoutConfidence: String // "HIGH", "MODERATE", "FORMING", "NONE"
) {
    val symbol: String get() = quote.symbol
    val ltp: Double get() = quote.ltp
    val changePercent: Double get() = quote.changePercent
    val volume: Long get() = quote.volume
}

// ==========================================
// 2. SWING TRADING MODELS
// ==========================================

enum class SwingFilterType(val label: String) {
    ALL_SETUPS("All Setups"),
    BULLISH_SWING("Bullish Swings"),
    BEARISH_SWING("Bearish Swings"),
    BREAKOUT_CANDIDATES("Breakout Candidates"),
    BREAKDOWN_CANDIDATES("Breakdown Candidates"),
    PULLBACK_SETUPS("EMA Pullbacks"),
    MA_CROSSOVERS("Moving Avg Cross"),
    RSI_DIVERGENCE("RSI Divergences"),
    MACD_CROSSOVERS("MACD Crossovers"),
    VOLUME_EXPANSION("Volume Surges")
}

data class TechnicalIndicatorsProfile(
    val ema9: Double,
    val ema20: Double,
    val ema50: Double,
    val ema200: Double,
    val sma20: Double,
    val sma50: Double,
    val sma200: Double,
    val rsi14: Double,
    val macdLine: Double,
    val macdSignal: Double,
    val macdHistogram: Double,
    val adx: Double,
    val atr: Double,
    val bollingerUpper: Double,
    val bollingerMiddle: Double,
    val bollingerLower: Double,
    val volumeRatio: Double
)

data class SwingSetup(
    val stock: StockQuote,
    val indicators: TechnicalIndicatorsProfile,
    val setupName: String,
    val direction: String, // "BULLISH" or "BEARISH"
    val entryZoneMin: Double,
    val entryZoneMax: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val riskRewardRatio: String, // e.g. "1 : 2.5"
    val setupStrength: String, // "A+ Elite", "A Strong", "B+ Moderate"
    val primaryReason: String,
    val secondaryReasons: List<String>,
    val timeframe: String = "1D" // "4H", "1D", "1W"
)

// ==========================================
// 3. OI CLOCK & MONITOR MODELS
// ==========================================

data class OiStrikeRow(
    val strikePrice: Double,
    val callLtp: Double,
    val callChange: Double,
    val callOi: Long,
    val callOiChange: Long,
    val callIv: Double,
    val callDelta: Double,
    val putLtp: Double,
    val putChange: Double,
    val putOi: Long,
    val putOiChange: Long,
    val putIv: Double,
    val putDelta: Double,
    val isAtm: Boolean,
    val netOiDelta: Long // putOi - callOi
)

data class OiAlertItem(
    val id: String,
    val timestamp: String,
    val symbol: String,
    val alertType: String, // "LARGE_CALL_WRITING", "LARGE_PUT_WRITING", "UNUSUAL_OI_SURGE", "PCR_SPIKE", "SR_BREAK"
    val title: String,
    val message: String,
    val severity: String // "BULLISH", "BEARISH", "NEUTRAL", "HIGH_ALERT"
)

data class OiClockInterval(
    val timeLabel: String, // "09:30", "10:00", etc.
    val callOiAdditions: Long,
    val putOiAdditions: Long,
    val pcr: Double,
    val spotPrice: Double,
    val sentiment: String // "Bullish", "Bearish", "Rangebound"
)

data class OiClockData(
    val underlying: String,
    val spotPrice: Double,
    val totalCallOi: Long,
    val totalPutOi: Long,
    val callOiChange: Long,
    val putOiChange: Long,
    val pcr: Double,
    val maxPain: Double,
    val dominantCallStrike: Double, // Major resistance
    val dominantPutStrike: Double,  // Major support
    val buildupType: BuildupType,
    val interpretationText: String,
    val strikes: List<OiStrikeRow>,
    val timeline: List<OiClockInterval>,
    val alerts: List<OiAlertItem>,
    val lastUpdated: String
)

// ==========================================
// 4. FII / DII INSTITUTIONAL MODELS
// ==========================================

data class InstitutionalDerivativePositions(
    val indexFuturesNetContracts: Long,
    val indexFuturesNetCr: Double,
    val indexOptionsLongCalls: Long,
    val indexOptionsLongPuts: Long,
    val indexOptionsShortCalls: Long,
    val indexOptionsShortPuts: Long,
    val stockFuturesNetContracts: Long,
    val stockFuturesNetCr: Double,
    val clientVsFiiRatio: Double
)

data class FiiDiiActivityDetail(
    val date: String,
    val formattedDate: String,
    val fiiCashBuyCr: Double,
    val fiiCashSellCr: Double,
    val fiiCashNetCr: Double,
    val diiCashBuyCr: Double,
    val diiCashSellCr: Double,
    val diiCashNetCr: Double,
    val combinedNetCr: Double,
    val isProvisional: Boolean,
    val derivativePositions: InstitutionalDerivativePositions,
    val marketStance: String // "Aggressive Buyers", "Moderate Buyers", "Neutral", "Net Sellers"
) {
    val isFiiNetBuyer: Boolean get() = fiiCashNetCr >= 0
    val isDiiNetBuyer: Boolean get() = diiCashNetCr >= 0
    val isCombinedNetBuyer: Boolean get() = combinedNetCr >= 0
}

data class FiiDiiPeriodSummary(
    val periodName: String, // "This Week", "Last Week", "This Month", "Last Month"
    val fiiTotalNetCr: Double,
    val diiTotalNetCr: Double,
    val combinedTotalNetCr: Double,
    val tradingDaysCount: Int
)

data class FiiDiiDataSourceInfo(
    val primarySource: String = "National Stock Exchange of India (NSE) & NSDL FPI Monitor",
    val publicationTime: String = "Published daily after 18:30 IST (Provisional) & 21:00 IST (Final)",
    val cashMarketScope: String = "Equity Cash Segment Gross Purchases & Gross Sales",
    val derivativeScope: String = "NSE Participant-wise Open Interest & Trading Volumes",
    val disclaimer: String = "Official exchange published statistics. Not individual broker client activity."
)
