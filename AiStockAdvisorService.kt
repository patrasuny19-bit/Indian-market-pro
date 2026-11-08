package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull

private const val TAG = "AiStockAdvisor"

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val suggestedFollowUps: List<String> = emptyList()
)

enum class MessageSender {
    USER,
    GEMINI
}

enum class GeminiModelOption(val modelId: String, val displayName: String, val badge: String) {
    FLASH_3_8("gemini-3.8-flash", "Gemini 3.8 Flash", "Default • High Speed"),
    FLASH_LITE_3_5("gemini-3.5-flash-lite", "Gemini 3.5 Lite", "Ultra Fast"),
    ARGON_4("gemini-4-argon", "Gemini 4 Argon", "Deep Reasoning")
}

class AiStockAdvisorService {

    private val systemPrompt = """
        You are 'India Market Pro AI' (powered by Google Gemini), an elite Certified Indian Stock Market Research Analyst and Quantitative Strategist.
        You specialize in the National Stock Exchange of India (NSE) and Bombay Stock Exchange (BSE), including:
        1. Nifty 50, Bank Nifty, FinNifty, Midcap Nifty indices & derivative dynamics.
        2. Large-cap, Mid-cap, Small-cap Indian equities (e.g., Reliance, HDFC Bank, Tata Motors, Infosys, ICICI Bank, SBI, ITC, L&T, etc.).
        3. Real technical analysis: Key Support and Resistance price levels, 20/50/200 Day EMAs, RSI(14), MACD crossovers, Volume Price Action, Breakout / Reversal chart patterns.
        4. F&O Desk insights: Call/Put Open Interest (OI) buildup, Put-Call Ratio (PCR), Max Pain strikes, and Short Covering / Long Unwinding trends.
        5. Actionable trade setups when asked: State clear Entry Range, Target 1, Target 2, Strict Stop-Loss (SL), and Risk:Reward ratio.
        6. Market context: RBI interest rate policy, FII & DII institutional cash flows, Crude oil price impact, US Fed cues, quarterly corporate earnings results.

        Format your answers clearly with markdown:
        - Use bold headings and bullet points.
        - Highlight price levels and key metrics in bold (e.g. **₹2,980.50**, **RSI: 58.4**).
        - Keep responses concise, objective, and actionable.
        - Conclude with a brief standard Indian disclaimer that market investments are subject to risk and provided for educational analysis.
    """.trimIndent()

    // Lazy initialization of GenerativeModels to avoid startup overhead
    private val flashModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GeminiModelOption.FLASH_3_8.modelId,
            systemInstruction = content { text(systemPrompt) }
        )
    }

    private val liteModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GeminiModelOption.FLASH_LITE_3_5.modelId,
            systemInstruction = content { text(systemPrompt) }
        )
    }

    private val argonModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = GeminiModelOption.ARGON_4.modelId,
            systemInstruction = content { text(systemPrompt) }
        )
    }

    private fun getModel(option: GeminiModelOption): GenerativeModel {
        return when (option) {
            GeminiModelOption.FLASH_3_8 -> flashModel
            GeminiModelOption.FLASH_LITE_3_5 -> liteModel
            GeminiModelOption.ARGON_4 -> argonModel
        }
    }

    /**
     * Streams response chunks from Gemini for multi-turn conversation.
     * Preserves conversation history and returns Flow of string chunks.
     */
    fun streamChatResponse(
        history: List<ChatMessage>,
        newPrompt: String,
        modelOption: GeminiModelOption = GeminiModelOption.FLASH_3_8
    ): Flow<String> {
        val model = getModel(modelOption)

        // Gemini API strictly requires multi-turn history to start with 'user'
        // and strictly alternate between 'user' and 'model'.
        val contents = mutableListOf<Content>()
        var expectingRole = "user"

        for (msg in history.takeLast(10)) {
            if (msg.isError || msg.text.isBlank()) continue
            val msgRole = if (msg.sender == MessageSender.USER) "user" else "model"
            if (msgRole == expectingRole) {
                contents.add(content(msgRole) { text(msg.text) })
                expectingRole = if (expectingRole == "user") "model" else "user"
            }
        }

        // If the last message added was a user message, remove it so newPrompt takes its turn
        if (contents.isNotEmpty() && expectingRole == "model") {
            contents.removeAt(contents.size - 1)
        }

        // Add the new user prompt
        contents.add(content("user") { text(newPrompt) })

        return model.generateContentStream(contents)
            .mapNotNull { it.text }
            .filter { it.isNotEmpty() }
    }
}
