package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun OrderDialog(
    stock: StockQuote,
    initialType: OrderType,
    availableMargin: Double,
    onDismiss: () -> Unit,
    onExecuteOrder: (String, OrderType, ProductType, OrderCategory, Int, Double) -> Unit,
    onAddDemoFunds: ((Double) -> Unit)? = null
) {
    var orderType by remember { mutableStateOf(initialType) }
    var productType by remember { mutableStateOf(if (stock.isFnO) ProductType.MIS else ProductType.CNC) }
    var orderCategory by remember { mutableStateOf(OrderCategory.MARKET) }
    var quantity by remember { mutableIntStateOf(if (stock.lotSize > 1) stock.lotSize else 10) }
    var limitPriceText by remember { mutableStateOf(String.format(java.util.Locale.US, "%.2f", stock.ltp)) }

    val execPrice = if (orderCategory == OrderCategory.MARKET) stock.ltp else (limitPriceText.toDoubleOrNull() ?: stock.ltp)
    val marginRequired = (execPrice * quantity) / if (productType == ProductType.MIS) 5.0 else 1.0

    val isBuy = orderType == OrderType.BUY
    val themeColor = if (isBuy) MarketGreen else MarketRed

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("order_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top header with Buy/Sell toggle and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isBuy) MarketGreen else androidx.compose.ui.graphics.Color.Transparent,
                            modifier = Modifier.clickable { orderType = OrderType.BUY }
                        ) {
                            Text(
                                text = "BUY",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isBuy) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isBuy) MarketRed else androidx.compose.ui.graphics.Color.Transparent,
                            modifier = Modifier.clickable { orderType = OrderType.SELL }
                        ) {
                            Text(
                                text = "SELL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (!isBuy) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Symbol info & LTP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stock.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stock.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${String.format(java.util.Locale.US, "%,.2f", stock.ltp)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = themeColor
                        )
                        Text(
                            text = "${if (stock.isPositive) "+" else ""}${String.format(java.util.Locale.US, "%.2f", stock.changePercent)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Product Type: MIS vs CNC
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PRODUCT TYPE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = productType == ProductType.MIS,
                            onClick = { productType = ProductType.MIS },
                            label = { Text("Intraday (MIS 5x)", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = productType == ProductType.CNC,
                            onClick = { productType = ProductType.CNC },
                            label = { Text("Delivery (CNC)", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Order Category: MARKET vs LIMIT
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ORDER TYPE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = orderCategory == OrderCategory.MARKET,
                            onClick = { orderCategory = OrderCategory.MARKET },
                            label = { Text("Market", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = orderCategory == OrderCategory.LIMIT,
                            onClick = { orderCategory = OrderCategory.LIMIT },
                            label = { Text("Limit", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Quantity Stepper with lot size
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("QUANTITY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (stock.lotSize > 1) {
                            Text("Lot Size: ${stock.lotSize}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                val step = if (stock.lotSize > 1) stock.lotSize else 5
                                quantity = (quantity - step).coerceAtLeast(step)
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }

                        OutlinedTextField(
                            value = quantity.toString(),
                            onValueChange = { quantity = it.toIntOrNull() ?: 1 },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        FilledTonalIconButton(
                            onClick = {
                                val step = if (stock.lotSize > 1) stock.lotSize else 5
                                quantity += step
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                // Limit Price if LIMIT order
                if (orderCategory == OrderCategory.LIMIT) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("LIMIT PRICE (₹)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = limitPriceText,
                            onValueChange = { limitPriceText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Margin details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Margin Req.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(java.util.Locale.US, "%,.2f", marginRequired)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Available Margin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(java.util.Locale.US, "%,.2f", availableMargin)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MarketGreen)
                    }
                }

                // Demo Funds Instant Top-up Option
                if (onAddDemoFunds != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (marginRequired > availableMargin) "⚠️ Low Demo Margin" else "Practice Account",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (marginRequired > availableMargin) MarketRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (marginRequired > availableMargin) FontWeight.Bold else FontWeight.Normal
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

                // Execute Button
                Button(
                    onClick = {
                        onExecuteOrder(
                            stock.symbol,
                            orderType,
                            productType,
                            orderCategory,
                            quantity,
                            execPrice
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("execute_order_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColor,
                        contentColor = androidx.compose.ui.graphics.Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${orderType.label} $quantity ${stock.symbol} @ ₹${String.format(java.util.Locale.US, "%,.2f", execPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
