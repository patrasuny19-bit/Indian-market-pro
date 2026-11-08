package com.example.data.model

enum class HeatmapSizingMode(val label: String, val description: String) {
    BY_MARKET_CAP("Market Cap", "Tile size proportional to company valuation"),
    BY_VOLUME("Volume", "Tile size proportional to traded shares"),
    EQUAL_WEIGHT("Equal Size", "Consistent grid layout for rapid scanning")
}

enum class SectorSortFilter(val label: String) {
    ALL_STOCKS("All Stocks"),
    TOP_GAINERS("Gainers"),
    TOP_LOSERS("Decliners"),
    HIGH_VOLUME("High Volume"),
    HIGH_MOMENTUM("High Momentum"),
    PRICE_ABOVE_1PCT("> +1.0%"),
    PRICE_BELOW_1PCT("< -1.0%")
}

enum class SectorRankingType(val label: String) {
    TOP_GAINERS("Top Gainers"),
    TOP_LOSERS("Top Losers"),
    HIGHEST_VOLUME("Most Active"),
    MOMENTUM_LEADERS("Momentum Leaders")
}

data class SectorPerformance(
    val sectorName: String,
    val stocks: List<StockQuote>,
    val advancingCount: Int,
    val decliningCount: Int,
    val unchangedCount: Int,
    val avgChangePercent: Double,
    val totalVolume: Long,
    val totalTurnoverCr: Double,
    val totalMarketCapCr: Double,
    val topGainer: StockQuote?,
    val topLoser: StockQuote?,
    val momentumScore: Double
) {
    val isPositive: Boolean get() = avgChangePercent >= 0
    val totalCount: Int get() = stocks.size
    val advanceRatio: Float get() = if (totalCount > 0) advancingCount.toFloat() / totalCount else 0.5f
}

data class SectorRankingLeaderboard(
    val topGainers: List<SectorPerformance> = emptyList(),
    val topLosers: List<SectorPerformance> = emptyList(),
    val highestVolume: List<SectorPerformance> = emptyList(),
    val strongestMomentum: List<SectorPerformance> = emptyList()
)

object SectorClassification {

    val PRIMARY_SECTORS = listOf(
        "Banking",
        "Financial Services",
        "IT",
        "Automobile",
        "Pharma & Healthcare",
        "FMCG",
        "Metals & Mining",
        "Energy & Utilities",
        "Realty",
        "Telecom",
        "Infrastructure & Capital Goods",
        "Cement",
        "Chemicals",
        "Consumer & Retail",
        "Conglomerate"
    )

    fun standardizeSector(rawSector: String): String {
        val s = rawSector.trim().lowercase()
        return when {
            s.contains("bank") -> "Banking"
            s.contains("financ") || s.contains("nbfc") || s.contains("insurance") || s.contains("housing") || s.contains("amc") -> "Financial Services"
            s.contains("information") || s.contains(" it") || s == "it" || (s.contains("tech") && !s.contains("fintech") && !s.contains("petrochem")) -> "IT"
            s.contains("auto") || s.contains("motor") || s.contains("tyre") || s.contains("battery") -> "Automobile"
            s.contains("pharma") || s.contains("health") || s.contains("laborator") || s.contains("hospital") || s.contains("diagnostic") -> "Pharma & Healthcare"
            s.contains("fmcg") || s.contains("beverage") || s.contains("tobacco") || s.contains("spirit") || s.contains("brew") -> "FMCG"
            s.contains("metal") || s.contains("mining") || s.contains("steel") || s.contains("aluminum") || s.contains("copper") -> "Metals & Mining"
            s.contains("energy") || s.contains("oil") || s.contains("gas") || s.contains("power") || s.contains("petrochem") || s.contains("utilities") -> "Energy & Utilities"
            s.contains("realt") || s.contains("real estate") || s.contains("property") -> "Realty"
            s.contains("telecom") || s.contains("communication") || s.contains("media") || s.contains("entertainment") -> "Telecom"
            s.contains("cement") -> "Cement"
            s.contains("chemical") || s.contains("fertiliz") || s.contains("agri") -> "Chemicals"
            s.contains("infra") || s.contains("capital goods") || s.contains("defense") || s.contains("railway") || s.contains("engineering") || s.contains("port") || s.contains("aviation") -> "Infrastructure & Capital Goods"
            s.contains("conglomerate") -> "Conglomerate"
            s.contains("consumer") || s.contains("retail") || s.contains("internet") || s.contains("fintech") || s.contains("jewel") || s.contains("paint") || s.contains("textile") || s.contains("appliances") || s.contains("electronics") -> "Consumer & Retail"
            else -> "Other F&O Sectors"
        }
    }

    fun calculateSectorPerformance(sectorName: String, stocks: List<StockQuote>): SectorPerformance {
        val count = stocks.size
        if (count == 0) {
            return SectorPerformance(
                sectorName = sectorName,
                stocks = emptyList(),
                advancingCount = 0,
                decliningCount = 0,
                unchangedCount = 0,
                avgChangePercent = 0.0,
                totalVolume = 0L,
                totalTurnoverCr = 0.0,
                totalMarketCapCr = 0.0,
                topGainer = null,
                topLoser = null,
                momentumScore = 0.0
            )
        }

        val advances = stocks.count { it.changePercent > 0.05 }
        val declines = stocks.count { it.changePercent < -0.05 }
        val unchanged = count - advances - declines
        val avgChange = Math.round((stocks.map { it.changePercent }.average()) * 100.0) / 100.0
        val totalVol = stocks.sumOf { it.volume }
        val totalTurnover = Math.round(stocks.sumOf { it.turnoverCr } * 10.0) / 10.0
        val totalCap = Math.round(stocks.sumOf { it.marketCapCr } * 10.0) / 10.0

        val sorted = stocks.sortedByDescending { it.changePercent }
        val gainer = sorted.firstOrNull()
        val loser = sorted.lastOrNull()

        // Momentum Score: weighted avg return (60%) + breadth bias (30%) + volume weighting (10%)
        val breadthBias = if (count > 0) ((advances - declines).toDouble() / count) * 2.0 else 0.0
        val momentum = Math.round((avgChange * 0.6 + breadthBias * 0.4) * 100.0) / 100.0

        return SectorPerformance(
            sectorName = sectorName,
            stocks = stocks,
            advancingCount = advances,
            decliningCount = declines,
            unchangedCount = unchanged,
            avgChangePercent = avgChange,
            totalVolume = totalVol,
            totalTurnoverCr = totalTurnover,
            totalMarketCapCr = totalCap,
            topGainer = gainer,
            topLoser = loser,
            momentumScore = momentum
        )
    }

    fun groupAllSectors(allStocks: List<StockQuote>): List<SectorPerformance> {
        val grouped = allStocks.groupBy { standardizeSector(it.sector) }
        val list = mutableListOf<SectorPerformance>()

        // Add primary sectors in order
        PRIMARY_SECTORS.forEach { sec ->
            val stocks = grouped[sec] ?: emptyList()
            if (stocks.isNotEmpty()) {
                list.add(calculateSectorPerformance(sec, stocks))
            }
        }

        // Add any remaining groups
        grouped.forEach { (sec, stocks) ->
            if (sec !in PRIMARY_SECTORS && stocks.isNotEmpty()) {
                list.add(calculateSectorPerformance(sec, stocks))
            }
        }

        return list
    }

    fun computeLeaderboard(sectors: List<SectorPerformance>): SectorRankingLeaderboard {
        val nonZero = sectors.filter { it.stocks.isNotEmpty() }
        return SectorRankingLeaderboard(
            topGainers = nonZero.sortedByDescending { it.avgChangePercent }.take(5),
            topLosers = nonZero.sortedBy { it.avgChangePercent }.take(5),
            highestVolume = nonZero.sortedByDescending { it.totalTurnoverCr }.take(5),
            strongestMomentum = nonZero.sortedByDescending { it.momentumScore }.take(5)
        )
    }
}
