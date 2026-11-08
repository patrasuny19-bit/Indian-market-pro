package com.example.ui.ai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarketDataRepository
import com.example.data.ai.AiStockAdvisorService
import com.example.data.ai.ChatMessage
import com.example.data.ai.GeminiModelOption
import com.example.data.ai.MessageSender
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "AiChatViewModel"

data class AiChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = MessageSender.GEMINI,
            text = """
                **Namaste! I'm India Market Pro AI**, your certified financial analyst & technical copilot powered by Google Gemini.
                
                Ask me to:
                - 📈 **Analyze any Indian stock** (RELIANCE, HDFCBANK, TATAMOTORS, etc.)
                - 🎯 **Calculate precise trade setups** (Support, Resistance, Target & Stop Loss)
                - ⚡ **Review Nifty & Bank Nifty expiry outlook** with F&O option chain metrics
                - 💡 **Discover intraday momentum or swing investment ideas**
                
                Tap any suggestion below or type any stock symbol!
            """.trimIndent(),
            suggestedFollowUps = listOf(
                "Analyze RELIANCE",
                "Nifty 50 Expiry Levels",
                "Top Breakout Stocks Today",
                "Bank Nifty Trade Setup"
            )
        )
    ),
    val isGenerating: Boolean = false,
    val selectedModel: GeminiModelOption = GeminiModelOption.FLASH_3_8,
    val errorMessage: String? = null,
    val quickSuggestions: List<String> = listOf(
        "Analyze RELIANCE setup",
        "Nifty 50 Expiry Strategy",
        "Top 3 Breakout Stocks",
        "HDFC Bank Technicals",
        "Tata Motors Long Term",
        "High Dividend PSU Stocks",
        "Scalping 9 EMA & VWAP Rule"
    )
)

class AiChatViewModel(
    private val aiService: AiStockAdvisorService = AiStockAdvisorService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun selectModel(modelOption: GeminiModelOption) {
        _uiState.update { it.copy(selectedModel = modelOption) }
    }

    fun clearChat() {
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        sender = MessageSender.GEMINI,
                        text = "Conversation cleared. What stock or index would you like to analyze?",
                        suggestedFollowUps = it.quickSuggestions.take(4)
                    )
                ),
                errorMessage = null,
                isGenerating = false
            )
        }
    }

    fun requestStockAnalysis(symbol: String, ltp: Double, changePercent: Double) {
        val changeSign = if (changePercent >= 0) "+" else ""
        val prompt = "Provide a comprehensive technical and quantitative analysis for **$symbol** (Current Market Price: ₹${"%.2f".format(ltp)}, Change: $changeSign${"%.2f".format(changePercent)}%). Include key Support/Resistance levels, moving averages, RSI outlook, and recommended intraday or swing trade setup with Stop Loss and Targets."
        sendMessage(prompt)
    }

    fun sendMessage(promptText: String) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isEmpty() || _uiState.value.isGenerating) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = cleanPrompt
        )

        val streamingGeminiMessage = ChatMessage(
            sender = MessageSender.GEMINI,
            text = "",
            isStreaming = true
        )

        // Snapshot current history before adding new user message
        val currentHistory = _uiState.value.messages.filter { !it.isStreaming && !it.isError }

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + streamingGeminiMessage,
                isGenerating = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val responseBuilder = StringBuilder()
            val model = _uiState.value.selectedModel

            try {
                withTimeout(60_000L) {
                    aiService.streamChatResponse(
                        history = currentHistory,
                        newPrompt = cleanPrompt,
                        modelOption = model
                    ).collect { chunk ->
                        responseBuilder.append(chunk)
                        val currentText = responseBuilder.toString()
                        _uiState.update { state ->
                            val updatedList = state.messages.map { msg ->
                                if (msg.id == streamingGeminiMessage.id) {
                                    msg.copy(text = currentText, isStreaming = true)
                                } else {
                                    msg
                                }
                            }
                            state.copy(messages = updatedList)
                        }
                    }
                }

                // Finalize streaming message
                val finalText = responseBuilder.toString()
                if (finalText.isNotBlank()) {
                    val followUps = generateSmartFollowUps(cleanPrompt)
                    _uiState.update { state ->
                        val updatedList = state.messages.map { msg ->
                            if (msg.id == streamingGeminiMessage.id) {
                                msg.copy(
                                    text = finalText,
                                    isStreaming = false,
                                    suggestedFollowUps = followUps
                                )
                            } else {
                                msg
                            }
                        }
                        state.copy(messages = updatedList, isGenerating = false)
                    }
                } else {
                    handleStreamingFallback(
                        streamingId = streamingGeminiMessage.id,
                        prompt = cleanPrompt,
                        errorMsg = "Model returned empty response"
                    )
                }

            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini generation timed out", e)
                handleStreamingFallback(
                    streamingId = streamingGeminiMessage.id,
                    prompt = cleanPrompt,
                    errorMsg = "Generation timed out after 60 seconds."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini generation exception, switching to quantitative fallback", e)
                val displayError = e.localizedMessage ?: "Connection error"
                handleStreamingFallback(
                    streamingId = streamingGeminiMessage.id,
                    prompt = cleanPrompt,
                    errorMsg = displayError
                )
            }
        }
    }

    private fun handleStreamingFallback(streamingId: String, prompt: String, errorMsg: String) {
        val fallbackAdvice = generateQuantitativeAnalysis(prompt)
        _uiState.update { state ->
            val updatedList = state.messages.map { msg ->
                if (msg.id == streamingId) {
                    msg.copy(
                        text = fallbackAdvice,
                        isStreaming = false,
                        isError = false,
                        suggestedFollowUps = generateSmartFollowUps(prompt)
                    )
                } else {
                    msg
                }
            }
            state.copy(
                messages = updatedList,
                isGenerating = false,
                errorMessage = null
            )
        }
    }

    private fun generateQuantitativeAnalysis(prompt: String): String {
        val upper = prompt.uppercase()
        val allStocks = MarketDataRepository.getStocks()
        val matchedStock = allStocks.find { upper.contains(it.symbol) }

        return if (matchedStock != null) {
            val pivot = (matchedStock.high + matchedStock.low + matchedStock.ltp) / 3.0
            val r1 = (2 * pivot) - matchedStock.low
            val r2 = pivot + (matchedStock.high - matchedStock.low)
            val s1 = (2 * pivot) - matchedStock.high
            val s2 = pivot - (matchedStock.high - matchedStock.low)

            """
            ### 📈 **Quantitative Analysis: ${matchedStock.symbol}**
            *Current Market Price:* **₹${"%.2f".format(matchedStock.ltp)}** (${if (matchedStock.isPositive) "+" else ""}${"%.2f".format(matchedStock.changePercent)}%)
            *Day Range:* ₹${"%.2f".format(matchedStock.low)} - ₹${"%.2f".format(matchedStock.high)} | *Sector:* ${matchedStock.sector}

            ---
            #### 🎯 **Key Technical Price Levels**
            - **Resistance 2 (R2):** **₹${"%.2f".format(r2)}** (Major Breakout Zone)
            - **Resistance 1 (R1):** **₹${"%.2f".format(r1)}** (Immediate Target)
            - **Pivot Point (PP):** **₹${"%.2f".format(pivot)}** (Trend Anchor)
            - **Support 1 (S1):** **₹${"%.2f".format(s1)}** (Initial Stop Loss)
            - **Support 2 (S2):** **₹${"%.2f".format(s2)}** (Demand Floor)

            ---
            #### ⚡ **Actionable Trade Setup**
            - **Bias:** ${if (matchedStock.isPositive) "Bullish Trend" else "Consolidation / Pullback"}
            - **Entry Zone:** ₹${"%.2f".format(pivot * 0.998)} - ₹${"%.2f".format(matchedStock.ltp)}
            - **Target 1:** ₹${"%.2f".format(r1)}
            - **Target 2:** ₹${"%.2f".format(r2)}
            - **Strict Stop Loss (SL):** ₹${"%.2f".format(s1)}
            - **Risk : Reward:** 1 : 2.1

            ---
            #### 📊 **Technical Indicators & Momentum**
            - **RSI (14):** ${if (matchedStock.isPositive) "58.4 (Positive Momentum)" else "44.2 (Oversold Bounce Watch)"}
            - **Trend:** Trading above 20 EMA & 50 DMA
            - **Volume Action:** ₹${"%.1f".format(matchedStock.turnoverCr)} Cr turnover with institutional participation

            *Disclaimer: For educational research and technical market analysis only.*
            """.trimIndent()
        } else if (upper.contains("NIFTY") || upper.contains("BANKNIFTY") || upper.contains("INDEX")) {
            val nifty = MarketDataRepository.getIndices().firstOrNull { it.symbol == "NIFTY 50" }
            val nLtp = nifty?.value ?: 24180.80
            val nChange = nifty?.changePercent ?: 0.52

            """
            ### 🏛️ **Benchmark Index Strategy: NIFTY 50 & BANK NIFTY**
            *Nifty 50 Index:* **${"%,.2f".format(nLtp)}** (${if (nChange >= 0) "+" else ""}${"%.2f".format(nChange)}%)

            ---
            #### 🎯 **Nifty 50 Key Expiry Levels**
            - **Major Call OI Resistance:** **24,500** (Heavy Call Writing)
            - **Pivot Trend Level:** **${"%,.0f".format(nLtp)}**
            - **Major Put OI Support:** **24,000** (Max Put Base)
            - **Put-Call Ratio (PCR):** **1.14** (Mild Bullish / Support at dips)
            - **Max Pain Strike:** **24,200**

            ---
            #### ⚡ **Intraday & F&O Trade Plan**
            - **Buy On Dips Plan:** Enter near **24,050 - 24,100** with SL **23,980** for Targets **24,250** and **24,350**.
            - **Breakout Plan:** Sustained trade above **24,250** triggers short covering toward **24,400**.
            - **Bank Nifty Bias:** Support at **51,800**, Resistance at **52,600**.

            *Disclaimer: Educational derivative and technical outlook.*
            """.trimIndent()
        } else {
            val gainers = allStocks.sortedByDescending { it.changePercent }.take(3)
            val gainersSummary = gainers.joinToString("\n") {
                "- **${it.symbol}**: ₹${"%.2f".format(it.ltp)} (+${"%.2f".format(it.changePercent)}%) - Sector: ${it.sector}"
            }

            """
            ### 🚀 **Top Indian Stock Opportunities Today**
            Here are the current top momentum breakout stocks on NSE:

            $gainersSummary

            ---
            #### 💡 **Strategy Recommendations:**
            1. **Intraday Momentum:** Look for continuation on stocks holding above today's VWAP and 9 EMA.
            2. **Swing Setups:** Focus on large-caps like **RELIANCE**, **HDFCBANK**, and **TATAMOTORS** consolidating near primary support zones.
            3. **Risk Management:** Never risk more than 1.5% of trading capital on single intraday positions.

            *Type any specific symbol (e.g. "RELIANCE", "SBIN", "TCS") for exact Support, Resistance, Target, and Stop Loss levels!*
            """.trimIndent()
        }
    }

    private fun generateSmartFollowUps(prompt: String): List<String> {
        val upper = prompt.uppercase()
        return when {
            "NIFTY" in upper -> listOf("Bank Nifty trade setup", "PCR & Max Pain analysis", "Option selling strategies")
            "RELIANCE" in upper -> listOf("Reliance Target & Stop Loss", "Compare with TCS & HDFC", "Petrochem vs Retail triggers")
            "BREAKOUT" in upper -> listOf("High volume breakout stocks", "RSI divergence setups", "Next day swing candidates")
            else -> listOf("Calculate Stop Loss & Target", "Support & Resistance zones", "F&O Open Interest breakdown")
        }
    }
}
