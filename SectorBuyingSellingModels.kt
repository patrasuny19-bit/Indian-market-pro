package com.example.data.model

import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import androidx.compose.ui.graphics.Color

enum class FlowDirection(val label: String, val isPositive: Boolean) {
    STRONG_INFLOW("Strong Inflow", true),
    MODERATE_INFLOW("Moderate Inflow", true),
    NEUTRAL("Neutral Flow", true),
    MODERATE_OUTFLOW("Moderate Outflow", false),
    HEAVY_OUTFLOW("Heavy Outflow", false)
}

enum class SmartSectorSignal(val title: String, val badgeColorHex: Long, val description: String) {
    STRONG_BUYING("Strong Buying", 0xFF00E676, "Aggressive buying volume > 65% with positive market breadth"),
    STRONG_SELLING("Strong Selling", 0xFFFF5252, "Aggressive selling volume > 65% with negative market breadth"),
    SECTOR_BREAKOUT("Sector Breakout", 0xFF00E5FF, "Price near 52W high + relative volume spike + strong buying"),
    SECTOR_BREAKDOWN("Sector Breakdown", 0xFFFF1744, "Price breaking recent support with surging sell volume"),
    ACCUMULATION("Accumulation", 0xFF69F0AE, "Positive net money flow with steady delivery percentage"),
    DISTRIBUTION("Distribution", 0xFFFF8A80, "Heavy turnover with declining price structure"),
    REVERSAL_BULLISH("Bullish Reversal", 0xFFFFD700, "Oversold RSI rebound with buyer volume expansion"),
    REVERSAL_BEARISH("Bearish Reversal", 0xFFFF6E40, "Overbought exhaustion with aggressive seller emergence"),
    NEUTRAL("Consolidation", 0xFFB0BEC5, "Balanced order flow inside consolidation range")
}

enum class RotationQuadrant(val label: String, val description: String, val colorHex: Long) {
    LEADING("Leading", "Strong Relative Strength + Gaining Momentum", 0xFF00E676),
    IMPROVING("Improving", "Recovering Strength + Rapidly Accelerating", 0xFF00E5FF),
    WEAKENING("Weakening", "High Strength but Decelerating Momentum", 0xFFFFD700),
    LAGGING("Lagging", "Underperforming Benchmark + Negative Momentum", 0xFFFF5252)
}

enum class StockSortOption(val label: String) {
    HIGHEST_BUYING("Highest Buying"),
    HIGHEST_SELLING("Highest Selling"),
    HIGHEST_VOLUME("Highest Volume"),
    TOP_GAINERS("Top Gainers"),
    TOP_LOSERS("Top Losers"),
    HIGHEST_REL_VOL("Highest Rel Vol"),
    STRONGEST_RSI("Strongest RSI"),
    HIGHEST_OI_CHANGE("Highest OI Change")
}

data class StockFlowItem(
    val stock: StockQuote,
    val buyingVolume: Long,
    val sellingVolume: Long,
    val netFlowVolume: Long,
    val netFlowCr: Double,
    val buyingPressurePercent: Double,
    val sellingPressurePercent: Double,
    val relativeVolume: Double,
    val rsi: Double,
    val vwap: Double,
    val deliveryPercent: Double,
    val openInterest: Long,
    val oiChangePercent: Double,
    val trendDirection: String, // "BULLISH", "BEARISH", "SIDEWAYS"
    val pressureBadge: String
)

data class SectorFlowMetric(
    val sectorName: String,
    val indexName: String,
    val indexValue: Double,
    val indexChangePercent: Double,
    val totalStocks: Int,
    val advancingCount: Int,
    val decliningCount: Int,
    val unchangedCount: Int,
    val totalVolume: Long,
    val buyingVolume: Long,
    val sellingVolume: Long,
    val netFlowVolume: Long,
    val netFlowCr: Double,
    val buyingPressurePercent: Double,
    val sellingPressurePercent: Double,
    val relativeStrength: Double, // vs NIFTY 50
    val momentumScore: Double,
    val moneyFlowDirection: FlowDirection,
    val smartSignal: SmartSectorSignal,
    val rotationQuadrant: RotationQuadrant,
    val stocks: List<StockFlowItem>
) {
    val isPositive: Boolean get() = indexChangePercent >= 0
    val advanceRatio: Float get() = if (totalStocks > 0) advancingCount.toFloat() / totalStocks else 0.5f
}

data class SectorFlowDashboardSummary(
    val allSectors: List<SectorFlowMetric>,
    val highestBuyingSectors: List<SectorFlowMetric>,
    val lowestBuyingSectors: List<SectorFlowMetric>,
    val highestSellingSectors: List<SectorFlowMetric>,
    val lowestSellingSectors: List<SectorFlowMetric>,
    val breakoutSectors: List<SectorFlowMetric>,
    val totalMarketBuyingVol: Long,
    val totalMarketSellingVol: Long,
    val netMarketFlowCr: Double,
    val averageBuyingPressure: Double,
    val lastUpdatedTimestamp: Long,
    val dataStatusText: String = "LIVE STREAMING (NSE)"
)
