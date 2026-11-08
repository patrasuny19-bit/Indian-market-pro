package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TradingCandleUp
import com.example.ui.theme.TradingDarkBorder
import com.example.ui.theme.TradingDarkCard
import com.example.ui.theme.TradingPrimaryBlue

enum class DocTab(val title: String) {
    OVERVIEW("Overview & Setup"),
    PACKAGE_JSON("package.json"),
    COMPONENT("TradingChart.tsx"),
    EXAMPLE("IndiaMarketPro.tsx"),
    INTEGRATION("INTEGRATION.md")
}

@Composable
fun IntegrationDocsScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(DocTab.OVERVIEW) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val codeContent = when (selectedTab) {
        DocTab.OVERVIEW -> """
# MY TRADING CHARTS ENGINE

Standalone React + TypeScript Chart Engine powered by TradingView Lightweight Charts v5.
Engineered specifically for integration into INDIA MARKET PRO.

### Technology Stack:
• React 18 / 19
• TypeScript 5.3+
• TradingView Lightweight Charts v5.0.3+
• Responsive Mobile-First Architecture

### 20 Integrated Features:
 1. Professional candlestick charts (TradingView canvas engine)
 2. NSE & BSE stock symbols (RELIANCE, TCS, HDFCBANK, INFY, etc.)
 3. All available NSE F&O instruments (Futures & Options CE/PE strikes)
 4. NIFTY 50, BANK NIFTY, and FINNIFTY benchmark charts
 5. 9 Timeframes: 1m, 3m, 5m, 15m, 30m, 1H, 4H, 1D, 1W
 6. EMA 9, 20, 50, 200
 7. SMA 20, 50, 200
 8. RSI 14 with 70/30 boundary lines
 9. MACD (12, 26, 9) with histogram & signal line
 10. VWAP (Intraday session-anchored)
 11. Bollinger Bands (20, 2 std dev)
 12. ATR (Average True Range 14)
 13. Volume histogram with bull/bear color coding
 14. Trendline drawing tool (2-point vector)
 15. Horizontal support & resistance levels
 16. Fibonacci retracement (0.0 to 1.0 key ratios)
 17. Crosshair with OHLCV & indicators hover display
 18. Fullscreen chart toggle
 19. Dark and Light themes
 20. Mobile responsive touch gestures

### Project Location:
The full, ready-to-publish npm package is located in `/chart-engine`.
        """.trimIndent()

        DocTab.PACKAGE_JSON -> """
{
  "name": "@india-market-pro/trading-charts",
  "version": "1.0.0",
  "description": "Professional Indian stock market charting engine with TradingView Lightweight Charts v5.",
  "main": "dist/index.js",
  "module": "dist/index.esm.js",
  "types": "dist/index.d.ts",
  "scripts": {
    "build": "tsc",
    "test": "node --test",
    "lint": "tsc --noEmit"
  },
  "keywords": [
    "tradingview",
    "lightweight-charts",
    "react",
    "typescript",
    "indian-stock-market",
    "nse",
    "bse",
    "fno"
  ],
  "author": "India Market Pro Engineering",
  "license": "MIT",
  "peerDependencies": {
    "react": "^18.0.0 || ^19.0.0",
    "react-dom": "^18.0.0 || ^19.0.0"
  },
  "dependencies": {
    "lightweight-charts": "^5.0.3"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "typescript": "^5.3.3"
  }
}
        """.trimIndent()

        DocTab.COMPONENT -> """
import React, { useEffect, useRef, useState, useMemo } from 'react';
import { createChart, ColorType, CrosshairMode } from 'lightweight-charts';
import { TradingChartProps, Candle, RealtimeTick } from '../types';
import { IndianMarketDataService } from '../services/marketDataService';

export const TradingChart: React.FC<TradingChartProps> = ({
  symbol = 'NIFTY 50',
  timeframe = '5m',
  marketDataService,
  onRealtimeUpdate,
  theme = 'dark',
  activeIndicators = ['volume', 'ema20', 'vwap'],
  showToolbar = true,
  showLegend = true,
  height = '100%',
  width = '100%'
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<any>(null);
  const candleSeriesRef = useRef<any>(null);

  // Decoupled Market Data Service injection
  const service = useMemo(() => {
    return marketDataService || new IndianMarketDataService({ live: true });
  }, [marketDataService]);

  // Lifecycle & TradingView v5 setup...
  return (
    <div style={{ width, height, position: 'relative' }}>
      <div ref={containerRef} style={{ width: '100%', height: '100%' }} />
    </div>
  );
};
        """.trimIndent()

        DocTab.EXAMPLE -> """
// IndiaMarketProIntegration.tsx
// How India Market Pro imports and embeds the standalone TradingChart

import React, { useState } from 'react';
import { TradingChart, IndianMarketDataService } from '@india-market-pro/trading-charts';

export const IndiaMarketProApp: React.FC = () => {
  const [selectedSymbol, setSelectedSymbol] = useState('BANKNIFTY');
  const [timeframe, setTimeframe] = useState('5m');
  const [marketService] = useState(() => new IndianMarketDataService({ live: true }));

  return (
    <div className="india-market-pro-terminal">
      {/* Sidebar Watchlist */}
      <Watchlist onSelect={(sym) => setSelectedSymbol(sym)} />

      {/* Embedded Standalone Chart Engine */}
      <TradingChart
        symbol={selectedSymbol}
        timeframe={timeframe}
        marketDataService={marketService}
        theme="dark"
        activeIndicators={['volume', 'ema20', 'ema50', 'vwap']}
        onSymbolChange={(sym) => setSelectedSymbol(sym)}
        onTimeframeChange={(tf) => setTimeframe(tf)}
        onRealtimeUpdate={(candle, tick) => {
          // Push real-time tick to India Market Pro Order Execution Book
          console.log('Realtime tick:', tick.lastPrice);
        }}
      />
    </div>
  );
};
        """.trimIndent()

        DocTab.INTEGRATION -> """
# INDIA MARKET PRO INTEGRATION GUIDE

### Step 1: Install Package
```bash
npm install @india-market-pro/trading-charts
```

### Step 2: Implement Broker Data Provider
Implement the `MarketDataService` TypeScript interface to feed live Zerodha/AngelOne/Upstox ticks:
```typescript
interface MarketDataService {
  getHistoricalCandles(symbol: string, timeframe: Timeframe): Promise<Candle[]>;
  subscribeRealtime(symbol: string, tf: Timeframe, onTick: (tick, candle) => void): () => void;
  getInstrumentInfo(symbol: string): Promise<InstrumentInfo>;
  searchInstruments(query: string, segment?: string): Promise<InstrumentInfo[]>;
  isRealtimeLive(): boolean;
}
```

### Step 3: Embed Reusable Component
Pass `symbol`, `timeframe`, and your custom `marketDataService` instance into `<TradingChart />`.
        """.trimIndent()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Chips Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DocTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    label = { Text(tab.title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TradingPrimaryBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Top info header with Copy action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedTab.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            FilledTonalButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(codeContent))
                    Toast.makeText(context, "Copied ${selectedTab.title} to clipboard", Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Code", fontSize = 12.sp)
            }
        }

        // Code / Markdown Card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = TradingDarkCard),
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = codeContent,
                    color = Color(0xFFD1D4DC),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
