package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.SectorFlowMetric
import com.example.ui.theme.MarketCyan
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import java.util.Locale

@Composable
fun SectorOrderFlowChartsSection(
    sectors: List<SectorFlowMetric>,
    selectedTimeframe: String,
    onTimeframeChange: (String) -> Unit,
    onSectorClick: (SectorFlowMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableIntStateOf(0) } // 0: Buying vs Selling, 1: Net Money Flow, 2: Advance/Decline Breadth, 3: Momentum Ranking

    val timeframes = listOf("1m", "5m", "15m", "30m", "1H", "1D", "1W")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeframe selector bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIMEFRAME:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                )
                timeframes.forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .clickable { onTimeframeChange(tf) }
                            .testTag("sector_tf_$tf")
                    ) {
                        Text(
                            text = tf,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Chart Type Selector Tabs
        TabRow(
            selectedTabIndex = chartType,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(selected = chartType == 0, onClick = { chartType = 0 }, text = { Text("Buy vs Sell", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = chartType == 1, onClick = { chartType = 1 }, text = { Text("Net Flow (₹ Cr)", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = chartType == 2, onClick = { chartType = 2 }, text = { Text("Breadth", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = chartType == 3, onClick = { chartType = 3 }, text = { Text("Momentum", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
        }

        // Render the Selected Interactive Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (chartType) {
                    0 -> {
                        // Buying vs Selling Comparative Bars
                        Text(
                            text = "SECTOR BUYING VS SELLING PRESSURE (%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        sectors.forEach { sec ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSectorClick(sec) }
                                    .padding(vertical = 3.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sec.sectorName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${String.format(Locale.US, "%.0f", sec.buyingPressurePercent)}% Buy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MarketGreen)
                                        Text("${String.format(Locale.US, "%.0f", sec.sellingPressurePercent)}% Sell", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MarketRed)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MarketRed)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((sec.buyingPressurePercent.toFloat() / 100f).coerceIn(0.05f, 0.95f))
                                            .fillMaxHeight()
                                            .background(MarketGreen)
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Net Flow in ₹ Crores (Horizontal Bar Histogram)
                        Text(
                            text = "INSTITUTIONAL NET FLOW (₹ CRORES)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val maxFlow = sectors.maxOfOrNull { Math.abs(it.netFlowCr) }?.coerceAtLeast(10.0) ?: 100.0

                        sectors.sortedByDescending { it.netFlowCr }.forEach { sec ->
                            val isNetPos = sec.netFlowCr >= 0
                            val flowRatio = (Math.abs(sec.netFlowCr) / maxFlow).toFloat().coerceIn(0.04f, 1.0f)
                            val flowColor = if (isNetPos) MarketGreen else MarketRed

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSectorClick(sec) }
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sec.sectorName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(110.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .padding(horizontal = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(flowRatio)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(flowColor)
                                    )
                                }

                                Text(
                                    text = "${if (isNetPos) "+" else ""}₹${String.format(Locale.US, "%,.1f", sec.netFlowCr)} Cr",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = flowColor,
                                    modifier = Modifier.width(85.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }
                    }
                    2 -> {
                        // Breadth Ratio (Advances vs Declines)
                        Text(
                            text = "SECTOR MARKET BREADTH (ADVANCES VS DECLINES)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        sectors.forEach { sec ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSectorClick(sec) }
                                    .padding(vertical = 3.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sec.sectorName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${sec.advancingCount} Adv • ${sec.decliningCount} Dec • ${sec.unchangedCount} Unch",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MarketRed)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((sec.advanceRatio).coerceIn(0.04f, 0.96f))
                                            .fillMaxHeight()
                                            .background(MarketGreen)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // Momentum Score Ranking
                        Text(
                            text = "SECTOR RELATIVE MOMENTUM SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        sectors.sortedByDescending { it.momentumScore }.forEach { sec ->
                            val isPosMom = sec.momentumScore >= 0
                            val momColor = if (isPosMom) MarketGreen else MarketRed

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSectorClick(sec) }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(sec.sectorName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("RS vs Nifty: ${if (sec.relativeStrength >= 0) "+" else ""}${sec.relativeStrength}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = momColor.copy(alpha = 0.16f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, momColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "${if (isPosMom) "+" else ""}${String.format(Locale.US, "%.2f", sec.momentumScore)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = momColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
