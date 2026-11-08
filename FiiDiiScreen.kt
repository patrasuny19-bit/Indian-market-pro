package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FiiDiiActivityDetail
import com.example.data.model.FiiDiiPeriodSummary
import com.example.ui.MarketUiState
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun FiiDiiScreen(
    uiState: MarketUiState,
    onTabChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val history = uiState.fiiDiiHistory
    val summaries = uiState.fiiDiiSummaries
    val sourceInfo = uiState.fiiDiiSourceInfo
    val selectedTab = uiState.selectedFiiDiiTab

    val latestDay = history.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("fii_dii_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MarketGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "INSTITUTIONAL FII / DII ACTIVITY",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Exchange Verified Cash & Derivative Flows",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Provisional / Final tag
                        if (latestDay != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (latestDay.isProvisional) MarketGold.copy(alpha = 0.2f) else MarketGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (latestDay.isProvisional) "PROVISIONAL" else "EXCHANGE FINAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (latestDay.isProvisional) MarketGold else MarketGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Combined Net Summary
                    if (latestDay != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("COMBINED NET FLOW (TODAY)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${if (latestDay.combinedNetCr >= 0) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", latestDay.combinedNetCr)} Cr",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (latestDay.combinedNetCr >= 0) MarketGreen else MarketRed
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = latestDay.marketStance,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Navigation Tabs (Daily Cash, Derivatives Matrix, Weekly/Monthly Trends)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MarketGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabChange(0) },
                    text = { Text("Daily Cash Flow", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabChange(1) },
                    text = { Text("Derivatives Matrix", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { onTabChange(2) },
                    text = { Text("Historical Summaries", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Content based on tab
        when (selectedTab) {
            // Tab 0: Daily Cash Market Flow
            0 -> {
                // Today's Breakdown Card
                if (latestDay != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "TODAY'S CASH MARKET FLOW BREAKDOWN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MarketGold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // FII Card
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Box(Modifier.size(6.dp).clip(CircleShape).background(if (latestDay.isFiiNetBuyer) MarketGreen else MarketRed))
                                                Text("FII / FPI CASH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            }
                                            Text(
                                                text = "${if (latestDay.fiiCashNetCr >= 0) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", latestDay.fiiCashNetCr)} Cr",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Black,
                                                color = if (latestDay.fiiCashNetCr >= 0) MarketGreen else MarketRed
                                            )
                                            Text("Buy: ₹${latestDay.fiiCashBuyCr.toInt()} Cr", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Sell: ₹${latestDay.fiiCashSellCr.toInt()} Cr", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    // DII Card
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Box(Modifier.size(6.dp).clip(CircleShape).background(if (latestDay.isDiiNetBuyer) MarketGreen else MarketRed))
                                                Text("DII CASH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            }
                                            Text(
                                                text = "${if (latestDay.diiCashNetCr >= 0) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", latestDay.diiCashNetCr)} Cr",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Black,
                                                color = if (latestDay.diiCashNetCr >= 0) MarketGreen else MarketRed
                                            )
                                            Text("Buy: ₹${latestDay.diiCashBuyCr.toInt()} Cr", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Sell: ₹${latestDay.diiCashSellCr.toInt()} Cr", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Daily Historical Flow Table
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "RECENT SESSIONS CASH FLOW (₹ CRORES)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MarketGold
                            )

                            // Table Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DATE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.1f))
                                Text("FII NET", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                Text("DII NET", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                Text("COMBINED", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            history.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = row.formattedDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1.1f)
                                    )

                                    Text(
                                        text = "${if (row.fiiCashNetCr >= 0) "+" else ""}${row.fiiCashNetCr.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (row.fiiCashNetCr >= 0) MarketGreen else MarketRed,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )

                                    Text(
                                        text = "${if (row.diiCashNetCr >= 0) "+" else ""}${row.diiCashNetCr.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (row.diiCashNetCr >= 0) MarketGreen else MarketRed,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )

                                    Text(
                                        text = "${if (row.combinedNetCr >= 0) "+" else ""}${row.combinedNetCr.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (row.combinedNetCr >= 0) MarketGreen else MarketRed,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tab 1: Derivatives Position Matrix
            1 -> {
                if (latestDay != null) {
                    val deriv = latestDay.derivativePositions
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "PARTICIPANT-WISE DERIVATIVE POSITIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MarketGold
                                )

                                // Index Futures Row
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("INDEX FUTURES NET OPEN INTEREST", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${deriv.indexFuturesNetContracts} Long Contracts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MarketGreen)
                                        }
                                        Text("₹${deriv.indexFuturesNetCr.toInt()} Cr", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MarketGreen)
                                    }
                                }

                                // Stock Futures Row
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("STOCK FUTURES NET OPEN INTEREST", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${deriv.stockFuturesNetContracts} Net Contracts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MarketGreen)
                                        }
                                        Text("₹${deriv.stockFuturesNetCr.toInt()} Cr", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MarketGreen)
                                    }
                                }

                                // Index Options Breakdown
                                Text("INDEX OPTIONS POSITION SPLIT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text("CALLS LONG", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketGreen)
                                            Text("${(deriv.indexOptionsLongCalls / 1000)}k", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text("Short: ${(deriv.indexOptionsShortCalls / 1000)}k", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text("PUTS LONG", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketRed)
                                            Text("${(deriv.indexOptionsLongPuts / 1000)}k", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text("Short: ${(deriv.indexOptionsShortPuts / 1000)}k", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text("CLIENT / FII RATIO", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketGold)
                                            Text("${deriv.clientVsFiiRatio}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text("DII Bullish", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 2: Historical Summaries & Verification Disclosures
            2 -> {
                item {
                    Text(
                        text = "PERIODIC SUMMARY (WEEKLY & MONTHLY)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }

                items(summaries) { summary ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(summary.periodName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                                Text("${summary.tradingDaysCount} Trading Sessions", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("FII NET", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${if (summary.fiiTotalNetCr >= 0) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", summary.fiiTotalNetCr)} Cr",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (summary.fiiTotalNetCr >= 0) MarketGreen else MarketRed
                                    )
                                }

                                Column {
                                    Text("DII NET", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${if (summary.diiTotalNetCr >= 0) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", summary.diiTotalNetCr)} Cr",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (summary.diiTotalNetCr >= 0) MarketGreen else MarketRed
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("COMBINED NET", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${if (summary.combinedTotalNetCr >= 0) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", summary.combinedTotalNetCr)} Cr",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (summary.combinedTotalNetCr >= 0) MarketGreen else MarketRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Official Source Disclosure Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(16.dp))
                        Text("DATA SOURCE & EXCHANGE SPECIFICATIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MarketGreen)
                    }
                    Text("• ${sourceInfo.primarySource}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text("• ${sourceInfo.publicationTime}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text("• Scope: ${sourceInfo.cashMarketScope}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text("• ${sourceInfo.disclaimer}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
    }
}
