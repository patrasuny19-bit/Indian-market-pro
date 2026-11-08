package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.data.model.PortfolioSummary
import com.example.ui.MarketUiState
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import java.util.Locale

@Composable
fun PortfolioScreen(
    uiState: MarketUiState,
    onSquareOff: (String) -> Unit,
    onExploreWatchlist: () -> Unit,
    onOpenAddFundsDialog: () -> Unit = {},
    onAddDemoFunds: (Double) -> Unit = {},
    onResetAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPortfolioTab by remember { mutableIntStateOf(0) } // 0: Positions, 1: Orders
    val portfolio = uiState.portfolio

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("portfolio_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "PAPER TRADING TERMINAL",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Unlimited Demo Trading Account • Zero Risk Practice on Real Market Feeds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Account Portfolio Summary Card
        item {
            val totalPnl = portfolio.totalPnl
            val isTotalPnlPos = totalPnl >= 0
            val pnlColor = if (isTotalPnlPos) MarketGreen else MarketRed

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header: Account Value
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL ACCOUNT VALUE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(java.util.Locale.US, "%,.2f", portfolio.totalAccountValue)}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = pnlColor.copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, pnlColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Total P&L: ${if (isTotalPnlPos) "+" else ""}₹${String.format(java.util.Locale.US, "%,.2f", totalPnl)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = pnlColor
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                    // Breakdown Row: Available Margin, Used Margin, Unrealized PnL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Available Funds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format(java.util.Locale.US, "%,.1f", portfolio.availableMargin)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MarketGreen)
                        }
                        Column {
                            Text("Margin Utilized", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format(java.util.Locale.US, "%,.1f", portfolio.usedMargin)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Live Unrealized", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val unPnl = portfolio.unrealizedPnl
                            val unColor = if (unPnl >= 0) MarketGreen else MarketRed
                            Text("${if (unPnl >= 0) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", unPnl)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = unColor)
                        }
                    }
                }
            }
        }

        // Demo Funds Success Notification Banner
        val successMsg = uiState.demoFundsSuccessMessage
        if (successMsg != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MarketGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("demo_funds_success_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(20.dp))
                        Text(
                            text = successMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MarketGreen
                        )
                    }
                }
            }
        }

        // UNLIMITED DEMO FUNDS CONTROLLER CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("demo_funds_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                    .background(MarketGreen.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCard,
                                    contentDescription = null,
                                    tint = MarketGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "UNLIMITED DEMO FUNDS",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Top-up virtual margin anytime for testing strategies",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onOpenAddFundsDialog,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MarketGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(34.dp).testTag("custom_add_funds_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.Black)
                            Spacer(Modifier.width(4.dp))
                            Text("Add Funds", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        }
                    }

                    // Quick Top-up Chips (Instant 1-Tap Credit)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            50000.0 to "+₹50K",
                            100000.0 to "+₹1 Lakh",
                            500000.0 to "+₹5 Lakhs",
                            1000000.0 to "+₹10 Lakhs",
                            2500000.0 to "+₹25 Lakhs",
                            10000000.0 to "+₹1 Crore"
                        ).forEach { (amount, label) ->
                            SuggestionChip(
                                onClick = { onAddDemoFunds(amount) },
                                label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    borderColor = MarketGreen.copy(alpha = 0.5f),
                                    enabled = true
                                )
                            )
                        }
                    }
                }
            }
        }

        // Sub-tabs: Positions vs Order History
        item {
            TabRow(
                selectedTabIndex = selectedPortfolioTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedPortfolioTab == 0,
                    onClick = { selectedPortfolioTab = 0 },
                    text = { Text("Open Positions (${portfolio.positions.size})", style = MaterialTheme.typography.labelMedium) }
                )
                Tab(
                    selected = selectedPortfolioTab == 1,
                    onClick = { selectedPortfolioTab = 1 },
                    text = { Text("Order Book (${portfolio.orders.size})", style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        if (selectedPortfolioTab == 0) {
            // Open Positions List
            if (portfolio.positions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                            Text("No Open Positions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("You have squared off all trades or haven't placed an order yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(onClick = onExploreWatchlist, modifier = Modifier.padding(top = 8.dp)) {
                                Text("Browse Stocks & F&O")
                            }
                        }
                    }
                }
            } else {
                items(portfolio.positions) { pos ->
                    val isPosPnl = pos.pnl >= 0
                    val posColor = if (isPosPnl) MarketGreen else MarketRed

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Top Row: Symbol, Product, Buy/Sell tag, P&L
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface) {
                                        Text(
                                            text = pos.product.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (isPosPnl) "+" else ""}₹${String.format(java.util.Locale.US, "%,.2f", pos.pnl)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = posColor
                                    )
                                    Text(
                                        text = "${if (isPosPnl) "+" else ""}${String.format(java.util.Locale.US, "%.2f", pos.pnlPercent)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = posColor
                                    )
                                }
                            }

                            // Price details: Qty, Buy Avg, LTP
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Qty: ${pos.quantity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Avg: ₹${String.format(java.util.Locale.US, "%,.2f", pos.buyPrice)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("LTP: ₹${String.format(java.util.Locale.US, "%,.2f", pos.currentPrice)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                                OutlinedButton(
                                    onClick = { onSquareOff(pos.id) },
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Square Off", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Order History List
            if (portfolio.orders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                            Text("No Orders Executed Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Your executed and completed paper trades will appear here once you place a trade.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(portfolio.orders) { ord ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
                                Text(
                                    text = ord.type.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ord.type == OrderType.BUY) MarketGreen else MarketRed
                                )
                                Text(
                                    text = ord.symbol,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${ord.quantity} Qty • ${ord.product.label} • ${ord.category.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "₹${String.format(java.util.Locale.US, "%,.2f", ord.price)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(shape = RoundedCornerShape(4.dp), color = MarketGreen.copy(alpha = 0.15f)) {
                                    Text(
                                        text = ord.status.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MarketGreen,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = ord.timeFormatted,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
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
