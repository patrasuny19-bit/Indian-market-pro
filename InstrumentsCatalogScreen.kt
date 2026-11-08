package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstrumentSegment
import com.example.model.MarketDataCatalog
import com.example.model.MarketInstrument
import com.example.ui.theme.TradingCandleDown
import com.example.ui.theme.TradingCandleUp
import com.example.ui.theme.TradingDarkBorder
import com.example.ui.theme.TradingDarkCard
import com.example.ui.theme.TradingPrimaryBlue

@Composable
fun InstrumentsCatalogScreen(
    modifier: Modifier = Modifier,
    onSelectInstrument: (MarketInstrument) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSegment by remember { mutableStateOf<InstrumentSegment?>(null) }

    val filteredInstruments = remember(searchQuery, selectedSegment) {
        MarketDataCatalog.INSTRUMENTS.filter { inst ->
            val matchQuery = searchQuery.isBlank() ||
                inst.symbol.contains(searchQuery, ignoreCase = true) ||
                inst.name.contains(searchQuery, ignoreCase = true)
            val matchSegment = selectedSegment == null || inst.segment == selectedSegment
            matchQuery && matchSegment
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search NSE, BSE, Indices, Futures, Options...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = TradingPrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Segment Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedSegment == null,
                onClick = { selectedSegment = null },
                label = { Text("ALL (${MarketDataCatalog.INSTRUMENTS.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TradingPrimaryBlue,
                    selectedLabelColor = Color.White
                )
            )

            InstrumentSegment.values().forEach { seg ->
                val isSel = selectedSegment == seg
                FilterChip(
                    selected = isSel,
                    onClick = { selectedSegment = seg },
                    label = { Text(seg.name, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TradingPrimaryBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Instruments List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredInstruments, key = { it.symbol }) { inst ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectInstrument(inst) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = inst.symbol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Surface(
                                    color = when (inst.segment) {
                                        InstrumentSegment.INDEX -> Color(0xFF2962FF).copy(alpha = 0.2f)
                                        InstrumentSegment.EQUITY -> Color(0xFF00BCD4).copy(alpha = 0.2f)
                                        InstrumentSegment.FUTURES -> Color(0xFFAB47BC).copy(alpha = 0.2f)
                                        InstrumentSegment.OPTIONS -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${inst.exchange} • ${inst.segment}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (inst.segment) {
                                            InstrumentSegment.INDEX -> Color(0xFF2962FF)
                                            InstrumentSegment.EQUITY -> Color(0xFF00BCD4)
                                            InstrumentSegment.FUTURES -> Color(0xFFAB47BC)
                                            InstrumentSegment.OPTIONS -> Color(0xFFFF9800)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = inst.name,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )

                            if (inst.lotSize > 1) {
                                Text(
                                    text = "Lot Size: ${inst.lotSize}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFF5A623),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹" + String.format("%.2f", inst.currentPrice),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val color = if (inst.isPositive) TradingCandleUp else TradingCandleDown
                            Text(
                                text = "${if (inst.isPositive) "+" else ""}${String.format("%.2f", inst.change)} (${if (inst.isPositive) "+" else ""}${String.format("%.2f", inst.changePercent)}%)",
                                color = color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.clickable { onSelectInstrument(inst) }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ShowChart,
                                    contentDescription = "Chart",
                                    tint = TradingPrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Open Chart", color = TradingPrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
