package com.example

import com.example.data.MarketDataRepository
import com.example.data.model.OrderCategory
import com.example.data.model.OrderType
import com.example.data.model.ProductType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `core indian benchmark indices are present`() {
        val indices = MarketDataRepository.getIndices()
        val symbols = indices.map { it.symbol }
        assertTrue("NIFTY 50 must be in benchmark indices", symbols.contains("NIFTY 50"))
        assertTrue("BANK NIFTY must be in benchmark indices", symbols.contains("BANK NIFTY"))
        assertTrue("SENSEX must be in benchmark indices", symbols.contains("SENSEX"))
        assertTrue("FINNIFTY must be in benchmark indices", symbols.contains("FINNIFTY"))

        val nifty = indices.first { it.symbol == "NIFTY 50" }
        assertTrue("NIFTY 50 value must be positive", nifty.value > 20000.0)
        assertTrue("NIFTY 50 high must be >= low", nifty.high >= nifty.low)
    }

    @Test
    fun `option chain data generates valid strikes and PCR`() {
        val chain = MarketDataRepository.getOptionChain("NIFTY 50")
        assertNotNull(chain)
        assertEquals("NIFTY 50", chain.underlying)
        assertTrue("PCR should be > 0", chain.pcr > 0.0)
        assertTrue("Strikes count should be positive", chain.strikes.isNotEmpty())
        assertTrue("There must be at least one ATM strike", chain.strikes.any { it.isAtm })
    }

    @Test
    fun `market breadth has advances and declines`() {
        val breadth = MarketDataRepository.getMarketBreadth()
        assertTrue("Total advances + declines must equal total", breadth.total == breadth.advances + breadth.declines + breadth.unchanged)
        assertTrue("Advance ratio must be between 0 and 1", breadth.advanceRatio in 0f..1f)
    }

    @Test
    fun `paper trading initial portfolio is valid`() {
        val portfolio = MarketDataRepository.getInitialPortfolio()
        assertEquals(1000000.0, portfolio.startingCapital, 0.01)
        assertEquals(1000000.0, portfolio.availableMargin, 0.01)
        assertEquals(0.0, portfolio.usedMargin, 0.01)
        assertTrue("Initial positions must be empty until user explicitly trades", portfolio.positions.isEmpty())
        assertTrue("Initial orders must be empty until user explicitly trades", portfolio.orders.isEmpty())
    }

    @Test
    fun `fast scalping execution and lot multiplier work correctly`() {
        val vm = com.example.ui.MarketViewModel()
        vm.setScalpLotMultiplier(2)
        assertEquals(2, vm.uiState.value.scalpLotMultiplier)

        // Execute quick scalp buy
        vm.executeQuickScalp(OrderType.BUY)
        val state = vm.uiState.value
        assertNotNull(state.scalpTradeMessage)
        assertTrue("Message should indicate fast scalp", state.scalpTradeMessage!!.contains("FAST SCALP"))
        assertTrue("Positions should exist", state.portfolio.positions.isNotEmpty())

        // Test Exit All positions
        vm.exitAllScalpPositions()
        assertEquals(0, vm.uiState.value.portfolio.positions.size)
        assertTrue("Message should indicate all positions squared off", vm.uiState.value.scalpTradeMessage!!.contains("SQUARED OFF"))
    }

    @Test
    fun `live market symbol mapping is correct for nse and bse`() {
        assertEquals("%5ENSEI", com.example.data.api.NseMarketRepository.mapToApiSymbol("NIFTY 50"))
        assertEquals("%5ENSEBANK", com.example.data.api.NseMarketRepository.mapToApiSymbol("BANK NIFTY"))
        assertEquals("%5EBSESN", com.example.data.api.NseMarketRepository.mapToApiSymbol("SENSEX"))
        assertEquals("RELIANCE.NS", com.example.data.api.NseMarketRepository.mapToApiSymbol("RELIANCE"))
        assertEquals("HDFCBANK.NS", com.example.data.api.NseMarketRepository.mapToApiSymbol("HDFCBANK"))
    }

    @Test
    fun `retrofit client initializes api service properly`() {
        assertNotNull(com.example.data.api.RetrofitClient.apiService)
    }

    @Test
    fun `gemini model options include flash 3_8 as default`() {
        val defaultModel = com.example.data.ai.GeminiModelOption.FLASH_3_8
        assertEquals("gemini-3.8-flash", defaultModel.modelId)
        assertEquals("gemini-3.5-flash-lite", com.example.data.ai.GeminiModelOption.FLASH_LITE_3_5.modelId)
        assertEquals("gemini-4-argon", com.example.data.ai.GeminiModelOption.ARGON_4.modelId)
    }

    @Test
    fun `ai chat initial state contains greeting and quick stock suggestions`() {
        val initialUiState = com.example.ui.ai.AiChatUiState()
        assertTrue("Messages should not be empty", initialUiState.messages.isNotEmpty())
        assertEquals(com.example.data.ai.MessageSender.GEMINI, initialUiState.messages.first().sender)
        assertTrue("Quick suggestions should exist", initialUiState.quickSuggestions.isNotEmpty())
        assertTrue("Reliance suggestion should be present", initialUiState.quickSuggestions.any { it.contains("RELIANCE") })
    }

    @Test
    fun `indian market calendar detects weekend properly`() {
        // Saturday 8th Nov 2025
        val cal = java.util.Calendar.getInstance(com.example.data.market.IndianMarketCalendar.IST_TIME_ZONE).apply {
            set(2025, java.util.Calendar.NOVEMBER, 8, 11, 30, 0)
        }
        val schedule = com.example.data.market.IndianMarketCalendar.getCurrentSchedule(cal.time)
        assertEquals(com.example.data.market.MarketSessionStatus.CLOSED_WEEKEND, schedule.status)
        assertEquals("WEEKEND CLOSED", schedule.statusBadgeText)
        assertTrue("isLiveTradingActive must be false on weekend", !schedule.isLiveTradingActive)
    }

    @Test
    fun `indian market calendar detects official exchange holiday properly`() {
        // Independence Day 15th Aug 2025
        val cal = java.util.Calendar.getInstance(com.example.data.market.IndianMarketCalendar.IST_TIME_ZONE).apply {
            set(2025, java.util.Calendar.AUGUST, 15, 10, 30, 0)
        }
        val schedule = com.example.data.market.IndianMarketCalendar.getCurrentSchedule(cal.time)
        assertEquals(com.example.data.market.MarketSessionStatus.CLOSED_HOLIDAY, schedule.status)
        assertEquals("Independence Day", schedule.holidayName)
        assertTrue("isLiveTradingActive must be false on exchange holiday", !schedule.isLiveTradingActive)
    }

    @Test
    fun `indian market calendar detects live market hours properly`() {
        // Tuesday 11th Nov 2025 at 11:30 AM IST (regular trading day)
        val cal = java.util.Calendar.getInstance(com.example.data.market.IndianMarketCalendar.IST_TIME_ZONE).apply {
            set(2025, java.util.Calendar.NOVEMBER, 11, 11, 30, 0)
        }
        val schedule = com.example.data.market.IndianMarketCalendar.getCurrentSchedule(cal.time)
        assertEquals(com.example.data.market.MarketSessionStatus.OPEN_REGULAR, schedule.status)
        assertEquals("LIVE MARKET OPEN", schedule.statusBadgeText)
        assertTrue("isLiveTradingActive must be true during regular hours", schedule.isLiveTradingActive)
    }

    @Test
    fun `indian market calendar detects pre-market session properly`() {
        // Tuesday 11th Nov 2025 at 09:05 AM IST (pre-market session)
        val cal = java.util.Calendar.getInstance(com.example.data.market.IndianMarketCalendar.IST_TIME_ZONE).apply {
            set(2025, java.util.Calendar.NOVEMBER, 11, 9, 5, 0)
        }
        val schedule = com.example.data.market.IndianMarketCalendar.getCurrentSchedule(cal.time)
        assertEquals(com.example.data.market.MarketSessionStatus.PRE_MARKET, schedule.status)
        assertEquals("PRE-MARKET (NSE/BSE)", schedule.statusBadgeText)
        assertTrue("isLiveTradingActive must be true during pre-market", schedule.isLiveTradingActive)
    }

    @Test
    fun `market data provider config supports multiple licensed providers`() {
        val providers = com.example.data.market.DataProviderType.values()
        assertTrue("Direct exchange feed exists", providers.contains(com.example.data.market.DataProviderType.DIRECT_EXCHANGE_FEED))
        assertTrue("Upstox V2 exists", providers.contains(com.example.data.market.DataProviderType.UPSTOX_V2))
        assertTrue("Zerodha Kite exists", providers.contains(com.example.data.market.DataProviderType.ZERODHA_KITE))
        assertTrue("Angel One SmartAPI exists", providers.contains(com.example.data.market.DataProviderType.ANGEL_ONE_SMARTAPI))
        assertTrue("Institutional Proxy exists", providers.contains(com.example.data.market.DataProviderType.CUSTOM_BACKEND))
    }

    @Test
    fun `sector standardization maps raw sectors accurately`() {
        assertEquals("Banking", com.example.data.model.SectorClassification.standardizeSector("Private Banking"))
        assertEquals("Banking", com.example.data.model.SectorClassification.standardizeSector("PSU Banking"))
        assertEquals("Financial Services", com.example.data.model.SectorClassification.standardizeSector("NBFC / Financials"))
        assertEquals("IT", com.example.data.model.SectorClassification.standardizeSector("Information Tech"))
        assertEquals("Automobile", com.example.data.model.SectorClassification.standardizeSector("Automobile"))
        assertEquals("Pharma & Healthcare", com.example.data.model.SectorClassification.standardizeSector("Pharmaceuticals"))
        assertEquals("FMCG", com.example.data.model.SectorClassification.standardizeSector("FMCG"))
        assertEquals("Metals & Mining", com.example.data.model.SectorClassification.standardizeSector("Metals & Mining"))
        assertEquals("Energy & Utilities", com.example.data.model.SectorClassification.standardizeSector("Energy & Petrochem"))
        assertEquals("Realty", com.example.data.model.SectorClassification.standardizeSector("Real Estate"))
        assertEquals("Telecom", com.example.data.model.SectorClassification.standardizeSector("Telecom"))
        assertEquals("Infrastructure & Capital Goods", com.example.data.model.SectorClassification.standardizeSector("Infrastructure & Ports"))
        assertEquals("Consumer & Retail", com.example.data.model.SectorClassification.standardizeSector("Fintech & Internet"))
    }

    @Test
    fun `group all sectors produces valid sector performance metrics`() {
        val stocks = MarketDataRepository.getStocks()
        val sectors = com.example.data.model.SectorClassification.groupAllSectors(stocks)
        assertTrue("Should have multiple sectors", sectors.isNotEmpty())

        val banking = sectors.firstOrNull { it.sectorName == "Banking" }
        assertNotNull("Banking sector must exist", banking)
        assertTrue("Banking stocks count must be > 0", banking!!.stocks.isNotEmpty())
        assertTrue("Total turnover must be positive", banking.totalTurnoverCr > 0.0)
        assertNotNull("Top gainer in banking should exist", banking.topGainer)
        assertNotNull("Top loser in banking should exist", banking.topLoser)
        assertEquals(banking.stocks.size, banking.advancingCount + banking.decliningCount + banking.unchangedCount)
    }

    @Test
    fun `sector leaderboard ranks top gainers and highest volume correctly`() {
        val stocks = MarketDataRepository.getStocks()
        val sectors = com.example.data.model.SectorClassification.groupAllSectors(stocks)
        val leaderboard = com.example.data.model.SectorClassification.computeLeaderboard(sectors)

        assertTrue("Top gainers must not be empty", leaderboard.topGainers.isNotEmpty())
        assertTrue("Top losers must not be empty", leaderboard.topLosers.isNotEmpty())
        assertTrue("Highest volume sectors must not be empty", leaderboard.highestVolume.isNotEmpty())
        assertTrue("Momentum leaders must not be empty", leaderboard.strongestMomentum.isNotEmpty())

        // Top gainer avg change should be >= next in list
        if (leaderboard.topGainers.size >= 2) {
            assertTrue(leaderboard.topGainers[0].avgChangePercent >= leaderboard.topGainers[1].avgChangePercent)
        }
    }

    @Test
    fun `heatmap color grading provides correct colors for winners and losers`() {
        val (greenBg, _, textWhite) = com.example.ui.components.getHeatmapColors(3.5)
        assertEquals(androidx.compose.ui.graphics.Color.White, textWhite)

        val (redBg, _, redText) = com.example.ui.components.getHeatmapColors(-2.8)
        assertEquals(androidx.compose.ui.graphics.Color.White, redText)

        val (neutralBg, _, _) = com.example.ui.components.getHeatmapColors(0.0)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF263238), neutralBg)
    }

    @Test
    fun `complete nse fno stock universe contains all 182 eligible instruments`() {
        val master = com.example.data.fno.FnoStockMasterDatabase.ALL_INSTRUMENTS
        assertEquals("Master database must contain exactly 182 eligible individual equity F&O instruments", 182, master.size)

        // Check for zero duplicates
        val symbols = master.map { it.symbol }
        val uniqueSymbols = symbols.toSet()
        assertEquals("There must be zero duplicate symbols in F&O universe", symbols.size, uniqueSymbols.size)

        // Verify key representative F&O stocks exist
        assertTrue("RELIANCE must be in universe", uniqueSymbols.contains("RELIANCE"))
        assertTrue("HDFCBANK must be in universe", uniqueSymbols.contains("HDFCBANK"))
        assertTrue("ICICIBANK must be in universe", uniqueSymbols.contains("ICICIBANK"))
        assertTrue("SBIN must be in universe", uniqueSymbols.contains("SBIN"))
        assertTrue("TCS must be in universe", uniqueSymbols.contains("TCS"))
        assertTrue("INFY must be in universe", uniqueSymbols.contains("INFY"))
        assertTrue("TATAMOTORS must be in universe", uniqueSymbols.contains("TATAMOTORS"))
        assertTrue("ZOMATO must be in universe", uniqueSymbols.contains("ZOMATO"))
        assertTrue("HAL must be in universe", uniqueSymbols.contains("HAL"))
        assertTrue("BEL must be in universe", uniqueSymbols.contains("BEL"))
        assertTrue("TRENT must be in universe", uniqueSymbols.contains("TRENT"))
    }

    @Test
    fun `every fno instrument has valid lot size, token, company name and sector`() {
        val master = com.example.data.fno.FnoStockMasterDatabase.ALL_INSTRUMENTS
        master.forEach { inst ->
            assertTrue("Symbol must not be blank for ${inst.symbol}", inst.symbol.isNotBlank())
            assertTrue("Company name must not be blank for ${inst.symbol}", inst.companyName.isNotBlank())
            assertTrue("Lot size must be positive for ${inst.symbol}", inst.lotSize > 0)
            assertTrue("Instrument token must be positive for ${inst.symbol}", inst.instrumentToken > 0L)
            assertTrue("Sector must not be blank for ${inst.symbol}", inst.sector.isNotBlank())
            assertTrue("Base LTP must be positive for ${inst.symbol}", inst.baseLtp > 0.0)
            assertTrue("Prev close must be positive for ${inst.symbol}", inst.prevClose > 0.0)
            assertTrue("52w High must be >= 52w Low for ${inst.symbol}", inst.week52High >= inst.week52Low)
        }

        // Verify specific revised NSE lot sizes
        val reliance = master.first { it.symbol == "RELIANCE" }
        assertEquals(500, reliance.lotSize)

        val hdfc = master.first { it.symbol == "HDFCBANK" }
        assertEquals(550, hdfc.lotSize)

        val sbin = master.first { it.symbol == "SBIN" }
        assertEquals(750, sbin.lotSize)

        val tataMotors = master.first { it.symbol == "TATAMOTORS" }
        assertEquals(1425, tataMotors.lotSize)
    }

    @Test
    fun `all 182 fno stocks cleanly categorize into primary sectors without others`() {
        val quotes = com.example.data.fno.FnoUniverseRepository.getAllInitialStockQuotes()
        assertEquals(182, quotes.size)

        val unmapped = quotes.filter {
            com.example.data.model.SectorClassification.standardizeSector(it.sector) == "Other F&O Sectors"
        }
        assertTrue("No stock should be left unmapped in Other F&O Sectors: ${unmapped.map { "${it.symbol}:${it.sector}" }}", unmapped.isEmpty())

        val sectors = com.example.data.model.SectorClassification.groupAllSectors(quotes)
        assertTrue("Should have at least 10 active sectors", sectors.size >= 10)

        val banking = sectors.firstOrNull { it.sectorName == "Banking" }
        assertNotNull("Banking sector must exist", banking)
        assertEquals("Banking must have 15 stocks", 15, banking!!.stocks.size)

        val itSector = sectors.firstOrNull { it.sectorName == "IT" }
        assertNotNull("IT sector must exist", itSector)
        assertEquals("IT must have 12 stocks", 12, itSector!!.stocks.size)
    }

    @Test
    fun `fno scanner filters correctly identify gainers, losers, volume and momentum`() {
        val quotes = com.example.data.fno.FnoUniverseRepository.getAllInitialStockQuotes()

        // 1. All F&O
        val all = com.example.data.fno.FnoUniverseRepository.scanFnoStocks(quotes, com.example.data.model.FnoScannerFilter.ALL_FNO)
        assertEquals(182, all.size)

        // 2. High Volume
        val highVol = com.example.data.fno.FnoUniverseRepository.scanFnoStocks(quotes, com.example.data.model.FnoScannerFilter.HIGH_VOLUME)
        assertTrue("High volume list must be non-empty", highVol.isNotEmpty())
        if (highVol.size >= 2) {
            assertTrue("Sorted by volume descending", highVol[0].volume >= highVol[1].volume)
        }

        // 3. Search by token ID
        val tokenStock = com.example.data.fno.FnoStockMasterDatabase.ALL_INSTRUMENTS.first { it.symbol == "RELIANCE" }
        val searchByToken = com.example.data.fno.FnoUniverseRepository.scanFnoStocks(
            stocks = quotes,
            filter = com.example.data.model.FnoScannerFilter.ALL_FNO,
            searchQuery = tokenStock.instrumentToken.toString()
        )
        assertTrue("Search by token should find RELIANCE", searchByToken.any { it.symbol == "RELIANCE" })

        // 4. Search by Company Name
        val searchByName = com.example.data.fno.FnoUniverseRepository.scanFnoStocks(
            stocks = quotes,
            filter = com.example.data.model.FnoScannerFilter.ALL_FNO,
            searchQuery = "Infosys"
        )
        assertTrue("Search by company name should find INFY", searchByName.any { it.symbol == "INFY" })
    }

    @Test
    fun `interday boost calculates technical indicators, momentum scores and filters 182 stocks`() {
        val quotes = com.example.data.fno.FnoUniverseRepository.getAllInitialStockQuotes()
        val boostItems = com.example.data.analytics.TradingAnalyticsEngine.computeInterdayData(quotes)
        assertEquals(182, boostItems.size)

        boostItems.forEach { item ->
            assertTrue("Momentum score must be within 0..100 for ${item.symbol}", item.momentumScore in 0.0..100.0)
            assertTrue("Volume strength must be within 0..100 for ${item.symbol}", item.volumeStrengthScore in 0.0..100.0)
            assertTrue("RSI-14 must be within 0..100 for ${item.symbol}", item.technicals.rsi14 in 0.0..100.0)
            assertTrue("VWAP must be positive for ${item.symbol}", item.technicals.vwap > 0.0)
            assertTrue("Relative volume must be positive for ${item.symbol}", item.technicals.relativeVolume > 0.0)
            assertTrue("Buyer pressure must be within 0..100 for ${item.symbol}", item.technicals.buyerPressurePct in 0..100)
        }

        // Test filtering
        val topBullish = com.example.data.analytics.TradingAnalyticsEngine.filterInterdayStocks(
            items = boostItems,
            filter = com.example.data.model.InterdayFilter.BULLISH_MOMENTUM
        )
        topBullish.forEach {
            assertTrue("Bullish stock must have positive change", it.changePercent > 0.0)
            assertTrue("Bullish stock must be at or above VWAP", it.ltp >= it.technicals.vwap)
        }

        val highVol = com.example.data.analytics.TradingAnalyticsEngine.filterInterdayStocks(
            items = boostItems,
            filter = com.example.data.model.InterdayFilter.HIGH_VOLUME
        )
        if (highVol.size >= 2) {
            assertTrue("High volume must be sorted descending", highVol[0].volume >= highVol[1].volume)
        }
    }

    @Test
    fun `swing trading engine computes transparent entry, stop loss, targets and indicator matrix`() {
        val quotes = com.example.data.fno.FnoUniverseRepository.getAllInitialStockQuotes()
        val setups = com.example.data.analytics.TradingAnalyticsEngine.generateSwingSetups(quotes, "1D")
        assertEquals(182, setups.size)

        setups.forEach { setup ->
            assertTrue("Entry min must be positive for ${setup.stock.symbol}", setup.entryZoneMin > 0.0)
            assertTrue("Entry max must be >= Entry min for ${setup.stock.symbol}", setup.entryZoneMax >= setup.entryZoneMin)
            assertTrue("Stop loss must be positive for ${setup.stock.symbol}", setup.stopLoss > 0.0)
            assertTrue("Target 1 must be positive for ${setup.stock.symbol}", setup.target1 > 0.0)
            assertTrue("Target 2 must be positive for ${setup.stock.symbol}", setup.target2 > 0.0)

            if (setup.direction == "BULLISH") {
                assertTrue("Bullish target 1 must be > LTP for ${setup.stock.symbol}", setup.target1 > setup.stock.ltp)
                assertTrue("Bullish target 2 must be > target 1 for ${setup.stock.symbol}", setup.target2 > setup.target1)
                assertTrue("Bullish stop loss must be < LTP for ${setup.stock.symbol}", setup.stopLoss < setup.stock.ltp)
            } else {
                assertTrue("Bearish target 1 must be < LTP for ${setup.stock.symbol}", setup.target1 < setup.stock.ltp)
                assertTrue("Bearish stop loss must be > LTP for ${setup.stock.symbol}", setup.stopLoss > setup.stock.ltp)
            }

            // Verify technical indicator matrix
            assertTrue("EMA 20 must be positive for ${setup.stock.symbol}", setup.indicators.ema20 > 0.0)
            assertTrue("SMA 50 must be positive for ${setup.stock.symbol}", setup.indicators.sma50 > 0.0)
            assertTrue("Bollinger Upper must be >= Lower for ${setup.stock.symbol}", setup.indicators.bollingerUpper >= setup.indicators.bollingerLower)
            assertTrue("ATR must be positive for ${setup.stock.symbol}", setup.indicators.atr > 0.0)
        }
    }

    @Test
    fun `oi clock calculates strike rows, call vs put open interest, pcr and session timeline`() {
        val clock = com.example.data.analytics.TradingAnalyticsEngine.generateOiClock("NIFTY 50")
        assertNotNull(clock)
        assertEquals("NIFTY 50", clock.underlying)
        assertTrue("Spot price must be positive", clock.spotPrice > 0.0)
        assertTrue("Total Call OI must be positive", clock.totalCallOi > 0L)
        assertTrue("Total Put OI must be positive", clock.totalPutOi > 0L)
        assertTrue("PCR must be positive", clock.pcr > 0.0)
        assertTrue("Strikes list must not be empty", clock.strikes.isNotEmpty())
        assertTrue("Timeline intervals must be populated", clock.timeline.size >= 6)
        assertTrue("OI alerts must be present", clock.alerts.isNotEmpty())

        val hasAtm = clock.strikes.any { it.isAtm }
        assertTrue("At least one strike must be marked ATM", hasAtm)
    }

    @Test
    fun `fii dii module correctly provides cash market flows, derivatives matrices and period summaries`() {
        val (history, summaries) = com.example.data.analytics.TradingAnalyticsEngine.generateFiiDiiHistory()
        assertTrue("History records must not be empty", history.isNotEmpty())
        assertTrue("Summaries must have weekly and monthly records", summaries.size >= 4)

        val latest = history.first()
        assertTrue("Latest record must have non-zero buy flow", latest.fiiCashBuyCr > 0.0)
        assertTrue("Latest record must have non-zero DII buy flow", latest.diiCashBuyCr > 0.0)
        assertEquals("Combined net must equal FII net + DII net", latest.fiiCashNetCr + latest.diiCashNetCr, latest.combinedNetCr, 0.01)

        val deriv = latest.derivativePositions
        assertTrue("Index futures contracts must be positive", deriv.indexFuturesNetContracts > 0L)
        assertTrue("Index options calls must be non-zero", deriv.indexOptionsLongCalls > 0L)
    }

    @Test
    fun `individual fno equity option chains generate accurate strikes, lot sizes, greeks, and pcr`() {
        val testSymbols = listOf("RELIANCE", "TCS", "HDFCBANK", "TATAMOTORS", "ZOMATO")
        testSymbols.forEach { sym ->
            val chain = com.example.data.MarketDataRepository.getOptionChain(sym)
            assertNotNull(chain)
            assertEquals(sym, chain.underlying)
            assertTrue("Spot price for $sym must be positive", chain.spotPrice > 0.0)
            assertTrue("PCR for $sym must be positive", chain.pcr > 0.0)
            assertTrue("Max pain strike for $sym must be positive", chain.maxPainStrike > 0.0)
            assertTrue("ATM strike for $sym must be positive", chain.atmStrike > 0.0)
            assertTrue("Strikes list for $sym must not be empty", chain.strikes.isNotEmpty())
            assertTrue("Total Call OI for $sym must be positive", chain.totalCallOi > 0L)
            assertTrue("Total Put OI for $sym must be positive", chain.totalPutOi > 0L)

            // Verify strike-by-strike data
            chain.strikes.forEach { strike ->
                assertTrue("Call LTP must be non-negative", strike.callLtp >= 0.0)
                assertTrue("Put LTP must be non-negative", strike.putLtp >= 0.0)
                assertTrue("Call IV must be positive", strike.callIv > 0.0)
                assertTrue("Put IV must be positive", strike.putIv > 0.0)
                assertTrue("Call Delta must be between 0 and 1", strike.callDelta in 0.0..1.0)
                assertTrue("Put Delta must be between -1 and 0", strike.putDelta in -1.0..0.0)
            }
        }
    }

    @Test
    fun `sector scope contains exclusively eligible fno universe stocks across all sectors`() {
        val allStocks = com.example.data.fno.FnoUniverseRepository.getAllInitialStockQuotes()
        assertTrue("Universe must contain all 182 F&O stocks", allStocks.size >= 182)
        assertTrue("Every stock in the universe must be marked as F&O eligible", allStocks.all { it.isFnO })

        val groupedSectors = com.example.data.model.SectorClassification.groupAllSectors(allStocks)
        assertTrue("All primary sectors must be represented", groupedSectors.isNotEmpty())

        groupedSectors.forEach { sector ->
            assertTrue("Sector ${sector.sectorName} must have F&O stocks", sector.stocks.isNotEmpty())
            assertTrue("All stocks in sector ${sector.sectorName} must be F&O eligible", sector.stocks.all { it.isFnO })
            assertTrue("Sector turnover must be positive", sector.totalTurnoverCr >= 0.0)
        }
    }

    @Test
    fun `auto sync is configured for automatic 3-second intervals`() {
        val state = com.example.ui.MarketUiState()
        assertEquals("Auto-sync interval must be 3 seconds", 3, state.autoSyncSeconds)
    }

    @Test
    fun `user can add unlimited demo funds to trading account`() {
        val vm = com.example.ui.MarketViewModel()
        val initialMargin = vm.uiState.value.portfolio.availableMargin
        val initialValue = vm.uiState.value.portfolio.totalAccountValue

        // Add 5 Lakhs
        vm.addDemoFunds(500000.0)
        assertEquals(initialMargin + 500000.0, vm.uiState.value.portfolio.availableMargin, 0.01)
        assertEquals(initialValue + 500000.0, vm.uiState.value.portfolio.totalAccountValue, 0.01)
        assertTrue(vm.uiState.value.demoFundsSuccessMessage?.contains("500,000") == true)

        // Add 1 Crore unlimited
        val currentMargin = vm.uiState.value.portfolio.availableMargin
        vm.addDemoFunds(10000000.0)
        assertEquals(currentMargin + 10000000.0, vm.uiState.value.portfolio.availableMargin, 0.01)

        // Add 5 Crores unlimited
        val afterCroreMargin = vm.uiState.value.portfolio.availableMargin
        vm.addDemoFunds(50000000.0)
        assertEquals(afterCroreMargin + 50000000.0, vm.uiState.value.portfolio.availableMargin, 0.01)
        assertTrue("Account margin should exceed 6 Crores", vm.uiState.value.portfolio.availableMargin > 60000000.0)
    }

    @Test
    fun `user can reset demo account to default capital`() {
        val vm = com.example.ui.MarketViewModel()
        vm.addDemoFunds(10000000.0)
        assertTrue(vm.uiState.value.portfolio.availableMargin > 5000000.0)

        // Reset account
        vm.resetDemoAccount(1000000.0)
        assertEquals(1000000.0, vm.uiState.value.portfolio.availableMargin, 0.01)
        assertEquals(1000000.0, vm.uiState.value.portfolio.totalAccountValue, 0.01)
        assertEquals(0, vm.uiState.value.portfolio.positions.size)
        assertTrue(vm.uiState.value.demoFundsSuccessMessage?.contains("reset", ignoreCase = true) == true)
    }

    @Test
    fun `sector flow dashboard contains all 20 sectors with complete F&O stock mapping`() {
        val allStocks = com.example.data.fno.FnoUniverseRepository.getAllInitialStockQuotes()
        assertTrue("Stock universe must have 182 stocks", allStocks.size >= 182)

        val dashboard = com.example.data.analytics.SectorOrderFlowEngine.buildSectorFlowDashboard(allStocks)
        assertEquals("Dashboard must contain all 20 sectors", 20, dashboard.allSectors.size)
        assertTrue("Never display fewer than 12 sectors", dashboard.allSectors.size >= 12)

        val mandatorySectors = listOf(
            "Banking", "IT", "Automobile", "Pharma", "FMCG", "Metal", "Realty", "Energy",
            "Oil & Gas", "Infrastructure", "Capital Goods", "PSU Banks", "Private Banks",
            "Financial Services", "Chemicals", "Telecom", "Media", "Consumer Durables", "Cement", "Healthcare"
        )
        val presentSectors = dashboard.allSectors.map { it.sectorName }
        mandatorySectors.forEach { sec ->
            assertTrue("Sector $sec must be present", presentSectors.contains(sec))
        }

        dashboard.allSectors.forEach { sec ->
            assertTrue("Sector ${sec.sectorName} must have mapped stocks", sec.stocks.isNotEmpty())
            assertTrue("Total stocks count must match", sec.totalStocks == sec.stocks.size)
            assertTrue("Index value must be positive", sec.indexValue > 0.0)
        }
    }

    @Test
    fun `sector buying and selling metrics calculate accurate order flow and pressures`() {
        val allStocks = com.example.data.fno.FnoUniverseRepository.getAllInitialStockQuotes()
        val dashboard = com.example.data.analytics.SectorOrderFlowEngine.buildSectorFlowDashboard(allStocks)

        dashboard.allSectors.forEach { sec ->
            assertTrue("Buying pressure must be between 0 and 100", sec.buyingPressurePercent in 0.0..100.0)
            assertTrue("Selling pressure must be between 0 and 100", sec.sellingPressurePercent in 0.0..100.0)
            assertEquals("Buying + Selling pressure must sum to 100%", 100.0, sec.buyingPressurePercent + sec.sellingPressurePercent, 0.5)
            assertEquals("Net flow volume must equal buy vol minus sell vol", sec.netFlowVolume, sec.buyingVolume - sec.sellingVolume)
            assertTrue("Total stocks must equal advances + declines + unchanged", sec.totalStocks == sec.advancingCount + sec.decliningCount + sec.unchangedCount)
        }

        // Leaderboards must have 5 entries each
        assertEquals(5, dashboard.highestBuyingSectors.size)
        assertEquals(5, dashboard.lowestBuyingSectors.size)
        assertEquals(5, dashboard.highestSellingSectors.size)
        assertEquals(5, dashboard.lowestSellingSectors.size)
    }

    @Test
    fun `smart sector signals and rotation detector categorize market phases`() {
        val allStocks = com.example.data.fno.FnoUniverseRepository.getAllInitialStockQuotes()
        val dashboard = com.example.data.analytics.SectorOrderFlowEngine.buildSectorFlowDashboard(allStocks)

        val quadrants = dashboard.allSectors.map { it.rotationQuadrant }.toSet()
        assertTrue("Rotation matrix must identify active market phases", quadrants.isNotEmpty())

        dashboard.allSectors.forEach { sec ->
            assertNotNull(sec.smartSignal)
            assertNotNull(sec.moneyFlowDirection)
        }
    }
}

