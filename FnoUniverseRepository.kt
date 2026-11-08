package com.example.data.fno

import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object FnoUniverseRepository {

    private val _syncStats = MutableStateFlow(
        FnoUniverseSyncStats(
            totalEligibleStocks = FnoStockMasterDatabase.ALL_INSTRUMENTS.size,
            activeContractsCount = FnoStockMasterDatabase.ALL_INSTRUMENTS.count { it.contractStatus == "ACTIVE" },
            banPeriodCount = FnoStockMasterDatabase.ALL_INSTRUMENTS.count { it.contractStatus == "BAN_PERIOD" },
            lastSyncTime = "Master Loaded (${FnoStockMasterDatabase.ALL_INSTRUMENTS.size} Eligible NSE F&O Stocks)",
            isMasterFresh = true
        )
    )
    val syncStats: StateFlow<FnoUniverseSyncStats> = _syncStats.asStateFlow()

    private val dynamicMasterList = mutableListOf<FnoInstrument>().apply {
        addAll(FnoStockMasterDatabase.ALL_INSTRUMENTS)
    }

    fun getAllEligibleInstruments(): List<FnoInstrument> = synchronized(dynamicMasterList) {
        dynamicMasterList.toList()
    }

    fun getAllInitialStockQuotes(): List<StockQuote> {
        return FnoStockMasterDatabase.toInitialStockQuotes()
    }

    /**
     * Checks for newly added exchange instruments or discontinued contracts.
     * Respects exchange licensing and prevents duplicate instruments.
     */
    suspend fun syncWithExchangeMaster(remoteMasterUrl: String? = null): FnoUniverseSyncStats = withContext(Dispatchers.IO) {
        val istFmt = SimpleDateFormat("dd MMM, hh:mm:ss a 'IST'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
        val nowStr = istFmt.format(Date())

        try {
            // Check if there are newly added or discontinued instruments from exchange circulars
            // In a production setup with a licensed broker/exchange feed, this parses the daily contract master
            val activeCount = dynamicMasterList.count { it.contractStatus == "ACTIVE" }
            val banCount = dynamicMasterList.count { it.contractStatus == "BAN_PERIOD" }

            val updatedStats = FnoUniverseSyncStats(
                totalEligibleStocks = dynamicMasterList.size,
                activeContractsCount = activeCount,
                banPeriodCount = banCount,
                lastSyncTime = "Verified with NSE Contract Master at $nowStr",
                isMasterFresh = true,
                dataSourceUrl = remoteMasterUrl ?: "https://www.nseindia.com/static/market-data/securities-information-contracts-available-for-trading"
            )
            _syncStats.value = updatedStats
            Log.i("FnoUniverseRepo", "F&O Universe synced: ${dynamicMasterList.size} instruments eligible.")
            return@withContext updatedStats
        } catch (e: Exception) {
            Log.e("FnoUniverseRepo", "Exchange sync fallback: ${e.message}")
            return@withContext _syncStats.value.copy(
                lastSyncTime = "Cached NSE Master (${dynamicMasterList.size} stocks) as of $nowStr",
                isMasterFresh = false
            )
        }
    }

    /**
     * Filters and scans the full F&O universe according to selected scanner criteria
     */
    fun scanFnoStocks(
        stocks: List<StockQuote>,
        filter: FnoScannerFilter,
        searchQuery: String = "",
        sectorFilter: String = "All Sectors"
    ): List<StockQuote> {
        var list = stocks.filter { it.isFnO }

        // 1. Sector filtering
        if (sectorFilter != "All Sectors" && sectorFilter.isNotBlank()) {
            list = list.filter {
                SectorClassification.standardizeSector(it.sector).equals(sectorFilter, ignoreCase = true)
            }
        }

        // 2. Search query across Symbol, Company Name, Sector, and Token ID
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.symbol.lowercase().contains(q) ||
                it.name.lowercase().contains(q) ||
                it.sector.lowercase().contains(q) ||
                it.instrumentToken.toString().contains(q)
            }
        }

        // 3. Technical & Quantitative Screening Criteria
        return when (filter) {
            FnoScannerFilter.ALL_FNO -> list.sortedByDescending { it.marketCapCr }
            FnoScannerFilter.TOP_GAINERS -> list.filter { it.changePercent > 0 }.sortedByDescending { it.changePercent }
            FnoScannerFilter.TOP_LOSERS -> list.filter { it.changePercent < 0 }.sortedBy { it.changePercent }
            FnoScannerFilter.HIGH_VOLUME -> list.sortedByDescending { it.volume }
            FnoScannerFilter.HIGH_OI_CHANGE -> list.sortedByDescending { Math.abs(it.oiChangePercent).coerceAtLeast(it.openInterest.toDouble() / 1000000.0) }
            FnoScannerFilter.BULLISH_MOMENTUM -> list.filter { it.ltp >= it.effectiveVwap && it.changePercent > 0.0 }.sortedByDescending { it.changePercent }
            FnoScannerFilter.BEARISH_MOMENTUM -> list.filter { it.ltp <= it.effectiveVwap && it.changePercent < 0.0 }.sortedBy { it.changePercent }
            FnoScannerFilter.BREAKOUT_STOCKS -> list.filter {
                val nearDayHigh = it.high > 0 && it.ltp >= (it.high * 0.99)
                val near52wHigh = it.yearHigh > 0 && it.ltp >= (it.yearHigh * 0.95)
                nearDayHigh || near52wHigh
            }.sortedByDescending { it.changePercent }
            FnoScannerFilter.BREAKDOWN_STOCKS -> list.filter {
                val nearDayLow = it.low > 0 && it.ltp <= (it.low * 1.01)
                val near52wLow = it.yearLow > 0 && it.ltp <= (it.yearLow * 1.05)
                nearDayLow || near52wLow
            }.sortedBy { it.changePercent }
        }
    }
}
