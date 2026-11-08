package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderType
import com.example.data.model.StockQuote
import com.example.ui.ai.AiChatViewModel
import com.example.ui.components.AddDemoFundsDialog
import com.example.ui.components.FnoTradeDialog
import com.example.ui.components.MarketTickerTape
import com.example.ui.components.OrderDialog
import com.example.ui.components.ProviderSettingsDialog
import com.example.ui.components.SearchStocksDialog
import com.example.ui.screens.*
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MarketViewModel,
    aiViewModel: AiChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle system back navigation (returns to previous tab when on details, else dashboard)
    BackHandler(enabled = uiState.currentTab != 0) {
        viewModel.navigateBack()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 680.dp

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Trading Terminal Bull & Trend Emblem
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .border(1.dp, MarketGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = MarketGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "INDIA MARKET PRO",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(MarketGreen)
                                        )
                                        Text(
                                            text = "LIVE NSE MARKET & TRADINGVIEW",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MarketGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        actions = {
                            // Global Search Button
                            IconButton(
                                onClick = { viewModel.openSearchDialog() },
                                modifier = Modifier.testTag("global_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search any stock or F&O",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Automatic 3-Second Real-Time Sync Indicator Badge (No manual button)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(MarketGreen)
                                    )
                                    Text(
                                        text = "LIVE 3s ⚡",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MarketGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Quick AI Copilot Action Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (uiState.currentTab == 5) MarketGold else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clickable { viewModel.setTab(5) }
                                    .testTag("topbar_ai_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = "AI Stock Copilot",
                                        tint = if (uiState.currentTab == 5) androidx.compose.ui.graphics.Color.Black else MarketGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "AI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (uiState.currentTab == 5) androidx.compose.ui.graphics.Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // Quick Jump to Sector Scope & Live Heatmap
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (uiState.currentTab == 7) MarketGreen else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clickable { viewModel.setTab(7) }
                                    .testTag("topbar_sectors_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.GridView,
                                        contentDescription = "360° Sector Scope & Live Heatmap",
                                        tint = if (uiState.currentTab == 7) androidx.compose.ui.graphics.Color.Black else MarketGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "HEATMAP",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (uiState.currentTab == 7) androidx.compose.ui.graphics.Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // Quick Jump to Scalper Terminal Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (uiState.currentTab == 1) MarketGreen else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clickable { viewModel.setTab(1) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (uiState.currentTab == 1) androidx.compose.ui.graphics.Color.Black else MarketGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "SCALP",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (uiState.currentTab == 1) androidx.compose.ui.graphics.Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // Data Provider & Licensed Gateway Settings Button
                            IconButton(
                                onClick = { viewModel.openProviderSettingsDialog() },
                                modifier = Modifier.testTag("provider_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Licensed Data Provider Settings",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Theme toggle button
                            IconButton(
                                onClick = { viewModel.toggleTheme() },
                                modifier = Modifier.testTag("theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = if (uiState.isDarkMode) MarketGold else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Live Indices Ticker Tape
                    MarketTickerTape(
                        indices = uiState.indices,
                        recentTickFlash = uiState.recentTickFlash,
                        onIndexClick = { index ->
                            viewModel.selectIndexForChart(index)
                            viewModel.setTab(1)
                        }
                    )

                    // Quick Pro Navigation Bar (1-Tap Direct Access to All Desks)
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val proNavPills = listOf(
                                Triple(0, "Dashboard", Icons.Default.Dashboard),
                                Triple(8, "Boost ⚡", Icons.Default.Bolt),
                                Triple(9, "Swing 📈", Icons.Default.Timeline),
                                Triple(10, "OI Clock ⏱️", Icons.Default.AccessTime),
                                Triple(11, "FII / DII 🏛️", Icons.Default.AccountBalance),
                                Triple(7, "Heatmap 🔥", Icons.Default.GridView),
                                Triple(2, "F&O Desk 📊", Icons.Default.Analytics),
                                Triple(1, "Scalp ⚡", Icons.Default.ElectricBolt),
                                Triple(3, "Watchlist ⭐", Icons.Default.FormatListBulleted),
                                Triple(4, "Portfolio 💼", Icons.Default.AccountBalanceWallet),
                                Triple(5, "AI Copilot 🤖", Icons.Default.AutoAwesome)
                            )

                            proNavPills.forEach { (tabId, label, icon) ->
                                val isSelected = uiState.currentTab == tabId
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) MarketGreen else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MarketGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .clickable { viewModel.setTab(tabId) }
                                        .testTag("nav_pill_$tabId")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        NavigationBarItem(
                            selected = uiState.currentTab == 0,
                            onClick = { viewModel.setTab(0) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                    contentDescription = "Dashboard"
                                )
                            },
                            label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("tab_dashboard")
                        )

                        NavigationBarItem(
                            selected = uiState.currentTab == 7,
                            onClick = { viewModel.setTab(7) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == 7) Icons.Default.GridView else Icons.Outlined.GridView,
                                    contentDescription = "360° Sector Scope & Live Heatmap"
                                )
                            },
                            label = { Text("Heatmap", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("tab_sectors")
                        )

                        NavigationBarItem(
                            selected = uiState.currentTab == 2,
                            onClick = { viewModel.setTab(2) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == 2) Icons.Default.Analytics else Icons.Outlined.Analytics,
                                    contentDescription = "F&O Desk"
                                )
                            },
                            label = { Text("F&O Desk", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("tab_fno")
                        )

                        NavigationBarItem(
                            selected = uiState.currentTab == 3,
                            onClick = { viewModel.setTab(3) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == 3) Icons.Default.FormatListBulleted else Icons.Outlined.FormatListBulleted,
                                    contentDescription = "Watchlist"
                                )
                            },
                            label = { Text("Watchlist", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("tab_watchlist")
                        )

                        NavigationBarItem(
                            selected = uiState.currentTab == 4,
                            onClick = { viewModel.setTab(4) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == 4) Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = "Portfolio"
                                )
                            },
                            label = { Text("Portfolio", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("tab_portfolio")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Wide Screen Navigation Rail (For Tablets & Foldables)
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        NavigationRailItem(
                            selected = uiState.currentTab == 0,
                            onClick = { viewModel.setTab(0) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 1,
                            onClick = { viewModel.setTab(1) },
                            icon = { Icon(Icons.Default.Bolt, contentDescription = "Scalper") },
                            label = { Text("Scalper") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 2,
                            onClick = { viewModel.setTab(2) },
                            icon = { Icon(Icons.Default.Analytics, contentDescription = "F&O Desk") },
                            label = { Text("F&O Desk") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 7,
                            onClick = { viewModel.setTab(7) },
                            icon = { Icon(Icons.Default.GridView, contentDescription = "Sectors & Heatmap") },
                            label = { Text("Sectors") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 8,
                            onClick = { viewModel.setTab(8) },
                            icon = { Icon(Icons.Default.Bolt, contentDescription = "Interday Boost") },
                            label = { Text("Boost") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 9,
                            onClick = { viewModel.setTab(9) },
                            icon = { Icon(Icons.Default.Timeline, contentDescription = "Swing Trading") },
                            label = { Text("Swing") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 10,
                            onClick = { viewModel.setTab(10) },
                            icon = { Icon(Icons.Default.AccessTime, contentDescription = "OI Clock") },
                            label = { Text("OI Clock") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 11,
                            onClick = { viewModel.setTab(11) },
                            icon = { Icon(Icons.Default.AccountBalance, contentDescription = "FII/DII") },
                            label = { Text("FII/DII") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 3,
                            onClick = { viewModel.setTab(3) },
                            icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = "Watchlist") },
                            label = { Text("Watchlist") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 5,
                            onClick = { viewModel.setTab(5) },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Advisor") },
                            label = { Text("AI Copilot") }
                        )
                        NavigationRailItem(
                            selected = uiState.currentTab == 4,
                            onClick = { viewModel.setTab(4) },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Portfolio") },
                            label = { Text("Portfolio") }
                        )
                    }
                }

                // Main Screen Content Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (uiState.currentTab) {
                        0 -> DashboardScreen(
                            uiState = uiState,
                            onIndexClick = { index ->
                                viewModel.selectIndexForChart(index)
                                viewModel.setTab(1)
                            },
                            onStockClick = { stock ->
                                viewModel.selectStock(stock)
                                viewModel.setTab(6)
                            },
                            onViewAllStocksClick = { viewModel.setTab(3) },
                            onRefreshLiveMarket = { viewModel.syncRealMarketData() },
                            onOpenSearch = { viewModel.openSearchDialog() },
                            onOpenSectorScope = { viewModel.setTab(7) },
                            onOpenProviderSettings = { viewModel.openProviderSettingsDialog() },
                            onOpenAiChat = { viewModel.setTab(5) },
                            onAskAiStock = { stock ->
                                aiViewModel.requestStockAnalysis(stock.symbol, stock.ltp, stock.changePercent)
                                viewModel.setTab(5)
                            },
                            onOpenInterdayBoost = { viewModel.setTab(8) },
                            onOpenSwingTrading = { viewModel.setTab(9) },
                            onOpenOiClock = { viewModel.setTab(10) },
                            onOpenFiiDii = { viewModel.setTab(11) }
                        )
                        1 -> ScalpingTerminalScreen(
                            uiState = uiState,
                            onSelectStock = { stock -> viewModel.selectStock(stock) },
                            onQuickScalp = { type -> viewModel.executeQuickScalp(type) },
                            onExitAllScalp = { viewModel.exitAllScalpPositions() },
                            onSquareOffSingle = { id -> viewModel.squareOffPosition(id) },
                            onSetLotMultiplier = { mult -> viewModel.setScalpLotMultiplier(mult) },
                            onSetStopLoss = { pts -> viewModel.setScalpStopLoss(pts) },
                            onSetTarget = { pts -> viewModel.setScalpTarget(pts) },
                            onSetInterval = { interval -> viewModel.setScalpInterval(interval) },
                            onToggleChartType = { viewModel.toggleTradingViewMode() },
                            onDismissMessage = { viewModel.dismissScalpMessage() },
                            onAskAi = { stock ->
                                aiViewModel.requestStockAnalysis(stock.symbol, stock.ltp, stock.changePercent)
                                viewModel.setTab(5)
                            },
                            onOpenAddFunds = { viewModel.openAddDemoFundsDialog() }
                        )
                        2 -> FnoScreen(
                            uiState = uiState,
                            onUnderlyingSelected = { sym -> viewModel.selectFnoUnderlying(sym) },
                            onStrikeSelected = { strike, isCall ->
                                val contractSymbol = "${uiState.selectedFnoUnderlying} ${strike.strikePrice.toInt()} ${if (isCall) "CE" else "PE"}"
                                val premium = if (isCall) strike.callLtp else strike.putLtp
                                val dummyQuote = StockQuote(
                                    symbol = contractSymbol,
                                    name = "${uiState.selectedFnoUnderlying} Weekly Option",
                                    ltp = premium,
                                    change = if (isCall) strike.callChange else strike.putChange,
                                    changePercent = 4.5,
                                    open = premium * 0.95,
                                    high = premium * 1.2,
                                    low = premium * 0.85,
                                    prevClose = premium * 0.92,
                                    volume = 152000L,
                                    turnoverCr = 45.0,
                                    sector = "Options",
                                    marketCapCr = 0.0,
                                    peRatio = 0.0,
                                    isFnO = true,
                                    lotSize = when (uiState.selectedFnoUnderlying) {
                                        "BANK NIFTY" -> 15
                                        "NIFTY 50", "FINNIFTY" -> 25
                                        else -> uiState.stocks.find { it.symbol.equals(uiState.selectedFnoUnderlying, ignoreCase = true) }?.lotSize ?: 250
                                    },
                                    yearHigh = premium * 3.0,
                                    yearLow = premium * 0.1
                                )
                                viewModel.openOrderDialog(dummyQuote, OrderType.BUY)
                            },
                            onStockClick = { symbol ->
                                val stock = uiState.stocks.find { it.symbol == symbol }
                                if (stock != null) {
                                    viewModel.selectStock(stock)
                                    viewModel.setTab(6)
                                }
                            },
                            onTradeStock = { stock, type -> viewModel.openOrderDialog(stock, type) },
                            onTradeFno = { stock -> viewModel.openFnoTradeDialog(stock) },
                            onScannerFilterChange = { filter -> viewModel.setFnoScannerFilter(filter) },
                            onScannerSearchChange = { query -> viewModel.setFnoScannerSearchQuery(query) },
                            onScannerSectorChange = { sector -> viewModel.setFnoScannerSectorFilter(sector) },
                            onRefreshMaster = { viewModel.syncFnoUniverseMaster() },
                            onFnoSubTabChange = { tab -> viewModel.setFnoSubTab(tab) }
                        )
                        3 -> WatchlistScreen(
                            uiState = uiState,
                            onStockClick = { stock ->
                                viewModel.selectStock(stock)
                                viewModel.setTab(6)
                            },
                            onFilterChange = { filter -> viewModel.setWatchlistFilter(filter) },
                            onSearchChange = { query -> viewModel.setSearchQuery(query) },
                            onToggleFavorite = { symbol -> viewModel.toggleFavorite(symbol) },
                            onQuickTrade = { stock, type -> viewModel.openOrderDialog(stock, type) },
                            onOpenSearchDialog = { viewModel.openSearchDialog() },
                            onTradeFno = { stock -> viewModel.openFnoTradeDialog(stock) }
                        )
                        4 -> PortfolioScreen(
                            uiState = uiState,
                            onSquareOff = { posId -> viewModel.squareOffPosition(posId) },
                            onExploreWatchlist = { viewModel.setTab(3) },
                            onOpenAddFundsDialog = { viewModel.openAddDemoFundsDialog() },
                            onAddDemoFunds = { amt -> viewModel.addDemoFunds(amt) },
                            onResetAccount = { viewModel.resetDemoAccount() }
                        )
                        5 -> AiChatScreen(
                            aiViewModel = aiViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                        6 -> StockDetailScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateBack() },
                            onTimeframeSelect = { tf -> viewModel.selectTimeframe(tf) },
                            onToggleFavorite = { sym -> viewModel.toggleFavorite(sym) },
                            onToggleTradingView = { viewModel.toggleTradingViewMode() },
                            onOpenOrder = { stock, type -> viewModel.openOrderDialog(stock, type) },
                            onAskAi = { stock ->
                                aiViewModel.requestStockAnalysis(stock.symbol, stock.ltp, stock.changePercent)
                                viewModel.setTab(5)
                            }
                        )
                        7 -> SectorScopeScreen(
                            uiState = uiState,
                            onStockClick = { stock ->
                                viewModel.selectStock(stock)
                                viewModel.setTab(6)
                            },
                            onTradeStock = { stock, type ->
                                viewModel.openOrderDialog(stock, type)
                            },
                            onTradeFno = { stock ->
                                viewModel.openFnoTradeDialog(stock)
                            },
                            onOpenOptionChain = { sym ->
                                viewModel.openStockOptionChain(sym)
                            }
                        )
                        8 -> InterdayBoostScreen(
                            uiState = uiState,
                            onFilterChange = { viewModel.setInterdayFilter(it) },
                            onSearchChange = { viewModel.setInterdaySearchQuery(it) },
                            onTimeframeChange = { viewModel.setInterdayTimeframe(it) },
                            onStockClick = { stock ->
                                viewModel.selectStock(stock)
                                viewModel.setTab(6)
                            },
                            onTradeStock = { stock, type -> viewModel.openOrderDialog(stock, type) },
                            onTradeFno = { stock -> viewModel.openFnoTradeDialog(stock) }
                        )
                        9 -> SwingTradingScreen(
                            uiState = uiState,
                            onFilterChange = { viewModel.setSwingFilter(it) },
                            onSearchChange = { viewModel.setSwingSearchQuery(it) },
                            onTimeframeChange = { viewModel.setSwingTimeframe(it) },
                            onStockClick = { stock ->
                                viewModel.selectStock(stock)
                                viewModel.setTab(6)
                            },
                            onTradeStock = { stock, type -> viewModel.openOrderDialog(stock, type) },
                            onTradeFno = { stock -> viewModel.openFnoTradeDialog(stock) }
                        )
                        10 -> OiClockScreen(
                            uiState = uiState,
                            onSelectUnderlying = { viewModel.selectOiClockUnderlying(it) },
                            onRefresh = { viewModel.refreshOiClock() },
                            onStrikeSelected = { strike, isCall ->
                                val contractSymbol = "${uiState.selectedOiClockUnderlying} ${strike.strikePrice.toInt()} ${if (isCall) "CE" else "PE"}"
                                val premium = if (isCall) strike.callLtp else strike.putLtp
                                val dummyQuote = StockQuote(
                                    symbol = contractSymbol,
                                    name = "${uiState.selectedOiClockUnderlying} Option Contract",
                                    ltp = premium,
                                    change = if (isCall) strike.callChange else strike.putChange,
                                    changePercent = 4.2,
                                    open = premium * 0.95,
                                    high = premium * 1.25,
                                    low = premium * 0.85,
                                    prevClose = premium * 0.92,
                                    volume = 240000L,
                                    turnoverCr = 58.0,
                                    sector = "Options",
                                    marketCapCr = 0.0,
                                    peRatio = 0.0,
                                    isFnO = true,
                                    lotSize = when (uiState.selectedOiClockUnderlying) {
                                        "BANK NIFTY" -> 15
                                        else -> 25
                                    },
                                    yearHigh = premium * 3.0,
                                    yearLow = premium * 0.1
                                )
                                viewModel.openOrderDialog(dummyQuote, OrderType.BUY)
                            }
                        )
                        11 -> FiiDiiScreen(
                            uiState = uiState,
                            onTabChange = { viewModel.setFiiDiiTab(it) }
                        )
                    }
                }
            }
        }

        // Global Search Dialog Overlay (Search Any Stock or F&O, Toggle Watchlist)
        if (uiState.isSearchDialogOpen) {
            SearchStocksDialog(
                allStocks = uiState.stocks,
                allIndices = uiState.indices,
                favoriteSymbols = uiState.favoriteSymbols,
                isSearchingOnline = uiState.isSearchingOnline,
                onlineSearchResult = uiState.onlineSearchResult,
                onDismiss = { viewModel.dismissSearchDialog() },
                onSelectStock = { stock ->
                    viewModel.selectStock(stock)
                    viewModel.setTab(6)
                },
                onSelectIndex = { index ->
                    viewModel.selectIndexForChart(index)
                    viewModel.setTab(1)
                },
                onToggleWatchlist = { symbol ->
                    viewModel.toggleFavorite(symbol)
                },
                onTradeEquity = { stock, type ->
                    viewModel.openOrderDialog(stock, type)
                },
                onTradeFno = { stock ->
                    viewModel.openFnoTradeDialog(stock)
                },
                onSearchOnline = { query ->
                    viewModel.searchOnline(query)
                }
            )
        }

        // F&O Options Trade Dialog Overlay
        if (uiState.showFnoTradeDialog && uiState.fnoTradeStock != null) {
            FnoTradeDialog(
                stock = uiState.fnoTradeStock!!,
                initialContract = uiState.fnoTradeInitialContract,
                availableMargin = uiState.portfolio.availableMargin,
                onDismiss = { viewModel.dismissFnoTradeDialog() },
                onExecuteFnoTrade = { contractSymbol, type, quantity, price ->
                    viewModel.executeFnoOrder(contractSymbol, type, quantity, price)
                },
                onAddDemoFunds = { amt -> viewModel.addDemoFunds(amt) }
            )
        }

        // Data Provider & Licensed Gateway Settings Overlay
        if (uiState.showProviderSettingsDialog) {
            ProviderSettingsDialog(
                currentConfig = uiState.providerConfig,
                feedHealth = uiState.feedHealth,
                marketSchedule = uiState.marketSchedule,
                onDismiss = { viewModel.dismissProviderSettingsDialog() },
                onSaveConfig = { newConfig ->
                    viewModel.saveProviderConfig(newConfig)
                },
                onTestConnection = {
                    viewModel.testProviderConnection()
                }
            )
        }

        // Paper Trading Order Dialog Overlay
        if (uiState.showOrderDialog && uiState.orderDialogStock != null) {
            OrderDialog(
                stock = uiState.orderDialogStock!!,
                initialType = uiState.orderDialogType,
                availableMargin = uiState.portfolio.availableMargin,
                onDismiss = { viewModel.dismissOrderDialog() },
                onExecuteOrder = { symbol, type, product, category, quantity, price ->
                    viewModel.executePaperOrder(symbol, type, product, category, quantity, price)
                },
                onAddDemoFunds = { amt -> viewModel.addDemoFunds(amt) }
            )
        }

        // Unlimited Demo Funds Top-Up Dialog
        if (uiState.showAddDemoFundsDialog) {
            AddDemoFundsDialog(
                currentAvailableFunds = uiState.portfolio.availableMargin,
                onDismiss = { viewModel.dismissAddDemoFundsDialog() },
                onAddFunds = { amt -> viewModel.addDemoFunds(amt) },
                onResetAccount = { amt -> viewModel.resetDemoAccount(amt) }
            )
        }
    }
}
