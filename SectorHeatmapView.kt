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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeatmapSizingMode
import com.example.data.model.StockQuote

@Composable
fun SectorHeatmapView(
    stocks: List<StockQuote>,
    sizingMode: HeatmapSizingMode,
    onStockClick: (StockQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    if (stocks.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No stocks match the selected sector/filter",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Determine grid columns based on sizing mode
    val columnsCount = when (sizingMode) {
        HeatmapSizingMode.BY_MARKET_CAP -> 4
        HeatmapSizingMode.BY_VOLUME -> 4
        HeatmapSizingMode.EQUAL_WEIGHT -> 3
    }

    // Calculate maximum market cap and volume for proportional sizing
    val maxCap = stocks.maxOfOrNull { it.marketCapCr } ?: 1.0
    val maxVol = stocks.maxOfOrNull { it.volume } ?: 1L

    // Pack stocks into rows of tiles to safely embed inside LazyColumn without infinite height crash
    val rows = remember(stocks, sizingMode, maxCap, maxVol) {
        val list = mutableListOf<List<Pair<StockQuote, Int>>>()
        var currentRow = mutableListOf<Pair<StockQuote, Int>>()
        var currentSlots = 0

        stocks.forEach { stock ->
            val span = when (sizingMode) {
                HeatmapSizingMode.BY_MARKET_CAP -> {
                    val ratio = stock.marketCapCr / maxCap
                    if (ratio > 0.45) 2 else 1
                }
                HeatmapSizingMode.BY_VOLUME -> {
                    val ratio = stock.volume.toDouble() / maxVol.coerceAtLeast(1L)
                    if (ratio > 0.40) 2 else 1
                }
                HeatmapSizingMode.EQUAL_WEIGHT -> 1
            }

            if (currentSlots + span > columnsCount) {
                if (currentRow.isNotEmpty()) {
                    list.add(currentRow)
                }
                currentRow = mutableListOf(Pair(stock, span))
                currentSlots = span
            } else {
                currentRow.add(Pair(stock, span))
                currentSlots += span
            }
        }
        if (currentRow.isNotEmpty()) {
            list.add(currentRow)
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sector_heatmap_grid"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val usedSlots = rowItems.sumOf { it.second }
                rowItems.forEach { (stock, span) ->
                    HeatmapTile(
                        stock = stock,
                        sizingMode = sizingMode,
                        maxMarketCap = maxCap,
                        maxVolume = maxVol,
                        onClick = { onStockClick(stock) },
                        modifier = Modifier.weight(span.toFloat())
                    )
                }
                if (usedSlots < columnsCount) {
                    Spacer(modifier = Modifier.weight((columnsCount - usedSlots).toFloat()))
                }
            }
        }
    }
}

@Composable
fun HeatmapTile(
    stock: StockQuote,
    sizingMode: HeatmapSizingMode,
    maxMarketCap: Double,
    maxVolume: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pct = stock.changePercent

    // Intensity color mapping for financial heatmap
    val (bgColor, borderColor, textOnTileColor) = getHeatmapColors(pct)

    // Compute tile min height based on sizing mode
    val minHeight = when (sizingMode) {
        HeatmapSizingMode.BY_MARKET_CAP -> {
            val ratio = (stock.marketCapCr / maxMarketCap).coerceIn(0.0, 1.0)
            (76 + (ratio * 34)).dp
        }
        HeatmapSizingMode.BY_VOLUME -> {
            val ratio = (stock.volume.toDouble() / maxVolume.coerceAtLeast(1L)).coerceIn(0.0, 1.0)
            (76 + (ratio * 34)).dp
        }
        HeatmapSizingMode.EQUAL_WEIGHT -> 84.dp
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("heatmap_tile_${stock.symbol}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Symbol + Live status dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = textOnTileColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.5.sp
                )

                if (stock.isRealTimeFresh) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(Color.White.copy(alpha = 0.85f))
                    )
                }
            }

            // Middle: Percentage change badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val icon = if (pct >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textOnTileColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${if (pct > 0) "+" else ""}${String.format(java.util.Locale.US, "%.2f", pct)}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = textOnTileColor,
                    fontSize = 12.sp
                )
            }

            // Bottom: LTP and Volume
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "₹${String.format(java.util.Locale.US, "%,.2f", stock.ltp)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = textOnTileColor.copy(alpha = 0.95f),
                    fontSize = 10.5.sp,
                    maxLines = 1
                )

                val volStr = when {
                    stock.volume >= 10_000_000 -> "${String.format(java.util.Locale.US, "%.1f", stock.volume / 10_000_000.0)}Cr"
                    stock.volume >= 100_000 -> "${String.format(java.util.Locale.US, "%.1f", stock.volume / 100_000.0)}L"
                    stock.volume >= 1_000 -> "${String.format(java.util.Locale.US, "%.1f", stock.volume / 1_000.0)}K"
                    else -> "${stock.volume}"
                }

                Text(
                    text = "Vol: $volStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = textOnTileColor.copy(alpha = 0.75f),
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Returns (Background Color, Border Color, Text Color) based on percentage change.
 */
fun getHeatmapColors(changePercent: Double): Triple<Color, Color, Color> {
    return when {
        // Deep emerald green for big winners
        changePercent >= 3.0 -> Triple(
            Color(0xFF00695C), // Deep Teal Green
            Color(0xFF26A69A),
            Color.White
        )
        changePercent >= 1.5 -> Triple(
            Color(0xFF0F9D58), // Strong Green
            Color(0xFF48BB78),
            Color.White
        )
        changePercent >= 0.5 -> Triple(
            Color(0xFF1E7E34), // Medium Green
            Color(0xFF38A169),
            Color.White
        )
        changePercent > 0.0 -> Triple(
            Color(0xFF1B5E20).copy(alpha = 0.75f), // Soft Green
            Color(0xFF2E7D32),
            Color(0xFFE8F5E9)
        )
        // Deep crimson red for big decliners
        changePercent <= -3.0 -> Triple(
            Color(0xFFB71C1C), // Deep Crimson Red
            Color(0xFFE53935),
            Color.White
        )
        changePercent <= -1.5 -> Triple(
            Color(0xFFC62828), // Strong Red
            Color(0xFFEF5350),
            Color.White
        )
        changePercent <= -0.5 -> Triple(
            Color(0xFFD32F2F).copy(alpha = 0.85f), // Medium Red
            Color(0xFFE57373),
            Color.White
        )
        changePercent < 0.0 -> Triple(
            Color(0xFF7F1D1D), // Soft Dark Red
            Color(0xFF991B1B),
            Color(0xFFFFEBEE)
        )
        // Neutral unchanged
        else -> Triple(
            Color(0xFF263238),
            Color(0xFF37474F),
            Color(0xFFECEFF1)
        )
    }
}
