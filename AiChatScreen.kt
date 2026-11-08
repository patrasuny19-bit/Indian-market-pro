package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ChatMessage
import com.example.data.ai.GeminiModelOption
import com.example.data.ai.MessageSender
import com.example.ui.ai.AiChatUiState
import com.example.ui.ai.AiChatViewModel
import com.example.ui.theme.MarketGold
import com.example.ui.theme.MarketGreen
import com.example.ui.theme.MarketRed
import kotlinx.coroutines.launch

@Composable
fun AiChatScreen(
    aiViewModel: AiChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by aiViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Auto-scroll to bottom when messages update
    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.text?.length) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AI Model Selection & Status Header
        AiHeaderBar(
            selectedModel = uiState.selectedModel,
            isGenerating = uiState.isGenerating,
            onSelectModel = { aiViewModel.selectModel(it) },
            onClearChat = { aiViewModel.clearChat() }
        )

        // Quick Stock Prompts Carousel
        QuickPromptCarousel(
            prompts = uiState.quickSuggestions,
            isGenerating = uiState.isGenerating,
            onPromptClick = { prompt ->
                aiViewModel.sendMessage(prompt)
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    selectedModel = uiState.selectedModel,
                    onFollowUpClick = { followUp ->
                        aiViewModel.sendMessage(followUp)
                    },
                    onCopyText = { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Market AI Advice", text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied analysis to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Active Error Banner
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Surface(
                color = MarketRed.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MarketRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MarketRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MarketRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom Input Field
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Ask Gemini (e.g. Analyze RELIANCE, Nifty levels)...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MarketGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        trailingIcon = {
                            if (inputText.isNotEmpty()) {
                                IconButton(onClick = { inputText = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear input",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Send Button
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !uiState.isGenerating) {
                                val textToSend = inputText
                                inputText = ""
                                aiViewModel.sendMessage(textToSend)
                            }
                        },
                        enabled = inputText.isNotBlank() && !uiState.isGenerating,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ai_send_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MarketGreen,
                            contentColor = Color.Black,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        if (uiState.isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send to Gemini",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiHeaderBar(
    selectedModel: GeminiModelOption,
    isGenerating: Boolean,
    onSelectModel: (GeminiModelOption) -> Unit,
    onClearChat: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MarketGold, MarketGreen)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "GEMINI STOCK COPILOT",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MarketGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI LOGIC",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MarketGreen
                                )
                            }
                        }
                        Text(
                            text = if (isGenerating) "Analyzing real-time market data..." else "Online • Multi-Turn Indian Market Advisor",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isGenerating) MarketGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.testTag("ai_clear_chat_button")
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Clear Conversation",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Model Switcher Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GeminiModelOption.entries.forEach { modelOption ->
                    val isSelected = modelOption == selectedModel
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) MarketGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .clickable { onSelectModel(modelOption) }
                            .testTag("model_chip_${modelOption.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MarketGold)
                                )
                            }
                            Text(
                                text = modelOption.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPromptCarousel(
    prompts: List<String>,
    isGenerating: Boolean,
    onPromptClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(prompts) { prompt ->
            SuggestionChip(
                onClick = { if (!isGenerating) onPromptClick(prompt) },
                label = {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                },
                icon = {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MarketGold,
                        modifier = Modifier.size(14.dp)
                    )
                },
                enabled = !isGenerating,
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    selectedModel: GeminiModelOption,
    onFollowUpClick: (String) -> Unit,
    onCopyText: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Label / Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MarketGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = "India Market Pro AI • ${selectedModel.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MarketGreen
                )
            } else {
                Text(
                    text = "You",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bubble Content Card
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            },
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isUser) {
                    MarketGold.copy(alpha = 0.3f)
                } else if (message.isError) {
                    MarketRed.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }
            ),
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(vertical = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.isStreaming && message.text.isEmpty()) {
                    // Pulsing Loading State
                    PulsingLoadingDots()
                } else {
                    SelectionContainer {
                        FormattedMarkdownText(
                            text = message.text,
                            isStreaming = message.isStreaming
                        )
                    }
                }

                // Copy Action & Timestamp for AI responses
                if (!isUser && !message.isStreaming && message.text.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onCopyText(message.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.ContentCopy,
                                contentDescription = "Copy text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Suggested Follow-up chips
        if (!isUser && !message.isStreaming && message.suggestedFollowUps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.suggestedFollowUps.take(2).forEach { followUp ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MarketGold.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onFollowUpClick(followUp) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MarketGold,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = followUp,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormattedMarkdownText(
    text: String,
    isStreaming: Boolean
) {
    // Parse simplified markdown (Bold with **text**, Bullet points with -, headers with ##)
    val annotatedString = remember(text, isStreaming) {
        buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("###") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MarketGold)) {
                            append(trimmed.removePrefix("###").trim())
                        }
                    }
                    trimmed.startsWith("##") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Black, fontSize = 15.sp, color = MarketGreen)) {
                            append(trimmed.removePrefix("##").trim())
                        }
                    }
                    trimmed.startsWith("#") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Black, fontSize = 16.sp, color = MarketGold)) {
                            append(trimmed.removePrefix("#").trim())
                        }
                    }
                    trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                        withStyle(SpanStyle(color = MarketGreen, fontWeight = FontWeight.Bold)) {
                            append(" • ")
                        }
                        appendFormattedInline(trimmed.substring(2))
                    }
                    else -> {
                        appendFormattedInline(line)
                    }
                }
                if (index < lines.size - 1) {
                    append("\n")
                }
            }

            if (isStreaming) {
                withStyle(SpanStyle(color = MarketGold, fontWeight = FontWeight.Black)) {
                    append(" ▊")
                }
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 20.sp,
            letterSpacing = 0.2.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendFormattedInline(text: String) {
    val boldPattern = Regex("\\*\\*(.*?)\\*\\*")
    var lastIndex = 0

    boldPattern.findAll(text).forEach { matchResult ->
        val range = matchResult.range
        if (range.first > lastIndex) {
            append(text.substring(lastIndex, range.first))
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(matchResult.groupValues[1])
        }
        lastIndex = range.last + 1
    }

    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}

@Composable
private fun PulsingLoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dots_alpha"
    )

    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MarketGold.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MarketGreen.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MarketGold.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Gemini is thinking...",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
