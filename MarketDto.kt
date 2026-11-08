package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChartResponseDto(
    @Json(name = "chart") val chart: ChartResultDto? = null
)

@JsonClass(generateAdapter = true)
data class ChartResultDto(
    @Json(name = "result") val result: List<ChartItemDto>? = null,
    @Json(name = "error") val error: ChartErrorDto? = null
)

@JsonClass(generateAdapter = true)
data class ChartErrorDto(
    @Json(name = "code") val code: String? = null,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ChartItemDto(
    @Json(name = "meta") val meta: ChartMetaDto? = null,
    @Json(name = "timestamp") val timestamp: List<Long>? = null,
    @Json(name = "indicators") val indicators: ChartIndicatorsDto? = null
)

@JsonClass(generateAdapter = true)
data class ChartMetaDto(
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "regularMarketPrice") val regularMarketPrice: Double? = null,
    @Json(name = "regularMarketDayHigh") val regularMarketDayHigh: Double? = null,
    @Json(name = "regularMarketDayLow") val regularMarketDayLow: Double? = null,
    @Json(name = "previousClose") val previousClose: Double? = null,
    @Json(name = "chartPreviousClose") val chartPreviousClose: Double? = null,
    @Json(name = "fiftyTwoWeekHigh") val fiftyTwoWeekHigh: Double? = null,
    @Json(name = "fiftyTwoWeekLow") val fiftyTwoWeekLow: Double? = null,
    @Json(name = "regularMarketVolume") val regularMarketVolume: Long? = null,
    @Json(name = "exchangeName") val exchangeName: String? = null,
    @Json(name = "instrumentType") val instrumentType: String? = null,
    @Json(name = "shortName") val shortName: String? = null,
    @Json(name = "longName") val longName: String? = null
) {
    val effectivePrevClose: Double?
        get() = previousClose ?: chartPreviousClose
}

@JsonClass(generateAdapter = true)
data class ChartIndicatorsDto(
    @Json(name = "quote") val quote: List<ChartQuoteDto>? = null
)

@JsonClass(generateAdapter = true)
data class ChartQuoteDto(
    @Json(name = "open") val open: List<Double?>? = null,
    @Json(name = "high") val high: List<Double?>? = null,
    @Json(name = "low") val low: List<Double?>? = null,
    @Json(name = "close") val close: List<Double?>? = null,
    @Json(name = "volume") val volume: List<Long?>? = null
)
