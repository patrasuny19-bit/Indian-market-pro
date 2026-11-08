package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.OrderType
import com.example.data.model.StockQuote
import com.example.data.model.SwingFilterType
import com.example.data.model.SwingSetup
import com.example.ui.MarketUiState
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun SwingTradingScreen(
    uiState: MarketUiState,
    onFilterChange: (SwingFilterType) -> Unit,
    onSearchChange: (String) -> Unit,
    onTimeframeChange: (String) -> Unit,
    onStockClick: (StockQuote) -> Unit,
    onTradeStock: (StockQuote, OrderType) -> Unit,
    onTradeFno: (StockQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    val setups = remember(uiState.swingSetups, uiState.selectedSwingFilter, uiState.swingSearchQuery) {
        val query = uiState.swingSearchQuery.trim().lowercase()
        val searched = if (query.isEmpty()) uiState.swingSetups else uiState.swingSetups.filter {
            it.stock.symbol.lowercase().contains(query) ||
            it.stock.name.lowercase().contains(query) ||
            it.stock.sector.lowercase().contains(query) ||
            it.setupName.lowercase().contains(query)
        }

        when (uiState.selectedSwingFilter) {
            SwingFilterType.ALL_SETUPS -> searched.sortedByDescending { if (it.setupStrength == "A+ Elite") 2 else if (it.setupStrength == "A Strong") 1 else 0 }
            SwingFilterType.BULLISH_SWING -> searched.filter { it.direction == "BULLISH" }
            SwingFilterType.BEARISH_SWING -> searched.filter { it.direction == "BEARISH" }
            SwingFilterType.BREAKOUT_CANDIDATES -> searched.filter { it.setupName.contains("Breakout") }
            SwingFilterType.BREAKDOWN_CANDIDATES -> searched.filter { it.setupName.contains("Breakdown") }
            SwingFilterType.PULLBACK_SETUPS -> searched.filter { it.setupName.contains("Pullback") }
            SwingFilterType.MA_CROSSOVERS -> searched.filter { it.indicators.ema9 >= it.indicators.ema20 }
            SwingFilterType.RSI_DIVERGENCE -> searched.filter { it.indicators.rsi14 in 45.0..62.0 }
            SwingFilterType.MACD_CROSSOVERS -> searched.filter { it.indicators.macdHistogram > 0.0 }
            SwingFilterType.VOLUME_EXPANSION -> searched.filter { it.indicators.volumeRatio >= 1.4 }
        }
    }

    val timeframes = listOf("4H", "1D", "1W")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("swing_trading_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MarketGold.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MarketGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    tint = MarketGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "SWING TRADING DESK",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Multi-day Positional Setups • Transparent Risk:Reward",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Timeframe Switcher
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            timeframes.forEach { tf ->
                                val isSelected = tf == uiState.selectedSwingTimeframe
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) MarketGold else Color.Transparent)
                                        .clickable { onTimeframeChange(tf) }
                                        .padding(horizontal = 9.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tf,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Disclaimer notice banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MarketGold, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Entry, stop-loss & targets are algorithmic technical estimates, not guaranteed recommendations.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Search Field
        item {
            OutlinedTextField(
                value = uiState.swingSearchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("swing_search_field"),
                placeholder = {
                    Text(
                        "Search setup, stock or sector...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (uiState.swingSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MarketGold,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // 3. Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SwingFilterType.values()) { filter ->
                    val isSelected = filter == uiState.selectedSwingFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MarketGold,
                            selectedLabelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MarketGold else MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }
        }

        // 4. Header count
        item {
            Text(
                text = "${setups.size} SETUPS DISCOVERED IN ${uiState.selectedSwingFilter.label.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        }

        // 5. Setup Cards
        if (setups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No swing setups currently match this filter criteria",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(setups, key = { it.stock.symbol }) { setup ->
                SwingSetupCard(
                    setup = setup,
                    onStockClick = { onStockClick(setup.stock) },
                    onTradeStock = { type -> onTradeStock(setup.stock, type) },
                    onTradeFno = { onTradeFno(setup.stock) }
                )
            }
        }
    }
}

@Composable
fun SwingSetupCard(
    setup: SwingSetup,
    onStockClick: () -> Unit,
    onTradeStock: (OrderType) -> Unit,
    onTradeFno: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBullish = setup.direction == "BULLISH"
    val directionColor = if (isBullish) MarketGreen else MarketRed
    val ind = setup.indicators
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("swing_card_${setup.stock.symbol}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, directionColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Symbol, Direction, Strength, and LTP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = setup.stock.symbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isBullish) MarketGreen.copy(alpha = 0.15f) else MarketRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = setup.direction,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = directionColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MarketGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = setup.setupStrength,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MarketGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp
                            )
                        }
                    }

                    Text(
                        text = "${setup.stock.name} • ${setup.stock.sector}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format(java.util.Locale.US, "%,.2f", setup.stock.ltp)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${if (setup.stock.changePercent >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.2f", setup.stock.changePercent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (setup.stock.changePercent >= 0) MarketGreen else MarketRed
                    )
                }
            }

            // Setup Pattern Title
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PATTERN: ${setup.setupName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "R:R ${setup.riskRewardRatio}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MarketGreen
                    )
                }
            }

            // Row 2: Target & Risk Levels (Entry, Stop Loss, Target 1, Target 2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Entry Zone
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Column(modifier = Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("ENTRY ZONE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${setup.entryZoneMin.toInt()} - ₹${setup.entryZoneMax.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                // Stop Loss
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MarketRed.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("STOP LOSS", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketRed)
                        Text("₹${setup.stopLoss.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MarketRed)
                    }
                }

                // Target 1
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MarketGreen.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("TARGET 1 (1.5R)", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketGreen)
                        Text("₹${setup.target1.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MarketGreen)
                    }
                }

                // Target 2
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MarketGreen.copy(alpha = 0.18f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("TARGET 2 (2.6R)", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketGreen)
                        Text("₹${setup.target2.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MarketGreen)
                    }
                }
            }

            // Row 3: Primary Technical Reason
            Text(
                text = "• ${setup.primaryReason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp
            )

            // Secondary reasons & Full Technical Indicator Matrix (Expandable)
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    setup.secondaryReasons.forEach { reason ->
                        Text(
                            text = "  ✓ $reason",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "TECHNICAL INDICATORS (${setup.timeframe})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MarketGold,
                        fontSize = 9.sp
                    )

                    // Indicator Matrix Table
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("EMA (9 / 20 / 50 / 200)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${ind.ema9.toInt()} / ₹${ind.ema20.toInt()} / ₹${ind.ema50.toInt()} / ₹${ind.ema200.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SMA (20 / 50 / 200)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${ind.sma20.toInt()} / ₹${ind.sma50.toInt()} / ₹${ind.sma200.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("RSI (14) • MACD • ADX", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${ind.rsi14} • Hist: ${ind.macdHistogram} • ADX: ${ind.adx}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bollinger Bands (Upper / Lower)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${ind.bollingerUpper.toInt()} / ₹${ind.bollingerLower.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("ATR (Volatility Range)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${ind.atr} pts", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Expand / Collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide Technical Matrix" else "View Indicator Matrix (EMA, SMA, RSI, MACD, BB)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MarketGold,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Row 4: Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStockClick,
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Chart", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onTradeFno,
                    modifier = Modifier.weight(1.2f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(4.dp))
                    Text("Trade F&O", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Button(
                    onClick = { onTradeStock(if (isBullish) OrderType.BUY else OrderType.SELL) },
                    modifier = Modifier.weight(1.2f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = directionColor)
                ) {
                    Text(
                        text = if (isBullish) "ENTER LONG" else "ENTER SHORT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isBullish) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
