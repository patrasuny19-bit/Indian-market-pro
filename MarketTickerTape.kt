package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketIndex
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed

@Composable
fun MarketTickerTape(
    indices: List<MarketIndex>,
    recentTickFlash: Map<String, Boolean>,
    onIndexClick: (MarketIndex) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("market_ticker_tape"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Market Pulse Dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MarketGreen)
            )
            Text(
                text = "NSE LIVE",
                style = MaterialTheme.typography.labelSmall,
                color = MarketGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }

        indices.forEach { index ->
            val isFlashing = recentTickFlash.containsKey(index.symbol)
            val flashColor = if (isFlashing) {
                if (recentTickFlash[index.symbol] == true) MarketGreen.copy(alpha = 0.25f)
                else MarketRed.copy(alpha = 0.25f)
            } else Color.Transparent

            val animatedBg by animateColorAsState(
                targetValue = flashColor,
                animationSpec = tween(durationMillis = 400),
                label = "flashBg"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(animatedBg)
                    .clickable { onIndexClick(index) }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = index.symbol,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = String.format(java.util.Locale.US, "%,.2f", index.value),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val isPos = index.isPositive
                val sign = if (isPos) "+" else ""
                val color = if (isPos) MarketGreen else MarketRed

                Text(
                    text = "$sign${String.format(java.util.Locale.US, "%.2f", index.changePercent)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
