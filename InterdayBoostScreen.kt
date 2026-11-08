package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.analytics.TradingAnalyticsEngine
import com.example.data.model.InterdayFilter
import com.example.data.model.InterdayStockData
import com.example.data.model.OrderType
import com.example.data.model.StockQuote
import com.example.ui.MarketUiState
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun InterdayBoostScreen(
    uiState: MarketUiState,
    onFilterChange: (InterdayFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onTimeframeChange: (String) -> Unit,
    onStockClick: (StockQuote) -> Unit,
    onTradeStock: (StockQuote, OrderType) -> Unit,
    onTradeFno: (StockQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredStocks = remember(uiState.interdayStocks, uiState.selectedInterdayFilter, uiState.interdaySearchQuery) {
        TradingAnalyticsEngine.filterInterdayStocks(
            items = uiState.interdayStocks,
            filter = uiState.selectedInterdayFilter,
            searchQuery = uiState.interdaySearchQuery
        )
    }

    val bullishCount = remember(uiState.interdayStocks) {
        uiState.interdayStocks.count { it.changePercent > 0.0 && it.ltp >= it.technicals.vwap }
    }
    val bearishCount = remember(uiState.interdayStocks) {
        uiState.interdayStocks.count { it.changePercent < 0.0 && it.ltp < it.technicals.vwap }
    }

    val timeframes = listOf("1m", "3m", "5m", "15m", "30m")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("interday_boost_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner & Stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.35f))
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
                                    .background(MarketGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = MarketGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "INTERDAY BOOST",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Intraday Momentum & Breakout Scanner • 182 F&O Stocks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Timeframe Segmented Control
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            timeframes.forEach { tf ->
                                val isSelected = tf == uiState.selectedInterdayTimeframe
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) MarketGreen else Color.Transparent)
                                        .clickable { onTimeframeChange(tf) }
                                        .padding(horizontal = 7.dp, vertical = 4.dp),
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

                    // Breadth / Momentum bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Bullish Above VWAP: $bullishCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MarketGreen
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = MarketRed, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Bearish Below VWAP: $bearishCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MarketRed
                            )
                        }
                    }
                }
            }
        }

        // 2. Search Field
        item {
            OutlinedTextField(
                value = uiState.interdaySearchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interday_search_field"),
                placeholder = {
                    Text(
                        "Search symbol, company name, or sector...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (uiState.interdaySearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MarketGreen,
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
                items(InterdayFilter.values()) { filter ->
                    val isSelected = filter == uiState.selectedInterdayFilter
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
                            selectedContainerColor = MarketGreen,
                            selectedLabelColor = Color.Black
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MarketGreen else MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }
        }

        // 4. Stock Count & Active Filter Indicator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredStocks.size} STOCKS IN ${uiState.selectedInterdayFilter.label.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = uiState.selectedInterdayFilter.shortDesc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketGold,
                    fontSize = 11.sp
                )
            }
        }

        // 5. Stock Cards List
        if (filteredStocks.isEmpty()) {
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
                            text = "No F&O stocks currently match the '${uiState.selectedInterdayFilter.label}' criteria",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredStocks, key = { it.symbol }) { item ->
                InterdayStockCard(
                    data = item,
                    onStockClick = { onStockClick(item.quote) },
                    onTradeStock = { type -> onTradeStock(item.quote, type) },
                    onTradeFno = { onTradeFno(item.quote) }
                )
            }
        }
    }
}

@Composable
fun InterdayStockCard(
    data: InterdayStockData,
    onStockClick: () -> Unit,
    onTradeStock: (OrderType) -> Unit,
    onTradeFno: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stock = data.quote
    val tech = data.technicals
    val isPositive = stock.changePercent >= 0.0
    val trendColor = if (isPositive) MarketGreen else MarketRed

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onStockClick() }
            .testTag("interday_card_${data.symbol}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Symbol, Name, Badges & LTP
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
                            text = stock.symbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "LOT ${stock.lotSize}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                fontSize = 9.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stock.sector,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = stock.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format(java.util.Locale.US, "%,.2f", stock.ltp)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format(java.util.Locale.US, "%.2f", stock.changePercent)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }
            }

            // Row 2: Scores (Momentum, Volume Strength, Breakout Confirmation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Momentum Score
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("MOMENTUM", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${data.momentumScore}/100",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (data.momentumScore >= 65) MarketGreen else if (data.momentumScore <= 40) MarketRed else MarketGold
                        )
                    }
                }

                // Relative Volume
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("REL VOL (RVOL)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${tech.relativeVolume}x",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (tech.relativeVolume >= 1.8) MarketGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Trend Direction
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("TREND STRENGTH", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = tech.trendDirection,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (tech.trendDirection.contains("Bullish")) MarketGreen else if (tech.trendDirection.contains("Bearish")) MarketRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Row 3: Technical Indicators Grid (VWAP, RSI, MACD, ADX, S1/R1)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("VWAP", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(java.util.Locale.US, "%,.1f", tech.vwap)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RSI (14)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${tech.rsi14}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (tech.rsi14 >= 60) MarketGreen else if (tech.rsi14 <= 40) MarketRed else MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MACD", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${if (tech.macdHistogram >= 0) "+" else ""}${tech.macdHistogram}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (tech.macdHistogram >= 0) MarketGreen else MarketRed)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ADX", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${tech.adx14}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("S1 / R1", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${tech.supportLevel.toInt()} / ₹${tech.resistanceLevel.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Row 4: Buyer vs Seller Pressure Bar
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Buyers: ${tech.buyerPressurePct}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MarketGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sellers: ${100 - tech.buyerPressurePct}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MarketRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MarketRed)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(tech.buyerPressurePct / 100f)
                            .background(MarketGreen)
                    )
                }
            }

            // Row 5: Action Buttons
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
                    Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(15.dp))
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
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(4.dp))
                    Text("Trade F&O", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Button(
                    onClick = { onTradeStock(OrderType.BUY) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MarketGreen)
                ) {
                    Text("BUY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Black)
                }

                Button(
                    onClick = { onTradeStock(OrderType.SELL) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MarketRed)
                ) {
                    Text("SELL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}
