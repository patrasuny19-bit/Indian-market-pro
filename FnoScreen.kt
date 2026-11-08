package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.fno.FnoUniverseRepository
import com.example.data.model.*
import com.example.ui.MarketUiState
import com.example.ui.components.OptionChainTable
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FnoScreen(
    uiState: MarketUiState,
    onUnderlyingSelected: (String) -> Unit,
    onStrikeSelected: (OptionStrike, Boolean) -> Unit,
    onStockClick: (String) -> Unit,
    onTradeStock: (StockQuote, OrderType) -> Unit = { _, _ -> },
    onTradeFno: (StockQuote) -> Unit = {},
    onScannerFilterChange: (FnoScannerFilter) -> Unit = {},
    onScannerSearchChange: (String) -> Unit = {},
    onScannerSectorChange: (String) -> Unit = {},
    onRefreshMaster: () -> Unit = {},
    onFnoSubTabChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedTab = uiState.selectedFnoSubTab
    var optionChainSearchQuery by remember { mutableStateOf("") }

    // Popular Underlyings: Benchmark Indices + Major NSE F&O Stocks
    val quickUnderlyings = remember {
        listOf(
            "NIFTY 50", "BANK NIFTY", "FINNIFTY",
            "RELIANCE", "HDFCBANK", "TCS", "INFY", "TATAMOTORS",
            "SBIN", "ICICIBANK", "BHARTIARTL", "ZOMATO", "ITC",
            "BAJFINANCE", "MARUTI", "LT", "AXISBANK", "KOTAKBANK"
        )
    }

    // Filtered stocks using full F&O universe scanner logic
    val scannedStocks = remember(
        uiState.stocks,
        uiState.selectedFnoScannerFilter,
        uiState.fnoScannerSearchQuery,
        uiState.fnoScannerSectorFilter
    ) {
        FnoUniverseRepository.scanFnoStocks(
            stocks = uiState.stocks,
            filter = uiState.selectedFnoScannerFilter,
            searchQuery = uiState.fnoScannerSearchQuery,
            sectorFilter = uiState.fnoScannerSectorFilter
        )
    }

    val availableSectors = remember {
        listOf("All Sectors") + SectorClassification.PRIMARY_SECTORS
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("fno_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Header with Universe Master Stats & Auto-Sync Status ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fno_desk_header_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = MarketGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "NSE DERIVATIVES & F&O UNIVERSE",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${uiState.fnoSyncStats.totalEligibleStocks} Eligible Stocks • All Option Chains Available",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Automatic 3-Second Sync Live Badge (No manual button)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(MarketGreen)
                                )
                                Text(
                                    text = "AUTO 3s ⚡",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MarketGreen,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Status & Sync Banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.fnoSyncStats.isMasterFresh) MarketGreen else MarketGold)
                                )
                                Text(
                                    text = uiState.fnoSyncStats.lastSyncTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "LOT SIZE REVISED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Main Navigation Tabs: Scanner | Option Chain | OI Buildup | Strategy ---
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).testTag("fno_sub_tab_row")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onFnoSubTabChange(0) },
                    text = { Text("F&O Scanner", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onFnoSubTabChange(1) },
                    text = { Text("Option Chain", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { onFnoSubTabChange(2) },
                    text = { Text("OI Buildup", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { onFnoSubTabChange(3) },
                    text = { Text("Strategy Desk", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // ==========================================
                // TAB 0: COMPLETE F&O STOCK SCANNER
                // ==========================================

                // Search Bar across full F&O Universe
                item {
                    OutlinedTextField(
                        value = uiState.fnoScannerSearchQuery,
                        onValueChange = onScannerSearchChange,
                        placeholder = {
                            Text(
                                "Search by Symbol, Company, Sector, or Token ID...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MarketGreen)
                        },
                        trailingIcon = {
                            if (uiState.fnoScannerSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { onScannerSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fno_scanner_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MarketGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }

                // Quantitative Scanner Filters (Horizontally scrollable)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FnoScannerFilter.values().forEach { filter ->
                            val isSelected = filter == uiState.selectedFnoScannerFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = { onScannerFilterChange(filter) },
                                label = {
                                    Text(
                                        text = filter.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    when (filter) {
                                        FnoScannerFilter.ALL_FNO -> Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                                        FnoScannerFilter.TOP_GAINERS -> Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(14.dp))
                                        FnoScannerFilter.TOP_LOSERS -> Icon(Icons.Default.TrendingDown, contentDescription = null, tint = MarketRed, modifier = Modifier.size(14.dp))
                                        FnoScannerFilter.HIGH_VOLUME -> Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(14.dp))
                                        FnoScannerFilter.HIGH_OI_CHANGE -> Icon(Icons.Default.BarChart, contentDescription = null, tint = MarketCyan, modifier = Modifier.size(14.dp))
                                        FnoScannerFilter.BULLISH_MOMENTUM -> Icon(Icons.Default.NorthEast, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(14.dp))
                                        FnoScannerFilter.BEARISH_MOMENTUM -> Icon(Icons.Default.SouthEast, contentDescription = null, tint = MarketRed, modifier = Modifier.size(14.dp))
                                        FnoScannerFilter.BREAKOUT_STOCKS -> Icon(Icons.Default.FlashOn, contentDescription = null, tint = MarketGold, modifier = Modifier.size(14.dp))
                                        FnoScannerFilter.BREAKDOWN_STOCKS -> Icon(Icons.Default.Warning, contentDescription = null, tint = MarketRed, modifier = Modifier.size(14.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Sector Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableSectors.forEach { sectorName ->
                            val isSelected = sectorName == uiState.fnoScannerSectorFilter
                            SuggestionChip(
                                onClick = { onScannerSectorChange(sectorName) },
                                label = {
                                    Text(
                                        text = sectorName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    enabled = true
                                )
                            )
                        }
                    }
                }

                // Results Summary Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Showing ${scannedStocks.size} NSE F&O Stocks",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.selectedFnoScannerFilter.description,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MarketGreen
                        )
                    }
                }

                // Full Stock Cards List with direct Option Chain access
                items(scannedStocks, key = { it.symbol }) { stock ->
                    FnoStockScannerCard(
                        stock = stock,
                        onCardClick = { onStockClick(stock.symbol) },
                        onTradeClick = { type -> onTradeStock(stock, type) },
                        onFnoTradeClick = { onTradeFno(stock) },
                        onOptionChainClick = {
                            onUnderlyingSelected(stock.symbol)
                            onFnoSubTabChange(1)
                        }
                    )
                }
            }

            1 -> {
                // ==========================================
                // TAB 1: ALL NSE F&O STOCKS & INDICES OPTION CHAIN
                // ==========================================

                // Quick Underlying Selector Chips (Horizontal Scroll)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "SELECT UNDERLYING (INDICES & TOP F&O STOCKS)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickUnderlyings.forEach { sym ->
                                val isSelected = sym.equals(uiState.selectedFnoUnderlying, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onUnderlyingSelected(sym) },
                                    label = {
                                        Text(
                                            text = sym,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                }

                // Search any of 182 NSE F&O Stocks
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = optionChainSearchQuery,
                            onValueChange = { optionChainSearchQuery = it },
                            placeholder = {
                                Text("Search any of 182 NSE F&O Stocks (e.g. RELIANCE, TCS, MARUTI)...", fontSize = 12.sp)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MarketGreen, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (optionChainSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { optionChainSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("option_chain_search_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MarketGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )

                        // If user is searching, show matching F&O stock chips
                        if (optionChainSearchQuery.isNotBlank()) {
                            val matches = uiState.stocks.filter {
                                it.symbol.contains(optionChainSearchQuery.trim(), ignoreCase = true) ||
                                it.name.contains(optionChainSearchQuery.trim(), ignoreCase = true)
                            }.take(8)

                            if (matches.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    matches.forEach { matchStock ->
                                        SuggestionChip(
                                            onClick = {
                                                onUnderlyingSelected(matchStock.symbol)
                                                optionChainSearchQuery = ""
                                            },
                                            label = {
                                                Text(
                                                    "${matchStock.symbol} (₹${matchStock.ltp.toInt()})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Underlying Live Details & Derivatives Metrics Banner
                item {
                    val chain = uiState.optionChain
                    if (chain != null) {
                        val matchingStock = uiState.stocks.find { it.symbol.equals(chain.underlying, ignoreCase = true) }
                        val matchingIndex = uiState.indices.find { it.symbol.equals(chain.underlying, ignoreCase = true) }
                        val isIndex = matchingIndex != null
                        val changePct = matchingIndex?.changePercent ?: matchingStock?.changePercent ?: 0.0
                        val isPos = changePct >= 0
                        val accentColor = if (isPos) MarketGreen else MarketRed

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = chain.underlying,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (isIndex) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                Text(
                                                    text = if (isIndex) "INDEX OPTION" else "EQUITY F&O OPTION",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = matchingIndex?.name ?: matchingStock?.name ?: "NSE Derivative Contract",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${String.format(Locale.US, "%,.2f", chain.spotPrice)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", changePct)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                // Derivatives Stats Grid: PCR, Max Pain, ATM Strike, Lot Size
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("PUT/CALL RATIO", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("${chain.pcr}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                                            Text(
                                                text = if (chain.pcr >= 1.0) "BULLISH" else "BEARISH",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (chain.pcr >= 1.0) MarketGreen else MarketRed
                                            )
                                        }
                                    }

                                    Column {
                                        Text("MAX PAIN", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("₹${chain.maxPainStrike.toInt()}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                                    }

                                    Column {
                                        Text("ATM STRIKE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("₹${chain.atmStrike.toInt()}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MarketGold)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("LOT SIZE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        val lot = matchingStock?.lotSize ?: when (chain.underlying) {
                                            "BANK NIFTY" -> 15
                                            else -> 25
                                        }
                                        Text("$lot shares", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                                    }
                                }

                                // Expiries selector
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    chain.expiries.take(3).forEach { exp ->
                                        val isExpSelected = exp == chain.selectedExpiry
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isExpSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isExpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = exp,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isExpSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 10.sp,
                                                color = if (isExpSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(vertical = 5.dp),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    uiState.optionChain?.let { chain ->
                        OptionChainTable(
                            optionChain = chain,
                            onStrikeSelected = onStrikeSelected
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "💡 Tip: Tap any Call or Put LTP box to open the quick trade ticket for that strike.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            2 -> {
                // ==========================================
                // TAB 2: OI BUILDUP
                // ==========================================
                item {
                    Text(
                        text = "OPEN INTEREST (OI) ANALYSIS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(uiState.fnoBuildups) { item ->
                    val typeColor = when (item.type) {
                        BuildupType.LONG_BUILDUP -> MarketGreen
                        BuildupType.SHORT_BUILDUP -> MarketRed
                        BuildupType.SHORT_COVERING -> MarketCyan
                        BuildupType.LONG_UNWINDING -> MarketGold
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { onStockClick(item.symbol) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = item.symbol,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = item.sector,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "LTP: ₹${String.format(Locale.US, "%,.2f", item.ltp)} (${if (item.priceChangePercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", item.priceChangePercent)}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.priceChangePercent >= 0) MarketGreen else MarketRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = typeColor.copy(alpha = 0.16f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = item.type.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = typeColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = "OI Chg: ${if (item.oiChangePercent >= 0) "+" else ""}${item.oiChangePercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            3 -> {
                // ==========================================
                // TAB 3: STRATEGY DESK
                // ==========================================
                item {
                    StrategyDeskView()
                }
            }
        }
    }
}

/**
 * High-density Stock Card for the F&O Scanner
 */
@Composable
fun FnoStockScannerCard(
    stock: StockQuote,
    onCardClick: () -> Unit,
    onTradeClick: (OrderType) -> Unit,
    onFnoTradeClick: () -> Unit,
    onOptionChainClick: () -> Unit = {}
) {
    val isPos = stock.isPositive
    val accentColor = if (isPos) MarketGreen else MarketRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onCardClick)
            .testTag("fno_stock_card_${stock.symbol}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: Symbol + Sector + Lot Size + LTP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stock.symbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Lot ${stock.lotSize}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }

                        if (stock.instrumentToken > 0) {
                            Text(
                                text = "#${stock.instrumentToken}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Text(
                        text = stock.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price and Percentage Change
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format(Locale.US, "%,.2f", stock.ltp)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isPos) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", stock.change)} (${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", stock.changePercent)}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Row 2: Metrics Grid (VWAP, Volume, Day Range, Bid/Ask)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("VWAP", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${String.format(Locale.US, "%,.2f", stock.effectiveVwap)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }

                Column {
                    Text("VOLUME", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val volStr = if (stock.volume > 10000000) "${String.format(Locale.US, "%.1f", stock.volume / 10000000.0)}Cr"
                    else if (stock.volume > 100000) "${String.format(Locale.US, "%.1f", stock.volume / 100000.0)}L"
                    else "${stock.volume}"
                    Text(volStr, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }

                Column {
                    Text("RANGE (L - H)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${stock.low.toInt()} - ₹${stock.high.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }

                Column {
                    Text("SECTOR", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(SectorClassification.standardizeSector(stock.sector), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }

            // Row 3: Action Bar (BUY, SELL, OPT CHAIN, F&O TRADE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onTradeClick(OrderType.BUY) },
                    modifier = Modifier.weight(0.9f).height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MarketGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.6f))
                ) {
                    Text("BUY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onTradeClick(OrderType.SELL) },
                    modifier = Modifier.weight(0.9f).height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MarketRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MarketRed.copy(alpha = 0.6f))
                ) {
                    Text("SELL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOptionChainClick,
                    modifier = Modifier.weight(1.2f).height(32.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("OPT CHAIN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onFnoTradeClick,
                    modifier = Modifier.weight(1.2f).height(32.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("F&O TRADE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StrategyDeskView() {
    val strategies = listOf(
        Triple("Bull Call Spread", "Moderate Bullish • Limited Risk", Pair("+1 ATM Call (Buy)", "-1 OTM Call (Sell)")),
        Triple("Bear Put Spread", "Moderate Bearish • Limited Risk", Pair("+1 ATM Put (Buy)", "-1 OTM Put (Sell)")),
        Triple("Short Straddle", "Rangebound / High Theta Decay", Pair("-1 ATM Call (Sell)", "-1 ATM Put (Sell)")),
        Triple("Iron Condor", "Non-Directional • Defined Risk Wing", Pair("Sell ATM Strangle", "Buy OTM Wings"))
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "OPTION STRATEGY BLUEPRINTS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        strategies.forEach { (name, outlook, legs) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                text = "EST. ROI: ~18-24%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(outlook, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Leg 1: ${legs.first}", style = MaterialTheme.typography.labelSmall, color = MarketGreen, fontWeight = FontWeight.SemiBold)
                        Text("Leg 2: ${legs.second}", style = MaterialTheme.typography.labelSmall, color = MarketRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
