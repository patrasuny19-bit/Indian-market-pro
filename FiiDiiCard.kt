package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FiiDiiRecord
import com.example.ui.theme.MarketCyan
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun FiiDiiCard(
    records: List<FiiDiiRecord>,
    modifier: Modifier = Modifier
) {
    val latest = records.firstOrNull() ?: return
    val totalNet = latest.fiiNetCash + latest.diiNetCash

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .testTag("fii_dii_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "INSTITUTIONAL RADAR (FII / DII)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cash & Derivatives Flow in ₹ Crores",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (totalNet >= 0) MarketGreen.copy(alpha = 0.15f) else MarketRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (totalNet >= 0) "NET INFLOW" else "NET OUTFLOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (totalNet >= 0) MarketGreen else MarketRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Two main stat boxes: FII Cash vs DII Cash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // FII Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(MarketCyan))
                            Text("FII Net Cash", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val fiiIsPos = latest.fiiNetCash >= 0
                        Text(
                            text = "${if (fiiIsPos) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", latest.fiiNetCash)} Cr",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (fiiIsPos) MarketGreen else MarketRed
                        )
                    }
                }

                // DII Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(MarketGold))
                            Text("DII Net Cash", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val diiIsPos = latest.diiNetCash >= 0
                        Text(
                            text = "${if (diiIsPos) "+" else ""}₹${String.format(java.util.Locale.US, "%,.1f", latest.diiNetCash)} Cr",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (diiIsPos) MarketGreen else MarketRed
                        )
                    }
                }
            }

            // Historical 5-session comparison table
            Text(
                text = "Recent Sessions Activity",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            records.take(4).forEach { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "FII: ${if (record.fiiNetCash >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.0f", record.fiiNetCash)} Cr",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (record.fiiNetCash >= 0) MarketGreen else MarketRed,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "DII: ${if (record.diiNetCash >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.0f", record.diiNetCash)} Cr",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (record.diiNetCash >= 0) MarketGreen else MarketRed,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
