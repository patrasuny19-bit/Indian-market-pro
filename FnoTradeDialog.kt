package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.OrderType
import com.example.data.model.StockQuote
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import java.util.Locale

@Composable
fun FnoTradeDialog(
    stock: StockQuote,
    initialContract: String? = null,
    availableMargin: Double,
    onDismiss: () -> Unit,
    onExecuteFnoTrade: (contractSymbol: String, type: OrderType, quantity: Int, price: Double) -> Unit,
    onAddDemoFunds: ((Double) -> Unit)? = null
) {
    val spotPrice = stock.ltp
    val lotSize = if (stock.lotSize > 0) stock.lotSize else 25

    // Option strike step calculation based on price
    val strikeStep = remember(spotPrice) {
        when {
            spotPrice >= 40000 -> 100.0
            spotPrice >= 15000 -> 50.0
            spotPrice >= 5000 -> 50.0
            spotPrice >= 2000 -> 20.0
            spotPrice >= 800 -> 10.0
            spotPrice >= 200 -> 5.0
            else -> 2.5
        }
    }

    val atmStrike = remember(spotPrice, strikeStep) {
        (Math.round(spotPrice / strikeStep) * strikeStep)
    }

    // Generate strikes (-4 to +4 from ATM)
    val strikes = remember(atmStrike, strikeStep) {
        (-3..3).map { i -> atmStrike + (i * strikeStep) }
    }

    // State
    var isCall by remember { mutableStateOf(!initialContract.orEmpty().contains("PE")) }
    var selectedStrike by remember {
        mutableDoubleStateOf(
            initialContract?.split(" ")?.find { it.toDoubleOrNull() != null }?.toDoubleOrNull() ?: atmStrike
        )
    }
    var orderType by remember { mutableStateOf(OrderType.BUY) }
    var lots by remember { mutableIntStateOf(1) }
    var selectedExpiry by remember { mutableStateOf("Weekly (Thu)") }

    // Estimate option premium based on distance from spot
    val estimatedPremium = remember(selectedStrike, spotPrice, isCall) {
        val diff = if (isCall) spotPrice - selectedStrike else selectedStrike - spotPrice
        val intrinsic = diff.coerceAtLeast(0.0)
        val timeValue = (spotPrice * 0.015).coerceAtLeast(5.0)
        val prem = intrinsic + timeValue
        Math.round(prem * 10.0) / 10.0
    }

    var limitPriceText by remember(estimatedPremium) {
        mutableStateOf(String.format(Locale.US, "%.2f", estimatedPremium))
    }
    var isMarketOrder by remember { mutableStateOf(true) }

    val execPrice = if (isMarketOrder) estimatedPremium else (limitPriceText.toDoubleOrNull() ?: estimatedPremium)
    val totalQty = lots * lotSize
    val totalPremiumRequired = execPrice * totalQty

    val contractSymbol = "${stock.symbol} ${selectedStrike.toInt()} ${if (isCall) "CE" else "PE"}"
    val isBuy = orderType == OrderType.BUY
    val themeColor = if (isBuy) MarketGreen else MarketRed

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("fno_trade_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header: Contract info and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "TRADE F&O OPTIONS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MarketGold.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MarketGold.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "DERIVATIVES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MarketGold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${stock.symbol} Spot: ₹${String.format(Locale.US, "%,.2f", spotPrice)} | Lot: $lotSize",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Call (CE) vs Put (PE) Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCall) MarketGreen else androidx.compose.ui.graphics.Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isCall = true }
                    ) {
                        Text(
                            text = "CALL (CE) - Bullish ▲",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isCall) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isCall) MarketRed else androidx.compose.ui.graphics.Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isCall = false }
                    ) {
                        Text(
                            text = "PUT (PE) - Bearish ▼",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (!isCall) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // Buy vs Sell Action Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isBuy) MarketGreen.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent,
                        border = if (isBuy) androidx.compose.foundation.BorderStroke(1.dp, MarketGreen) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { orderType = OrderType.BUY }
                    ) {
                        Text(
                            text = "BUY OPTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isBuy) MarketGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (!isBuy) MarketRed.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent,
                        border = if (!isBuy) androidx.compose.foundation.BorderStroke(1.dp, MarketRed) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { orderType = OrderType.SELL }
                    ) {
                        Text(
                            text = "SELL (WRITE)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (!isBuy) MarketRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                // Strike Price Picker (Horizontal Chips)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SELECT STRIKE PRICE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Active: ${selectedStrike.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(strikes) { strike ->
                            val isSelected = strike == selectedStrike
                            val isAtm = strike == atmStrike
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStrike = strike },
                                label = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${strike.toInt()}${if (isAtm) " (ATM)" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected || isAtm) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isCall) MarketGreen else MarketRed,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Lot Selector (Lots x LotSize = Qty)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("QUANTITY (LOTS)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$lots Lots = $totalQty Qty", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { if (lots > 1) lots-- },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = "$lots",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { lots++ },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Order Price & Calculation Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Contract:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(contractSymbol, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Est. Premium Price:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format(Locale.US, "%.2f", execPrice)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = themeColor)
                        }
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBuy) "Premium Payable:" else "Margin Required:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", totalPremiumRequired)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = themeColor
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Available Margin:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format(Locale.US, "%,.2f", availableMargin)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (availableMargin >= totalPremiumRequired) MarketGreen else MarketRed)
                        }

                        if (onAddDemoFunds != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (totalPremiumRequired > availableMargin) "⚠️ Low Demo Margin" else "Practice Account",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (totalPremiumRequired > availableMargin) MarketRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (totalPremiumRequired > availableMargin) FontWeight.Bold else FontWeight.Normal
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MarketGreen.copy(alpha = 0.16f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable { onAddDemoFunds(500000.0) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = MarketGreen, modifier = Modifier.size(13.dp))
                                        Text("+₹5 Lakh Demo Funds", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MarketGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                // Execute Order Button
                Button(
                    onClick = {
                        onExecuteFnoTrade(contractSymbol, orderType, totalQty, execPrice)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("execute_fno_trade_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${orderType.name} $contractSymbol (₹${String.format(Locale.US, "%,.0f", totalPremiumRequired)})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
