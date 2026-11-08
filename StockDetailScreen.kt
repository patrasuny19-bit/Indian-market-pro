package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderType
import com.example.data.model.StockQuote
import com.example.ui.MarketUiState
import com.example.ui.components.CandlestickChart
import com.example.ui.components.MarketDepthView
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun StockDetailScreen(
    uiState: MarketUiState,
    onBack: () -> Unit,
    onTimeframeSelect: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleTradingView: () -> Unit = {},
    onOpenOrder: (StockQuote, OrderType) -> Unit,
    onAskAi: ((StockQuote) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val stock = uiState.selectedStock ?: return
    val isFavorite = uiState.favoriteSymbols.contains(stock.symbol)
    val isPos = stock.isPositive
    val accentColor = if (isPos) MarketGreen else MarketRed

    Scaffold(
        modifier = modifier.testTag("stock_detail_screen"),
        bottomBar = {
            // Sticky Buy / Sell Action Bar
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onOpenOrder(stock, OrderType.BUY) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("terminal_buy_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MarketGreen,
                            contentColor = androidx.compose.ui.graphics.Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("BUY", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onOpenOrder(stock, OrderType.SELL) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("terminal_sell_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MarketRed,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SELL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar with back button, symbol, and star
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stock.symbol,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "NSE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = stock.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onAskAi != null) {
                            IconButton(onClick = { onAskAi(stock) }) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Ask Gemini AI",
                                    tint = MarketGold
                                )
                            }
                        }

                        IconButton(onClick = { onToggleFavorite(stock.symbol) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) MarketGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Big LTP & Change Row
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "₹${String.format(java.util.Locale.US, "%,.2f", stock.ltp)}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentColor.copy(alpha = 0.16f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${if (isPos) "+" else ""}${String.format(java.util.Locale.US, "%.2f", stock.change)} (${String.format(java.util.Locale.US, "%.2f", stock.changePercent)}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Open: ₹${String.format(java.util.Locale.US, "%,.1f", stock.open)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("High: ₹${String.format(java.util.Locale.US, "%,.1f", stock.high)}", style = MaterialTheme.typography.labelSmall, color = MarketGreen)
                            Text("Low: ₹${String.format(java.util.Locale.US, "%,.1f", stock.low)}", style = MaterialTheme.typography.labelSmall, color = MarketRed)
                            Text("Prev: ₹${String.format(java.util.Locale.US, "%,.1f", stock.prevClose)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Chart Header & Toggle (TradingView vs Native Pro)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isTradingViewActive) "TRADINGVIEW LIVE CHART" else "TECHNICAL CANDLESTICK",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(
                        onClick = onToggleTradingView,
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (uiState.isTradingViewActive) "Switch to Native" else "Switch to TradingView",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Interactive Chart (TradingView or Native Candlestick)
            item {
                if (uiState.isTradingViewActive) {
                    com.example.ui.components.TradingViewChart(
                        symbol = stock.symbol,
                        interval = uiState.scalpInterval,
                        isDarkMode = uiState.isDarkMode,
                        onFallbackToNative = onToggleTradingView,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    )
                } else {
                    CandlestickChart(
                        candles = uiState.candles,
                        selectedTimeframe = uiState.selectedTimeframe,
                        onTimeframeSelected = onTimeframeSelect
                    )
                }
            }

            // Market Depth (Level 2)
            item {
                uiState.marketDepth?.let { depth ->
                    MarketDepthView(depth = depth)
                }
            }

            // Fundamentals & Metrics Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "KEY FINANCIAL METRICS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("52-Week High", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(java.util.Locale.US, "%,.2f", stock.yearHigh)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MarketGreen)
                            }
                            Column {
                                Text("52-Week Low", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(java.util.Locale.US, "%,.2f", stock.yearLow)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MarketRed)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("P/E Ratio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${stock.peRatio}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(String.format(java.util.Locale.US, "%,d", stock.volume), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("Turnover", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(java.util.Locale.US, "%,.1f", stock.turnoverCr)} Cr", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Market Cap", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(java.util.Locale.US, "%,.0f", stock.marketCapCr)} Cr", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}
