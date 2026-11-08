package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.MarketUiState
import com.example.ui.components.CandlestickChart
import com.example.ui.components.TradingViewChart
import com.example.ui.theme.MarketCyan
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun ScalpingTerminalScreen(
    uiState: MarketUiState,
    onSelectStock: (StockQuote) -> Unit,
    onQuickScalp: (OrderType) -> Unit,
    onExitAllScalp: () -> Unit,
    onSquareOffSingle: (String) -> Unit,
    onSetLotMultiplier: (Int) -> Unit,
    onSetStopLoss: (Double) -> Unit,
    onSetTarget: (Double) -> Unit,
    onSetInterval: (String) -> Unit,
    onToggleChartType: () -> Unit,
    onDismissMessage: () -> Unit,
    onAskAi: ((StockQuote) -> Unit)? = null,
    onOpenAddFunds: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentStock = uiState.selectedStock ?: uiState.stocks.firstOrNull() ?: return
    val baseLot = if (currentStock.lotSize > 1) currentStock.lotSize else 25
    val currentQuantity = baseLot * uiState.scalpLotMultiplier

    val isPos = currentStock.isPositive
    val isFlashing = uiState.recentTickFlash.containsKey(currentStock.symbol)
    val isFlashGreen = uiState.recentTickFlash[currentStock.symbol] ?: true

    val flashBg by animateColorAsState(
        targetValue = if (isFlashing) {
            if (isFlashGreen) MarketGreen.copy(alpha = 0.25f) else MarketRed.copy(alpha = 0.25f)
        } else Color.Transparent,
        animationSpec = tween(durationMillis = 350),
        label = "scalpPriceFlash"
    )

    val scalpTimeframes = listOf(
        Pair("1m", "1"),
        Pair("3m", "3"),
        Pair("5m", "5"),
        Pair("15m", "15"),
        Pair("1D", "D")
    )

    val popularScalpSymbols = listOf("NIFTY 50", "BANK NIFTY", "FINNIFTY", "RELIANCE", "HDFCBANK", "ICICIBANK", "SBIN", "TATAMOTORS")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("scalping_terminal_screen"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Notification Banner if order executed
        item {
            AnimatedVisibility(
                visible = uiState.scalpTradeMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(20.dp))
                            Text(
                                text = uiState.scalpTradeMessage ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = onDismissMessage, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Quick Symbol Switcher Bar
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(popularScalpSymbols) { sym ->
                    val isSelected = sym == currentStock.symbol
                    val matchedStock = uiState.stocks.find { it.symbol == sym } ?: currentStock
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectStock(matchedStock) },
                        label = {
                            Text(
                                text = sym,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        // Header: Active Symbol, Live LTP, and Session P&L
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(flashBg)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = currentStock.symbol,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(
                                        text = "SCALP DESK",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                if (onAskAi != null) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MarketGold.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MarketGold.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .clickable { onAskAi(currentStock) }
                                            .testTag("scalp_ask_ai_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = MarketGold,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                text = "GEMINI ADVICE",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MarketGold
                                            )
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "₹${String.format(java.util.Locale.US, "%,.2f", currentStock.ltp)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isPos) MarketGreen else MarketRed
                                )
                                Text(
                                    text = "${if (isPos) "+" else ""}${String.format(java.util.Locale.US, "%.2f", currentStock.changePercent)}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPos) MarketGreen else MarketRed
                                )
                            }
                        }

                        // Floating Session P&L
                        val sessionPnl = uiState.portfolio.totalPnl
                        val isSessionPnlPos = sessionPnl >= 0
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSessionPnlPos) MarketGreen.copy(alpha = 0.16f) else MarketRed.copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSessionPnlPos) MarketGreen.copy(alpha = 0.4f) else MarketRed.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "SESSION P&L",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${if (isSessionPnlPos) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", sessionPnl)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSessionPnlPos) MarketGreen else MarketRed
                                )
                                if (onOpenAddFunds != null) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MarketGreen.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MarketGreen.copy(alpha = 0.6f)),
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .clickable { onOpenAddFunds() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(10.dp))
                                            Text(
                                                text = "+₹ Demo Funds",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MarketGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chart Header Controls: Chart Type Toggle & Scalping Timeframes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timeframe Chips for Scalpers
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    scalpTimeframes.forEach { (label, value) ->
                        val isSelected = uiState.scalpInterval == value
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetInterval(value) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(28.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }

                // TradingView vs Native Switcher Button
                OutlinedButton(
                    onClick = onToggleChartType,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isTradingViewActive) Icons.Default.Language else Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isTradingViewActive) "TradingView" else "Pro Native",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Primary Chart Display Area: TradingView Widget OR Native Candlestick
        item {
            if (uiState.isTradingViewActive) {
                TradingViewChart(
                    symbol = currentStock.symbol,
                    interval = uiState.scalpInterval,
                    isDarkMode = uiState.isDarkMode,
                    onFallbackToNative = onToggleChartType,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                )
            } else {
                CandlestickChart(
                    candles = uiState.candles,
                    selectedTimeframe = "1D",
                    onTimeframeSelected = { /* handled */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                )
            }
        }

        // 1-Click Fast Scalp Execution Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ 1-CLICK SCALP EXECUTION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface) {
                            Text(
                                text = "Intraday MIS (5x)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Lot Multiplier Selector (1x, 2x, 5x, 10x)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Qty: $currentQuantity (${uiState.scalpLotMultiplier}x Lot)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 2, 5, 10).forEach { mult ->
                                val isSelected = uiState.scalpLotMultiplier == mult
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable { onSetLotMultiplier(mult) }
                                ) {
                                    Text(
                                        text = "${mult}x",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 1-Click Action Buttons: BUY (LONG) vs SELL (SHORT)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // BUY CALL / LONG BUTTON
                        Button(
                            onClick = { onQuickScalp(OrderType.BUY) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("fast_scalp_buy_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MarketGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("BUY / LONG", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                }
                                Text(
                                    text = "$currentQuantity Qty @ ₹${String.format(java.util.Locale.US, "%,.2f", currentStock.ltp)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // SELL PUT / SHORT BUTTON
                        Button(
                            onClick = { onQuickScalp(OrderType.SELL) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("fast_scalp_sell_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MarketRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("SELL / SHORT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                }
                                Text(
                                    text = "$currentQuantity Qty @ ₹${String.format(java.util.Locale.US, "%,.2f", currentStock.ltp)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Risk Presets: SL pts and Target pts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Stop Loss:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            listOf(5.0, 10.0, 20.0).forEach { pts ->
                                val isSelected = uiState.scalpStopLossPts == pts
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) MarketRed.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable { onSetStopLoss(pts) }
                                ) {
                                    Text(
                                        text = "${pts.toInt()}pt",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MarketRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Target:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            listOf(10.0, 20.0, 30.0).forEach { pts ->
                                val isSelected = uiState.scalpTargetPts == pts
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) MarketGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable { onSetTarget(pts) }
                                ) {
                                    Text(
                                        text = "${pts.toInt()}pt",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MarketGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Emergency Panic Button: "EXIT ALL SCALPS"
                    if (uiState.portfolio.positions.isNotEmpty()) {
                        Button(
                            onClick = onExitAllScalp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("exit_all_scalp_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MarketGold.copy(alpha = 0.2f),
                                contentColor = MarketGold
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGold.copy(alpha = 0.5f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("EXIT ALL SCALP POSITIONS (SQUARE OFF)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live Open Scalp Positions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE SCALP POSITIONS (${uiState.portfolio.positions.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Live Auto-P&L",
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketGreen
                )
            }
        }

        if (uiState.portfolio.positions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "No open scalp trades. Tap BUY / LONG or SELL / SHORT above for immediate 1-click execution.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(uiState.portfolio.positions) { pos ->
                val isPnlPos = pos.pnl >= 0
                val pnlColor = if (isPnlPos) MarketGreen else MarketRed

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (pos.type == OrderType.BUY) MarketGreen.copy(alpha = 0.2f) else MarketRed.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = pos.type.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pos.type == OrderType.BUY) MarketGreen else MarketRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = pos.symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${pos.quantity} Qty • Avg: ₹${String.format(java.util.Locale.US, "%,.2f", pos.buyPrice)} • LTP: ₹${String.format(java.util.Locale.US, "%,.2f", pos.currentPrice)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isPnlPos) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", pos.pnl)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = pnlColor
                                )
                                Text(
                                    text = "${if (isPnlPos) "+" else ""}${String.format(java.util.Locale.US, "%.2f", pos.pnlPercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = pnlColor
                                )
                            }

                            // Quick Square Off "X" button
                            FilledTonalIconButton(
                                onClick = { onSquareOffSingle(pos.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Exit Scalp", tint = MarketRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
