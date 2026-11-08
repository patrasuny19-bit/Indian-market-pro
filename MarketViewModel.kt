package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarketDataRepository
import com.example.data.api.NseMarketRepository
import com.example.data.market.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MarketUiState(
    val indices: List<MarketIndex> = emptyList(),
    val stocks: List<StockQuote> = emptyList(),
    val selectedStock: StockQuote? = null,
    val selectedTimeframe: String = "1D",
    val candles: List<CandleData> = emptyList(),
    val marketDepth: MarketDepth? = null,
    val optionChain: OptionChainData? = null,
    val selectedFnoUnderlying: String = "NIFTY 50",
    val fnoBuildups: List<FnoBuildupItem> = emptyList(),
    val fiiDii: List<FiiDiiRecord> = emptyList(),
    val marketBreadth: MarketBreadth = MarketBreadth(34, 15, 1),
    val portfolio: PortfolioSummary = PortfolioSummary(),
    val watchlistFilter: String = "All Stocks",
    val searchQuery: String = "",
    val favoriteSymbols: Set<String> = setOf("RELIANCE", "HDFCBANK", "TATAMOTORS"),
    val recentTickFlash: Map<String, Boolean> = emptyMap(), // symbol -> isGreen
    val isDarkMode: Boolean = true,
    val currentTab: Int = 0, // 0: Dashboard, 1: Scalper, 2: F&O Desk, 3: Watchlist, 4: Portfolio, 5: AI Advisor, 6: Detail, 7: Sectors
    val previousTab: Int = 0,
    val showOrderDialog: Boolean = false,
    val orderDialogStock: StockQuote? = null,
    val orderDialogType: OrderType = OrderType.BUY,
    // TradingView & Scalping Terminal Properties
    val isTradingViewActive: Boolean = false, // Defaults to high-performance Native Candlestick Chart
    val scalpInterval: String = "1", // 1 min for scalping
    val scalpLotMultiplier: Int = 1,
    val scalpStopLossPts: Double = 10.0,
    val scalpTargetPts: Double = 20.0,
    val scalpTradeMessage: String? = null,
    // Real Live Exchange Connection Status
    val isRealMarketLive: Boolean = false,
    val isLiveSyncing: Boolean = false,
    val lastLiveSyncTime: String = "Connecting to NSE...",
    // Market Calendar & Schedule (IST Market Hours & Holidays)
    val marketSchedule: MarketScheduleInfo = IndianMarketCalendar.getCurrentSchedule(),
    // Licensed Data Provider & Feed Health
    val providerConfig: MarketDataProviderConfig = MarketDataProviderConfig(),
    val feedHealth: MarketFeedHealth = MarketFeedHealth(),
    val showProviderSettingsDialog: Boolean = false,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: String? = null,
    // Search & Watchlist
    val isSearchDialogOpen: Boolean = false,
    val searchCategory: String = "All", // "All", "Stocks", "Indices", "F&O Contracts", "Watchlist"
    val isSearchingOnline: Boolean = false,
    val onlineSearchResult: StockQuote? = null,
    // F&O Trade Dialog
    val showFnoTradeDialog: Boolean = false,
    val fnoTradeStock: StockQuote? = null,
    val fnoTradeInitialContract: String? = null,
    // Demo Trading Unlimited Funds Dialog
    val showAddDemoFundsDialog: Boolean = false,
    val demoFundsSuccessMessage: String? = null,
    // Complete NSE F&O Universe & Scanner
    val fnoSyncStats: FnoUniverseSyncStats = com.example.data.fno.FnoUniverseRepository.syncStats.value,
    val selectedFnoScannerFilter: FnoScannerFilter = FnoScannerFilter.ALL_FNO,
    val fnoScannerSearchQuery: String = "",
    val fnoScannerSectorFilter: String = "All Sectors",
    val selectedFnoSubTab: Int = 0, // 0: F&O Scanner, 1: Option Chain, 2: OI Buildup, 3: Strategy Desk
    // 1. Interday Boost Module
    val selectedInterdayFilter: InterdayFilter = InterdayFilter.ALL_BOOST,
    val interdaySearchQuery: String = "",
    val selectedInterdayTimeframe: String = "5m",
    val interdayStocks: List<InterdayStockData> = emptyList(),
    // 2. Swing Trading Module
    val selectedSwingFilter: SwingFilterType = SwingFilterType.ALL_SETUPS,
    val swingSearchQuery: String = "",
    val selectedSwingTimeframe: String = "1D",
    val swingSetups: List<SwingSetup> = emptyList(),
    // 3. OI Clock Module
    val selectedOiClockUnderlying: String = "NIFTY 50",
    val oiClockData: OiClockData? = null,
    val isOiClockRefreshing: Boolean = false,
    // 4. FII / DII Institutional Module
    val fiiDiiHistory: List<FiiDiiActivityDetail> = emptyList(),
    val fiiDiiSummaries: List<FiiDiiPeriodSummary> = emptyList(),
    val fiiDiiSourceInfo: FiiDiiDataSourceInfo = FiiDiiDataSourceInfo(),
    val selectedFiiDiiTab: Int = 0,
    // Automatic Real-Time Market Sync (every 3 seconds)
    val autoSyncSeconds: Int = 3
)

class MarketViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    private var syncJob: Job? = null
    private var pollingJob: Job? = null

    init {
        loadInitialData()
        syncFnoUniverseMaster()
        syncRealMarketData()
        startRealMarketPolling()
        startMarketScheduleTicker()
    }

    private fun startMarketScheduleTicker() {
        viewModelScope.launch {
            while (true) {
                delay(10000)
                val schedule = IndianMarketCalendar.getCurrentSchedule()
                _uiState.update { it.copy(marketSchedule = schedule) }
            }
        }
    }

    private fun loadInitialData() {
        val initialIndices = MarketDataRepository.getIndices()
        val initialStocks = MarketDataRepository.getStocks()
        val defaultStock = initialStocks.first()
        val initialCandles = MarketDataRepository.getCandles(defaultStock.symbol, "1D")
        val initialDepth = MarketDataRepository.getMarketDepth(defaultStock.ltp)
        val initialFno = MarketDataRepository.getOptionChain("NIFTY 50")
        val initialBuildups = MarketDataRepository.getFnoBuildups()
        val initialFiiDii = MarketDataRepository.getFiiDiiData()
        val initialBreadth = MarketDataRepository.getMarketBreadth()
        val initialPortfolio = MarketDataRepository.getInitialPortfolio()

        val interday = com.example.data.analytics.TradingAnalyticsEngine.computeInterdayData(initialStocks)
        val swing = com.example.data.analytics.TradingAnalyticsEngine.generateSwingSetups(initialStocks, "1D")
        val oiClock = com.example.data.analytics.TradingAnalyticsEngine.generateOiClock("NIFTY 50")
        val (fiiDiiHistory, fiiDiiSummaries) = com.example.data.analytics.TradingAnalyticsEngine.generateFiiDiiHistory()

        _uiState.update {
            it.copy(
                indices = initialIndices,
                stocks = initialStocks,
                selectedStock = defaultStock,
                candles = initialCandles,
                marketDepth = initialDepth,
                optionChain = initialFno,
                fnoBuildups = initialBuildups,
                fiiDii = initialFiiDii,
                marketBreadth = initialBreadth,
                portfolio = initialPortfolio,
                interdayStocks = interday,
                swingSetups = swing,
                oiClockData = oiClock,
                fiiDiiHistory = fiiDiiHistory,
                fiiDiiSummaries = fiiDiiSummaries
            )
        }
    }

    /**
     * Pulls actual live market prices directly from NSE / BSE feeds
     */
    fun syncRealMarketData() {
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val currentSchedule = IndianMarketCalendar.getCurrentSchedule()

            _uiState.update { 
                it.copy(
                    isLiveSyncing = true,
                    marketSchedule = currentSchedule,
                    feedHealth = it.feedHealth.copy(status = ProviderConnectionStatus.CONNECTING)
                ) 
            }

            try {
                val currentState = _uiState.value

                // 1. Fetch real live indices in parallel
                val updatedIndices = currentState.indices.map { index ->
                    async {
                        NseMarketRepository.fetchLiveIndex(index.symbol, index)
                    }
                }.awaitAll()

                // 2. Prioritize active stock and favorites/watchlist stocks first
                val prioritySymbols = (setOfNotNull(currentState.selectedStock?.symbol) + currentState.favoriteSymbols + setOf("RELIANCE", "HDFCBANK", "ICICIBANK", "INFY", "TCS", "TATAMOTORS")).toSet()
                
                val priorityStocks = currentState.stocks.filter { it.symbol in prioritySymbols }
                val otherStocks = currentState.stocks.filterNot { it.symbol in prioritySymbols }

                val updatedPriorityStocks = priorityStocks.map { stock ->
                    async {
                        NseMarketRepository.fetchLiveStock(stock.symbol, stock)
                    }
                }.awaitAll()

                // Merge priority updates immediately
                val currentStockMap = (otherStocks + updatedPriorityStocks).associateBy { it.symbol }.toMutableMap()

                // 3. Update selected stock quote with real market price
                val currentSelected = currentState.selectedStock?.let { sel ->
                    currentStockMap[sel.symbol] ?: sel
                } ?: updatedPriorityStocks.firstOrNull()

                // 4. Fetch real live candles for currently selected stock
                val realCandles = if (currentSelected != null) {
                    val fetched = NseMarketRepository.fetchLiveCandles(currentSelected.symbol, currentState.selectedTimeframe)
                    if (fetched.isNotEmpty()) fetched else currentState.candles
                } else currentState.candles

                // 5. Update option chain spot price to match real live index or stock price
                val updatedOptionChain = currentState.optionChain?.let { chain ->
                    val matchingIndex = updatedIndices.find { it.symbol.equals(chain.underlying, ignoreCase = true) }
                    val matchingStock = currentStockMap[chain.underlying] ?: currentState.stocks.find { it.symbol.equals(chain.underlying, ignoreCase = true) }
                    val spot = matchingIndex?.value ?: matchingStock?.ltp
                    if (spot != null) {
                        MarketDataRepository.getOptionChain(chain.underlying, spot)
                    } else chain
                }

                // 6. Recalculate portfolio positions with real live market prices
                val updatedPositions = currentState.portfolio.positions.map { pos ->
                    val matchingStock = currentStockMap[pos.symbol]
                    if (matchingStock != null) {
                        pos.copy(currentPrice = matchingStock.ltp)
                    } else pos
                }

                val latency = System.currentTimeMillis() - startTime
                val istFmt = SimpleDateFormat("hh:mm:ss a 'IST'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                }
                val syncTimeStr = istFmt.format(Date())

                val anyFresh = updatedIndices.any { it.isRealTimeFresh } || updatedPriorityStocks.any { it.isRealTimeFresh }
                val connectionStatus = when {
                    !currentSchedule.isLiveTradingActive -> ProviderConnectionStatus.CLOSED_OUT_OF_HOURS
                    anyFresh -> ProviderConnectionStatus.CONNECTED_LIVE
                    else -> ProviderConnectionStatus.STALE_CACHED
                }

                _uiState.update { state ->
                    state.copy(
                        indices = updatedIndices,
                        stocks = currentStockMap.values.toList(),
                        selectedStock = currentSelected,
                        candles = realCandles,
                        optionChain = updatedOptionChain ?: state.optionChain,
                        portfolio = state.portfolio.copy(positions = updatedPositions),
                        isRealMarketLive = anyFresh && currentSchedule.isLiveTradingActive,
                        isLiveSyncing = false,
                        lastLiveSyncTime = syncTimeStr,
                        feedHealth = MarketFeedHealth(
                            status = connectionStatus,
                            providerName = state.providerConfig.activeProvider.displayName,
                            lastSuccessTimestamp = System.currentTimeMillis(),
                            lastSyncTimeFormatted = syncTimeStr,
                            latencyMs = latency,
                            warningMessage = if (!anyFresh && currentSchedule.isLiveTradingActive) "Exchange latency detected. Reconnecting..." else null
                        )
                    )
                }

                // 7. Background-fetch remaining stocks in batches without flooding state updates
                val updatedOtherList = mutableListOf<StockQuote>()
                for (chunk in otherStocks.chunked(10)) {
                    if (!isActive) break
                    val updatedChunk = chunk.map { stock ->
                        async {
                            NseMarketRepository.fetchLiveStock(stock.symbol, stock)
                        }
                    }.awaitAll()
                    updatedOtherList.addAll(updatedChunk)
                }
                if (updatedOtherList.isNotEmpty() && isActive) {
                    updatedOtherList.forEach { currentStockMap[it.symbol] = it }
                    _uiState.update { state ->
                        state.copy(stocks = currentStockMap.values.toList())
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLiveSyncing = false,
                        feedHealth = state.feedHealth.copy(
                            status = ProviderConnectionStatus.DISCONNECTED_ERROR,
                            warningMessage = "Data feed synchronization error: ${e.localizedMessage ?: "Network timeout"}. Showing cached official closing prices."
                        )
                    )
                }
            }
        }
    }

    /**
     * Automatically polls and synchronizes real live market quotes every 3 seconds
     */
    private fun startRealMarketPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(3000L)
                syncRealMarketData()
            }
        }
    }

    fun openProviderSettingsDialog() {
        _uiState.update { it.copy(showProviderSettingsDialog = true, connectionTestResult = null) }
    }

    fun dismissProviderSettingsDialog() {
        _uiState.update { it.copy(showProviderSettingsDialog = false, connectionTestResult = null) }
    }

    fun saveProviderConfig(config: MarketDataProviderConfig) {
        _uiState.update { it.copy(providerConfig = config, showProviderSettingsDialog = false) }
        syncRealMarketData()
    }

    fun manualRefreshMarketData() {
        syncRealMarketData()
    }

    fun testProviderConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionTestResult = null) }
            val start = System.currentTimeMillis()
            val result = NseMarketRepository.fetchDynamicStock("RELIANCE")
            val latency = System.currentTimeMillis() - start
            val success = result != null && result.ltp > 0
            val msg = if (success) {
                "Connected to ${result?.name} (LTP: ₹${result?.ltp}) in ${latency}ms via ${_uiState.value.providerConfig.activeProvider.displayName}"
            } else {
                "Exchange feed unreachable or credentials required. Retrying gateway connection."
            }
            _uiState.update { 
                it.copy(
                    isTestingConnection = false, 
                    connectionTestResult = msg
                ) 
            }
        }
    }

    fun selectStock(stock: StockQuote) {
        viewModelScope.launch {
            val depth = MarketDataRepository.getMarketDepth(stock.ltp)
            _uiState.update {
                it.copy(
                    selectedStock = stock,
                    marketDepth = depth
                )
            }
            // Fetch real live candles for selected stock
            val realCandles = NseMarketRepository.fetchLiveCandles(stock.symbol, _uiState.value.selectedTimeframe)
            if (realCandles.isNotEmpty()) {
                _uiState.update { it.copy(candles = realCandles) }
            }
        }
    }

    fun selectIndexForChart(index: MarketIndex) {
        viewModelScope.launch {
            val dummyQuote = StockQuote(
                symbol = index.symbol,
                name = index.name,
                ltp = index.value,
                change = index.change,
                changePercent = index.changePercent,
                open = index.open,
                high = index.high,
                low = index.low,
                prevClose = index.prevClose,
                volume = 15820000L,
                turnoverCr = 2890.0,
                sector = "Benchmark Index",
                marketCapCr = 0.0,
                peRatio = 22.4,
                isFnO = true,
                lotSize = when (index.symbol) {
                    "BANK NIFTY" -> 15
                    "FINNIFTY" -> 25
                    else -> 25
                },
                yearHigh = index.yearHigh,
                yearLow = index.yearLow
            )
            val depth = MarketDataRepository.getMarketDepth(index.value)
            _uiState.update {
                it.copy(
                    selectedStock = dummyQuote,
                    marketDepth = depth
                )
            }
            val realCandles = NseMarketRepository.fetchLiveCandles(index.symbol, _uiState.value.selectedTimeframe)
            if (realCandles.isNotEmpty()) {
                _uiState.update { it.copy(candles = realCandles) }
            }
        }
    }

    fun selectTimeframe(tf: String) {
        _uiState.update { it.copy(selectedTimeframe = tf) }
        viewModelScope.launch {
            val sym = _uiState.value.selectedStock?.symbol ?: "NIFTY 50"
            val realCandles = NseMarketRepository.fetchLiveCandles(sym, tf)
            if (realCandles.isNotEmpty()) {
                _uiState.update { it.copy(candles = realCandles) }
            } else {
                val fallbackCandles = MarketDataRepository.getCandles(sym, tf)
                _uiState.update { it.copy(candles = fallbackCandles) }
            }
        }
    }

    fun toggleFavorite(symbol: String) {
        _uiState.update { state ->
            val set = state.favoriteSymbols.toMutableSet()
            if (set.contains(symbol)) {
                set.remove(symbol)
            } else {
                set.add(symbol)
            }
            state.copy(favoriteSymbols = set)
        }
    }

    fun selectFnoUnderlying(underlying: String) {
        val cleanSym = underlying.trim().uppercase()
        val matchingIndex = _uiState.value.indices.find { it.symbol.equals(cleanSym, ignoreCase = true) }
        val matchingStock = _uiState.value.stocks.find { it.symbol.equals(cleanSym, ignoreCase = true) }
        val spotPrice = matchingIndex?.value ?: matchingStock?.ltp
        val chain = MarketDataRepository.getOptionChain(cleanSym, spotPrice)
        _uiState.update {
            it.copy(selectedFnoUnderlying = cleanSym, optionChain = chain)
        }
    }

    /**
     * Direct navigation to any F&O stock's option chain
     */
    fun openStockOptionChain(symbol: String) {
        selectFnoUnderlying(symbol)
        _uiState.update { it.copy(selectedFnoSubTab = 1) } // Switch directly to Option Chain tab
        setTab(2) // Navigate to F&O Desk
    }

    fun setFnoSubTab(subTab: Int) {
        _uiState.update { it.copy(selectedFnoSubTab = subTab) }
    }

    fun setWatchlistFilter(filter: String) {
        _uiState.update { it.copy(watchlistFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setTab(index: Int) {
        _uiState.update { state ->
            val prev = if (index != state.currentTab && state.currentTab != 6) state.currentTab else state.previousTab
            state.copy(currentTab = index, previousTab = prev)
        }
    }

    fun navigateBack() {
        _uiState.update { state ->
            val target = if (state.currentTab == 6) state.previousTab else 0
            state.copy(currentTab = target)
        }
    }

    fun setFnoScannerFilter(filter: FnoScannerFilter) {
        _uiState.update { it.copy(selectedFnoScannerFilter = filter) }
    }

    fun setFnoScannerSearchQuery(query: String) {
        _uiState.update { it.copy(fnoScannerSearchQuery = query) }
    }

    fun setFnoScannerSectorFilter(sector: String) {
        _uiState.update { it.copy(fnoScannerSectorFilter = sector) }
    }

    fun syncFnoUniverseMaster() {
        viewModelScope.launch {
            val stats = com.example.data.fno.FnoUniverseRepository.syncWithExchangeMaster()
            _uiState.update { it.copy(fnoSyncStats = stats) }
        }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun openOrderDialog(stock: StockQuote, type: OrderType) {
        _uiState.update {
            it.copy(
                showOrderDialog = true,
                orderDialogStock = stock,
                orderDialogType = type
            )
        }
    }

    fun dismissOrderDialog() {
        _uiState.update { it.copy(showOrderDialog = false) }
    }

    fun openSearchDialog() {
        _uiState.update { it.copy(isSearchDialogOpen = true, onlineSearchResult = null) }
    }

    fun dismissSearchDialog() {
        _uiState.update { it.copy(isSearchDialogOpen = false, isSearchingOnline = false) }
    }

    fun setSearchCategory(category: String) {
        _uiState.update { it.copy(searchCategory = category) }
    }

    fun searchOnline(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingOnline = true) }
            val fetched = NseMarketRepository.fetchDynamicStock(query)
            _uiState.update { state ->
                val updatedStocks = if (fetched != null && state.stocks.none { it.symbol.equals(fetched.symbol, ignoreCase = true) }) {
                    listOf(fetched) + state.stocks
                } else state.stocks
                state.copy(
                    isSearchingOnline = false,
                    onlineSearchResult = fetched,
                    stocks = updatedStocks
                )
            }
        }
    }

    fun openFnoTradeDialog(stock: StockQuote, initialContract: String? = null) {
        _uiState.update {
            it.copy(
                showFnoTradeDialog = true,
                fnoTradeStock = stock,
                fnoTradeInitialContract = initialContract
            )
        }
    }

    fun dismissFnoTradeDialog() {
        _uiState.update { it.copy(showFnoTradeDialog = false) }
    }

    fun addStockToWatchlist(stock: StockQuote) {
        _uiState.update { state ->
            val set = state.favoriteSymbols.toMutableSet()
            set.add(stock.symbol)
            val updatedStocks = if (state.stocks.none { it.symbol.equals(stock.symbol, ignoreCase = true) }) {
                listOf(stock) + state.stocks
            } else state.stocks
            state.copy(favoriteSymbols = set, stocks = updatedStocks)
        }
    }

    fun removeStockFromWatchlist(symbol: String) {
        _uiState.update { state ->
            val set = state.favoriteSymbols.toMutableSet()
            set.remove(symbol)
            state.copy(favoriteSymbols = set)
        }
    }

    fun executeFnoOrder(
        contractSymbol: String,
        type: OrderType,
        quantity: Int,
        price: Double
    ) {
        executePaperOrder(
            symbol = contractSymbol,
            type = type,
            product = ProductType.NRML,
            category = OrderCategory.MARKET,
            quantity = quantity,
            price = price
        )
        _uiState.update {
            it.copy(
                showFnoTradeDialog = false,
                scalpTradeMessage = "F&O Order: ${type.name} $quantity Qty $contractSymbol @ ₹${String.format(Locale.US, "%.2f", price)}"
            )
        }
    }

    fun executePaperOrder(
        symbol: String,
        type: OrderType,
        product: ProductType,
        category: OrderCategory,
        quantity: Int,
        price: Double
    ) {
        val timeFmt = SimpleDateFormat("hh:mm a", Locale.US)
        val order = PaperOrder(
            id = "ORD-${System.currentTimeMillis() % 10000}",
            symbol = symbol,
            type = type,
            product = product,
            category = category,
            quantity = quantity,
            price = price,
            status = OrderStatus.EXECUTED,
            timestamp = System.currentTimeMillis(),
            timeFormatted = timeFmt.format(Date())
        )

        _uiState.update { state ->
            val existingPosIndex = state.portfolio.positions.indexOfFirst {
                it.symbol == symbol && it.product == product
            }

            val updatedPositions = state.portfolio.positions.toMutableList()
            val marginImpact = (quantity * price) / if (product == ProductType.MIS) 5.0 else 1.0

            if (type == OrderType.BUY) {
                if (existingPosIndex >= 0) {
                    val pos = updatedPositions[existingPosIndex]
                    val totalQty = pos.quantity + quantity
                    val totalCost = (pos.buyPrice * pos.quantity) + (price * quantity)
                    val newAvg = totalCost / totalQty
                    updatedPositions[existingPosIndex] = pos.copy(
                        quantity = totalQty,
                        buyPrice = newAvg,
                        currentPrice = price
                    )
                } else {
                    updatedPositions.add(
                        PaperPosition(
                            id = "POS-${System.currentTimeMillis() % 10000}",
                            symbol = symbol,
                            type = type,
                            product = product,
                            quantity = quantity,
                            buyPrice = price,
                            currentPrice = price
                        )
                    )
                }
            } else {
                // SELL order
                if (existingPosIndex >= 0) {
                    val pos = updatedPositions[existingPosIndex]
                    if (pos.quantity <= quantity) {
                        updatedPositions.removeAt(existingPosIndex)
                    } else {
                        updatedPositions[existingPosIndex] = pos.copy(
                            quantity = pos.quantity - quantity
                        )
                    }
                }
            }

            val newOrders = listOf(order) + state.portfolio.orders
            val newAvailable = (state.portfolio.availableMargin - marginImpact).coerceAtLeast(0.0)
            val newUsed = state.portfolio.usedMargin + marginImpact

            state.copy(
                showOrderDialog = false,
                portfolio = state.portfolio.copy(
                    availableMargin = newAvailable,
                    usedMargin = newUsed,
                    positions = updatedPositions,
                    orders = newOrders
                )
            )
        }
    }

    fun squareOffPosition(posId: String) {
        _uiState.update { state ->
            val pos = state.portfolio.positions.find { it.id == posId } ?: return@update state
            val realized = state.portfolio.realizedPnl + pos.pnl
            val releasedMargin = if (pos.product == ProductType.MIS) pos.investedAmount / 5.0 else pos.investedAmount

            val updatedPositions = state.portfolio.positions.filterNot { it.id == posId }
            val timeFmt = SimpleDateFormat("hh:mm a", Locale.US)
            val exitOrder = PaperOrder(
                id = "ORD-${System.currentTimeMillis() % 10000}",
                symbol = pos.symbol,
                type = if (pos.type == OrderType.BUY) OrderType.SELL else OrderType.BUY,
                product = pos.product,
                category = OrderCategory.MARKET,
                quantity = pos.quantity,
                price = pos.currentPrice,
                status = OrderStatus.EXECUTED,
                timestamp = System.currentTimeMillis(),
                timeFormatted = timeFmt.format(Date())
            )

            state.copy(
                portfolio = state.portfolio.copy(
                    availableMargin = state.portfolio.availableMargin + releasedMargin + pos.pnl,
                    usedMargin = (state.portfolio.usedMargin - releasedMargin).coerceAtLeast(0.0),
                    realizedPnl = realized,
                    positions = updatedPositions,
                    orders = listOf(exitOrder) + state.portfolio.orders
                )
            )
        }
    }

    // --- Unlimited Demo Funds Management ---

    fun openAddDemoFundsDialog() {
        _uiState.update { it.copy(showAddDemoFundsDialog = true, demoFundsSuccessMessage = null) }
    }

    fun dismissAddDemoFundsDialog() {
        _uiState.update { it.copy(showAddDemoFundsDialog = false) }
    }

    fun dismissDemoFundsSuccessMessage() {
        _uiState.update { it.copy(demoFundsSuccessMessage = null) }
    }

    /**
     * Credits unlimited virtual capital to user's demo trading account.
     */
    fun addDemoFunds(amount: Double) {
        if (amount <= 0.0) return
        _uiState.update { state ->
            val updatedAvailable = state.portfolio.availableMargin + amount
            val updatedStarting = state.portfolio.startingCapital + amount
            state.copy(
                portfolio = state.portfolio.copy(
                    startingCapital = updatedStarting,
                    availableMargin = updatedAvailable
                ),
                showAddDemoFundsDialog = false,
                demoFundsSuccessMessage = "+₹${String.format(Locale.US, "%,.0f", amount)} Demo Funds Credited! Available: ₹${String.format(Locale.US, "%,.2f", updatedAvailable)}"
            )
        }
    }

    /**
     * Resets demo trading account with initial virtual capital.
     */
    fun resetDemoAccount(initialBalance: Double = 1000000.0) {
        _uiState.update { state ->
            state.copy(
                portfolio = PortfolioSummary(
                    startingCapital = initialBalance,
                    availableMargin = initialBalance,
                    usedMargin = 0.0,
                    realizedPnl = 0.0,
                    positions = emptyList(),
                    orders = emptyList()
                ),
                showAddDemoFundsDialog = false,
                demoFundsSuccessMessage = "Demo trading account reset to ₹${String.format(Locale.US, "%,.0f", initialBalance)} virtual capital."
            )
        }
    }

    // --- Scalping Desk Methods ---

    fun executeQuickScalp(type: OrderType) {
        val currentStock = _uiState.value.selectedStock ?: return
        val baseLot = if (currentStock.lotSize > 1) currentStock.lotSize else 25
        val totalQty = baseLot * _uiState.value.scalpLotMultiplier
        val price = currentStock.ltp

        executePaperOrder(
            symbol = currentStock.symbol,
            type = type,
            product = ProductType.MIS, // Scalping is Intraday 5x
            category = OrderCategory.MARKET,
            quantity = totalQty,
            price = price
        )

        val msg = "⚡ FAST SCALP: ${type.label} $totalQty ${currentStock.symbol} @ ₹${String.format(java.util.Locale.US, "%,.2f", price)} Executed!"
        _uiState.update { it.copy(scalpTradeMessage = msg) }
    }

    fun exitAllScalpPositions() {
        val positions = _uiState.value.portfolio.positions
        if (positions.isEmpty()) return

        positions.forEach { pos ->
            squareOffPosition(pos.id)
        }
        _uiState.update { it.copy(scalpTradeMessage = "🚨 ALL SCALP POSITIONS SQUARED OFF AT MARKET!") }
    }

    fun setScalpLotMultiplier(multiplier: Int) {
        _uiState.update { it.copy(scalpLotMultiplier = multiplier) }
    }

    fun setScalpStopLoss(pts: Double) {
        _uiState.update { it.copy(scalpStopLossPts = pts) }
    }

    fun setScalpTarget(pts: Double) {
        _uiState.update { it.copy(scalpTargetPts = pts) }
    }

    fun setScalpInterval(interval: String) {
        _uiState.update { it.copy(scalpInterval = interval) }
    }

    fun toggleTradingViewMode() {
        _uiState.update { it.copy(isTradingViewActive = !it.isTradingViewActive) }
    }

    fun dismissScalpMessage() {
        _uiState.update { it.copy(scalpTradeMessage = null) }
    }

    // ==========================================
    // 1. Interday Boost Actions
    // ==========================================
    fun setInterdayFilter(filter: InterdayFilter) {
        _uiState.update { it.copy(selectedInterdayFilter = filter) }
    }

    fun setInterdaySearchQuery(query: String) {
        _uiState.update { it.copy(interdaySearchQuery = query) }
    }

    fun setInterdayTimeframe(timeframe: String) {
        _uiState.update { it.copy(selectedInterdayTimeframe = timeframe) }
    }

    // ==========================================
    // 2. Swing Trading Actions
    // ==========================================
    fun setSwingFilter(filter: SwingFilterType) {
        _uiState.update { it.copy(selectedSwingFilter = filter) }
    }

    fun setSwingSearchQuery(query: String) {
        _uiState.update { it.copy(swingSearchQuery = query) }
    }

    fun setSwingTimeframe(timeframe: String) {
        _uiState.update { state ->
            val refreshedSetups = com.example.data.analytics.TradingAnalyticsEngine.generateSwingSetups(state.stocks, timeframe)
            state.copy(selectedSwingTimeframe = timeframe, swingSetups = refreshedSetups)
        }
    }

    // ==========================================
    // 3. OI Clock Actions
    // ==========================================
    fun selectOiClockUnderlying(underlying: String) {
        _uiState.update { state ->
            val data = com.example.data.analytics.TradingAnalyticsEngine.generateOiClock(underlying)
            state.copy(selectedOiClockUnderlying = underlying, oiClockData = data)
        }
    }

    fun refreshOiClock() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOiClockRefreshing = true) }
            delay(350)
            val underlying = _uiState.value.selectedOiClockUnderlying
            val data = com.example.data.analytics.TradingAnalyticsEngine.generateOiClock(underlying)
            _uiState.update { it.copy(oiClockData = data, isOiClockRefreshing = false) }
        }
    }

    // ==========================================
    // 4. FII / DII Actions
    // ==========================================
    fun setFiiDiiTab(tab: Int) {
        _uiState.update { it.copy(selectedFiiDiiTab = tab) }
    }
}

