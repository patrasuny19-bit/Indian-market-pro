# IndiaMarketPro + My Trading Charts — Integration Package

## What has been merged
- Kept the original IndiaMarketPro Android project intact.
- Added the supplied chart-engine HTML, CSS and JavaScript source under:
  `app/src/main/assets/advanced-chart-engine/`
- Added the chart engine integration guide, package metadata, and Kotlin reference files under:
  `advanced-chart-engine-source/`
- Patched the chart engine's status badge so generated/synthetic candles are always labelled **DEMO DATA**, never LIVE TICK.

## Important: not yet activated in the app
The supplied chart ZIP references this local script from `index.html`:

`dist/lightweight-charts.js`

That file is not present in the supplied ZIP. Without the actual Lightweight Charts v5 browser bundle, the advanced chart page cannot initialize. I did not invent or substitute a fake library file. The new engine is therefore included as integration source/assets, but is not wired into the running IndiaMarketPro screens yet. The existing IndiaMarketPro chart components remain unchanged.

## What is needed to finish activation
1. Obtain the official Lightweight Charts v5 browser bundle matching the supplied engine/package version.
2. Place it at `app/src/main/assets/advanced-chart-engine/dist/lightweight-charts.js`.
3. Wire the WebView bridge to IndiaMarketPro's actual market-data repository and selected instrument/timeframe.
4. Feed historical OHLC candles and broker-authorized WebSocket ticks. Until then, the chart engine's generated candles are demo-only.

## Live market data warning
A visual LIVE badge does not make data live. Use a licensed exchange/broker data source and clearly display DEMO DATA whenever generated/test candles are used. Do not use the sample symbols/prices in the standalone chart package as current market quotes.

## Build status
This is a source-level integration package. I have not claimed a successful Android build or runtime test; the missing browser bundle and live-data adapter must be supplied before the advanced engine can be activated and tested.
