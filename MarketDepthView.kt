package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketDepth
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun MarketDepthView(
    depth: MarketDepth,
    modifier: Modifier = Modifier
) {
    val maxQty = maxOf(
        depth.rows.maxOfOrNull { it.bidQty } ?: 1L,
        depth.rows.maxOfOrNull { it.askQty } ?: 1L
    ).toFloat()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .testTag("market_depth_view"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Market Depth (Level 2) and sentiment percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORDER BOOK (DEPTH)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${depth.buyerPercentage}% Buyers vs ${100 - depth.buyerPercentage}% Sellers",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (depth.buyerPercentage >= 50) MarketGreen else MarketRed,
                    fontWeight = FontWeight.Bold
                )
            }

            // Buyer / Seller sentiment gauge bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MarketRed.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(depth.buyerPercentage / 100f)
                        .fillMaxHeight()
                        .background(MarketGreen)
                )
            }

            // Depth Columns Table Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bid side headers
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Orders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Bid Qty", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Bid Price", style = MaterialTheme.typography.labelSmall, color = MarketGreen, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Ask side headers
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ask Price", style = MaterialTheme.typography.labelSmall, color = MarketRed, fontWeight = FontWeight.Bold)
                    Text("Ask Qty", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Orders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 5 Depth Rows
            depth.rows.forEach { row ->
                val bidRatio = (row.bidQty / maxQty).coerceIn(0.05f, 1f)
                val askRatio = (row.askQty / maxQty).coerceIn(0.05f, 1f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bid Side Box with background fill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        // Depth bar background
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(bidRatio)
                                .align(Alignment.CenterEnd)
                                .background(MarketGreen.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${row.bidOrders}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${row.bidQty}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f", row.bidPrice),
                                style = MaterialTheme.typography.labelSmall,
                                color = MarketGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Ask Side Box with background fill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Depth bar background
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(askRatio)
                                .align(Alignment.CenterStart)
                                .background(MarketRed.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f", row.askPrice),
                                style = MaterialTheme.typography.labelSmall,
                                color = MarketRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${row.askQty}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${row.askOrders}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Totals Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Bid Quantity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    Text(String.format(java.util.Locale.US, "%,d", depth.totalBidQty), style = MaterialTheme.typography.labelMedium, color = MarketGreen, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Ask Quantity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    Text(String.format(java.util.Locale.US, "%,d", depth.totalAskQty), style = MaterialTheme.typography.labelMedium, color = MarketRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
