package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RotationQuadrant
import com.example.data.model.SectorFlowMetric
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import java.util.Locale

@Composable
fun SectorRotationMatrixSection(
    sectors: List<SectorFlowMetric>,
    onSectorClick: (SectorFlowMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    val leadingSectors = sectors.filter { it.rotationQuadrant == RotationQuadrant.LEADING }
    val improvingSectors = sectors.filter { it.rotationQuadrant == RotationQuadrant.IMPROVING }
    val weakeningSectors = sectors.filter { it.rotationQuadrant == RotationQuadrant.WEAKENING }
    val laggingSectors = sectors.filter { it.rotationQuadrant == RotationQuadrant.LAGGING }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Explanatory Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SECTOR ROTATION DETECTOR (VS NIFTY 50)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tracks relative strength vs benchmark and volume momentum to detect institutional capital rotation between market sectors.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 4 Quadrants Display
        QuadrantCard(
            title = "LEADING SECTORS (OUTPERFORMING)",
            subtitle = "High Relative Strength + Strong Upward Momentum",
            color = Color(0xFF00E676),
            sectors = leadingSectors,
            onSectorClick = onSectorClick
        )

        QuadrantCard(
            title = "IMPROVING SECTORS (REVERSAL & EARLY BUY)",
            subtitle = "Low Relative Strength but Accelerating Fast",
            color = Color(0xFF00E5FF),
            sectors = improvingSectors,
            onSectorClick = onSectorClick
        )

        QuadrantCard(
            title = "WEAKENING SECTORS (PROFIT BOOKING)",
            subtitle = "High Strength but Losing Buying Momentum",
            color = Color(0xFFFFD700),
            sectors = weakeningSectors,
            onSectorClick = onSectorClick
        )

        QuadrantCard(
            title = "LAGGING SECTORS (UNDERPERFORMING)",
            subtitle = "Weak Strength + Negative Money Flow",
            color = Color(0xFFFF5252),
            sectors = laggingSectors,
            onSectorClick = onSectorClick
        )
    }
}

@Composable
private fun QuadrantCard(
    title: String,
    subtitle: String,
    color: Color,
    sectors: List<SectorFlowMetric>,
    onSectorClick: (SectorFlowMetric) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Black, color = color)
                    Text(text = subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.16f)) {
                    Text(
                        text = "${sectors.size} SECTORS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (sectors.isEmpty()) {
                Text(
                    text = "No sectors currently in this phase",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                sectors.forEach { sec ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSectorClick(sec) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = sec.sectorName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = "RS: ${if (sec.relativeStrength >= 0) "+" else ""}${sec.relativeStrength}% • Mom: ${String.format(Locale.US, "%.1f", sec.momentumScore)}",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (sec.isPositive) "+" else ""}${String.format(Locale.US, "%.2f", sec.indexChangePercent)}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (sec.isPositive) MarketGreen else MarketRed
                                    )
                                    Text(
                                        text = "Buy: ${String.format(Locale.US, "%.0f", sec.buyingPressurePercent)}%",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MarketGreen
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
