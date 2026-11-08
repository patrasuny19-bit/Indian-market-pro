package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.market.DataProviderType
import com.example.data.market.MarketDataProviderConfig
import com.example.data.market.MarketFeedHealth
import com.example.data.market.MarketScheduleInfo
import com.example.data.market.ProviderConnectionStatus
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun ProviderSettingsDialog(
    currentConfig: MarketDataProviderConfig,
    feedHealth: MarketFeedHealth,
    marketSchedule: MarketScheduleInfo,
    onDismiss: () -> Unit,
    onSaveConfig: (MarketDataProviderConfig) -> Unit,
    onTestConnection: () -> Unit
) {
    var selectedProvider by remember { mutableStateOf(currentConfig.activeProvider) }
    var apiKeyText by remember { mutableStateOf(currentConfig.apiKey) }
    var accessTokenText by remember { mutableStateOf(currentConfig.accessToken) }
    var customGatewayUrlText by remember { mutableStateOf(currentConfig.customGatewayUrl) }
    var pollingInterval by remember { mutableIntStateOf(currentConfig.autoPollingIntervalSecs) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("provider_settings_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = MarketGreen)
                            Text(
                                text = "MARKET DATA PROVIDER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Licensed Indian Market Data Architecture",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Current Market Session Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (marketSchedule.isLiveTradingActive) MarketGreen else MarketGold)
                                )
                                Text(
                                    text = marketSchedule.statusBadgeText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (marketSchedule.isLiveTradingActive) MarketGreen else MarketGold
                                )
                            }
                            Text(
                                text = marketSchedule.currentIstTimeFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = marketSchedule.nextSessionInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Connection Health:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = feedHealth.status.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (feedHealth.status) {
                                    ProviderConnectionStatus.CONNECTED_LIVE -> MarketGreen
                                    ProviderConnectionStatus.CLOSED_OUT_OF_HOURS -> MarketGold
                                    ProviderConnectionStatus.STALE_CACHED -> MarketGold
                                    else -> MarketRed
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Last Data Sync:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(feedHealth.lastSyncTimeFormatted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Provider Selection Section
                Text(
                    text = "SELECT DATA PROVIDER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                DataProviderType.values().forEach { provider ->
                    val isSelected = provider == selectedProvider
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.5.dp,
                                if (isSelected) MarketGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedProvider = provider },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedProvider = provider }
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = provider.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!provider.requiresCredentials) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MarketGreen.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "ACTIVE & READY",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = MarketGreen,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = provider.licensedOrg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Credential Configuration (if licensed broker provider selected)
                if (selectedProvider.requiresCredentials) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "BROKER CREDENTIALS (${selectedProvider.name})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (selectedProvider == DataProviderType.CUSTOM_BACKEND) {
                                OutlinedTextField(
                                    value = customGatewayUrlText,
                                    onValueChange = { customGatewayUrlText = it },
                                    label = { Text("Proxy / Backend Gateway URL") },
                                    placeholder = { Text("https://market-gateway.yourdomain.com") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                OutlinedTextField(
                                    value = apiKeyText,
                                    onValueChange = { apiKeyText = it },
                                    label = { Text("API Key / Client Code") },
                                    placeholder = { Text("Enter broker developer API Key") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = accessTokenText,
                                    onValueChange = { accessTokenText = it },
                                    label = { Text("Access Token / JWT Token") },
                                    placeholder = { Text("Enter daily session access token") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Text(
                                text = "Note: Client-side apps run on user devices. For institutional deployments, enter your broker developer credentials or deploy a secure proxy server.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Polling Rate Configuration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto Polling Interval", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Exchange compliance recommends 10s - 30s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(10, 15, 20, 30).forEach { sec ->
                            FilterChip(
                                selected = pollingInterval == sec,
                                onClick = { pollingInterval = sec },
                                label = { Text("${sec}s", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions: Test & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onTestConnection,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Feed")
                    }

                    Button(
                        onClick = {
                            val newConfig = currentConfig.copy(
                                activeProvider = selectedProvider,
                                apiKey = apiKeyText.trim(),
                                accessToken = accessTokenText.trim(),
                                customGatewayUrl = customGatewayUrlText.trim(),
                                autoPollingIntervalSecs = pollingInterval
                            )
                            onSaveConfig(newConfig)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MarketGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Connect")
                    }
                }
            }
        }
    }
}
