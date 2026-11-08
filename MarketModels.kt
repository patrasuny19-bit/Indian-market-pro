package com.example.data.model

data class MarketIndex(
    val symbol: String,
    val name: String,
    val exchange: String, // "NSE" or "BSE"
    val value: Double,
    val change: Double,
    val changePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val prevClose: Double,
    val yearHigh: Double,
    val yearLow: Double,
    val sparkline: List<Float> = emptyList(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val lastUpdatedFormatted: String = "",
    val isRealTimeFresh: Boolean = false
) {
    val isPositive: Boolean get() = change >= 0
}

data class StockQuote(
    val symbol: String,
    val name: String,
    val ltp: Double,
    val change: Double,
    val changePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val prevClose: Double,
    val volume: Long,
    val turnoverCr: Double,
    val sector: String,
    val marketCapCr: Double,
    val peRatio: Double,
    val isFnO: Boolean = true,
    val lotSize: Int = 1,
    val yearHigh: Double,
    val yearLow: Double,
    val sparkline: List<Float> = emptyList(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val lastUpdatedFormatted: String = "",
    val isRealTimeFresh: Boolean = false,
    val openInterest: Long = 0L,
    val oiChangePercent: Double = 0.0,
    val vwap: Double = 0.0,
    val bidPrice: Double = 0.0,
    val askPrice: Double = 0.0,
    val instrumentToken: Long = 0L,
    val contractStatus: String = "ACTIVE"
) {
    val isPositive: Boolean get() = change >= 0
    val effectiveVwap: Double get() = if (vwap > 0.0) vwap else Math.round(((open + high + low + (2 * ltp)) / 5.0) * 100.0) / 100.0
    val effectiveBid: Double get() = if (bidPrice > 0.0) bidPrice else Math.round((ltp - 0.15) * 100.0) / 100.0
    val effectiveAsk: Double get() = if (askPrice > 0.0) askPrice else Math.round((ltp + 0.15) * 100.0) / 100.0
}

data class CandleData(
    val timestamp: Long,
    val timeLabel: String,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long
)

data class OptionStrike(
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
    val isAtm: Boolean = false
)

data class OptionChainData(
    val underlying: String,
    val spotPrice: Double,
    val selectedExpiry: String,
    val expiries: List<String>,
    val strikes: List<OptionStrike>,
    val totalCallOi: Long,
    val totalPutOi: Long,
    val pcr: Double,
    val maxPainStrike: Double
) {
    val atmStrike: Double get() = strikes.find { it.isAtm }?.strikePrice ?: maxPainStrike
}

enum class BuildupType(val label: String, val description: String) {
    LONG_BUILDUP("Long Buildup", "Price ▲ OI ▲ (Bullish)"),
    SHORT_BUILDUP("Short Buildup", "Price ▼ OI ▲ (Bearish)"),
    SHORT_COVERING("Short Covering", "Price ▲ OI ▼ (Bullish reversal)"),
    LONG_UNWINDING("Long Unwinding", "Price ▼ OI ▼ (Bearish profit booking)")
}

data class FnoBuildupItem(
    val symbol: String,
    val ltp: Double,
    val priceChangePercent: Double,
    val oi: Long,
    val oiChangePercent: Double,
    val type: BuildupType,
    val sector: String
)

data class MarketBreadth(
    val advances: Int,
    val declines: Int,
    val unchanged: Int,
    val total: Int = advances + declines + unchanged
) {
    val advanceRatio: Float get() = if (total > 0) advances.toFloat() / total else 0.5f
}

data class FiiDiiRecord(
    val date: String,
    val fiiNetCash: Double,   // in ₹ Crores (+ is buy, - is sell)
    val diiNetCash: Double,
    val fiiIndexFuturesNet: Double,
    val fiiStockFuturesNet: Double
)

data class MarketDepthRow(
    val bidOrders: Int,
    val bidQty: Long,
    val bidPrice: Double,
    val askPrice: Double,
    val askQty: Long,
    val askOrders: Int
)

data class MarketDepth(
    val rows: List<MarketDepthRow>,
    val totalBidQty: Long,
    val totalAskQty: Long
) {
    val buyerPercentage: Int
        get() {
            val total = totalBidQty + totalAskQty
            return if (total > 0) ((totalBidQty.toDouble() / total) * 100).toInt() else 50
        }
}

enum class OrderType(val label: String) {
    BUY("BUY"),
    SELL("SELL")
}

enum class ProductType(val label: String, val leverage: String) {
    MIS("Intraday (MIS)", "5x Margin"),
    CNC("Delivery (CNC)", "1x Cash"),
    NRML("F&O Normal", "Span + Exposure")
}

enum class OrderCategory(val label: String) {
    MARKET("Market"),
    LIMIT("Limit")
}

enum class OrderStatus(val label: String) {
    EXECUTED("Executed"),
    PENDING("Pending"),
    CANCELLED("Cancelled")
}

data class PaperOrder(
    val id: String,
    val symbol: String,
    val type: OrderType,
    val product: ProductType,
    val category: OrderCategory,
    val quantity: Int,
    val price: Double,
    val status: OrderStatus,
    val timestamp: Long,
    val timeFormatted: String
)

data class PaperPosition(
    val id: String,
    val symbol: String,
    val type: OrderType,
    val product: ProductType,
    val quantity: Int,
    val buyPrice: Double,
    val currentPrice: Double
) {
    val pnl: Double get() = if (type == OrderType.BUY) {
        (currentPrice - buyPrice) * quantity
    } else {
        (buyPrice - currentPrice) * quantity
    }

    val pnlPercent: Double get() = if (buyPrice > 0) {
        (pnl / (buyPrice * quantity)) * 100.0
    } else 0.0

    val investedAmount: Double get() = buyPrice * quantity
    val currentValue: Double get() = currentPrice * quantity
}

data class PortfolioSummary(
    val startingCapital: Double = 1000000.0, // ₹10,00,000 Initial Paper Trading Balance
    val availableMargin: Double = 1000000.0,
    val usedMargin: Double = 0.0,
    val realizedPnl: Double = 0.0,
    val positions: List<PaperPosition> = emptyList(),
    val orders: List<PaperOrder> = emptyList()
) {
    val unrealizedPnl: Double get() = positions.sumOf { it.pnl }
    val totalPnl: Double get() = realizedPnl + unrealizedPnl
    val totalAccountValue: Double get() = availableMargin + usedMargin + unrealizedPnl
}
