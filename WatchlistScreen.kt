package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.ui.MarketUiState
import com.example.ui.components.SparklineMini
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun WatchlistScreen(
    uiState: MarketUiState,
    onStockClick: (StockQuote) -> Unit,
    onFilterChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQuickTrade: (StockQuote, OrderType) -> Unit,
    onOpenSearchDialog: () -> Unit,
    onTradeFno: (StockQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf("★ My Watchlist", "All Stocks", "F&O Heavyweights", "Banking", "IT", "Automobile", "Energy & Metals")

    val filteredStocks = uiState.stocks.filter { stock ->
        val matchesSearch = stock.symbol.contains(uiState.searchQuery, ignoreCase = true) ||
                stock.name.contains(uiState.searchQuery, ignoreCase = true)

        val matchesFilter = when (uiState.watchlistFilter) {
            "★ My Watchlist", "Favorites" -> uiState.favoriteSymbols.contains(stock.symbol)
            "F&O Heavyweights" -> stock.isFnO
            "Banking" -> stock.sector.contains("Bank", ignoreCase = true)
            "IT" -> stock.sector.contains("Tech", ignoreCase = true) || stock.sector.contains("IT", ignoreCase = true)
            "Automobile" -> stock.sector.contains("Auto", ignoreCase = true)
            "Energy & Metals" -> stock.sector.contains("Energy", ignoreCase = true) || stock.sector.contains("Metal", ignoreCase = true) || stock.sector.contains("Oil", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("watchlist_screen")
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar with Add Stock trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search any NSE stock, F&O...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("stock_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Dedicated "+ Add" Button to search any stock
            FilledTonalButton(
                onClick = onOpenSearchDialog,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("watchlist_add_stock_button"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }

        // Filter Categories Horizontal Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters) { filter ->
                val isSelected = filter == uiState.watchlistFilter || (filter == "★ My Watchlist" && uiState.watchlistFilter == "Favorites")
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterChange(filter) },
                    label = {
                        Text(
                            filter,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (filter == "★ My Watchlist") MarketGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary,
                        selectedLabelColor = if (filter == "★ My Watchlist") MarketGold else MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        // Stocks count header with active filter label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${uiState.watchlistFilter.uppercase()} (${filteredStocks.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Live NSE Quotes",
                style = MaterialTheme.typography.labelSmall,
                color = MarketGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }

        // Stocks List
        if (filteredStocks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MarketGold.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (uiState.watchlistFilter.contains("Watchlist")) "Your Watchlist is empty" else "No matching stocks found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (uiState.watchlistFilter.contains("Watchlist"))
                            "Tap below to search any Indian stock or index and add it to your personal watchlist."
                        else "Try searching another symbol or clearing your filter.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onOpenSearchDialog,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Search & Add Stocks to Watchlist")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredStocks, key = { it.symbol }) { stock ->
                    val isFavorite = uiState.favoriteSymbols.contains(stock.symbol)
                    val isFlashing = uiState.recentTickFlash.containsKey(stock.symbol)
                    val isFlashGreen = uiState.recentTickFlash[stock.symbol] ?: true

                    val flashBg by animateColorAsState(
                        targetValue = if (isFlashing) {
                            if (isFlashGreen) MarketGreen.copy(alpha = 0.2f) else MarketRed.copy(alpha = 0.2f)
                        } else Color.Transparent,
                        animationSpec = tween(durationMillis = 350),
                        label = "stockFlash"
                    )

                    val isPos = stock.isPositive
                    val accentColor = if (isPos) MarketGreen else MarketRed

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { onStockClick(stock) }
                            .testTag("stock_item_${stock.symbol}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(flashBg)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Top row: Symbol, tags, favorite star
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = stock.symbol,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (stock.isFnO) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    text = "F&O (Lot ${stock.lotSize})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { onToggleFavorite(stock.symbol) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                                            contentDescription = "Favorite",
                                            tint = if (isFavorite) MarketGold else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Middle Row: Company name, sparkline, and prices
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stock.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = stock.sector,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    }

                                    // Mini sparkline
                                    if (stock.sparkline.isNotEmpty()) {
                                        SparklineMini(
                                            data = stock.sparkline,
                                            color = accentColor,
                                            modifier = Modifier
                                                .width(60.dp)
                                                .height(26.dp)
                                                .padding(horizontal = 4.dp)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${String.format(java.util.Locale.US, "%,.2f", stock.ltp)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${if (isPos) "+" else ""}${String.format(java.util.Locale.US, "%.2f", stock.change)} (${String.format(java.util.Locale.US, "%.2f", stock.changePercent)}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }

                                // Quick action buttons: Buy / Sell / F&O Trade
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onQuickTrade(stock, OrderType.BUY) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MarketGreen.copy(alpha = 0.2f),
                                            contentColor = MarketGreen
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("BUY", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onQuickTrade(stock, OrderType.SELL) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MarketRed.copy(alpha = 0.2f),
                                            contentColor = MarketRed
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("SELL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }

                                    if (stock.isFnO) {
                                        OutlinedButton(
                                            onClick = { onTradeFno(stock) },
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(34.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(
                                                "TRADE F&O",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
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
    }
}
