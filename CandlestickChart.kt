package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandleData
import com.example.ui.theme.MarketCyan
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun CandlestickChart(
    candles: List<CandleData>,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inspectedCandleIndex by remember { mutableStateOf<Int?>(null) }
    val timeframes = listOf("1D", "1W", "1M", "1Y")

    val activeCandle = inspectedCandleIndex?.let { idx ->
        if (idx in candles.indices) candles[idx] else null
    } ?: candles.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
            .testTag("candlestick_chart_container")
    ) {
        // Timeframe selector bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                timeframes.forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTimeframeSelected(tf) },
                        label = {
                            Text(
                                text = tf,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }

            // Indicator Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MarketCyan)
                )
                Text(
                    text = "EMA 20",
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketCyan,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Candle Inspector HUD
        if (activeCandle != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeCandle.timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "O: ${String.format(java.util.Locale.US, "%.1f", activeCandle.open)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "H: ${String.format(java.util.Locale.US, "%.1f", activeCandle.high)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketGreen
                )
                Text(
                    text = "L: ${String.format(java.util.Locale.US, "%.1f", activeCandle.low)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketRed
                )
                Text(
                    text = "C: ${String.format(java.util.Locale.US, "%.1f", activeCandle.close)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (activeCandle.close >= activeCandle.open) MarketGreen else MarketRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles) {
                        detectTapGestures { offset ->
                            if (candles.isNotEmpty()) {
                                val candleWidth = size.width / candles.size
                                val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                inspectedCandleIndex = index
                            }
                        }
                    }
                    .pointerInput(candles) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                if (candles.isNotEmpty()) {
                                    val candleWidth = size.width / candles.size
                                    val index = (change.position.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                    inspectedCandleIndex = index
                                }
                            },
                            onDragEnd = { /* keep last inspected or release */ },
                            onDragCancel = { inspectedCandleIndex = null }
                        )
                    }
            ) {
                if (candles.isEmpty()) return@Canvas

                val minPrice = candles.minOf { it.low }
                val maxPrice = candles.maxOf { it.high }
                val priceRange = (maxPrice - minPrice).coerceAtLeast(1f)

                val maxVolume = candles.maxOf { it.volume }.coerceAtLeast(1L).toFloat()

                val chartHeight = size.height * 0.76f
                val volumeHeight = size.height * 0.20f
                val volumeBaseY = size.height

                val candleCount = candles.size
                val slotWidth = size.width / candleCount
                val candleBodyWidth = (slotWidth * 0.65f).coerceAtLeast(2f)

                // Draw Horizontal Grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = (chartHeight / gridLines) * i
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }

                // Calculate EMA 20 points
                val emaPath = Path()
                var ema = candles.first().close
                val k = 2f / (20 + 1)

                candles.forEachIndexed { i, candle ->
                    ema = (candle.close * k) + (ema * (1 - k))
                    val centerX = (i * slotWidth) + (slotWidth / 2f)
                    val emaY = chartHeight - ((ema - minPrice) / priceRange * chartHeight)
                    if (i == 0) emaPath.moveTo(centerX, emaY) else emaPath.lineTo(centerX, emaY)
                }

                // Draw Candlesticks & Volumes
                candles.forEachIndexed { i, candle ->
                    val isBullish = candle.close >= candle.open
                    val candleColor = if (isBullish) MarketGreen else MarketRed

                    val centerX = (i * slotWidth) + (slotWidth / 2f)
                    val highY = chartHeight - ((candle.high - minPrice) / priceRange * chartHeight)
                    val lowY = chartHeight - ((candle.low - minPrice) / priceRange * chartHeight)
                    val openY = chartHeight - ((candle.open - minPrice) / priceRange * chartHeight)
                    val closeY = chartHeight - ((candle.close - minPrice) / priceRange * chartHeight)

                    // Wick line
                    drawLine(
                        color = candleColor,
                        start = Offset(centerX, highY),
                        end = Offset(centerX, lowY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Candle Body
                    val bodyTop = Math.min(openY, closeY)
                    val bodyHeight = Math.max(Math.abs(closeY - openY), 2.5f)
                    drawRect(
                        color = candleColor,
                        topLeft = Offset(centerX - candleBodyWidth / 2f, bodyTop),
                        size = Size(candleBodyWidth, bodyHeight)
                    )

                    // Volume Bar at bottom
                    val volBarHeight = (candle.volume / maxVolume) * volumeHeight
                    drawRect(
                        color = candleColor.copy(alpha = 0.35f),
                        topLeft = Offset(centerX - candleBodyWidth / 2f, volumeBaseY - volBarHeight),
                        size = Size(candleBodyWidth, volBarHeight)
                    )
                }

                // Draw EMA 20 line
                drawPath(
                    path = emaPath,
                    color = MarketCyan,
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // Crosshair indicator if candle inspected
                inspectedCandleIndex?.let { idx ->
                    if (idx in candles.indices) {
                        val inspectedX = (idx * slotWidth) + (slotWidth / 2f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.5f),
                            start = Offset(inspectedX, 0f),
                            end = Offset(inspectedX, size.height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }
                }
            }
        }
    }
}
