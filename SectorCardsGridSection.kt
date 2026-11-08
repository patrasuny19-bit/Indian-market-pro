package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import java.util.Locale

@Composable
fun SectorCardsGridSection(
    sectors: List<SectorFlowMetric>,
    onSectorClick: (SectorFlowMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableIntStateOf(0) } // 0: All 20, 1: Buying Flow (>50%), 2: Selling Flow (<50%), 3: Breakouts

    val filteredSectors = remember(sectors, searchQuery, filterType) {
        var list = sectors
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.sectorName.lowercase().contains(q) ||
                it.indexName.lowercase().contains(q)
            }
        }
        when (filterType) {
            1 -> list.filter { it.buyingPressurePercent >= 50.0 }.sortedByDescending { it.buyingPressurePercent }
            2 -> list.filter { it.buyingPressurePercent < 50.0 }.sortedBy { it.buyingPressurePercent }
            3 -> list.sortedByDescending { it.relativeStrength }
            else -> list
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search & Filter Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search all 20 sectors (e.g. Banking, IT, Pharma, Auto)...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sector_grid_search_field"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == 0,
                onClick = { filterType = 0 },
                label = { Text("All 20 Sectors", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.height(30.dp)
            )
            FilterChip(
                selected = filterType == 1,
                onClick = { filterType = 1 },
                label = { Text("Buying Flow 🟢", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.height(30.dp)
            )
            FilterChip(
                selected = filterType == 2,
                onClick = { filterType = 2 },
                label = { Text("Selling Flow 🔴", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.height(30.dp)
            )
        }

        Text(
            text = "Showing ${filteredSectors.size} Sectors (Minimum 12 Guaranteed) • Tap any card for full stock list",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Detailed Sector Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            filteredSectors.forEach { sec ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSectorClick(sec) }
                        .testTag("sector_card_${sec.sectorName}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header Row: Sector Name, Index Name, Index Value, % Change
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = sec.sectorName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(sec.smartSignal.badgeColorHex).copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = sec.smartSignal.title,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(sec.smartSignal.badgeColorHex),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${sec.indexName} • ₹${String.format(Locale.US, "%,.1f", sec.indexValue)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (sec.isPositive) "+" else ""}${String.format(Locale.US, "%.2f", sec.indexChangePercent)}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (sec.isPositive) MarketGreen else MarketRed
                                    )
                                    Text(
                                        text = "${sec.totalStocks} Stocks",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "View all stocks in sector",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Order Flow Pressure Bar
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

                        // Detailed Metrics Grid: Volume, Net Flow, Adv/Dec, RS, Momentum
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("BUYING VOL", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format(Locale.US, "%.1f", sec.buyingVolume / 1000000.0)}M (${String.format(Locale.US, "%.0f", sec.buyingPressurePercent)}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MarketGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("NET FLOW (₹ CR)", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val netColor = if (sec.netFlowCr >= 0) MarketGreen else MarketRed
                                Text("${if (sec.netFlowCr >= 0) "+" else ""}₹${String.format(Locale.US, "%,.1f", sec.netFlowCr)} Cr", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = netColor)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("SELLING VOL", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format(Locale.US, "%.1f", sec.sellingVolume / 1000000.0)}M (${String.format(Locale.US, "%.0f", sec.sellingPressurePercent)}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MarketRed)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Breadth: ${sec.advancingCount} Adv / ${sec.decliningCount} Dec",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "RS: ${if (sec.relativeStrength >= 0) "+" else ""}${sec.relativeStrength}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sec.relativeStrength >= 0) MarketGreen else MarketRed
                            )
                            Text(
                                text = "Flow: ${sec.moneyFlowDirection.label}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sec.moneyFlowDirection.isPositive) MarketGreen else MarketRed
                            )
                            Text(
                                text = "Phase: ${sec.rotationQuadrant.label}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(sec.rotationQuadrant.colorHex)
                            )
                        }
                    }
                }
            }
        }
    }
}
