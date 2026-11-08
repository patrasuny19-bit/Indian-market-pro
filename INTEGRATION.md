# INTEGRATION GUIDE: INDIA MARKET PRO

This document explains how to integrate the **MY TRADING CHARTS** engine into the **INDIA MARKET PRO** application.

---

## 1. Architecture Overview

The chart engine is strictly decoupled into two distinct layers:

1. **Chart UI Layer (`TradingChart`)**:
   - Manages TradingView Lightweight Charts v5 instance lifecycle.
   - Handles canvas resize, touch gestures, pan & zoom, crosshair tracking.
   - Renders drawing overlays (Trendlines, Horizontal S&R, Fibonacci).
   - Computes mathematical indicators on the fly.
   - Does **NOT** make direct HTTP/WebSocket assumptions.

2. **Market Data Layer (`MarketDataService`)**:
   - Abstract TypeScript interface:
     ```typescript
     export interface MarketDataService {
       getHistoricalCandles(symbol: string, timeframe: Timeframe, count?: number): Promise<Candle[]>;
       subscribeRealtime(
         symbol: string,
         timeframe: Timeframe,
         onTick: (tick: RealtimeTick, updatedCandle: Candle) => void
       ): () => void;
       getInstrumentInfo(symbol: string): Promise<InstrumentInfo | undefined>;
       searchInstruments(query: string, segment?: InstrumentSegment): Promise<InstrumentInfo[]>;
       isRealtimeLive(): boolean;
     }
     ```
   - INDIA MARKET PRO can supply its own broker connection (Zerodha Kite Connect, Angel One SmartAPI, Upstox API, Fyers WebSocket, etc.) by implementing `MarketDataService`.

---

## 2. Connecting Live Broker WebSocket in INDIA MARKET PRO

To connect real-time broker feeds in INDIA MARKET PRO:

```typescript
import { MarketDataService, Candle, RealtimeTick, Timeframe } from '@india-market-pro/trading-charts';

export class IndiaMarketProBrokerService implements MarketDataService {
  private socket: WebSocket;

  constructor(brokerToken: string) {
    this.socket = new WebSocket(`wss://api.indiamarketpro.com/stream?token=${brokerToken}`);
  }

  async getHistoricalCandles(symbol: string, timeframe: Timeframe, count: number = 250): Promise<Candle[]> {
    const response = await fetch(`/api/v1/candles?symbol=${symbol}&interval=${timeframe}&limit=${count}`);
    const data = await response.json();
    return data.candles;
  }

  subscribeRealtime(
    symbol: string,
    timeframe: Timeframe,
    onTick: (tick: RealtimeTick, updatedCandle: Candle) => void
  ): () => void {
    const handleMessage = (event: MessageEvent) => {
      const msg = JSON.parse(event.data);
      if (msg.symbol === symbol) {
        onTick(msg.tick, msg.candle);
      }
    };

    this.socket.addEventListener('message', handleMessage);
    this.socket.send(JSON.stringify({ action: 'subscribe', symbol, timeframe }));

    return () => {
      this.socket.removeEventListener('message', handleMessage);
      this.socket.send(JSON.stringify({ action: 'unsubscribe', symbol }));
    };
  }

  async getInstrumentInfo(symbol: string) {
    // Return instrument metadata from India Market Pro master catalog
  }

  async searchInstruments(query: string, segment?: any) {
    // Search India Market Pro instrument database
  }

  isRealtimeLive(): boolean {
    return true; // Live broker connection
  }
}
```

---

## 3. Integrating into India Market Pro Terminal

```tsx
import React, { useState } from 'react';
import { TradingChart } from '@india-market-pro/trading-charts';
import { IndiaMarketProBrokerService } from './services/brokerService';

export function TerminalView() {
  const [activeSymbol, setActiveSymbol] = useState('BANKNIFTY');
  const [activeTimeframe, setActiveTimeframe] = useState('5m');
  const [brokerService] = useState(() => new IndiaMarketProBrokerService(userAuthToken));

  const handleOrderPlacement = (tick: RealtimeTick) => {
    // Sync order book execution price with active chart tick
  };

  return (
    <div className="terminal-container">
      <TradingChart
        symbol={activeSymbol}
        timeframe={activeTimeframe}
        marketDataService={brokerService}
        onRealtimeUpdate={(candle, tick) => handleOrderPlacement(tick)}
        onSymbolChange={setActiveSymbol}
        onTimeframeChange={setActiveTimeframe}
        theme="dark"
        activeIndicators={['volume', 'ema20', 'ema50', 'vwap']}
      />
    </div>
  );
}
```

---

## 4. Distinction Between Demo and Live Data

Per design requirements, demo simulated data and real exchange feeds are strictly separated:
- When using `IndianMarketDataService({ live: false })`, the engine renders a gold badge: `[DEMO DATA]`.
- When using a live feed or `IndianMarketDataService({ live: true })`, the engine renders an emerald badge: `[LIVE TICK]`.
- This ensures compliance with SEBI and financial software best practices, preventing traders from mistaking simulated historical test data for executable live exchange liquidity.
