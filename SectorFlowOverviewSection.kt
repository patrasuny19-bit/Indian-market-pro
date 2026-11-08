package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.model.*
import com.example.ui.theme.MarketCyan
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import java.util.Locale

@Composable
fun SectorFlowOverviewSection(
    summary: SectorFlowDashboardSummary,
    onSectorClick: (SectorFlowMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRankCategory by remember { mutableIntStateOf(0) } // 0: Highest Buying, 1: Lowest Buying, 2: Highest Selling, 3: Lowest Selling

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Market-Wide Order Flow Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INSTITUTIONAL SECTOR ORDER FLOW",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(MarketGreen))
                            Text(
                                text = "20 Sectors Analyzed • ${summary.dataStatusText}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MarketGreen
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (summary.netMarketFlowCr >= 0) MarketGreen.copy(alpha = 0.16f) else MarketRed.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (summary.netMarketFlowCr >= 0) MarketGreen.copy(alpha = 0.4f) else MarketRed.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.End) {
                            Text("NET SECTOR FLOW", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${if (summary.netMarketFlowCr >= 0) "+" else ""}₹${String.format(Locale.US, "%,.1f", summary.netMarketFlowCr)} Cr",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (summary.netMarketFlowCr >= 0) MarketGreen else MarketRed
                            )
                        }
                    }
                }

                // Volume Split Meter
                val totalMarketVol = (summary.totalMarketBuyingVol + summary.totalMarketSellingVol).coerceAtLeast(1L)
                val marketBuyRatio = (summary.totalMarketBuyingVol.toFloat() / totalMarketVol).coerceIn(0.05f, 0.95f)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Total Buying: ${String.format(Locale.US, "%.1f", summary.totalMarketBuyingVol / 1000000.0)}M (${String.format(Locale.US, "%.1f", summary.averageBuyingPressure)}%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MarketGreen
                        )
                        Text(
                            text = "Total Selling: ${String.format(Locale.US, "%.1f", summary.totalMarketSellingVol / 1000000.0)}M (${String.format(Locale.US, "%.1f", 100.0 - summary.averageBuyingPressure)}%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MarketRed
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MarketRed)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(marketBuyRatio)
                                .fillMaxHeight()
                                .background(MarketGreen)
                        )
                    }
                }

                Text(
                    text = "ℹ️ Order flow calculated using aggressive trade volume relative to intraday VWAP, bid-ask aggression, and volume-weighted tick delta.",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Smart Sector Signals Horizontal Scroller (Breakouts, Breakdowns, Accumulation)
        if (summary.breakoutSectors.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SMART SECTOR SIGNALS & DETECTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    summary.breakoutSectors.take(3).forEach { sec ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSectorClick(sec) }
                                .testTag("smart_signal_${sec.sectorName}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(sec.smartSignal.badgeColorHex).copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(sec.smartSignal.badgeColorHex).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = sec.smartSignal.title.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(sec.smartSignal.badgeColorHex),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = sec.sectorName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${if (sec.isPositive) "+" else ""}${String.format(Locale.US, "%.2f", sec.indexChangePercent)}% • Buy: ${String.format(Locale.US, "%.0f", sec.buyingPressurePercent)}%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sec.isPositive) MarketGreen else MarketRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Tabs for 4 Ranking Categories
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "SECTOR BUYING & SELLING RANKINGS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TabRow(
                selectedTabIndex = selectedRankCategory,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedRankCategory == 0,
                    onClick = { selectedRankCategory = 0 },
                    text = { Text("Highest Buying 🟢", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedRankCategory == 1,
                    onClick = { selectedRankCategory = 1 },
                    text = { Text("Lowest Buying", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedRankCategory == 2,
                    onClick = { selectedRankCategory = 2 },
                    text = { Text("Highest Selling 🔴", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedRankCategory == 3,
                    onClick = { selectedRankCategory = 3 },
                    text = { Text("Lowest Selling", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
            }

            // Display Ranked List of Sectors
            val currentRankedList = when (selectedRankCategory) {
                0 -> summary.highestBuyingSectors
                1 -> summary.lowestBuyingSectors
                2 -> summary.highestSellingSectors
                else -> summary.lowestSellingSectors
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                currentRankedList.forEachIndexed { rank, sec ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSectorClick(sec) }
                            .testTag("sector_rank_card_${sec.sectorName}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${rank + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = sec.sectorName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(sec.smartSignal.badgeColorHex).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = sec.smartSignal.title,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(sec.smartSignal.badgeColorHex),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${sec.totalStocks} stocks (${sec.advancingCount} Adv / ${sec.decliningCount} Dec) • Flow: ${sec.moneyFlowDirection.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${if (sec.isPositive) "+" else ""}${String.format(Locale.US, "%.2f", sec.indexChangePercent)}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (sec.isPositive) MarketGreen else MarketRed
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Open stocks",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Buy: ${String.format(Locale.US, "%.0f", sec.buyingPressurePercent)}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MarketGreen
                                    )
                                    Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val netColor = if (sec.netFlowCr >= 0) MarketGreen else MarketRed
                                    Text(
                                        text = "${if (sec.netFlowCr >= 0) "+" else ""}₹${String.format(Locale.US, "%.1f", sec.netFlowCr)}Cr",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = netColor
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
