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
import com.example.data.analytics.SectorOrderFlowEngine
import com.example.data.model.*
import com.example.ui.MarketUiState
import com.example.ui.components.*
import com.example.ui.theme.MarketCyan
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorScopeScreen(
    uiState: MarketUiState,
    onStockClick: (StockQuote) -> Unit,
    onTradeStock: (StockQuote, OrderType) -> Unit,
    onTradeFno: (StockQuote) -> Unit,
    onOpenOptionChain: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 1. Calculate live sector order flow data across all 20 sectors
    val flowDashboard = remember(uiState.stocks) {
        SectorOrderFlowEngine.buildSectorFlowDashboard(uiState.stocks)
    }

    // 2. Active Screen State
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Flow Overview, 1: 20 Sectors, 2: 360° Heatmap, 3: Charts, 4: Rotation
    var chartTimeframe by remember { mutableStateOf("1D") }
    var selectedSectorDetail by remember { mutableStateOf<SectorFlowMetric?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("sector_scope_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Header with Live Status & Institutional Breadth ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sector_scope_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "SECTOR BUYING & SELLING",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MarketGreen.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MarketGreen.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "20 SECTORS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MarketGreen,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Live Order Flow • Aggressive Buying/Selling • 360° Heatmap • Sector Rotation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Live Sync Indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MarketGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MarketGreen))
                                Text("LIVE AUTO (3s)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MarketGreen)
                            }
                        }
                    }

                    // Key Summary Metrics Pill Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ACTIVE SECTORS", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${flowDashboard.allSectors.size} Sectors", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AVG BUY PRESSURE", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format(Locale.US, "%.1f", flowDashboard.averageBuyingPressure)}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MarketGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("NET MONEY FLOW", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val nfColor = if (flowDashboard.netMarketFlowCr >= 0) MarketGreen else MarketRed
                            Text("${if (flowDashboard.netMarketFlowCr >= 0) "+" else ""}₹${String.format(Locale.US, "%,.1f", flowDashboard.netMarketFlowCr)} Cr", fontSize = 11.sp, fontWeight = FontWeight.Black, color = nfColor)
                        }
                    }
                }
            }
        }

        // --- 2. Five Primary Module Tabs ---
        item {
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("sector_dashboard_tabs")
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Flow Overview 📊", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("All 20 Sectors 🏢", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("360° Heatmap 🔥", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    text = { Text("Order Flow Charts 📈", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 4,
                    onClick = { activeSubTab = 4 },
                    text = { Text("Rotation Matrix 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // --- 3. Dynamic Section Content Based on Selected Tab ---
        item {
            when (activeSubTab) {
                0 -> {
                    // TAB 0: Buying/Selling Flow Overview (Highest Buying, Lowest Buying, Highest Selling, Lowest Selling, Smart Signals)
                    SectorFlowOverviewSection(
                        summary = flowDashboard,
                        onSectorClick = { sec -> selectedSectorDetail = sec }
                    )
                }
                1 -> {
                    // TAB 1: Complete 20 Sectors Grid with Search, Filter & Clickable Cards
                    SectorCardsGridSection(
                        sectors = flowDashboard.allSectors,
                        onSectorClick = { sec -> selectedSectorDetail = sec }
                    )
                }
                2 -> {
                    // TAB 2: Professional 360° Sector Heatmap (5-Tier Colors)
                    SectorHeatmap360View(
                        sectors = flowDashboard.allSectors,
                        onSectorClick = { sec -> selectedSectorDetail = sec }
                    )
                }
                3 -> {
                    // TAB 3: Interactive Multi-Timeframe Charts
                    SectorOrderFlowChartsSection(
                        sectors = flowDashboard.allSectors,
                        selectedTimeframe = chartTimeframe,
                        onTimeframeChange = { chartTimeframe = it },
                        onSectorClick = { sec -> selectedSectorDetail = sec }
                    )
                }
                4 -> {
                    // TAB 4: Sector Rotation Matrix (Leading, Improving, Weakening, Lagging)
                    SectorRotationMatrixSection(
                        sectors = flowDashboard.allSectors,
                        onSectorClick = { sec -> selectedSectorDetail = sec }
                    )
                }
            }
        }
    }

    // --- 4. CLICKABLE SECTOR STOCK LIST DIALOG (MOST IMPORTANT FEATURE) ---
    // Opens when ANY sector card or heatmap tile is clicked!
    val currentSector = selectedSectorDetail
    if (currentSector != null) {
        SectorDetailStockListDialog(
            sector = currentSector,
            onDismiss = { selectedSectorDetail = null },
            onStockClick = { stock ->
                selectedSectorDetail = null
                onStockClick(stock)
            },
            onTradeEquity = { stock, type ->
                onTradeStock(stock, type)
            },
            onTradeFno = { stock ->
                onTradeFno(stock)
            }
        )
    }
}
