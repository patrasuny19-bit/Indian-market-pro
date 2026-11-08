package com.example.data.market

enum class DataProviderType(val displayName: String, val licensedOrg: String, val requiresCredentials: Boolean) {
    DIRECT_EXCHANGE_FEED(
        displayName = "Direct Real-Time Exchange Gateway",
        licensedOrg = "NSE & BSE Live Market Data",
        requiresCredentials = false
    ),
    UPSTOX_V2(
        displayName = "Upstox V2 Market Feed",
        licensedOrg = "Upstox (RKSV Securities India)",
        requiresCredentials = true
    ),
    ZERODHA_KITE(
        displayName = "Zerodha Kite Connect 3",
        licensedOrg = "Zerodha Broking Ltd",
        requiresCredentials = true
    ),
    ANGEL_ONE_SMARTAPI(
        displayName = "Angel One SmartAPI",
        licensedOrg = "Angel One Ltd",
        requiresCredentials = true
    ),
    CUSTOM_BACKEND(
        displayName = "Institutional Proxy / Custom Gateway",
        licensedOrg = "Custom SEBI Broker Gateway",
        requiresCredentials = true
    )
}

enum class ProviderConnectionStatus(val label: String) {
    CONNECTED_LIVE("Connected (Live Feed)"),
    CONNECTING("Syncing Market Feed..."),
    CLOSED_OUT_OF_HOURS("Market Closed (Official Close)"),
    STALE_CACHED("Using Cached Quote (Reconnecting...)"),
    DISCONNECTED_ERROR("Connection Warning")
}

data class MarketDataProviderConfig(
    val activeProvider: DataProviderType = DataProviderType.DIRECT_EXCHANGE_FEED,
    val apiKey: String = "",
    val accessToken: String = "",
    val customGatewayUrl: String = "",
    val autoPollingIntervalSecs: Int = 15,
    val isWebSocketEnabled: Boolean = true
)

data class MarketFeedHealth(
    val status: ProviderConnectionStatus = ProviderConnectionStatus.CONNECTING,
    val providerName: String = "Direct Real-Time Exchange Gateway",
    val lastSuccessTimestamp: Long = 0L,
    val lastSyncTimeFormatted: String = "Connecting...",
    val latencyMs: Long = 0L,
    val warningMessage: String? = null
)
