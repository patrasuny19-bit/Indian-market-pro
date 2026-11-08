package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import java.util.Locale

@Composable
fun AddDemoFundsDialog(
    currentAvailableFunds: Double,
    onDismiss: () -> Unit,
    onAddFunds: (Double) -> Unit,
    onResetAccount: (Double) -> Unit = {}
) {
    var customAmountText by remember { mutableStateOf("500000") }
    var selectedPreset by remember { mutableDoubleStateOf(500000.0) }

    val presetAmounts = listOf(
        50000.0 to "+₹50,000",
        100000.0 to "+₹1 Lakh",
        500000.0 to "+₹5 Lakhs",
        1000000.0 to "+₹10 Lakhs",
        2500000.0 to "+₹25 Lakhs",
        5000000.0 to "+₹50 Lakhs",
        10000000.0 to "+₹1 Crore",
        50000000.0 to "+₹5 Crores"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("add_demo_funds_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MarketGreen.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MarketGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "ADD DEMO FUNDS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Unlimited Virtual Demo Capital",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Current Available Demo Capital Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT AVAILABLE DEMO BALANCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", currentAvailableFunds)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MarketGreen
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MarketGreen.copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MarketGreen.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "PRACTICE MODE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = MarketGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Preset Quick-Add Options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "QUICK TOP-UP PACKS (UNLIMITED)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 4 rows of 2 columns
                    presetAmounts.chunked(2).forEach { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPair.forEach { (amount, label) ->
                                val isSelected = selectedPreset == amount
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MarketGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedPreset = amount
                                            customAmountText = amount.toLong().toString()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = if (isSelected) MarketGreen else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom Amount Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "OR ENTER CUSTOM AMOUNT (₹)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = {
                            customAmountText = it
                            it.toDoubleOrNull()?.let { amt -> selectedPreset = amt }
                        },
                        leadingIcon = {
                            Text("₹", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MarketGreen, modifier = Modifier.padding(start = 12.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("custom_demo_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MarketGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }

                // Primary Action Button: Credit Funds
                Button(
                    onClick = {
                        val amount = customAmountText.toDoubleOrNull() ?: selectedPreset
                        if (amount > 0.0) {
                            onAddFunds(amount)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_add_funds_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MarketGreen,
                        contentColor = Color.Black
                    )
                ) {
                    val amount = customAmountText.toDoubleOrNull() ?: selectedPreset
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "ADD ₹${String.format(Locale.US, "%,.0f", amount)} TO DEMO ACCOUNT",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }

                // Secondary Action: Reset Demo Account
                OutlinedButton(
                    onClick = { onResetAccount(1000000.0) },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("reset_demo_account_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reset Account to Default (₹10 Lakhs)", fontSize = 12.sp)
                }
            }
        }
    }
}
