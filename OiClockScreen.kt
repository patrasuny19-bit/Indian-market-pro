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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildupType
import com.example.data.model.OiAlertItem
import com.example.data.model.OiClockData
import com.example.data.model.OiStrikeRow
import com.example.ui.MarketUiState
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun OiClockScreen(
    uiState: MarketUiState,
    onSelectUnderlying: (String) -> Unit,
    onRefresh: () -> Unit,
    onStrikeSelected: (OiStrikeRow, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val data = uiState.oiClockData
    val underlyings = listOf("NIFTY 50", "BANK NIFTY", "FINNIFTY", "SENSEX", "RELIANCE", "HDFCBANK")
    var selectedViewTab by remember { mutableIntStateOf(0) } // 0: OI Clock & Timeline, 1: Call vs Put Chart, 2: Index Option Chain

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("oi_clock_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Underlying Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MarketGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "OI CLOCK MONITOR",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Live Open Interest • PCR Trajectory • Buildups",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Refresh button with loading animation
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("oi_clock_refresh_button")
                        ) {
                            if (uiState.isOiClockRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MarketGreen
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh OI Clock",
                                    tint = MarketGreen
                                )
                            }
                        }
                    }

                    // Underlying selector chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(underlyings) { sym ->
                            val isSelected = sym == uiState.selectedOiClockUnderlying
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MarketGreen else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MarketGreen else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.clickable { onSelectUnderlying(sym) }
                            ) {
                                Text(
                                    text = sym,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (data != null) {
            // 2. Primary Metrics Cards Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Spot Price
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("SPOT PRICE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(java.util.Locale.US, "%,.1f", data.spotPrice)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                            }
                        }

                        // PCR
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("PUT / CALL RATIO (PCR)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${data.pcr}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (data.pcr >= 1.0) MarketGreen else MarketRed
                                )
                            }
                        }

                        // Max Pain
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("MAX PAIN STRIKE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${data.maxPain.toInt()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MarketGold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Call OI
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("CALL OI (RESISTANCE)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MarketRed)
                                Text("${(data.totalCallOi / 1000)}k", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MarketRed)
                                Text("Dominant: ₹${data.dominantCallStrike.toInt()}", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Total Put OI
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("PUT OI (SUPPORT)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MarketGreen)
                                Text("${(data.totalPutOi / 1000)}k", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MarketGreen)
                                Text("Dominant: ₹${data.dominantPutStrike.toInt()}", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 3. Mathematical OI Buildup Interpretation Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (data.buildupType == BuildupType.LONG_BUILDUP || data.buildupType == BuildupType.SHORT_COVERING) MarketGreen else MarketRed)
                                )
                                Text(
                                    text = data.buildupType.label.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (data.buildupType == BuildupType.LONG_BUILDUP || data.buildupType == BuildupType.SHORT_COVERING) MarketGreen else MarketRed
                                )
                            }

                            Text(
                                text = "Synced: ${data.lastUpdated}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = data.interpretationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 4. View Switcher Tabs (OI Clock Timeline, Call vs Put Chart, Option Chain)
            item {
                TabRow(
                    selectedTabIndex = selectedViewTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MarketGreen
                ) {
                    Tab(
                        selected = selectedViewTab == 0,
                        onClick = { selectedViewTab = 0 },
                        text = { Text("OI Clock Timeline", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedViewTab == 1,
                        onClick = { selectedViewTab = 1 },
                        text = { Text("Call vs Put Chart", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedViewTab == 2,
                        onClick = { selectedViewTab = 2 },
                        text = { Text("Option Chain (Index)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (selectedViewTab) {
                // Tab 0: OI Clock Timeline & Alerts
                0 -> {
                    // Timeline table
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("SESSION TIMELINE (15-30 MIN INTERVALS)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MarketGold)

                                // Header row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("TIME", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f))
                                    Text("CALL OI", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MarketRed, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                    Text("PUT OI", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MarketGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                    Text("PCR", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                                    Text("SENTIMENT", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                                }
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                data.timeline.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.timeLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                        Text("${(item.callOiAdditions / 1000)}k", style = MaterialTheme.typography.labelSmall, color = MarketRed, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                        Text("${(item.putOiAdditions / 1000)}k", style = MaterialTheme.typography.labelSmall, color = MarketGreen, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                        Text("${item.pcr}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                                        Text(
                                            text = item.sentiment,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (item.sentiment.contains("Bullish")) MarketGreen else if (item.sentiment.contains("Bearish")) MarketRed else MarketGold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.weight(1.3f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Alerts list
                    item {
                        Text(
                            text = "INSTITUTIONAL OI ALERTS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(data.alerts, key = { it.id }) { alert ->
                        OiAlertCard(alert = alert)
                    }
                }

                // Tab 1: Call vs Put Bar Visualizer
                1 -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("CALL (RED) VS PUT (GREEN) OPEN INTEREST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MarketGold)

                                val maxOi = data.strikes.maxOfOrNull { maxOf(it.callOi, it.putOi) }?.toFloat() ?: 1f

                                data.strikes.forEach { strike ->
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${(strike.callOi / 1000)}k CE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MarketRed,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = "₹${strike.strikePrice.toInt()}${if (strike.isAtm) " (ATM)" else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (strike.isAtm) FontWeight.Black else FontWeight.Bold,
                                                color = if (strike.isAtm) MarketGold else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${(strike.putOi / 1000)}k PE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MarketGreen,
                                                fontSize = 9.sp
                                            )
                                        }

                                        // Split bar chart
                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            // Call OI bar (aligns right to center)
                                            Box(
                                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(strike.callOi / maxOi)
                                                        .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                                                        .background(MarketRed)
                                                )
                                            }

                                            // Put OI bar (aligns left to center)
                                            Box(
                                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(strike.putOi / maxOi)
                                                        .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                                                        .background(MarketGreen)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 2: Index Option Chain Table
                2 -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "INDEX OPTION CHAIN TABLE • TAP STRIKE TO TRADE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MarketGold
                                )

                                // Header row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("CALL OI", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketRed, modifier = Modifier.weight(0.9f))
                                    Text("CE LTP", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                    Text("STRIKE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketGold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                    Text("PE LTP", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Start)
                                    Text("PUT OI", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MarketGreen, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                }
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                data.strikes.forEach { strike ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (strike.isAtm) MarketGold.copy(alpha = 0.12f) else Color.Transparent)
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${(strike.callOi / 1000)}k",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MarketRed,
                                            modifier = Modifier.weight(0.9f)
                                        )

                                        // Call LTP (Clickable)
                                        Text(
                                            text = "₹${strike.callLtp}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier
                                                .weight(0.9f)
                                                .clickable { onStrikeSelected(strike, true) },
                                            textAlign = TextAlign.End
                                        )

                                        // Strike
                                        Text(
                                            text = "${strike.strikePrice.toInt()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = if (strike.isAtm) MarketGold else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )

                                        // Put LTP (Clickable)
                                        Text(
                                            text = "₹${strike.putLtp}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier
                                                .weight(0.9f)
                                                .clickable { onStrikeSelected(strike, false) },
                                            textAlign = TextAlign.Start
                                        )

                                        Text(
                                            text = "${(strike.putOi / 1000)}k",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MarketGreen,
                                            modifier = Modifier.weight(0.9f),
                                            textAlign = TextAlign.End
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

@Composable
fun OiAlertCard(
    alert: OiAlertItem,
    modifier: Modifier = Modifier
) {
    val alertColor = when (alert.severity) {
        "BULLISH" -> MarketGreen
        "BEARISH" -> MarketRed
        else -> MarketGold
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, alertColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(alertColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (alert.severity == "BULLISH") Icons.Default.TrendingUp else Icons.Default.Warning,
                    contentDescription = null,
                    tint = alertColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = alert.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
