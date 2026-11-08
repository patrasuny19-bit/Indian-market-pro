package com.example.data.model

data class FnoInstrument(
    val symbol: String,                  // NSE Trading Symbol (e.g., "RELIANCE")
    val companyName: String,             // Full Legal / Company Name
    val instrumentToken: Long,           // NSE Instrument Identifier Token
    val sector: String,                  // Industry Sector
    val lotSize: Int,                    // Standard NSE Market Lot Size
    val availableExpiries: List<String>, // Expiry cycle dates
    val futuresAvailable: Boolean = true,
    val optionsAvailable: Boolean = true,
    val contractStatus: String = "ACTIVE", // ACTIVE, BAN_PERIOD, SUSPENDED
    val isin: String = "",
    val marketCapCategory: String = "LARGE_CAP", // LARGE_CAP, MID_CAP
    val baseLtp: Double = 1000.0,
    val prevClose: Double = 1000.0,
    val week52High: Double = 1200.0,
    val week52Low: Double = 800.0,
    val baseVolume: Long = 1000000L,
    val marketCapCr: Double = 50000.0,
    val peRatio: Double = 25.0
)

enum class FnoScannerFilter(val label: String, val description: String) {
    ALL_FNO("All F&O", "Complete NSE Equity Derivatives Universe"),
    TOP_GAINERS("Top Gainers", "Strongest percentage price gainers today"),
    TOP_LOSERS("Top Losers", "Heaviest price decliners today"),
    HIGH_VOLUME("High Volume", "Highest traded volume & liquidity"),
    HIGH_OI_CHANGE("Highest OI", "Substantial open interest buildup"),
    BULLISH_MOMENTUM("Bullish Momentum", "Price > VWAP with positive trend"),
    BEARISH_MOMENTUM("Bearish Momentum", "Price < VWAP with selling pressure"),
    BREAKOUT_STOCKS("Breakouts", "Near Day High or within 3% of 52-Week High"),
    BREAKDOWN_STOCKS("Breakdowns", "Near Day Low or within 3% of 52-Week Low")
}

data class FnoUniverseSyncStats(
    val totalEligibleStocks: Int = 182,
    val activeContractsCount: Int = 182,
    val banPeriodCount: Int = 0,
    val lastSyncTime: String = "Auto-Synced with NSE Instrument Master",
    val isMasterFresh: Boolean = true,
    val dataSourceUrl: String = "https://www.nseindia.com/static/market-data/securities-information-contracts-available-for-trading"
)
