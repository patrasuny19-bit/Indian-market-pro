# MY TRADING CHARTS

A professional, standalone Indian stock market charting engine built with **React**, **TypeScript**, and **TradingView Lightweight Charts v5**.

Designed specifically for clean, modular integration into **INDIA MARKET PRO** and other trading terminal applications.

---

## 🚀 Key Features

1. **Professional Candlestick Charts**: Built on TradingView Lightweight Charts v5 with sub-millisecond render performance.
2. **Indian Stock Market Support**:
   - **NSE & BSE Equities**: RELIANCE, TCS, HDFCBANK, INFY, ICICIBANK, SBIN, BHARTIARTL, ITC, LT, TATAMOTORS.
   - **Indian Indices**: NIFTY 50, BANK NIFTY, FINNIFTY, SENSEX.
   - **NSE F&O Instruments**: Monthly & Weekly Index/Equity Futures (`NIFTY-FUT`, `BANKNIFTY-FUT`), Option chain strikes (Call `CE` & Put `PE`).
3. **9 Timeframes**: `1m`, `3m`, `5m`, `15m`, `30m`, `1H`, `4H`, `1D`, `1W`.
4. **Technical Indicators**:
   - **EMAs**: EMA 9, EMA 20, EMA 50, EMA 200
   - **SMAs**: SMA 20, SMA 50, SMA 200
   - **Oscillators**: RSI 14, MACD (12, 26, 9)
   - **Volatility**: Bollinger Bands (20, 2), ATR 14
   - **Price Action & Volume**: VWAP (session-anchored), Volume Histogram (color-coded)
5. **Interactive Drawing Tools**:
   - Trendline drawing
   - Horizontal Support & Resistance levels
   - Fibonacci Retracement ratios (`0.0`, `0.236`, `0.382`, `0.5`, `0.618`, `0.786`, `1.0`)
6. **Crosshair & OHLC Legend**: Dynamic hover tracking with high-precision Open, High, Low, Close, Volume, and indicator values.
7. **Fullscreen & Theming**: Dark theme and Light theme presets matching TradingView standards.
8. **Real-time & Demo Mode**: Clean separation between live exchange streaming and demo simulated data feeds.
9. **Decoupled Architecture**: Clean separation between Chart UI and Market Data services via the `MarketDataService` interface.

---

## 📦 Installation

Add `@india-market-pro/trading-charts` to your React application:

```bash
npm install lightweight-charts@^5.0.3
# In India Market Pro workspace:
npm install ../chart-engine
```

Or copy the `chart-engine/src` directory directly into your project.

---

## ⚡ Quick Start

```tsx
import React, { useState } from 'react';
import { TradingChart, IndianMarketDataService } from '@india-market-pro/trading-charts';

export function ChartPage() {
  const [symbol, setSymbol] = useState('NIFTY 50');
  const [timeframe, setTimeframe] = useState('5m');

  return (
    <div style={{ width: '100vw', height: '100vh' }}>
      <TradingChart
        symbol={symbol}
        timeframe={timeframe}
        onSymbolChange={setSymbol}
        onTimeframeChange={setTimeframe}
        theme="dark"
        showToolbar={true}
        showLegend={true}
      />
    </div>
  );
}
```

---

## 📖 Component Props

| Prop | Type | Default | Description |
|---|---|---|---|
| `symbol` | `string` | `'NIFTY 50'` | Indian market symbol or instrument |
| `timeframe` | `Timeframe` | `'5m'` | Chart candle interval (`1m` to `1W`) |
| `marketDataService` | `MarketDataService` | `IndianMarketDataService` | Custom market data provider |
| `initialData` | `Candle[]` | `undefined` | Pre-loaded OHLCV historical candle array |
| `theme` | `'dark' \| 'light'` | `'dark'` | Visual color theme |
| `activeIndicators` | `IndicatorKey[]` | `['volume', 'ema20', 'vwap']` | List of enabled technical indicators |
| `drawingTool` | `DrawingTool` | `'cursor'` | Active drawing tool mode |
| `onRealtimeUpdate` | `(candle, tick) => void` | `undefined` | Real-time tick callback for order execution |
| `onSymbolChange` | `(symbol: string) => void` | `undefined` | Fired when user selects another symbol |
| `onTimeframeChange` | `(tf: Timeframe) => void` | `undefined` | Fired when timeframe is switched |
| `showToolbar` | `boolean` | `true` | Show/hide top control toolbar |
| `showLegend` | `boolean` | `true` | Show/hide OHLC & indicator legend |
| `height` | `number \| string` | `'100%'` | Chart container height |
| `width` | `number \| string` | `'100%'` | Chart container width |

For full integration details, see [INTEGRATION.md](./INTEGRATION.md).
