package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MarketIndex
import com.example.data.model.OrderType
import com.example.data.model.StockQuote
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import java.util.Locale

@Composable
fun SearchStocksDialog(
    allStocks: List<StockQuote>,
    allIndices: List<MarketIndex>,
    favoriteSymbols: Set<String>,
    isSearchingOnline: Boolean,
    onlineSearchResult: StockQuote?,
    onDismiss: () -> Unit,
    onSelectStock: (StockQuote) -> Unit,
    onSelectIndex: (MarketIndex) -> Unit,
    onToggleWatchlist: (String) -> Unit,
    onTradeEquity: (StockQuote, OrderType) -> Unit,
    onTradeFno: (StockQuote) -> Unit,
    onSearchOnline: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Stocks", "Indices", "F&O Eligible", "My Watchlist")

    // Filter local results
    val cleanQuery = query.trim()

    val matchedIndices = remember(cleanQuery, selectedCategory, allIndices) {
        if (selectedCategory == "Stocks") emptyList()
        else {
            allIndices.filter { index ->
                cleanQuery.isEmpty() ||
                        index.symbol.contains(cleanQuery, ignoreCase = true) ||
                        index.name.contains(cleanQuery, ignoreCase = true)
            }
        }
    }

    val matchedStocks = remember(cleanQuery, selectedCategory, allStocks, favoriteSymbols) {
        if (selectedCategory == "Indices") emptyList()
        else {
            allStocks.filter { stock ->
                val matchesQuery = cleanQuery.isEmpty() ||
                        stock.symbol.contains(cleanQuery, ignoreCase = true) ||
                        stock.name.contains(cleanQuery, ignoreCase = true) ||
                        stock.sector.contains(cleanQuery, ignoreCase = true) ||
                        (stock.instrumentToken > 0L && stock.instrumentToken.toString().contains(cleanQuery))

                val matchesCat = when (selectedCategory) {
                    "F&O Eligible" -> stock.isFnO
                    "My Watchlist" -> favoriteSymbols.contains(stock.symbol)
                    else -> true
                }

                matchesQuery && matchesCat
            }
        }
    }

    val popularSymbols = listOf("NIFTY 50", "BANK NIFTY", "RELIANCE", "TATAMOTORS", "HDFCBANK", "INFY", "HAL", "BEL", "ADANIENT", "ZOMATO")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("search_stocks_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Search Bar with back/close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                "Search any stock, index, F&O (e.g. Tata, Adani, HAL)...",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MarketGreen)
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("global_search_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Filter Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                // Quick Popular Searches when query is empty
                if (cleanQuery.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "POPULAR:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(popularSymbols) { sym ->
                                SuggestionChip(
                                    onClick = { query = sym },
                                    label = { Text(sym, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }

                // Dynamic Live Search Banner (if user typed something not matching or wanting live fetch)
                if (cleanQuery.length >= 2) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSearchOnline(cleanQuery) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.TravelExplore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Search live quote for '$cleanQuery' on NSE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isSearchingOnline) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Results List
                val totalResults = matchedIndices.size + matchedStocks.size

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RESULTS ($totalResults)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (totalResults == 0 && !isSearchingOnline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("No local matches found for '$cleanQuery'", style = MaterialTheme.typography.titleSmall)
                            Text("Tap 'Search live quote' above to look up any NSE stock online", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Matched Indices
                        items(matchedIndices, key = { "index_${it.symbol}" }) { index ->
                            IndexSearchResultCard(
                                index = index,
                                onClick = {
                                    onSelectIndex(index)
                                    onDismiss()
                                },
                                onTradeFno = {
                                    val dummy = StockQuote(
                                        symbol = index.symbol,
                                        name = index.name,
                                        ltp = index.value,
                                        change = index.change,
                                        changePercent = index.changePercent,
                                        open = index.open,
                                        high = index.high,
                                        low = index.low,
                                        prevClose = index.prevClose,
                                        volume = 10000000L,
                                        turnoverCr = 5000.0,
                                        sector = "Index",
                                        marketCapCr = 0.0,
                                        peRatio = 22.0,
                                        isFnO = true,
                                        lotSize = if (index.symbol == "BANK NIFTY") 15 else 25,
                                        yearHigh = index.yearHigh,
                                        yearLow = index.yearLow
                                    )
                                    onTradeFno(dummy)
                                }
                            )
                        }

                        // 2. Matched Stocks
                        items(matchedStocks, key = { "stock_${it.symbol}" }) { stock ->
                            val isFavorite = favoriteSymbols.contains(stock.symbol)
                            StockSearchResultCard(
                                stock = stock,
                                isFavorite = isFavorite,
                                onSelect = {
                                    onSelectStock(stock)
                                    onDismiss()
                                },
                                onToggleWatchlist = { onToggleWatchlist(stock.symbol) },
                                onTradeEquity = { onTradeEquity(stock, OrderType.BUY) },
                                onTradeFno = { onTradeFno(stock) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndexSearchResultCard(
    index: MarketIndex,
    onClick: () -> Unit,
    onTradeFno: () -> Unit
) {
    val isPos = index.isPositive
    val accentColor = if (isPos) MarketGreen else MarketRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = index.symbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "INDEX",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(text = index.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format(Locale.US, "%,.2f", index.value)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", index.changePercent)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))
                // Quick F&O Button for Indices
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MarketGold.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MarketGold.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onTradeFno() }
                ) {
                    Text(
                        text = "Trade F&O",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MarketGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StockSearchResultCard(
    stock: StockQuote,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onTradeEquity: () -> Unit,
    onTradeFno: () -> Unit
) {
    val isPos = stock.isPositive
    val accentColor = if (isPos) MarketGreen else MarketRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .testTag("search_result_${stock.symbol}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Symbol, Sector, Star, Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
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
                                text = "F&O • Lot ${stock.lotSize}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (stock.instrumentToken > 0L) {
                        Text(
                            text = "#${stock.instrumentToken}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Price and % change
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format(Locale.US, "%,.2f", stock.ltp)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", stock.changePercent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 2: Company Name & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stock.name} • ${stock.sector}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Watchlist star toggle
                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Watchlist",
                            tint = if (isFavorite) MarketGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Buy Equity Button
                    FilledTonalButton(
                        onClick = onTradeEquity,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MarketGreen.copy(alpha = 0.16f),
                            contentColor = MarketGreen
                        )
                    ) {
                        Text("Trade", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Trade F&O Button (if F&O eligible)
                    if (stock.isFnO) {
                        OutlinedButton(
                            onClick = onTradeFno,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("F&O", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
