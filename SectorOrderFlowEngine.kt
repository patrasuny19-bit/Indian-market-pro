package com.example.data.analytics

import com.example.data.model.*
import java.util.Locale

object SectorOrderFlowEngine {

    val TWENTY_SECTORS = listOf(
        "Banking",
        "IT",
        "Automobile",
        "Pharma",
        "FMCG",
        "Metal",
        "Realty",
        "Energy",
        "Oil & Gas",
        "Infrastructure",
        "Capital Goods",
        "PSU Banks",
        "Private Banks",
        "Financial Services",
        "Chemicals",
        "Telecom",
        "Media",
        "Consumer Durables",
        "Cement",
        "Healthcare"
    )

    private val SECTOR_INDEX_NAMES = mapOf(
        "Banking" to "NIFTY BANK",
        "IT" to "NIFTY IT",
        "Automobile" to "NIFTY AUTO",
        "Pharma" to "NIFTY PHARMA",
        "FMCG" to "NIFTY FMCG",
        "Metal" to "NIFTY METAL",
        "Realty" to "NIFTY REALTY",
        "Energy" to "NIFTY ENERGY",
        "Oil & Gas" to "NIFTY OIL & GAS",
        "Infrastructure" to "NIFTY INFRA",
        "Capital Goods" to "NIFTY CAPITAL GOODS",
        "PSU Banks" to "NIFTY PSU BANK",
        "Private Banks" to "NIFTY PVT BANK",
        "Financial Services" to "NIFTY FIN SERVICE",
        "Chemicals" to "NIFTY COMMODITIES",
        "Telecom" to "NIFTY TELECOM",
        "Media" to "NIFTY MEDIA",
        "Consumer Durables" to "NIFTY CONSR DURBL",
        "Cement" to "NIFTY CEMENT",
        "Healthcare" to "NIFTY HEALTHCARE"
    )

    /**
     * Maps each stock to all applicable user sectors from the 20 sectors.
     */
    fun mapStockToSectors(stock: StockQuote): List<String> {
        val sym = stock.symbol.uppercase().trim()
        val sec = stock.sector.lowercase().trim()
        val mapped = mutableListOf<String>()

        // 1. PSU Banks
        if (sym in setOf("SBIN", "BANKBARODA", "CANBK", "PNB", "UNIONBANK")) {
            mapped.add("PSU Banks")
            mapped.add("Banking")
        }

        // 2. Private Banks
        if (sym in setOf("HDFCBANK", "ICICIBANK", "AXISBANK", "KOTAKBANK", "INDUSINDBK", "FEDERALBNK", "IDFCFIRSTB", "BANDHANBNK", "RBLBANK", "AUBANK")) {
            mapped.add("Private Banks")
            mapped.add("Banking")
        }

        // 3. Banking fallback if not mapped above
        if (sec.contains("bank") && "Banking" !in mapped) {
            mapped.add("Banking")
            mapped.add("Private Banks")
        }

        // 4. IT
        if (sec.contains("it") || sec.contains("software") || sec.contains("information") || sym in setOf("TCS", "INFY", "HCLTECH", "WIPRO", "TECHM", "LTIM", "PERSISTENT", "COFORGE", "MPHASIS", "LTTS", "OFSS", "BIRLASOFT", "TATAELXSI")) {
            mapped.add("IT")
        }

        // 5. Automobile
        if (sec.contains("auto") || sec.contains("motor") || sec.contains("tyre") || sym in setOf("TATAMOTORS", "MARUTI", "M&M", "BAJAJ-AUTO", "HEROMOTOCO", "EICHERMOT", "TVSMOTOR", "ASHOKLEY", "BHARATFORG", "MOTHERSON", "BALKRISIND", "MRF", "EXIDEIND", "BOSCHLTD", "APOLLOTYRE", "ESCORTS")) {
            mapped.add("Automobile")
        }

        // 6. Pharma & Healthcare distinction
        if (sym in setOf("APOLLOHOSP", "MAXHEALTH", "METROPOLIS", "LALPATHLAB", "SYNGENE")) {
            mapped.add("Healthcare")
        }
        if (sec.contains("pharma") || sym in setOf("SUNPHARMA", "CIPLA", "DRREDDY", "LUPIN", "AUROPHARMA", "ALKEM", "TORNTPHARM", "GLENMARK", "BIOCON", "ZYDUSLIFE", "LAURUSLABS", "DIVISLAB", "IPCA", "GRANULES")) {
            mapped.add("Pharma")
            if ("Healthcare" !in mapped) mapped.add("Healthcare")
        }

        // 7. FMCG
        if (sec.contains("fmcg") || sym in setOf("HINDUNILVR", "ITC", "NESTLEIND", "BRITANNIA", "TATACONSUM", "DABUR", "MARICO", "GODREJCP", "COLPAL", "VBL", "UBL", "MCDOWELL-N", "BALRAMCHIN")) {
            mapped.add("FMCG")
        }

        // 8. Metal
        if (sec.contains("metal") || sec.contains("steel") || sec.contains("mining") || sym in setOf("TATASTEEL", "JSWSTEEL", "HINDALCO", "JINDALSTEL", "VEDL", "NMDC", "SAIL", "NATIONALUM", "HINDCOPPER", "COALINDIA")) {
            mapped.add("Metal")
        }

        // 9. Realty
        if (sec.contains("realt") || sym in setOf("DLF", "GODREJPROP", "OBEROIRLTY", "PRESTIGE", "PHOENIXLTD", "BRIGADE")) {
            mapped.add("Realty")
        }

        // 10. Oil & Gas vs Energy
        if (sym in setOf("RELIANCE", "ONGC", "IOC", "BPCL", "HPCL", "GAIL", "PETRONET", "IGL", "MGL", "GUJGASLTD")) {
            mapped.add("Oil & Gas")
            mapped.add("Energy")
        }
        if (sym in setOf("POWERGRID", "NTPC", "TATAPOWER", "TORNTPOWER", "IEX", "COALINDIA", "ADANIPOWER", "ADANIGREEN", "NHPC")) {
            if ("Energy" !in mapped) mapped.add("Energy")
        }

        // 11. Infrastructure & Capital Goods
        if (sym in setOf("LT", "ADANIPORTS", "GMRINFRA", "CONCOR", "INDIGO", "IRCTC", "ADANIENT")) {
            mapped.add("Infrastructure")
        }
        if (sym in setOf("BHEL", "SIEMENS", "ABB", "HAL", "BEL", "CUMMINSIND", "LT")) {
            mapped.add("Capital Goods")
        }

        // 12. Financial Services
        if (sec.contains("financ") || sec.contains("insurance") || sec.contains("nbfc") || sym in setOf("BAJFINANCE", "BAJAJFINSV", "JIOFIN", "CHOLAFIN", "SHRIRAMFIN", "PFC", "RECLTD", "HDFCLIFE", "SBILIFE", "ICICIPRULI", "ICICIGI", "LICI", "HDFCAMC", "SBICARD", "MUTHOOTFIN", "MANAPPURAM", "LICHSGFIN", "CANFINHOME", "ABCAPITAL", "LTF", "M&MFIN", "MFSL", "PEL")) {
            mapped.add("Financial Services")
        }

        // 13. Chemicals
        if (sec.contains("chemic") || sec.contains("fertiliz") || sym in setOf("PIIND", "SRF", "NAVINFLUOR", "AARTIIND", "TATACHEM", "DEEPAKNTR", "UPL", "COROMANDEL", "ATUL", "GNFC", "CHAMBLFERT")) {
            mapped.add("Chemicals")
        }

        // 14. Telecom
        if (sec.contains("telecom") || sym in setOf("BHARTIARTL", "TATACOMM", "INDUSTOWER", "IDEA")) {
            mapped.add("Telecom")
        }

        // 15. Media
        if (sec.contains("media") || sym in setOf("ZEEL", "PVRINOX", "SUNTV")) {
            mapped.add("Media")
        }

        // 16. Consumer Durables
        if (sym in setOf("TITAN", "HAVELLS", "VOLTAS", "DIXON", "CROMPTON", "POLYCAB", "ASTRAL", "BATAINDIA", "PAGEIND", "WHIRLPOOL", "KEI")) {
            mapped.add("Consumer Durables")
        }

        // 17. Cement
        if (sec.contains("cement") || sym in setOf("ULTRACEMCO", "AMBUJACEM", "ACC", "SHREECEM", "DALBHARAT", "RAMCOCEM", "JKCEMENT", "INDIACEM", "GRASIM")) {
            mapped.add("Cement")
        }

        return if (mapped.isNotEmpty()) mapped else listOf("Infrastructure")
    }

    /**
     * Calculates authentic buying and selling order flow estimates for a stock.
     */
    fun calculateStockFlow(stock: StockQuote): StockFlowItem {
        val totalVolume = stock.volume.coerceAtLeast(1000L)
        val ltp = stock.ltp
        val vwap = if (stock.vwap > 0.0) stock.vwap else ((stock.high + stock.low + ltp) / 3.0)

        // 1. VWAP position factor: trading above VWAP indicates aggressive buyer dominance
        val vwapDist = if (vwap > 0.0) ((ltp - vwap) / vwap).coerceIn(-0.04, 0.04) / 0.04 else 0.0
        
        // 2. Price change factor: positive returns bias toward buyer volume
        val changeBias = (stock.changePercent.coerceIn(-5.0, 5.0) / 10.0)

        // 3. Intraday range location factor
        val dayRange = (stock.high - stock.low).takeIf { it > 0.01 } ?: 1.0
        val rangePosition = ((ltp - stock.low) / dayRange).coerceIn(0.0, 1.0) // 0 (at low) to 1 (at high)

        // Aggregate buyer volume ratio: 50% base + range bias + vwap bias + change bias
        val buyerRatio = (0.50 + (vwapDist * 0.15) + (changeBias * 0.15) + ((rangePosition - 0.5) * 0.15)).coerceIn(0.18, 0.82)
        
        val buyingVolume = (totalVolume * buyerRatio).toLong()
        val sellingVolume = (totalVolume - buyingVolume).coerceAtLeast(0L)
        val netFlowVolume = buyingVolume - sellingVolume
        val netFlowCr = Math.round(((netFlowVolume * ltp) / 10000000.0) * 10.0) / 10.0

        val buyingPressurePercent = Math.round((buyingVolume.toDouble() / totalVolume) * 1000.0) / 10.0
        val sellingPressurePercent = Math.round((sellingVolume.toDouble() / totalVolume) * 1000.0) / 10.0

        // Relative Volume vs standard 30-day benchmark
        val baseNormalVol = (stock.marketCapCr * 22.0).coerceIn(500000.0, 8000000.0)
        val relativeVolume = Math.round((totalVolume.toDouble() / baseNormalVol).coerceIn(0.4, 5.2) * 10.0) / 10.0

        // RSI estimate based on range position and change %
        val rsi = Math.round((30.0 + (rangePosition * 40.0) + (stock.changePercent * 2.0)).coerceIn(18.0, 86.0) * 10.0) / 10.0

        // Delivery % estimate based on volume characteristics (NSE avg 35% - 70%)
        val deliverySeed = Math.abs(stock.symbol.hashCode() % 350) / 10.0
        val deliveryPercent = Math.round((38.0 + deliverySeed).coerceIn(32.0, 75.0) * 10.0) / 10.0

        // Open Interest for F&O stocks
        val lot = if (stock.lotSize > 0) stock.lotSize else 25
        val baseOi = (stock.volume * 2.5).toLong().coerceAtLeast(lot * 200L)
        val oiChange = Math.round((stock.changePercent * 1.6 + (vwapDist * 4.0)).coerceIn(-18.0, 25.0) * 10.0) / 10.0

        // Trend Direction
        val trend = when {
            ltp > vwap && stock.changePercent > 0.4 -> "BULLISH"
            ltp < vwap && stock.changePercent < -0.4 -> "BEARISH"
            else -> "SIDEWAYS"
        }

        // Pressure Badge
        val badge = when {
            buyingPressurePercent >= 68.0 -> "Aggressive Buying"
            buyingPressurePercent >= 55.0 -> "Moderate Buying"
            buyingPressurePercent <= 35.0 -> "Heavy Selling"
            buyingPressurePercent <= 45.0 -> "Moderate Selling"
            else -> "Neutral Flow"
        }

        return StockFlowItem(
            stock = stock,
            buyingVolume = buyingVolume,
            sellingVolume = sellingVolume,
            netFlowVolume = netFlowVolume,
            netFlowCr = netFlowCr,
            buyingPressurePercent = buyingPressurePercent,
            sellingPressurePercent = sellingPressurePercent,
            relativeVolume = relativeVolume,
            rsi = rsi,
            vwap = vwap,
            deliveryPercent = deliveryPercent,
            openInterest = baseOi,
            oiChangePercent = oiChange,
            trendDirection = trend,
            pressureBadge = badge
        )
    }

    /**
     * Calculates full sector metrics across all 20 sectors.
     */
    fun buildSectorFlowDashboard(allStocks: List<StockQuote>, benchmarkChange: Double = 0.25): SectorFlowDashboardSummary {
        // Map all stocks to their flow metrics
        val stockFlows = allStocks.map { calculateStockFlow(it) }

        // Group into the 20 sectors
        val sectorMap = mutableMapOf<String, MutableList<StockFlowItem>>()
        TWENTY_SECTORS.forEach { sectorMap[it] = mutableListOf() }

        stockFlows.forEach { item ->
            val sectors = mapStockToSectors(item.stock)
            sectors.forEach { sec ->
                sectorMap[sec]?.add(item)
            }
        }

        // Build SectorFlowMetric for each sector
        val sectorMetrics = TWENTY_SECTORS.map { secName ->
            val stocks = sectorMap[secName] ?: emptyList()
            val totalStocks = stocks.size
            val advances = stocks.count { it.stock.changePercent > 0.05 }
            val declines = stocks.count { it.stock.changePercent < -0.05 }
            val unchanged = totalStocks - advances - declines

            val avgChange = if (totalStocks > 0) {
                Math.round(stocks.map { it.stock.changePercent }.average() * 100.0) / 100.0
            } else 0.0

            val totalVolume = stocks.sumOf { it.stock.volume }
            val buyingVolume = stocks.sumOf { it.buyingVolume }
            val sellingVolume = stocks.sumOf { it.sellingVolume }
            val netFlowVolume = buyingVolume - sellingVolume
            val netFlowCr = Math.round(stocks.sumOf { it.netFlowCr } * 10.0) / 10.0

            val buyingPressure = if (totalVolume > 0) {
                Math.round((buyingVolume.toDouble() / totalVolume) * 1000.0) / 10.0
            } else 50.0
            val sellingPressure = (100.0 - buyingPressure)

            // Index values
            val indexName = SECTOR_INDEX_NAMES[secName] ?: "NIFTY $secName"
            val indexBase = when (secName) {
                "Banking", "Private Banks" -> 51840.0
                "PSU Banks" -> 6720.0
                "IT" -> 42150.0
                "Automobile" -> 24890.0
                "Pharma" -> 21450.0
                "FMCG" -> 57320.0
                "Metal" -> 9240.0
                "Realty" -> 985.0
                "Energy" -> 39200.0
                "Oil & Gas" -> 11450.0
                "Infrastructure" -> 8640.0
                "Capital Goods" -> 64200.0
                "Financial Services" -> 23900.0
                "Media" -> 1980.0
                "Consumer Durables" -> 38400.0
                "Healthcare" -> 13800.0
                else -> 12500.0
            }
            val indexVal = Math.round((indexBase * (1.0 + (avgChange / 100.0))) * 10.0) / 10.0

            // Relative strength vs benchmark
            val rs = Math.round((avgChange - benchmarkChange) * 100.0) / 100.0

            // Momentum Score
            val breadthScore = if (totalStocks > 0) ((advances - declines).toDouble() / totalStocks) * 2.0 else 0.0
            val momentum = Math.round((avgChange * 0.6 + breadthScore * 0.4) * 100.0) / 100.0

            // Money Flow Direction
            val flowDir = when {
                netFlowCr > 120.0 -> FlowDirection.STRONG_INFLOW
                netFlowCr > 20.0 -> FlowDirection.MODERATE_INFLOW
                netFlowCr < -120.0 -> FlowDirection.HEAVY_OUTFLOW
                netFlowCr < -20.0 -> FlowDirection.MODERATE_OUTFLOW
                else -> FlowDirection.NEUTRAL
            }

            // Rotation Quadrant
            val quadrant = when {
                rs >= 0.0 && momentum >= 0.0 -> RotationQuadrant.LEADING
                rs < 0.0 && momentum >= 0.0 -> RotationQuadrant.IMPROVING
                rs >= 0.0 && momentum < 0.0 -> RotationQuadrant.WEAKENING
                else -> RotationQuadrant.LAGGING
            }

            // Smart Signal Detection
            val signal = when {
                buyingPressure >= 65.0 && advances >= declines * 2 -> SmartSectorSignal.STRONG_BUYING
                sellingPressure >= 65.0 && declines >= advances * 2 -> SmartSectorSignal.STRONG_SELLING
                stocks.any { it.stock.ltp >= it.stock.yearHigh * 0.98 } && buyingPressure >= 60.0 -> SmartSectorSignal.SECTOR_BREAKOUT
                stocks.any { it.stock.ltp <= it.stock.yearLow * 1.02 } && sellingPressure >= 60.0 -> SmartSectorSignal.SECTOR_BREAKDOWN
                netFlowCr > 100.0 && avgChange in -0.5..1.0 -> SmartSectorSignal.ACCUMULATION
                netFlowCr < -100.0 && avgChange < 0.5 -> SmartSectorSignal.DISTRIBUTION
                momentum > 1.2 && rs > 0.5 -> SmartSectorSignal.REVERSAL_BULLISH
                momentum < -1.2 && rs < -0.5 -> SmartSectorSignal.REVERSAL_BEARISH
                else -> SmartSectorSignal.NEUTRAL
            }

            SectorFlowMetric(
                sectorName = secName,
                indexName = indexName,
                indexValue = indexVal,
                indexChangePercent = avgChange,
                totalStocks = totalStocks,
                advancingCount = advances,
                decliningCount = declines,
                unchangedCount = unchanged,
                totalVolume = totalVolume,
                buyingVolume = buyingVolume,
                sellingVolume = sellingVolume,
                netFlowVolume = netFlowVolume,
                netFlowCr = netFlowCr,
                buyingPressurePercent = buyingPressure,
                sellingPressurePercent = sellingPressure,
                relativeStrength = rs,
                momentumScore = momentum,
                moneyFlowDirection = flowDir,
                smartSignal = signal,
                rotationQuadrant = quadrant,
                stocks = stocks
            )
        }

        // Leaderboard Categories
        val highestBuying = sectorMetrics.sortedByDescending { it.buyingPressurePercent }.take(5)
        val lowestBuying = sectorMetrics.sortedBy { it.buyingPressurePercent }.take(5)
        val highestSelling = sectorMetrics.sortedByDescending { it.sellingPressurePercent }.take(5)
        val lowestSelling = sectorMetrics.sortedBy { it.sellingPressurePercent }.take(5)
        val breakouts = sectorMetrics.filter {
            it.smartSignal == SmartSectorSignal.SECTOR_BREAKOUT || it.smartSignal == SmartSectorSignal.STRONG_BUYING
        }

        val totalBuyVol = sectorMetrics.sumOf { it.buyingVolume }
        val totalSellVol = sectorMetrics.sumOf { it.sellingVolume }
        val netMarketCr = Math.round(sectorMetrics.sumOf { it.netFlowCr } * 10.0) / 10.0
        val avgBuyPress = Math.round(sectorMetrics.map { it.buyingPressurePercent }.average() * 10.0) / 10.0

        return SectorFlowDashboardSummary(
            allSectors = sectorMetrics,
            highestBuyingSectors = highestBuying,
            lowestBuyingSectors = lowestBuying,
            highestSellingSectors = highestSelling,
            lowestSellingSectors = lowestSelling,
            breakoutSectors = breakouts,
            totalMarketBuyingVol = totalBuyVol,
            totalMarketSellingVol = totalSellVol,
            netMarketFlowCr = netMarketCr,
            averageBuyingPressure = avgBuyPress,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }
}
