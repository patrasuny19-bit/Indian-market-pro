/**
 * @file chart-engine.js
 * MY TRADING CHARTS - TradingView Lightweight Charts v5 Engine
 * Robust initialization sequence with strict data format adherence.
 */

(function () {
  'use strict';

  // Indian Instruments Master Database
  const INSTRUMENTS = [
    { symbol: 'NIFTY 50', name: 'NIFTY 50 Index', exchange: 'NSE', segment: 'INDEX', price: 24360.50, prev: 24210.20, lot: 25 },
    { symbol: 'BANKNIFTY', name: 'NIFTY Bank Index', exchange: 'NSE', segment: 'INDEX', price: 51840.75, prev: 51520.10, lot: 15 },
    { symbol: 'FINNIFTY', name: 'NIFTY Financial Services', exchange: 'NSE', segment: 'INDEX', price: 23910.40, prev: 23780.00, lot: 25 },
    { symbol: 'SENSEX', name: 'BSE SENSEX 30', exchange: 'BSE', segment: 'INDEX', price: 80215.30, prev: 79780.50, lot: 10 },
    { symbol: 'RELIANCE', name: 'Reliance Industries', exchange: 'NSE', segment: 'EQUITY', price: 2742.60, prev: 2720.15, lot: 1 },
    { symbol: 'TCS', name: 'Tata Consultancy Services', exchange: 'NSE', segment: 'EQUITY', price: 4120.30, prev: 4095.00, lot: 1 },
    { symbol: 'HDFCBANK', name: 'HDFC Bank Ltd.', exchange: 'NSE', segment: 'EQUITY', price: 1748.90, prev: 1732.40, lot: 1 },
    { symbol: 'INFY', name: 'Infosys Ltd.', exchange: 'NSE', segment: 'EQUITY', price: 1865.40, prev: 1845.80, lot: 1 },
    { symbol: 'ICICIBANK', name: 'ICICI Bank Ltd.', exchange: 'NSE', segment: 'EQUITY', price: 1262.15, prev: 1251.00, lot: 1 },
    { symbol: 'SBIN', name: 'State Bank of India', exchange: 'NSE', segment: 'EQUITY', price: 818.50, prev: 809.20, lot: 1 },
    { symbol: 'BHARTIARTL', name: 'Bharti Airtel Ltd.', exchange: 'NSE', segment: 'EQUITY', price: 1612.00, prev: 1598.00, lot: 1 },
    { symbol: 'TATAMOTORS', name: 'Tata Motors Ltd.', exchange: 'NSE', segment: 'EQUITY', price: 875.25, prev: 864.10, lot: 1 },
    { symbol: 'NIFTY-FUT', name: 'NIFTY Current Month Futures', exchange: 'NFO', segment: 'FUTURES', price: 24410.00, prev: 24260.00, lot: 25 },
    { symbol: 'BANKNIFTY-FUT', name: 'BANKNIFTY Current Month Futures', exchange: 'NFO', segment: 'FUTURES', price: 51950.00, prev: 51630.00, lot: 15 },
    { symbol: 'RELIANCE-FUT', name: 'RELIANCE Current Month Futures', exchange: 'NFO', segment: 'FUTURES', price: 2755.00, prev: 2731.00, lot: 250 },
    { symbol: 'NIFTY 24500 CE', name: 'NIFTY 24500 Call Option', exchange: 'NFO', segment: 'OPTIONS', price: 142.50, prev: 110.00, lot: 25 },
    { symbol: 'NIFTY 24500 PE', name: 'NIFTY 24500 Put Option', exchange: 'NFO', segment: 'OPTIONS', price: 98.75, prev: 135.00, lot: 25 },
    { symbol: 'BANKNIFTY 52000 CE', name: 'BANKNIFTY 52000 Call Option', exchange: 'NFO', segment: 'OPTIONS', price: 285.40, prev: 215.00, lot: 15 },
    { symbol: 'BANKNIFTY 51500 PE', name: 'BANKNIFTY 51500 Put Option', exchange: 'NFO', segment: 'OPTIONS', price: 195.20, prev: 260.00, lot: 15 }
  ];

  const TIMEFRAME_SECS = {
    '1m': 60,
    '3m': 180,
    '5m': 300,
    '15m': 900,
    '30m': 1800,
    '1H': 3600,
    '4H': 14400,
    '1D': 86400,
    '1W': 604800
  };

  // State
  let currentSymbol = 'NIFTY 50';
  let currentTimeframe = '5m';
  let isDarkMode = true;
  let isLiveSimulation = false;
  let activeIndicators = new Set(['volume', 'ema20', 'vwap']);
  let activeDrawingTool = 'cursor';
  let drawings = [];
  let currentDrawingPoints = [];

  // Chart References
  let chart = null;
  let candleSeries = null;
  let volumeSeries = null;
  let indicatorSeriesMap = new Map();
  let candles = [];
  let streamTimer = null;

  // Cached DOM references (populated once DOM is ready)
  let chartEl = null;
  let drawingCanvas = null;
  let drawingCtx = null;
  let legendSymbol = null;
  let legendOhlc = null;
  let legendIndicators = null;
  let symbolBtn = null;
  let symbolModal = null;
  let indicatorsPopup = null;
  let liveDemoBadge = null;
  let themeBtn = null;
  let fullscreenBtn = null;
  let drawingBtn = null;
  let drawingPopup = null;

  // Math Calculations for Technical Indicators
  function calculateSMA(data, period) {
    const res = [];
    if (!data || data.length < period) return res;
    let sum = 0;
    for (let i = 0; i < data.length; i++) {
      sum += data[i].close;
      if (i >= period) sum -= data[i - period].close;
      if (i >= period - 1) {
        res.push({ time: data[i].time, value: +(sum / period).toFixed(2) });
      }
    }
    return res;
  }

  function calculateEMA(data, period) {
    const res = [];
    if (!data || data.length < period) return res;
    const mult = 2 / (period + 1);
    let sum = 0;
    for (let i = 0; i < period; i++) sum += data[i].close;
    let prev = sum / period;
    res.push({ time: data[period - 1].time, value: +prev.toFixed(2) });

    for (let i = period; i < data.length; i++) {
      const cur = (data[i].close - prev) * mult + prev;
      res.push({ time: data[i].time, value: +cur.toFixed(2) });
      prev = cur;
    }
    return res;
  }

  function calculateVWAP(data) {
    const res = [];
    if (!data || data.length === 0) return res;
    let cumVolTypical = 0;
    let cumVol = 0;
    let curDay = '';

    for (let i = 0; i < data.length; i++) {
      const d = new Date(data[i].time * 1000).toDateString();
      if (d !== curDay) {
        curDay = d;
        cumVolTypical = 0;
        cumVol = 0;
      }
      const typical = (data[i].high + data[i].low + data[i].close) / 3;
      const vol = data[i].volume || 1;
      cumVolTypical += typical * vol;
      cumVol += vol;
      res.push({ time: data[i].time, value: +(cumVolTypical / cumVol).toFixed(2) });
    }
    return res;
  }

  function calculateRSI(data, period = 14) {
    const res = [];
    if (!data || data.length <= period) return res;
    let gains = 0, losses = 0;
    for (let i = 1; i <= period; i++) {
      const diff = data[i].close - data[i - 1].close;
      if (diff > 0) gains += diff;
      else losses += Math.abs(diff);
    }
    let avgGain = gains / period;
    let avgLoss = losses / period;
    let rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
    res.push({ time: data[period].time, value: +(100 - (100 / (1 + rs))).toFixed(2) });

    for (let i = period + 1; i < data.length; i++) {
      const diff = data[i].close - data[i - 1].close;
      const gain = diff > 0 ? diff : 0;
      const loss = diff < 0 ? Math.abs(diff) : 0;
      avgGain = (avgGain * (period - 1) + gain) / period;
      avgLoss = (avgLoss * (period - 1) + loss) / period;
      rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
      res.push({ time: data[i].time, value: +(100 - (100 / (1 + rs))).toFixed(2) });
    }
    return res;
  }

  function calculateBollingerBands(data, period = 20, multiplier = 2) {
    const upper = [], middle = [], lower = [];
    if (!data || data.length < period) return { upper, middle, lower };

    for (let i = period - 1; i < data.length; i++) {
      const slice = data.slice(i - period + 1, i + 1);
      const mean = slice.reduce((a, b) => a + b.close, 0) / period;
      const variance = slice.reduce((a, b) => a + Math.pow(b.close - mean, 2), 0) / period;
      const sd = Math.sqrt(variance);
      const time = data[i].time;
      middle.push({ time, value: +mean.toFixed(2) });
      upper.push({ time, value: +(mean + multiplier * sd).toFixed(2) });
      lower.push({ time, value: +(mean - multiplier * sd).toFixed(2) });
    }
    return { upper, middle, lower };
  }

  function calculateMACD(data) {
    const fast = calculateEMA(data, 12);
    const slow = calculateEMA(data, 26);
    const fastMap = new Map(fast.map(f => [f.time, f.value]));
    const macd = [];
    for (let s of slow) {
      if (fastMap.has(s.time)) {
        macd.push({ time: s.time, value: +(fastMap.get(s.time) - s.value).toFixed(2) });
      }
    }
    const signal = [];
    const hist = [];
    if (macd.length >= 9) {
      let sum = 0;
      for (let i = 0; i < 9; i++) sum += macd[i].value;
      let prevSig = sum / 9;
      signal.push({ time: macd[8].time, value: +prevSig.toFixed(2) });
      hist.push({ time: macd[8].time, value: +(macd[8].value - prevSig).toFixed(2), color: '#26a69a' });

      for (let i = 9; i < macd.length; i++) {
        const curSig = (macd[i].value - prevSig) * (2 / 10) + prevSig;
        signal.push({ time: macd[i].time, value: +curSig.toFixed(2) });
        const hVal = +(macd[i].value - curSig).toFixed(2);
        hist.push({ time: macd[i].time, value: hVal, color: hVal >= 0 ? '#26a69a' : '#ef5350' });
        prevSig = curSig;
      }
    }
    return { macd, signal, hist };
  }

  function calculateATR(data, period = 14) {
    const res = [];
    if (!data || data.length <= period) return res;
    const trs = [];
    for (let i = 1; i < data.length; i++) {
      const h = data[i].high, l = data[i].low, pc = data[i - 1].close;
      trs.push(Math.max(h - l, Math.abs(h - pc), Math.abs(l - pc)));
    }
    let atr = trs.slice(0, period).reduce((a, b) => a + b, 0) / period;
    res.push({ time: data[period].time, value: +atr.toFixed(2) });
    for (let i = period; i < trs.length; i++) {
      atr = (atr * (period - 1) + trs[i]) / period;
      res.push({ time: data[i + 1].time, value: +atr.toFixed(2) });
    }
    return res;
  }

  /**
   * Generates historical OHLCV data adhering strictly to TradingView {time, open, high, low, close} format.
   * Timestamps are strictly unique, ascending integer seconds (UTCTimestamp).
   * Prices satisfy: low <= min(open, close) and high >= max(open, close).
   */
  function generateHistoricalCandles(symbol, tf, count = 200) {
    const inst = INSTRUMENTS.find(i => i.symbol === symbol) || INSTRUMENTS[0];
    const basePrice = Number(inst.price) || 24000;
    const interval = TIMEFRAME_SECS[tf] || 300;

    const now = Math.floor(Date.now() / 1000);
    const alignedNow = Math.floor(now / interval) * interval;
    const startTime = alignedNow - (count * interval);

    const list = [];
    let curClose = +(basePrice * (1 - (Math.random() * 0.02 - 0.01))).toFixed(2);
    const volScale = basePrice > 10000 ? 12 : basePrice > 1000 ? 2.0 : 0.5;

    for (let i = 0; i < count; i++) {
      const t = startTime + (i * interval); // strictly ascending unique integer seconds
      const trend = (Math.sin(i / 15) * 0.4 + (Math.random() - 0.49)) * volScale;

      const open = +curClose.toFixed(2);
      const step = trend + (Math.random() - 0.49) * volScale * 1.5;
      const close = Math.max(1, +(open + step).toFixed(2));

      const wickUpper = Math.max(0.1, +(Math.random() * volScale).toFixed(2));
      const wickLower = Math.max(0.1, +(Math.random() * volScale).toFixed(2));

      const high = +(Math.max(open, close) + wickUpper).toFixed(2);
      const low = Math.max(0.05, +(Math.min(open, close) - wickLower).toFixed(2));

      const vol = Math.floor((inst.segment === 'INDEX' ? 35000 : 8000) * (0.6 + Math.random() * 1.5));

      list.push({
        time: t,
        open: open,
        high: high,
        low: low,
        close: close,
        volume: vol
      });

      curClose = close;
    }
    return list;
  }

  // Robust Engine Initialization Sequence after DOM is Fully Ready
  function initChartEngine() {
    try {
      console.log('MY TRADING CHARTS: Initializing engine after DOM ready...');

      // 1. Resolve and verify DOM elements
      chartEl = document.getElementById('chart-element');
      drawingCanvas = document.getElementById('drawing-canvas');
      legendSymbol = document.getElementById('legend-symbol');
      legendOhlc = document.getElementById('legend-ohlc');
      legendIndicators = document.getElementById('legend-indicators');
      symbolBtn = document.getElementById('symbol-btn');
      symbolModal = document.getElementById('symbol-modal');
      indicatorsPopup = document.getElementById('indicators-popup');
      liveDemoBadge = document.getElementById('live-demo-badge');
      themeBtn = document.getElementById('theme-btn');
      fullscreenBtn = document.getElementById('fullscreen-btn');
      drawingBtn = document.getElementById('drawing-btn');
      drawingPopup = document.getElementById('drawing-popup');

      if (!chartEl) {
        console.warn('chart-element not found, retrying in 50ms...');
        setTimeout(initChartEngine, 50);
        return;
      }

      if (drawingCanvas) {
        drawingCtx = drawingCanvas.getContext('2d');
      }

      // 2. Verify Lightweight Charts library availability
      if (!window.LightweightCharts) {
        console.error('LightweightCharts global library is not loaded');
        return;
      }

      const {
        createChart,
        CandlestickSeries,
        HistogramSeries,
        LineSeries,
        ColorType,
        CrosshairMode
      } = window.LightweightCharts;

      // 3. Compute initial dimensions (guarded against 0 in WebView)
      let initialWidth = chartEl.clientWidth;
      let initialHeight = chartEl.clientHeight;
      if (!initialWidth || initialWidth <= 0) {
        initialWidth = window.innerWidth || 360;
      }
      if (!initialHeight || initialHeight <= 0) {
        initialHeight = (window.innerHeight - 45) || 500;
      }

      // Clear any prior chart contents
      chartEl.innerHTML = '';

      console.log('[MY TRADING CHARTS] Initializing chart instance on element #' + chartEl.id + ' with dimensions:', initialWidth, 'x', initialHeight);
      console.log('[MY TRADING CHARTS] LightweightCharts global object check:', {
        available: Boolean(window.LightweightCharts),
        hasCreateChart: typeof window.LightweightCharts?.createChart === 'function',
        hasCandlestickSeriesDef: Boolean(window.LightweightCharts?.CandlestickSeries)
      });

      // 4. Instantiate Lightweight Charts chart instance
      chart = createChart(chartEl, {
        width: initialWidth,
        height: initialHeight,
        layout: {
          background: { type: ColorType.Solid, color: isDarkMode ? '#131722' : '#ffffff' },
          textColor: isDarkMode ? '#d1d4dc' : '#131722',
          fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"
        },
        grid: {
          vertLines: { color: isDarkMode ? '#1e222d' : '#f0f3fa' },
          horzLines: { color: isDarkMode ? '#1e222d' : '#f0f3fa' }
        },
        crosshair: {
          mode: CrosshairMode.Normal,
          vertLine: { color: isDarkMode ? '#758696' : '#9598a1', width: 1, style: 3 },
          horzLine: { color: isDarkMode ? '#758696' : '#9598a1', width: 1, style: 3 }
        },
        rightPriceScale: {
          borderColor: isDarkMode ? '#2a2e39' : '#e0e3eb',
          scaleMargins: { top: 0.1, bottom: 0.22 }
        },
        timeScale: {
          borderColor: isDarkMode ? '#2a2e39' : '#e0e3eb',
          timeVisible: true,
          secondsVisible: false
        }
      });

      console.log('[MY TRADING CHARTS] Chart instance created successfully:', {
        chartObject: chart,
        hasAddSeries: typeof chart?.addSeries === 'function',
        hasTimeScale: Boolean(chart?.timeScale)
      });

      // 5. Correctly instantiate and append CandlestickSeries
      const candlestickOptions = {
        upColor: '#089981',
        downColor: '#f23645',
        borderUpColor: '#089981',
        borderDownColor: '#f23645',
        wickUpColor: '#089981',
        wickDownColor: '#f23645'
      };

      console.log('[MY TRADING CHARTS] Creating and appending CandlestickSeries to chart with options:', candlestickOptions);

      if (typeof chart.addSeries === 'function' && CandlestickSeries) {
        candleSeries = chart.addSeries(CandlestickSeries, candlestickOptions);
        console.log('[MY TRADING CHARTS] CandlestickSeries successfully created and appended via chart.addSeries(CandlestickSeries, ...):', candleSeries);
      } else if (typeof chart.addCandlestickSeries === 'function') {
        candleSeries = chart.addCandlestickSeries(candlestickOptions);
        console.log('[MY TRADING CHARTS] CandlestickSeries successfully created and appended via chart.addCandlestickSeries(...):', candleSeries);
      } else {
        console.error('[MY TRADING CHARTS] ERROR: Failed to instantiate CandlestickSeries: unsupported API version');
      }

      console.log('[MY TRADING CHARTS] CandlestickSeries instance verification:', {
        isDefined: Boolean(candleSeries),
        hasSetDataMethod: typeof candleSeries?.setData === 'function',
        hasUpdateMethod: typeof candleSeries?.update === 'function'
      });

      // 6. Instantiate and append Volume HistogramSeries
      const volumeOptions = {
        priceFormat: { type: 'volume' },
        priceScaleId: ''
      };

      if (typeof chart.addSeries === 'function' && HistogramSeries) {
        volumeSeries = chart.addSeries(HistogramSeries, volumeOptions);
      } else if (typeof chart.addHistogramSeries === 'function') {
        volumeSeries = chart.addHistogramSeries(volumeOptions);
      }

      if (volumeSeries && volumeSeries.priceScale) {
        volumeSeries.priceScale().applyOptions({
          scaleMargins: { top: 0.8, bottom: 0 }
        });
      }

      // 7. Subscribe to crosshair movement
      chart.subscribeCrosshairMove(param => {
        if (!param || !param.time || !param.seriesData || !candleSeries) {
          if (candles.length > 0) updateLegend(candles[candles.length - 1]);
          return;
        }
        const data = param.seriesData.get(candleSeries);
        if (data && data.open !== undefined) {
          updateLegend({
            time: Number(data.time),
            open: Number(data.open),
            high: Number(data.high),
            low: Number(data.low),
            close: Number(data.close),
            volume: 0
          });
        }
      });

      // 8. Bind resize handlers (window + ResizeObserver)
      if (window.ResizeObserver) {
        const ro = new ResizeObserver(entries => {
          if (!entries || entries.length === 0) return;
          const rect = entries[0].contentRect;
          const w = Math.floor(rect.width);
          const h = Math.floor(rect.height);
          if (w > 0 && h > 0 && chart) {
            chart.applyOptions({ width: w, height: h });
            if (drawingCanvas) {
              drawingCanvas.width = w;
              drawingCanvas.height = h;
              renderDrawings();
            }
          }
        });
        ro.observe(chartEl);
      }

      window.addEventListener('resize', handleResize);

      // 9. Attach UI event listeners
      setupUIEventListeners();

      // 10. Load historical candles and strictly format for setData()
      loadChartData();

      // 11. Post-layout resize sync to accommodate WebView asynchronous measurement
      setTimeout(handleResize, 100);
      setTimeout(handleResize, 300);

      console.log('MY TRADING CHARTS: Engine initialization sequence complete.');
    } catch (err) {
      console.error('Fatal error during chart-engine initialization:', err);
    }
  }

  function handleResize() {
    if (!chart || !chartEl) return;
    const w = Math.max(chartEl.clientWidth || 0, window.innerWidth || 360);
    const h = Math.max(chartEl.clientHeight || 0, (window.innerHeight - 45) || 500);
    if (w > 0 && h > 0) {
      chart.applyOptions({ width: w, height: h });
      if (drawingCanvas) {
        drawingCanvas.width = w;
        drawingCanvas.height = h;
        renderDrawings();
      }
    }
  }

  // Load and strictly format candle data passed to setData()
  function loadChartData() {
    if (!candleSeries) {
      console.warn('[MY TRADING CHARTS] loadChartData() skipped: candleSeries is not yet defined.');
      return;
    }

    console.log(`[MY TRADING CHARTS] loadChartData() triggered for symbol="${currentSymbol}", timeframe="${currentTimeframe}". Generating raw candle list...`);
    candles = generateHistoricalCandles(currentSymbol, currentTimeframe, 220);

    // Verify and map to strict required { time, open, high, low, close } format
    const strictCandleData = candles.map(c => ({
      time: Number(c.time),
      open: Number(c.open),
      high: Number(c.high),
      low: Number(c.low),
      close: Number(c.close)
    }));

    // Detailed field-level type validation checks before calling setData()
    console.log('[MY TRADING CHARTS] Validating historical candle data array before calling setData(). Total bars:', strictCandleData.length);

    if (strictCandleData.length > 0) {
      const first = strictCandleData[0];
      const last = strictCandleData[strictCandleData.length - 1];

      console.log('[MY TRADING CHARTS] First candle fields & types validation:', {
        time: { value: first.time, type: typeof first.time, isFinite: Number.isFinite(first.time) },
        open: { value: first.open, type: typeof first.open, isFinite: Number.isFinite(first.open) },
        high: { value: first.high, type: typeof first.high, isFinite: Number.isFinite(first.high) },
        low: { value: first.low, type: typeof first.low, isFinite: Number.isFinite(first.low) },
        close: { value: first.close, type: typeof first.close, isFinite: Number.isFinite(first.close) }
      });

      console.log('[MY TRADING CHARTS] Last candle fields & types validation:', {
        time: { value: last.time, type: typeof last.time, isFinite: Number.isFinite(last.time) },
        open: { value: last.open, type: typeof last.open, isFinite: Number.isFinite(last.open) },
        high: { value: last.high, type: typeof last.high, isFinite: Number.isFinite(last.high) },
        low: { value: last.low, type: typeof last.low, isFinite: Number.isFinite(last.low) },
        close: { value: last.close, type: typeof last.close, isFinite: Number.isFinite(last.close) }
      });

      // Assert all elements pass numerical type and OHLC bounds checking
      let invalidCount = 0;
      for (let i = 0; i < strictCandleData.length; i++) {
        const item = strictCandleData[i];
        const hasValidTypes = typeof item.time === 'number' &&
                              typeof item.open === 'number' &&
                              typeof item.high === 'number' &&
                              typeof item.low === 'number' &&
                              typeof item.close === 'number';
        const isFiniteNums = Number.isFinite(item.time) &&
                             Number.isFinite(item.open) &&
                             Number.isFinite(item.high) &&
                             Number.isFinite(item.low) &&
                             Number.isFinite(item.close);
        const validBounds = item.low <= item.open &&
                            item.low <= item.close &&
                            item.high >= item.open &&
                            item.high >= item.close &&
                            item.high >= item.low;

        if (!hasValidTypes || !isFiniteNums || !validBounds) {
          invalidCount++;
          console.error(`[MY TRADING CHARTS] Invalid candle at index ${i}:`, item);
        }
      }

      if (invalidCount === 0) {
        console.log(`[MY TRADING CHARTS] Data validation check PASSED: All ${strictCandleData.length} candle objects contain strictly valid numeric types for {time, open, high, low, close}. Calling candleSeries.setData()...`);
      } else {
        console.warn(`[MY TRADING CHARTS] Data validation check WARNING: Found ${invalidCount} irregular candles before setData()`);
      }
    } else {
      console.warn('[MY TRADING CHARTS] Warning: strictCandleData array is empty!');
    }

    // Pass strictly validated historical array to setData()
    candleSeries.setData(strictCandleData);
    console.log(`[MY TRADING CHARTS] CandlestickSeries.setData() executed successfully with ${strictCandleData.length} candles for ${currentSymbol}`);

    // Volume histogram setData
    if (volumeSeries) {
      if (activeIndicators.has('volume')) {
        const strictVolumeData = candles.map(c => ({
          time: Number(c.time),
          value: Number(c.volume),
          color: c.close >= c.open ? 'rgba(8, 153, 129, 0.45)' : 'rgba(242, 54, 69, 0.45)'
        }));
        volumeSeries.setData(strictVolumeData);
      } else {
        volumeSeries.setData([]);
      }
    }

    renderIndicators();

    // Auto-fit time scale to show recent candles comfortably
    if (chart && chart.timeScale) {
      chart.timeScale().fitContent();
    }

    if (candles.length > 0) {
      updateLegend(candles[candles.length - 1]);
    }

    startRealtimeStream();
  }

  // Render Line Series for Indicators using v5 addSeries API
  function renderIndicators() {
    if (!chart) return;
    const { LineSeries, HistogramSeries } = window.LightweightCharts;

    indicatorSeriesMap.forEach(s => {
      try { chart.removeSeries(s); } catch (e) {}
    });
    indicatorSeriesMap.clear();

    const addLine = (key, data, color, width = 2) => {
      if (!data || data.length === 0) return;
      const s = chart.addSeries(LineSeries, {
        color,
        lineWidth: width,
        priceLineVisible: false,
        lastValueVisible: false
      });
      s.setData(data);
      indicatorSeriesMap.set(key, s);
    };

    if (activeIndicators.has('ema9')) addLine('ema9', calculateEMA(candles, 9), '#2962ff');
    if (activeIndicators.has('ema20')) addLine('ema20', calculateEMA(candles, 20), '#ff9800');
    if (activeIndicators.has('ema50')) addLine('ema50', calculateEMA(candles, 50), '#e91e63');
    if (activeIndicators.has('ema200')) addLine('ema200', calculateEMA(candles, 200), '#9c27b0');

    if (activeIndicators.has('sma20')) addLine('sma20', calculateSMA(candles, 20), '#00bcd4');
    if (activeIndicators.has('sma50')) addLine('sma50', calculateSMA(candles, 50), '#4caf50');
    if (activeIndicators.has('sma200')) addLine('sma200', calculateSMA(candles, 200), '#ff5722');

    if (activeIndicators.has('vwap')) addLine('vwap', calculateVWAP(candles), '#f5a623');

    if (activeIndicators.has('bb20')) {
      const bb = calculateBollingerBands(candles, 20, 2);
      addLine('bb_u', bb.upper, 'rgba(38, 166, 154, 0.7)', 1);
      addLine('bb_m', bb.middle, 'rgba(38, 166, 154, 0.5)', 1);
      addLine('bb_l', bb.lower, 'rgba(38, 166, 154, 0.7)', 1);
    }

    if (activeIndicators.has('atr14')) addLine('atr14', calculateATR(candles, 14), '#ab47bc', 1);
    if (activeIndicators.has('rsi14')) addLine('rsi14', calculateRSI(candles, 14), '#7b1fa2', 2);

    if (activeIndicators.has('macd')) {
      const macd = calculateMACD(candles);
      addLine('macd_line', macd.macd, '#2962ff', 1);
      addLine('macd_sig', macd.signal, '#ff9800', 1);
      const hs = chart.addSeries(HistogramSeries, { priceLineVisible: false, lastValueVisible: false });
      hs.setData(macd.hist);
      indicatorSeriesMap.set('macd_hist', hs);
    }
  }

  // Real-time Tick Simulator (Indian Market Hours)
  function startRealtimeStream() {
    if (streamTimer) clearInterval(streamTimer);

    const interval = TIMEFRAME_SECS[currentTimeframe] || 300;
    const inst = INSTRUMENTS.find(i => i.symbol === currentSymbol) || INSTRUMENTS[0];

    streamTimer = setInterval(() => {
      if (!candles || candles.length === 0 || !candleSeries) return;
      const now = Math.floor(Date.now() / 1000);
      const activeCandleTime = Math.floor(now / interval) * interval;

      let lastCandle = candles[candles.length - 1];

      // New candle roll
      if (activeCandleTime > lastCandle.time) {
        lastCandle = {
          time: activeCandleTime,
          open: Number(lastCandle.close),
          high: Number(lastCandle.close),
          low: Number(lastCandle.close),
          close: Number(lastCandle.close),
          volume: 0
        };
        candles.push(lastCandle);
      }

      // Tick increment
      const priceScale = lastCandle.close > 10000 ? 0.8 : lastCandle.close > 1000 ? 0.25 : 0.05;
      const ticks = Math.round((Math.random() - 0.49) * 4);
      const delta = +(ticks * 0.05 * (priceScale / 0.05)).toFixed(2);
      const nextPrice = Math.max(1, +(lastCandle.close + delta).toFixed(2));

      lastCandle.close = nextPrice;
      lastCandle.high = Math.max(lastCandle.high, nextPrice);
      lastCandle.low = Math.min(lastCandle.low, nextPrice);
      lastCandle.volume += Math.floor(Math.random() * 45 + 5);

      // Update strictly adhering to CandlestickData
      candleSeries.update({
        time: Number(lastCandle.time),
        open: Number(lastCandle.open),
        high: Number(lastCandle.high),
        low: Number(lastCandle.low),
        close: Number(lastCandle.close)
      });

      if (activeIndicators.has('volume') && volumeSeries) {
        volumeSeries.update({
          time: Number(lastCandle.time),
          value: Number(lastCandle.volume),
          color: lastCandle.close >= lastCandle.open ? 'rgba(8, 153, 129, 0.45)' : 'rgba(242, 54, 69, 0.45)'
        });
      }

      updateLegend(lastCandle);

      // Notify Android Native Bridge if present
      if (window.AndroidBridge && window.AndroidBridge.onRealtimeTick) {
        window.AndroidBridge.onRealtimeTick(JSON.stringify({
          symbol: currentSymbol,
          price: nextPrice,
          high: lastCandle.high,
          low: lastCandle.low,
          volume: lastCandle.volume,
          change: +(nextPrice - inst.prev).toFixed(2),
          changePercent: inst.prev > 0 ? +(((nextPrice - inst.prev) / inst.prev) * 100).toFixed(2) : 0
        }));
      }
    }, 850);
  }

  // Update Legend HUD
  function updateLegend(c) {
    if (!c || c.open === undefined || !legendOhlc || !legendSymbol) return;
    const inst = INSTRUMENTS.find(i => i.symbol === currentSymbol) || INSTRUMENTS[0];
    const isUp = c.close >= c.open;
    const change = +(c.close - inst.prev).toFixed(2);
    const changePct = inst.prev > 0 ? +((change / inst.prev) * 100).toFixed(2) : 0;
    const col = isUp ? 'var(--candle-up)' : 'var(--candle-down)';

    legendSymbol.textContent = currentSymbol;
    legendOhlc.innerHTML = `
      <span>O <strong style="color:${col}">${c.open.toFixed(2)}</strong></span>
      <span>H <strong style="color:${col}">${c.high.toFixed(2)}</strong></span>
      <span>L <strong style="color:${col}">${c.low.toFixed(2)}</strong></span>
      <span>C <strong style="color:${col}">${c.close.toFixed(2)}</strong></span>
      <span style="color:${col}">${change >= 0 ? '+' : ''}${change.toFixed(2)} (${change >= 0 ? '+' : ''}${changePct.toFixed(2)}%)</span>
      <span>Vol <strong style="color:var(--text-muted)">${(c.volume || 0).toLocaleString('en-IN')}</strong></span>
    `;

    // Active indicators values
    if (legendIndicators) {
      const indParts = [];
      if (activeIndicators.has('ema20')) indParts.push(`<span style="color:#ff9800">EMA 20</span>`);
      if (activeIndicators.has('ema50')) indParts.push(`<span style="color:#e91e63">EMA 50</span>`);
      if (activeIndicators.has('vwap')) indParts.push(`<span style="color:#f5a623">VWAP</span>`);
      if (activeIndicators.has('rsi14')) indParts.push(`<span style="color:#7b1fa2">RSI 14</span>`);
      if (activeIndicators.has('macd')) indParts.push(`<span style="color:#00897b">MACD</span>`);
      legendIndicators.innerHTML = indParts.join(' • ');
    }

    if (symbolBtn) {
      const labelSpan = symbolBtn.querySelector('.sym-label');
      if (labelSpan) labelSpan.textContent = currentSymbol;
    }
  }

  // Drawing Tools Handler on HTML5 Canvas
  function renderDrawings() {
    if (!drawingCtx || !drawingCanvas) return;
    drawingCtx.clearRect(0, 0, drawingCanvas.width, drawingCanvas.height);
    const w = drawingCanvas.width;

    drawings.forEach(d => {
      if (d.type === 'horizontal') {
        drawingCtx.strokeStyle = '#f5a623';
        drawingCtx.lineWidth = 1.5;
        drawingCtx.setLineDash([6, 6]);
        drawingCtx.beginPath();
        drawingCtx.moveTo(0, d.y);
        drawingCtx.lineTo(w, d.y);
        drawingCtx.stroke();
        drawingCtx.setLineDash([]);

        // Label badge
        drawingCtx.fillStyle = '#f5a623';
        drawingCtx.fillRect(w - 75, d.y - 10, 70, 18);
        drawingCtx.fillStyle = '#000000';
        drawingCtx.font = 'bold 10px sans-serif';
        drawingCtx.fillText('S&R LEVEL', w - 70, d.y + 3);
      } else if (d.type === 'trendline' && d.p1 && d.p2) {
        drawingCtx.strokeStyle = '#2962ff';
        drawingCtx.lineWidth = 2;
        drawingCtx.beginPath();
        drawingCtx.moveTo(d.p1.x, d.p1.y);
        drawingCtx.lineTo(d.p2.x, d.p2.y);
        drawingCtx.stroke();

        // End circles
        drawingCtx.fillStyle = '#2962ff';
        drawingCtx.beginPath();
        drawingCtx.arc(d.p1.x, d.p1.y, 4, 0, Math.PI * 2);
        drawingCtx.arc(d.p2.x, d.p2.y, 4, 0, Math.PI * 2);
        drawingCtx.fill();
      } else if (d.type === 'fibonacci' && d.p1 && d.p2) {
        const y1 = d.p1.y;
        const y2 = d.p2.y;
        const diff = y2 - y1;
        const levels = [
          { r: 0.0, label: '0.0% (0.0)', col: '#787b86' },
          { r: 0.236, label: '23.6% (0.236)', col: '#f23645' },
          { r: 0.382, label: '38.2% (0.382)', col: '#ff9800' },
          { r: 0.5, label: '50.0% (0.500)', col: '#4caf50' },
          { r: 0.618, label: '61.8% (0.618)', col: '#089981' },
          { r: 0.786, label: '78.6% (0.786)', col: '#2962ff' },
          { r: 1.0, label: '100.0% (1.0)', col: '#787b86' }
        ];

        levels.forEach(lvl => {
          const y = y1 + diff * lvl.r;
          drawingCtx.strokeStyle = lvl.col;
          drawingCtx.lineWidth = 1;
          drawingCtx.setLineDash(lvl.r === 0.5 ? [] : [3, 3]);
          drawingCtx.beginPath();
          drawingCtx.moveTo(0, y);
          drawingCtx.lineTo(w, y);
          drawingCtx.stroke();

          drawingCtx.fillStyle = lvl.col;
          drawingCtx.font = 'bold 9px monospace';
          drawingCtx.fillText(lvl.label, 12, y - 4);
        });
        drawingCtx.setLineDash([]);
      }
    });

    // In-progress point
    if (currentDrawingPoints.length === 1) {
      drawingCtx.fillStyle = '#f5a623';
      drawingCtx.beginPath();
      drawingCtx.arc(currentDrawingPoints[0].x, currentDrawingPoints[0].y, 5, 0, Math.PI * 2);
      drawingCtx.fill();
    }
  }

  // Setup all event listeners once DOM is ready
  function setupUIEventListeners() {
    if (drawingCanvas) {
      drawingCanvas.addEventListener('pointerdown', e => {
        if (activeDrawingTool === 'cursor') return;
        const rect = drawingCanvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        if (activeDrawingTool === 'horizontal') {
          drawings.push({ type: 'horizontal', y });
          activeDrawingTool = 'cursor';
          drawingCanvas.classList.remove('drawing-active');
          updateDrawingToolUI();
          renderDrawings();
        } else if (activeDrawingTool === 'trendline' || activeDrawingTool === 'fibonacci') {
          if (currentDrawingPoints.length === 0) {
            currentDrawingPoints.push({ x, y });
            renderDrawings();
          } else {
            const p1 = currentDrawingPoints[0];
            drawings.push({ type: activeDrawingTool, p1, p2: { x, y } });
            currentDrawingPoints = [];
            activeDrawingTool = 'cursor';
            drawingCanvas.classList.remove('drawing-active');
            updateDrawingToolUI();
            renderDrawings();
          }
        }
      });
    }

    // Timeframe Buttons
    document.querySelectorAll('.timeframe-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.timeframe-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentTimeframe = btn.dataset.tf;
        loadChartData();
      });
    });

    // Symbol Modal
    if (symbolBtn && symbolModal) {
      symbolBtn.addEventListener('click', () => {
        renderSymbolList();
        symbolModal.classList.add('open');
      });

      const closeBtn = document.getElementById('modal-close-btn');
      if (closeBtn) {
        closeBtn.addEventListener('click', () => {
          symbolModal.classList.remove('open');
        });
      }

      const searchInput = document.getElementById('symbol-search-input');
      if (searchInput) {
        searchInput.addEventListener('input', () => {
          renderSymbolList(searchInput.value);
        });
      }

      document.querySelectorAll('.segment-pill').forEach(pill => {
        pill.addEventListener('click', () => {
          document.querySelectorAll('.segment-pill').forEach(p => p.classList.remove('active'));
          pill.classList.add('active');
          const q = searchInput ? searchInput.value : '';
          renderSymbolList(q, pill.dataset.seg);
        });
      });
    }

    // Indicators Popup
    const indBtn = document.getElementById('indicators-btn');
    if (indBtn && indicatorsPopup) {
      indBtn.addEventListener('click', e => {
        e.stopPropagation();
        indicatorsPopup.classList.toggle('open');
        if (drawingPopup) drawingPopup.classList.remove('open');
      });

      document.querySelectorAll('.indicator-item').forEach(row => {
        row.addEventListener('click', e => {
          e.stopPropagation();
          const key = row.dataset.key;
          if (activeIndicators.has(key)) {
            activeIndicators.delete(key);
            row.classList.remove('active');
          } else {
            activeIndicators.add(key);
            row.classList.add('active');
          }
          const badge = document.getElementById('indicator-count-badge');
          if (badge) badge.textContent = activeIndicators.size;
          renderIndicators();
          if (candles.length > 0) updateLegend(candles[candles.length - 1]);
        });
      });
    }

    // Drawings Popup
    if (drawingBtn && drawingPopup) {
      drawingBtn.addEventListener('click', e => {
        e.stopPropagation();
        drawingPopup.classList.toggle('open');
        if (indicatorsPopup) indicatorsPopup.classList.remove('open');
      });

      document.querySelectorAll('.drawing-option').forEach(opt => {
        opt.addEventListener('click', e => {
          e.stopPropagation();
          activeDrawingTool = opt.dataset.tool;
          updateDrawingToolUI();
          drawingPopup.classList.remove('open');
        });
      });

      const clearBtn = document.getElementById('clear-drawings-btn');
      if (clearBtn) {
        clearBtn.addEventListener('click', e => {
          e.stopPropagation();
          drawings = [];
          currentDrawingPoints = [];
          activeDrawingTool = 'cursor';
          updateDrawingToolUI();
          renderDrawings();
          drawingPopup.classList.remove('open');
        });
      }
    }

    // Synthetic candles are demo-only. Do not allow the UI to label them as live.
    if (liveDemoBadge) {
      liveDemoBadge.className = 'badge-status demo';
      liveDemoBadge.innerHTML = `<span class="pulse-dot" style="background:var(--badge-gold)"></span> DEMO DATA`;
      liveDemoBadge.style.cursor = 'default';
    }

    // Theme Toggle
    if (themeBtn) {
      themeBtn.addEventListener('click', () => {
        isDarkMode = !isDarkMode;
        document.documentElement.setAttribute('data-theme', isDarkMode ? 'dark' : 'light');
        themeBtn.textContent = isDarkMode ? '☀️' : '🌙';
        if (chart) {
          const { ColorType } = window.LightweightCharts;
          chart.applyOptions({
            layout: {
              background: { type: ColorType.Solid, color: isDarkMode ? '#131722' : '#ffffff' },
              textColor: isDarkMode ? '#d1d4dc' : '#131722'
            },
            grid: {
              vertLines: { color: isDarkMode ? '#1e222d' : '#f0f3fa' },
              horzLines: { color: isDarkMode ? '#1e222d' : '#f0f3fa' }
            }
          });
        }
        renderDrawings();
      });
    }

    // Fullscreen Toggle
    if (fullscreenBtn) {
      fullscreenBtn.addEventListener('click', () => {
        if (!document.fullscreenElement) {
          document.documentElement.requestFullscreen?.().catch(() => {});
        } else {
          document.exitFullscreen?.().catch(() => {});
        }
      });
    }

    // Close popups on outer click
    document.addEventListener('click', () => {
      if (indicatorsPopup) indicatorsPopup.classList.remove('open');
      if (drawingPopup) drawingPopup.classList.remove('open');
    });
  }

  function updateDrawingToolUI() {
    if (!drawingBtn || !drawingCanvas) return;
    const labels = {
      cursor: '✏️ Draw',
      trendline: '📏 Trendline',
      horizontal: '➖ S&R Line',
      fibonacci: '📐 Fibonacci'
    };
    drawingBtn.textContent = labels[activeDrawingTool] || '✏️ Draw';
    if (activeDrawingTool !== 'cursor') {
      drawingCanvas.classList.add('drawing-active');
      drawingBtn.classList.add('active');
    } else {
      drawingCanvas.classList.remove('drawing-active');
      drawingBtn.classList.remove('active');
    }
  }

  function renderSymbolList(query = '', seg = 'ALL') {
    const listEl = document.getElementById('instrument-list');
    if (!listEl) return;
    const q = (query || '').toUpperCase().trim();
    const filtered = INSTRUMENTS.filter(i => {
      const matchQ = !q || i.symbol.toUpperCase().includes(q) || i.name.toUpperCase().includes(q);
      const matchSeg = seg === 'ALL' || i.segment === seg;
      return matchQ && matchSeg;
    });

    listEl.innerHTML = filtered.map(i => {
      const diff = +(i.price - i.prev).toFixed(2);
      const isUp = diff >= 0;
      return `
        <div class="instrument-item" data-symbol="${i.symbol}">
          <div>
            <div style="font-weight:700;font-size:13px">${i.symbol}
              <span style="font-size:9px;padding:2px 5px;border-radius:3px;background:rgba(41,98,255,0.2);color:#2962ff;font-weight:700">${i.exchange} • ${i.segment}</span>
            </div>
            <div style="font-size:11px;color:var(--text-muted)">${i.name}</div>
          </div>
          <div style="text-align:right">
            <div style="font-weight:700;font-size:13px">₹${i.price.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
            <div style="font-size:11px;font-weight:600;color:${isUp ? 'var(--candle-up)' : 'var(--candle-down)'}">
              ${isUp ? '+' : ''}${diff.toFixed(2)}
            </div>
          </div>
        </div>
      `;
    }).join('');

    listEl.querySelectorAll('.instrument-item').forEach(item => {
      item.addEventListener('click', () => {
        currentSymbol = item.dataset.symbol;
        if (symbolModal) symbolModal.classList.remove('open');
        loadChartData();
      });
    });
  }

  // Export Engine API on window object for Android WebView interface
  window.TradingChartEngine = {
    setSymbol: sym => { currentSymbol = sym; loadChartData(); },
    setTimeframe: tf => { currentTimeframe = tf; loadChartData(); },
    toggleIndicator: key => {
      if (activeIndicators.has(key)) activeIndicators.delete(key);
      else activeIndicators.add(key);
      renderIndicators();
    },
    setDrawingTool: tool => {
      activeDrawingTool = tool;
      updateDrawingToolUI();
    },
    clearDrawings: () => {
      drawings = [];
      renderDrawings();
    },
    setTheme: mode => {
      isDarkMode = mode === 'dark';
      document.documentElement.setAttribute('data-theme', isDarkMode ? 'dark' : 'light');
    },
    refit: () => {
      handleResize();
      if (chart && chart.timeScale) chart.timeScale().fitContent();
    }
  };

  // Launch strictly after DOM is ready
  function onDOMReady(eventSource) {
    console.log(`[MY TRADING CHARTS] DOM is ready (source: ${eventSource}, document.readyState: "${document.readyState}"). Initiating chartEngine...`);
    initChartEngine();
  }

  if (document.readyState === 'complete' || document.readyState === 'interactive') {
    console.log(`[MY TRADING CHARTS] Document is already interactive/complete (readyState: "${document.readyState}"). Dispatching initChartEngine via setTimeout.`);
    setTimeout(() => onDOMReady('document.readyState check'), 10);
  } else {
    console.log('[MY TRADING CHARTS] Document still loading (readyState: "loading"). Registering DOMContentLoaded event listener...');
    document.addEventListener('DOMContentLoaded', () => {
      onDOMReady('DOMContentLoaded event listener');
    });
  }
})();
