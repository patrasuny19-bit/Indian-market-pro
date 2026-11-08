package com.example.ui.components

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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.MarketCyan
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketPurple
import com.example.ui.theme.MarketRed
import java.util.Locale

@Composable
fun SectorDetailStockListDialog(
    sector: SectorFlowMetric,
    onDismiss: () -> Unit,
    onStockClick: (StockQuote) -> Unit,
    onTradeEquity: (StockQuote, OrderType) -> Unit,
    onTradeFno: (StockQuote) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(StockSortOption.HIGHEST_BUYING) }

    // Filter and sort the sector's stocks
    val displayStocks = remember(sector.stocks, searchQuery, sortOption) {
        var list = sector.stocks

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.stock.symbol.lowercase().contains(q) ||
                it.stock.name.lowercase().contains(q)
            }
        }

        when (sortOption) {
            StockSortOption.HIGHEST_BUYING -> list.sortedByDescending { it.buyingPressurePercent }
            StockSortOption.HIGHEST_SELLING -> list.sortedByDescending { it.sellingPressurePercent }
            StockSortOption.HIGHEST_VOLUME -> list.sortedByDescending { it.stock.volume }
            StockSortOption.TOP_GAINERS -> list.sortedByDescending { it.stock.changePercent }
            StockSortOption.TOP_LOSERS -> list.sortedBy { it.stock.changePercent }
            StockSortOption.HIGHEST_REL_VOL -> list.sortedByDescending { it.relativeVolume }
            StockSortOption.STRONGEST_RSI -> list.sortedByDescending { it.rsi }
            StockSortOption.HIGHEST_OI_CHANGE -> list.sortedByDescending { it.oiChangePercent }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .testTag("sector_detail_stock_list_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Bar with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (sector.isPositive) MarketGreen.copy(alpha = 0.18f) else MarketRed.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sector.isPositive) MarketGreen.copy(alpha = 0.5f) else MarketRed.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (sector.isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (sector.isPositive) MarketGreen else MarketRed,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = sector.sectorName.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(
                                        text = "${sector.totalStocks} STOCKS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${sector.indexName} • ₹${String.format(Locale.US, "%,.1f", sector.indexValue)} (${if (sector.isPositive) "+" else ""}${String.format(Locale.US, "%.2f", sector.indexChangePercent)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (sector.isPositive) MarketGreen else MarketRed
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Sector Summary Stats Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("BUYING VOLUME", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format(Locale.US, "%.1f", sector.buyingVolume / 1000000.0)}M (${String.format(Locale.US, "%.1f", sector.buyingPressurePercent)}%)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MarketGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("NET MONEY FLOW", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val netFlowColor = if (sector.netFlowCr >= 0) MarketGreen else MarketRed
                                Text("${if (sector.netFlowCr >= 0) "+" else ""}₹${String.format(Locale.US, "%,.1f", sector.netFlowCr)} Cr", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = netFlowColor)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("SELLING VOLUME", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format(Locale.US, "%.1f", sector.sellingVolume / 1000000.0)}M (${String.format(Locale.US, "%.1f", sector.sellingPressurePercent)}%)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MarketRed)
                            }
                        }

                        // Order flow meter
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MarketRed)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(sector.buyingPressurePercent.toFloat() / 100f)
                                    .fillMaxHeight()
                                    .background(MarketGreen)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Breadth: ${sector.advancingCount} Adv / ${sector.decliningCount} Dec / ${sector.unchangedCount} Unch", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Signal: ${sector.smartSignal.title}", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(sector.smartSignal.badgeColorHex))
                        }
                    }
                }

                // Search Bar within Sector
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search ${sector.sectorName} stocks (e.g. ${sector.stocks.firstOrNull()?.stock?.symbol ?: "symbol"})...", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sector_stock_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Sort Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(StockSortOption.entries) { opt ->
                        val isSelected = opt == sortOption
                        FilterChip(
                            selected = isSelected,
                            onClick = { sortOption = opt },
                            label = { Text(opt.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                // Stock Count indicator
                Text(
                    text = "Showing ${displayStocks.size} of ${sector.stocks.size} stocks • Sorted by ${sortOption.label}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Clickable Stock Cards List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("sector_stocks_lazy_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayStocks, key = { it.stock.symbol }) { item ->
                        val stock = item.stock
                        val isStockPos = stock.isPositive
                        val themeColor = if (isStockPos) MarketGreen else MarketRed

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onStockClick(stock) }
                                .testTag("sector_stock_row_${stock.symbol}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Top row: Symbol, Name, Badges & LTP
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
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
                                            if (stock.isFnO) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MarketGold.copy(alpha = 0.2f),
                                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MarketGold.copy(alpha = 0.7f))
                                                ) {
                                                    Text(
                                                        text = "F&O",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MarketGold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (item.buyingPressurePercent >= 55.0) MarketGreen.copy(alpha = 0.15f) else MarketRed.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = item.pressureBadge,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (item.buyingPressurePercent >= 55.0) MarketGreen else MarketRed,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = stock.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${String.format(Locale.US, "%,.2f", stock.ltp)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = themeColor
                                        )
                                        Text(
                                            text = "${if (isStockPos) "+" else ""}${String.format(Locale.US, "%.2f", stock.changePercent)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColor
                                        )
                                    }
                                }

                                // Middle Row: Order Flow Breakdown (Buy Vol vs Sell Vol vs Net Flow)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("BUY VOL", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${String.format(Locale.US, "%.1f", item.buyingVolume / 1000.0)}K (${String.format(Locale.US, "%.0f", item.buyingPressurePercent)}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MarketGreen)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("NET FLOW", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            val nfColor = if (item.netFlowCr >= 0) MarketGreen else MarketRed
                                            Text("${if (item.netFlowCr >= 0) "+" else ""}₹${String.format(Locale.US, "%.1f", item.netFlowCr)} Cr", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = nfColor)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("SELL VOL", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${String.format(Locale.US, "%.1f", item.sellingVolume / 1000.0)}K (${String.format(Locale.US, "%.0f", item.sellingPressurePercent)}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MarketRed)
                                        }
                                    }
                                }

                                // Technical Indicators Grid: VWAP, RSI, Delivery %, RVOL, OI
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("VWAP: ₹${String.format(Locale.US, "%.1f", item.vwap)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("RSI: ${item.rsi}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (item.rsi >= 60) MarketGreen else if (item.rsi <= 40) MarketRed else MaterialTheme.colorScheme.onSurface)
                                    Text("Deliv: ${item.deliveryPercent}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("RVOL: ${item.relativeVolume}x", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MarketCyan)
                                    if (stock.isFnO) {
                                        Text("OI Chg: ${if (item.oiChangePercent >= 0) "+" else ""}${item.oiChangePercent}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (item.oiChangePercent >= 0) MarketGreen else MarketRed)
                                    }
                                }

                                // Bottom Trade Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onTradeEquity(stock, OrderType.BUY) },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MarketGreen),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("BUY", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                    }

                                    Button(
                                        onClick = { onTradeEquity(stock, OrderType.SELL) },
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MarketRed),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("SELL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }

                                    if (stock.isFnO) {
                                        OutlinedButton(
                                            onClick = { onTradeFno(stock) },
                                            modifier = Modifier.weight(1.2f).height(32.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGold),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(Icons.Default.Security, contentDescription = null, tint = MarketGold, modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(3.dp))
                                            Text("Options / F&O", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MarketGold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
