package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketIndex
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun IndexSummaryCard(
    index: MarketIndex,
    isFlashing: Boolean = false,
    isFlashGreen: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPos = index.isPositive
    val accentColor = if (isPos) MarketGreen else MarketRed

    val flashOverlay by animateColorAsState(
        targetValue = if (isFlashing) {
            if (isFlashGreen) MarketGreen.copy(alpha = 0.22f) else MarketRed.copy(alpha = 0.22f)
        } else Color.Transparent,
        animationSpec = tween(durationMillis = 350),
        label = "flashCard"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("index_card_${index.symbol.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(flashOverlay)
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Top row: Symbol, Exchange badge, Sparkline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = index.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = index.exchange,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Mini Sparkline
                    if (index.sparkline.isNotEmpty()) {
                        SparklineMini(
                            data = index.sparkline,
                            color = accentColor,
                            modifier = Modifier
                                .width(80.dp)
                                .height(26.dp)
                        )
                    }
                }

                // Middle row: LTP & Change Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%,.2f", index.value),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = if (isPos) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            val sign = if (isPos) "+" else ""
                            Text(
                                text = "$sign${String.format(java.util.Locale.US, "%.2f", index.change)} (${String.format(java.util.Locale.US, "%.2f", index.changePercent)}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }

                // Bottom row: Day's range bar
                val range = (index.high - index.low).coerceAtLeast(1.0)
                val progress = ((index.value - index.low) / range).coerceIn(0.0, 1.0).toFloat()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "L: ${String.format(java.util.Locale.US, "%,.1f", index.low)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Day Range",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 9.sp
                        )
                        Text(
                            text = "H: ${String.format(java.util.Locale.US, "%,.1f", index.high)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Range Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(accentColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SparklineMini(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val min = data.minOrNull() ?: 0f
        val max = data.maxOrNull() ?: 1f
        val range = (max - min).coerceAtLeast(0.001f)

        val path = Path()
        val stepX = size.width / (data.size - 1)

        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = size.height - ((value - min) / range * (size.height * 0.85f)) - (size.height * 0.05f)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
