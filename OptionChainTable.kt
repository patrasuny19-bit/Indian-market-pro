package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OptionChainData
import com.example.data.model.OptionStrike
import com.example.ui.theme.MarketCyan
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketPurple
import com.example.ui.theme.MarketRed

@Composable
fun OptionChainTable(
    optionChain: OptionChainData,
    onStrikeSelected: (OptionStrike, Boolean) -> Unit, // strike, isCall
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .testTag("option_chain_table"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Summary Header: PCR, Max Pain, Spot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${optionChain.underlying} OPTION CHAIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Expiry: ${optionChain.selectedExpiry}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (optionChain.pcr >= 1.0) MarketGreen.copy(alpha = 0.15f) else MarketRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (optionChain.pcr >= 1.0) MarketGreen.copy(alpha = 0.4f) else MarketRed.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "PCR: ${optionChain.pcr}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (optionChain.pcr >= 1.0) MarketGreen else MarketRed
                        )
                        Text(
                            text = if (optionChain.pcr >= 1.0) "BULLISH" else "BEARISH",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (optionChain.pcr >= 1.0) MarketGreen else MarketRed
                        )
                    }
                }
            }

            // Max Pain & OI stats bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Spot:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(java.util.Locale.US, "%,.1f", optionChain.spotPrice), style = MaterialTheme.typography.labelMedium, color = MarketGold, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Max Pain:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(java.util.Locale.US, "%,.0f", optionChain.maxPainStrike), style = MaterialTheme.typography.labelMedium, color = MarketCyan, fontWeight = FontWeight.Bold)
                }
            }

            // Table Column Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CALL OI",
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketCyan,
                    modifier = Modifier.weight(1.2f),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "CALL LTP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketGreen,
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "STRIKE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1.2f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "PUT LTP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketRed,
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PUT OI",
                    style = MaterialTheme.typography.labelSmall,
                    color = MarketPurple,
                    modifier = Modifier.weight(1.2f),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Bold
                )
            }

            // Strikes List
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                optionChain.strikes.forEach { strike ->
                    val isAtm = strike.isAtm
                    val rowBg = if (isAtm) {
                        MarketGold.copy(alpha = 0.12f)
                    } else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)

                    val rowBorderModifier = if (isAtm) {
                        Modifier.border(1.dp, MarketGold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    } else Modifier

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .then(rowBorderModifier)
                            .background(rowBg)
                            .padding(vertical = 5.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Call OI
                        Text(
                            text = String.format(java.util.Locale.US, "%.1fL", strike.callOi / 100000.0),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.Start
                        )

                        // Call LTP (Tappable to Buy Call)
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MarketGreen.copy(alpha = 0.12f))
                                .clickable { onStrikeSelected(strike, true) }
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", strike.callLtp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MarketGreen,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        }

                        // Strike Price
                        Box(
                            modifier = Modifier.weight(1.2f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%,.0f", strike.strikePrice),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isAtm) MarketGold else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Put LTP (Tappable to Buy Put)
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MarketRed.copy(alpha = 0.12f))
                                .clickable { onStrikeSelected(strike, false) }
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", strike.putLtp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MarketRed,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            )
                        }

                        // Put OI
                        Text(
                            text = String.format(java.util.Locale.US, "%.1fL", strike.putOi / 100000.0),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
