package com.example.data.market

import java.text.SimpleDateFormat
import java.util.*

enum class MarketSessionStatus(val label: String, val isLive: Boolean) {
    OPEN_REGULAR("LIVE MARKET OPEN", true),
    PRE_MARKET("PRE-MARKET SESSION", true),
    POST_MARKET("POST-MARKET CLOSING", false),
    CLOSED_AFTER_HOURS("MARKET CLOSED", false),
    CLOSED_WEEKEND("WEEKEND CLOSED", false),
    CLOSED_HOLIDAY("EXCHANGE HOLIDAY", false)
}

data class MarketScheduleInfo(
    val status: MarketSessionStatus,
    val statusBadgeText: String,
    val isLiveTradingActive: Boolean,
    val currentIstTimeFormatted: String,
    val currentIstDateFormatted: String,
    val nextSessionInfo: String,
    val holidayName: String? = null
)

object IndianMarketCalendar {

    val IST_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    // NSE & BSE Official Trading Holidays (YYYY-MM-DD -> Holiday Name)
    private val HOLIDAYS = mapOf(
        // 2025
        "2025-01-26" to "Republic Day",
        "2025-02-26" to "Mahashivratri",
        "2025-03-14" to "Holi",
        "2025-03-31" to "Id-Ul-Fitr (Ramzan Id)",
        "2025-04-10" to "Mahavir Jayanti",
        "2025-04-14" to "Dr. B.R. Ambedkar Jayanti",
        "2025-04-18" to "Good Friday",
        "2025-05-01" to "Maharashtra Day",
        "2025-06-07" to "Bakri Id",
        "2025-07-06" to "Moharram",
        "2025-08-15" to "Independence Day",
        "2025-08-27" to "Ganesh Chaturthi",
        "2025-10-02" to "Mahatma Gandhi Jayanti",
        "2025-10-21" to "Diwali (Laxmi Pujan - Muhurat Trading)",
        "2025-10-22" to "Diwali (Balipratipada)",
        "2025-11-05" to "Guru Nanak Jayanti",
        "2025-12-25" to "Christmas",
        // 2026
        "2026-01-26" to "Republic Day",
        "2026-02-15" to "Mahashivratri",
        "2026-03-04" to "Holi",
        "2026-03-20" to "Eid-ul-Fitr",
        "2026-03-31" to "Mahavir Jayanti",
        "2026-04-03" to "Good Friday",
        "2026-04-14" to "Dr. Ambedkar Jayanti",
        "2026-05-01" to "Maharashtra Day",
        "2026-05-27" to "Bakri Id",
        "2026-06-26" to "Muharram",
        "2026-08-15" to "Independence Day",
        "2026-09-14" to "Ganesh Chaturthi",
        "2026-10-02" to "Mahatma Gandhi Jayanti",
        "2026-10-20" to "Dussehra",
        "2026-11-08" to "Diwali (Laxmi Pujan)",
        "2026-11-09" to "Diwali Balipratipada",
        "2026-11-24" to "Guru Nanak Jayanti",
        "2026-12-25" to "Christmas"
    )

    fun getCurrentSchedule(date: Date = Date()): MarketScheduleInfo {
        val calendar = Calendar.getInstance(IST_TIME_ZONE).apply { time = date }

        val timeFmt = SimpleDateFormat("hh:mm:ss a 'IST'", Locale.US).apply { timeZone = IST_TIME_ZONE }
        val dateFmt = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US).apply { timeZone = IST_TIME_ZONE }
        val ymdFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = IST_TIME_ZONE }

        val currentIstTimeStr = timeFmt.format(date)
        val currentIstDateStr = dateFmt.format(date)
        val todayYmd = ymdFmt.format(date)

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 7 = Saturday
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        // 1. Check Weekend
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            val nextOpenDay = if (dayOfWeek == Calendar.SATURDAY) "Monday" else "Tomorrow (Mon)"
            return MarketScheduleInfo(
                status = MarketSessionStatus.CLOSED_WEEKEND,
                statusBadgeText = "WEEKEND CLOSED",
                isLiveTradingActive = false,
                currentIstTimeFormatted = currentIstTimeStr,
                currentIstDateFormatted = currentIstDateStr,
                nextSessionInfo = "Next Session: $nextOpenDay at 09:15 AM IST"
            )
        }

        // 2. Check Exchange Holiday
        val holidayName = HOLIDAYS[todayYmd]
        if (holidayName != null) {
            return MarketScheduleInfo(
                status = MarketSessionStatus.CLOSED_HOLIDAY,
                statusBadgeText = "HOLIDAY: $holidayName",
                isLiveTradingActive = false,
                currentIstTimeFormatted = currentIstTimeStr,
                currentIstDateFormatted = currentIstDateStr,
                nextSessionInfo = "Exchange Closed for $holidayName. Next Trading Day: 09:15 AM IST",
                holidayName = holidayName
            )
        }

        // 3. Weekday Market Hours
        // 09:00 - 09:15 -> Pre-market
        // 09:15 - 15:30 -> Regular Live Market
        // 15:30 - 16:00 -> Post-Market Closing
        // Before 09:00 or After 16:00 -> Market Closed
        val preMarketStart = 9 * 60        // 09:00 AM (540 mins)
        val regularMarketStart = 9 * 60 + 15 // 09:15 AM (555 mins)
        val regularMarketEnd = 15 * 60 + 30   // 03:30 PM (930 mins)
        val postMarketEnd = 16 * 60           // 04:00 PM (960 mins)

        return when {
            totalMinutes in preMarketStart until regularMarketStart -> {
                MarketScheduleInfo(
                    status = MarketSessionStatus.PRE_MARKET,
                    statusBadgeText = "PRE-MARKET (NSE/BSE)",
                    isLiveTradingActive = true,
                    currentIstTimeFormatted = currentIstTimeStr,
                    currentIstDateFormatted = currentIstDateStr,
                    nextSessionInfo = "Regular trading commences at 09:15 AM IST"
                )
            }
            totalMinutes in regularMarketStart until regularMarketEnd -> {
                val minsLeft = regularMarketEnd - totalMinutes
                val hoursLeft = minsLeft / 60
                val remainingMins = minsLeft % 60
                val closingIn = if (hoursLeft > 0) "${hoursLeft}h ${remainingMins}m" else "${remainingMins}m"
                MarketScheduleInfo(
                    status = MarketSessionStatus.OPEN_REGULAR,
                    statusBadgeText = "LIVE MARKET OPEN",
                    isLiveTradingActive = true,
                    currentIstTimeFormatted = currentIstTimeStr,
                    currentIstDateFormatted = currentIstDateStr,
                    nextSessionInfo = "Closes today in $closingIn (at 03:30 PM IST)"
                )
            }
            totalMinutes in regularMarketEnd until postMarketEnd -> {
                MarketScheduleInfo(
                    status = MarketSessionStatus.POST_MARKET,
                    statusBadgeText = "POST-MARKET CLOSING",
                    isLiveTradingActive = false,
                    currentIstTimeFormatted = currentIstTimeStr,
                    currentIstDateFormatted = currentIstDateStr,
                    nextSessionInfo = "Closing session in progress. Regular session ended at 03:30 PM IST"
                )
            }
            totalMinutes < preMarketStart -> {
                val minsUntilOpen = preMarketStart - totalMinutes
                val hours = minsUntilOpen / 60
                val mins = minsUntilOpen % 60
                val openingIn = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                MarketScheduleInfo(
                    status = MarketSessionStatus.CLOSED_AFTER_HOURS,
                    statusBadgeText = "MARKET CLOSED",
                    isLiveTradingActive = false,
                    currentIstTimeFormatted = currentIstTimeStr,
                    currentIstDateFormatted = currentIstDateStr,
                    nextSessionInfo = "Pre-market opens in $openingIn at 09:00 AM IST"
                )
            }
            else -> {
                // After 16:00 IST
                val nextDayStr = if (dayOfWeek == Calendar.FRIDAY) "Monday, 09:15 AM IST" else "Tomorrow, 09:15 AM IST"
                MarketScheduleInfo(
                    status = MarketSessionStatus.CLOSED_AFTER_HOURS,
                    statusBadgeText = "MARKET CLOSED",
                    isLiveTradingActive = false,
                    currentIstTimeFormatted = currentIstTimeStr,
                    currentIstDateFormatted = currentIstDateStr,
                    nextSessionInfo = "Market closed for the day. Next session: $nextDayStr"
                )
            }
        }
    }

    fun isMarketOpenNow(): Boolean {
        return getCurrentSchedule().isLiveTradingActive
    }
}
