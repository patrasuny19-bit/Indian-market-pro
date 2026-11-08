package com.example.data.api

import com.example.data.api.model.ChartResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NseMarketApiService {

    /**
     * Fetches real-time market data, quote metadata, and OHLC candlestick history
     * for Indian indices (e.g. ^NSEI, ^NSEBANK, ^BSESN) and NSE stocks (e.g. RELIANCE.NS, HDFCBANK.NS).
     */
    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path(value = "symbol", encoded = true) symbol: String,
        @Query("interval") interval: String = "1d",
        @Query("range") range: String = "1d"
    ): Response<ChartResponseDto>
}
