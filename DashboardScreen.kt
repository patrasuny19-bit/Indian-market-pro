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
import com.example.data.market.MarketSessionStatus
import com.example.data.model.MarketIndex
import com.example.data.model.SectorClassification
import com.example.data.model.StockQuote
import com.example.ui.MarketUiState
import com.example.ui.components.FiiDiiCard
import com.example.ui.components.IndexSummaryCard
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun DashboardScreen(
    uiState: MarketUiState,
    onIndexClick: (MarketIndex) -> Unit,
    onStockClick: (StockQuote) -> Unit,
    onViewAllStocksClick: () -> Unit,
    onRefreshLiveMarket: () -> Unit = {},
    onOpenSearch: (() -> Unit)? = null,
    onOpenSectorScope: (() -> Unit)? = null,
    onOpenProviderSettings: (() -> Unit)? = null,
    onOpenAiChat: (() -> Unit)? = null,
    onAskAiStock: ((StockQuote) -> Unit)? = null,
    onOpenInterdayBoost: (() -> Unit)? = null,
    onOpenSwingTrading: (() -> Unit)? = null,
    onOpenOiClock: (() -> Unit)? = null,
    onOpenFiiDii: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedMoversTab by remember { mutableIntStateOf(0) } // 0: Gainers, 1: Losers, 2: Most Active

    // Partition indices: Primary four requested by user
    val coreIndices = uiState.indices.filter {
        it.symbol in listOf("NIFTY 50", "BANK NIFTY", "SENSEX", "FINNIFTY")
    }
    val otherIndices = uiState.indices.filterNot {
        it.symbol in listOf("NIFTY 50", "BANK NIFTY", "SENSEX", "FINNIFTY")
    }

    val topGainers = uiState.stocks.sortedByDescending { it.changePercent }.take(5)
    val topLosers = uiState.stocks.sortedBy { it.changePercent }.take(5)
    val mostActive = uiState.stocks.sortedByDescending { it.turnoverCr }.take(5)

    val currentMovers = when (selectedMoversTab) {
        0 -> topGainers
        1 -> topLosers
        else -> mostActive
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Market Status Banner (Live Real Market Connection, Calendar Detection & Sync)
        item {
            val schedule = uiState.marketSchedule
            val statusColor = when (schedule.status) {
                MarketSessionStatus.OPEN_REGULAR -> MarketGreen
                MarketSessionStatus.PRE_MARKET, MarketSessionStatus.POST_MARKET -> MarketGold
                MarketSessionStatus.CLOSED_HOLIDAY -> MarketRed
                MarketSessionStatus.CLOSED_WEEKEND, MarketSessionStatus.CLOSED_AFTER_HOURS -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("market_status_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(RoundedCornerShape(4.5.dp))
                                        .background(statusColor)
                                )
                                Text(
                                    text = schedule.statusBadgeText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor
                                )
                                if (uiState.isRealMarketLive) {
                                    Surface(
                                        color = MarketGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "LIVE",
                                            color = MarketGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "OFFICIAL CLOSE",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = schedule.nextSessionInfo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )

                            Text(
                                text = "Synced: ${uiState.lastLiveSyncTime} (${uiState.feedHealth.latencyMs}ms)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 10.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Provider settings shortcut
                            IconButton(
                                onClick = { onOpenProviderSettings?.invoke() },
                                modifier = Modifier.size(32.dp).testTag("open_provider_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Data Provider Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Automatic 3-Second Sync Live Badge (No manual button)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.6f)),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Text(
                                        text = "AUTO 3s ⚡",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Stale warning bar if feed health has warning
                    if (uiState.feedHealth.warningMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            color = MarketGold.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGold.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MarketGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = uiState.feedHealth.warningMessage ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MarketGold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Search Bar Card on Dashboard
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSearch?.invoke() }
                    .testTag("dashboard_search_bar"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MarketGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Search 65+ stocks, F&O options, or add to Watchlist...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "SEARCH",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Pro Trading Desks & Advanced Modules Launcher
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRO TRADING DESKS & ANALYTICS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MarketGold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "182 NSE F&O UNIVERSE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                }

                // Horizontal scrollable pro desk cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Interday Boost
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .width(155.dp)
                                .clickable { onOpenInterdayBoost?.invoke() }
                                .testTag("launcher_interday_boost")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MarketGreen.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(15.dp))
                                    }
                                    Text("BOOST", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MarketGreen)
                                }
                                Text("Interday Boost", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("VWAP, ORB & Momentum Scanner", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }

                    // 2. Swing Trading
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGold.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .width(155.dp)
                                .clickable { onOpenSwingTrading?.invoke() }
                                .testTag("launcher_swing_trading")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MarketGold.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Timeline, contentDescription = null, tint = MarketGold, modifier = Modifier.size(15.dp))
                                    }
                                    Text("SWING", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MarketGold)
                                }
                                Text("Swing Trading", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("EMA Pullbacks & 1:2.5 R:R Targets", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }

                    // 3. OI Clock
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .width(155.dp)
                                .clickable { onOpenOiClock?.invoke() }
                                .testTag("launcher_oi_clock")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MarketGreen.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(15.dp))
                                    }
                                    Text("OI CLOCK", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MarketGreen)
                                }
                                Text("Open Interest", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Live Call/Put PCR & Timeline", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }

                    // 4. FII / DII Data
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGold.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .width(155.dp)
                                .clickable { onOpenFiiDii?.invoke() }
                                .testTag("launcher_fii_dii")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MarketGold.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MarketGold, modifier = Modifier.size(15.dp))
                                    }
                                    Text("INSTITUTION", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = MarketGold)
                                }
                                Text("FII / DII Flows", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Cash Flow & F&O Positions", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }

        // 360° Sector Scope & Live Heatmap Entry Card
        item {
            val topSectors = remember(uiState.stocks) {
                SectorClassification.groupAllSectors(uiState.stocks).take(4)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSectorScope?.invoke() }
                    .testTag("dashboard_sector_scope_card"),
                shape = RoundedCornerShape(12.dp),
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
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MarketGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = MarketGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "360° SECTOR SCOPE & LIVE HEATMAP",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "NSE F&O Sectors • Treemap • Breadth Analysis",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MarketGreen
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "VIEW",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    fontSize = 10.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    // Sector Quick Preview Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        topSectors.forEach { sec ->
                            val isPos = sec.isPositive
                            val col = if (isPos) MarketGreen else MarketRed
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, col.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = sec.sectorName.replace(" & Utilities", "").replace(" & Mining", "").replace(" & Healthcare", "").replace(" & Capital Goods", "").replace(" & Retail", ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 9.5.sp
                                    )
                                    Text(
                                        text = "${if (isPos) "+" else ""}${String.format(java.util.Locale.US, "%.1f", sec.avgChangePercent)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = col,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Stock Advisor Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAiChat?.invoke() }
                    .testTag("dashboard_gemini_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.MarketGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(com.example.ui.theme.MarketGold, MarketGreen)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "GEMINI STOCK ADVISOR",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(com.example.ui.theme.MarketGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "3.8 FLASH",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.MarketGold
                                )
                            }
                        }
                        Text(
                            text = "Get real-time entry/exit levels, technical indicators & F&O option bias for any Indian stock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    FilledTonalButton(
                        onClick = { onOpenAiChat?.invoke() },
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = com.example.ui.theme.MarketGold,
                            contentColor = androidx.compose.ui.graphics.Color.Black
                        )
                    ) {
                        Text(
                            text = "Ask AI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Section Title: Key Indices
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BENCHMARK INDICES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap to view chart",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Primary Four Indices Grid / Cards: NIFTY 50, BANK NIFTY, SENSEX, FINNIFTY
        items(coreIndices) { index ->
            val isFlashing = uiState.recentTickFlash.containsKey(index.symbol)
            val isFlashGreen = uiState.recentTickFlash[index.symbol] ?: true
            IndexSummaryCard(
                index = index,
                isFlashing = isFlashing,
                isFlashGreen = isFlashGreen,
                onClick = { onIndexClick(index) }
            )
        }

        // Secondary Indices Row (Nifty IT, Midcap Select, etc.)
        if (otherIndices.isNotEmpty()) {
            item {
                Text(
                    text = "SECTORAL & BROADER INDICES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(otherIndices) { index ->
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable { onIndexClick(index) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(index.symbol, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    String.format(java.util.Locale.US, "%,.2f", index.value),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val isPos = index.isPositive
                                Text(
                                    "${if (isPos) "+" else ""}${String.format(java.util.Locale.US, "%.2f", index.changePercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPos) MarketGreen else MarketRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Market Breadth Gauge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NIFTY 50 MARKET BREADTH",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.marketBreadth.advances} Adv / ${uiState.marketBreadth.declines} Dec",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MarketGreen
                        )
                    }

                    // Ratio Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MarketRed)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(uiState.marketBreadth.advanceRatio)
                                .fillMaxHeight()
                                .background(MarketGreen)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("34 Advances", style = MaterialTheme.typography.labelSmall, color = MarketGreen)
                        Text("1 Unchanged", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("15 Declines", style = MaterialTheme.typography.labelSmall, color = MarketRed)
                    }
                }
            }
        }

        // Institutional Activity Card (FII / DII)
        item {
            FiiDiiCard(records = uiState.fiiDii)
        }

        // Market Movers (Gainers / Losers / Active)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MARKET MOVERS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onViewAllStocksClick) {
                        Text("View Watchlist", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Sub-tabs: Gainers, Losers, Active
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedMoversTab == 0,
                        onClick = { selectedMoversTab = 0 },
                        label = { Text("Top Gainers", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedMoversTab == 1,
                        onClick = { selectedMoversTab = 1 },
                        label = { Text("Top Losers", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.TrendingDown, contentDescription = null, tint = MarketRed, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedMoversTab == 2,
                        onClick = { selectedMoversTab = 2 },
                        label = { Text("Most Active", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Movers List
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        currentMovers.forEachIndexed { index, stock ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onStockClick(stock) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stock.symbol,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stock.sector,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${String.format(java.util.Locale.US, "%,.2f", stock.ltp)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val isPos = stock.isPositive
                                        val color = if (isPos) MarketGreen else MarketRed
                                        Text(
                                            text = "${if (isPos) "+" else ""}${String.format(java.util.Locale.US, "%.2f", stock.changePercent)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }

                                    if (onAskAiStock != null) {
                                        IconButton(
                                            onClick = { onAskAiStock(stock) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("ask_ai_${stock.symbol}")
                                        ) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = "Ask Gemini for ${stock.symbol}",
                                                tint = com.example.ui.theme.MarketGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (index < currentMovers.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
