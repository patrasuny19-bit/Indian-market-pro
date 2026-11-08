package com.example.model

enum class InstrumentSegment {
    INDEX, EQUITY, FUTURES, OPTIONS
}

data class MarketInstrument(
    val symbol: String,
    val name: String,
    val exchange: String,
    val segment: InstrumentSegment,
    val currentPrice: Double,
    val previousClose: Double,
    val lotSize: Int,
    val strikePrice: Double? = null,
    val optionType: String? = null
) {
    val change: Double get() = currentPrice - previousClose
    val changePercent: Double get() = if (previousClose > 0) (change / previousClose) * 100 else 0.0
    val isPositive: Boolean get() = change >= 0
}

object MarketDataCatalog {
    val INSTRUMENTS = listOf(
        MarketInstrument("NIFTY 50", "NIFTY 50 Benchmark Index", "NSE", InstrumentSegment.INDEX, 24360.50, 24210.20, 25),
        MarketInstrument("BANKNIFTY", "NIFTY Banking Index", "NSE", InstrumentSegment.INDEX, 51840.75, 51520.10, 15),
        MarketInstrument("FINNIFTY", "NIFTY Financial Services", "NSE", InstrumentSegment.INDEX, 23910.40, 23780.00, 25),
        MarketInstrument("SENSEX", "BSE SENSEX 30", "BSE", InstrumentSegment.INDEX, 80215.30, 79780.50, 10),
        MarketInstrument("RELIANCE", "Reliance Industries Ltd.", "NSE", InstrumentSegment.EQUITY, 2742.60, 2720.15, 1),
        MarketInstrument("TCS", "Tata Consultancy Services Ltd.", "NSE", InstrumentSegment.EQUITY, 4120.30, 4095.00, 1),
        MarketInstrument("HDFCBANK", "HDFC Bank Ltd.", "NSE", InstrumentSegment.EQUITY, 1748.90, 1732.40, 1),
        MarketInstrument("INFY", "Infosys Ltd.", "NSE", InstrumentSegment.EQUITY, 1865.40, 1845.80, 1),
        MarketInstrument("ICICIBANK", "ICICI Bank Ltd.", "NSE", InstrumentSegment.EQUITY, 1262.15, 1251.00, 1),
        MarketInstrument("SBIN", "State Bank of India", "NSE", InstrumentSegment.EQUITY, 818.50, 809.20, 1),
        MarketInstrument("BHARTIARTL", "Bharti Airtel Ltd.", "NSE", InstrumentSegment.EQUITY, 1612.00, 1598.00, 1),
        MarketInstrument("TATAMOTORS", "Tata Motors Ltd.", "NSE", InstrumentSegment.EQUITY, 875.25, 864.10, 1),
        MarketInstrument("NIFTY-FUT", "NIFTY Current Month Futures", "NFO", InstrumentSegment.FUTURES, 24410.00, 24260.00, 25),
        MarketInstrument("BANKNIFTY-FUT", "BANKNIFTY Current Month Futures", "NFO", InstrumentSegment.FUTURES, 51950.00, 51630.00, 15),
        MarketInstrument("RELIANCE-FUT", "RELIANCE Current Month Futures", "NFO", InstrumentSegment.FUTURES, 2755.00, 2731.00, 250),
        MarketInstrument("NIFTY 24500 CE", "NIFTY 24500 Call Option", "NFO", InstrumentSegment.OPTIONS, 142.50, 110.00, 25, 24500.0, "CE"),
        MarketInstrument("NIFTY 24500 PE", "NIFTY 24500 Put Option", "NFO", InstrumentSegment.OPTIONS, 98.75, 135.00, 25, 24500.0, "PE"),
        MarketInstrument("BANKNIFTY 52000 CE", "BANKNIFTY 52000 Call Option", "NFO", InstrumentSegment.OPTIONS, 285.40, 215.00, 15, 52000.0, "CE"),
        MarketInstrument("BANKNIFTY 51500 PE", "BANKNIFTY 51500 Put Option", "NFO", InstrumentSegment.OPTIONS, 195.20, 260.00, 15, 51500.0, "PE")
    )
}
