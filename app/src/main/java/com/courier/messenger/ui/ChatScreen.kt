package com.courier.messenger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.courier.messenger.telegram.Format
import com.courier.messenger.telegram.TdlibClient
import com.courier.messenger.ui.components.Avatar
import com.courier.messenger.ui.components.ChevronLeft
import com.courier.messenger.ui.components.GlassPanel
import com.courier.messenger.ui.components.Hairline
import com.courier.messenger.ui.components.MicGlyph
import com.courier.messenger.ui.components.SendGlyph
import com.courier.messenger.ui.components.Ticks
import com.courier.messenger.ui.theme.IosType
import com.courier.messenger.ui.theme.LocalIos

@Composable
fun ChatScreen(chatId: Long, onBack: () -> Unit) {
    val ios = LocalIos.current
    val chatFlow = TdlibClient.activeChat.collectAsState()
    val messagesFlow = TdlibClient.messages.collectAsState()
    val connection = TdlibClient.connection.collectAsState()

    val chat = chatFlow.value
    val messages = messagesFlow.value[chatId].orEmpty()
    val listState = rememberLazyListState()
    var draft by remember { mutableStateOf("") }

    LaunchedEffect(chatId) { TdlibClient.openChat(chatId) }
    DisposableEffect(chatId) {
        onDispose { TdlibClient.closeChat() }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
            TdlibClient.markRead(chatId)
        }
    }

    Column(Modifier.fillMaxSize().background(ios.chatBackground).imePadding()) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            strength = 0.30f,
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onBack() }
                            .padding(horizontal = 2.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChevronLeft(ios.accent)
                        Spacer(Modifier.width(2.dp))
                        Text("Chats", style = IosType.body, color = ios.accent)
                    }
                    Spacer(Modifier.width(8.dp))
                    Avatar(
                        initials = chat?.initials ?: "#",
                        gradient = Format.gradientFor(chatId),
                        size = 32.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            chat?.title ?: "Chat",
                            style = IosType.title.copy(fontSize = 16.sp),
                            color = ios.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            connection.value,
                            style = IosType.caption.copy(fontSize = 12.sp),
                            color = if (connection.value == "Connected") ios.accent else ios.textSecondary,
                            maxLines = 1,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Hairline(color = ios.navHairline)
            }
        }

        if (messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "No messages yet",
                    style = IosType.subhead,
                    color = ios.textSecondary,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 12.dp),
            ) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Telegram",
                            style = IosType.caption.copy(fontSize = 12.sp),
                            color = Color.White,
                            modifier = Modifier
                                .background(ios.badgeMuted.copy(alpha = 0.6f), RoundedCornerShape(11.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                }
                items(messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visibleState = remember { androidx.compose.animation.core.MutableTransitionState(false) }
                            .apply { targetState = true },
                        enter = fadeIn() + scaleIn(initialScale = 0.94f),
                        exit = fadeOut(),
                    ) {
                        MessageRow(message)
                    }
                }
                item { Spacer(Modifier.height(4.dp)) }
            }
        }

        Composer(
            draft = draft,
            onDraftChange = { draft = it },
            onSend = {
                if (draft.isNotBlank()) {
                    TdlibClient.sendText(chatId, draft)
                    draft = ""
                }
            },
        )
    }
}

@Composable
private fun MessageRow(message: TdlibClient.MessageUi) {
    val ios = LocalIos.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (message.outgoing) Arrangement.End else Arrangement.Start,
    ) {
        Column(horizontalAlignment = if (message.outgoing) Alignment.End else Alignment.Start) {
            if (!message.outgoing && message.sender != null) {
                Text(
                    text = message.sender,
                    style = IosType.caption.copy(fontSize = 12.sp),
                    color = ios.accent,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp),
                )
            }
            val bubbleShape = if (message.outgoing) {
                RoundedCornerShape(18.dp, 18.dp, 5.dp, 18.dp)
            } else {
                RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp)
            }
            if (message.outgoing) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 306.dp)
                        .clip(bubbleShape)
                        .background(Brush.verticalGradient(listOf(ios.outgoingTop, ios.outgoingBottom)))
                        .padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 6.dp),
                ) {
                    MessageBody(message, outgoing = true)
                }
            } else {
                GlassPanel(
                    modifier = Modifier.widthIn(max = 306.dp),
                    shape = bubbleShape,
                    tint = ios.incomingBubble,
                    strength = if (ios.isDark) 0.22f else 0.55f,
                ) {
                    Box(Modifier.padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 6.dp)) {
                        MessageBody(message, outgoing = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBody(message: TdlibClient.MessageUi, outgoing: Boolean) {
    val ios = LocalIos.current
    val textColor = if (outgoing) ios.outgoingText else ios.textPrimary
    Column {
        Text(
            text = message.text.ifBlank { "Message" },
            style = IosType.body.copy(fontSize = 16.sp),
            color = textColor,
        )
        Row(
            modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = Format.messageTime(message.date),
                style = IosType.caption2,
                color = if (outgoing) Color.White.copy(alpha = 0.85f) else ios.textSecondary,
            )
            if (outgoing) {
                Spacer(Modifier.width(3.dp))
                if (message.pending) {
                    Text("·", color = Color.White.copy(alpha = 0.9f), style = IosType.caption2)
                } else {
                    Ticks(read = true, color = Color.White.copy(alpha = 0.95f))
                }
            }
        }
    }
}

@Composable
private fun Composer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val ios = LocalIos.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 800f),
        label = "sendScale",
    )

    Column {
        Hairline(color = ios.navHairline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ios.navBar.copy(alpha = 0.92f))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassPanel(shape = RoundedCornerShape(18.dp), strength = 0.30f) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Text("+", style = IosType.title.copy(fontSize = 22.sp), color = ios.accent)
                }
            }
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ios.inputField)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (draft.isEmpty()) {
                        Text("Message", style = IosType.body, color = ios.textSecondary)
                    }
                    BasicTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        textStyle = IosType.body.copy(color = ios.textPrimary),
                        cursorBrush = SolidColor(ios.accent),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSend() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            if (draft.isBlank()) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    MicGlyph(ios.accent)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(19.dp))
                        .background(Brush.verticalGradient(listOf(ios.outgoingTop, ios.outgoingBottom)))
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = onSend,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    SendGlyph(Color.White, size = 22.dp)
                }
            }
        }
    }
}
