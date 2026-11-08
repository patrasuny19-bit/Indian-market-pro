package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SectorFlowMetric
import java.util.Locale

@Composable
fun SectorHeatmap360View(
    sectors: List<SectorFlowMetric>,
    onSectorClick: (SectorFlowMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Color Legend Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "360° SECTOR BUYING & SELLING HEATMAP LEGEND",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem("Strong Buy", Color(0xFF007E33))
                    LegendItem("Mod Buy", Color(0xFF00C851))
                    LegendItem("Neutral", Color(0xFF757575))
                    LegendItem("Mod Sell", Color(0xFFFF4444))
                    LegendItem("Strong Sell", Color(0xFFCC0000))
                }
            }
        }

        // Sector Tiles arranged in 2 columns (safe inside scrollable parents without infinite constraint issues)
        sectors.chunked(2).forEach { rowPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPair.forEach { sector ->
                    val buyPct = sector.buyingPressurePercent

                    // 5 Exact Color Tiers
                    val (tileBg, textColor) = when {
                        buyPct >= 70.0 -> Pair(Color(0xFF007E33), Color.White) // Dark Green: Very Strong Buying
                        buyPct >= 55.0 -> Pair(Color(0xFF00C851), Color.Black) // Light Green: Moderate Buying
                        buyPct <= 30.0 -> Pair(Color(0xFFCC0000), Color.White) // Dark Red: Very Strong Selling
                        buyPct <= 45.0 -> Pair(Color(0xFFFF4444), Color.White) // Light Red: Moderate Selling
                        else -> Pair(Color(0xFF616161), Color.White) // Grey: Neutral (45-55%)
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSectorClick(sector) }
                            .testTag("sector_heatmap_tile_${sector.sectorName}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = tileBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Sector Name & % Change
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sector.sectorName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${if (sector.isPositive) "+" else ""}${String.format(Locale.US, "%.2f", sector.indexChangePercent)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )
                            }

                            // Buying vs Selling Strength
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("BUY STRENGTH", fontSize = 8.sp, color = textColor.copy(alpha = 0.8f))
                                    Text("${String.format(Locale.US, "%.0f", sector.buyingPressurePercent)}%", fontSize = 11.sp, fontWeight = FontWeight.Black, color = textColor)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("SELL STRENGTH", fontSize = 8.sp, color = textColor.copy(alpha = 0.8f))
                                    Text("${String.format(Locale.US, "%.0f", sector.sellingPressurePercent)}%", fontSize = 11.sp, fontWeight = FontWeight.Black, color = textColor)
                                }
                            }

                            // Advance / Decline breadth indicator
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Adv: ${sector.advancingCount} • Dec: ${sector.decliningCount}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor.copy(alpha = 0.95f)
                                )
                                Text(
                                    text = "${sector.totalStocks} F&O",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // If odd number of sectors in row, fill the spacer
                if (rowPair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
