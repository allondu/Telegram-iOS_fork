package com.courier.messenger.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.courier.messenger.telegram.Format
import com.courier.messenger.telegram.TdlibClient
import com.courier.messenger.ui.components.Avatar
import com.courier.messenger.ui.components.GlassInput
import com.courier.messenger.ui.components.GlassPanel
import com.courier.messenger.ui.components.Hairline
import com.courier.messenger.ui.components.Ticks
import com.courier.messenger.ui.components.UnreadBadge
import com.courier.messenger.ui.theme.IosType
import com.courier.messenger.ui.theme.LocalIos

@Composable
fun ChatListScreen(
    chats: List<TdlibClient.ChatUi>,
    connection: String,
    onOpenChat: (Long) -> Unit,
) {
    val ios = LocalIos.current
    var query by remember { mutableStateOf("") }

    val filtered = remember(chats, query) {
        if (query.isBlank()) chats
        else chats.filter {
            it.title.contains(query, ignoreCase = true) || it.preview.contains(query, ignoreCase = true)
        }
    }
    val pinned = filtered.filter { it.pinned }
    val rest = filtered.filter { !it.pinned }

    Column(Modifier.fillMaxSize().background(ios.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Chats",
                style = IosType.largeTitle,
                color = ios.textPrimary,
            )
            Spacer(Modifier.weight(1f))
            GlassPanel(shape = RoundedCornerShape(20.dp), strength = 0.20f) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Create, contentDescription = "New message", tint = ios.accent, modifier = Modifier.size(21.dp))
                }
            }
        }

        Box(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
            GlassInput(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search for messages or users",
                modifier = Modifier.fillMaxWidth(),
                textStyle = IosType.subhead,
            )
        }

        if (connection != "Connected") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .background(if (connection.contains("network", true)) ios.danger else ios.badgeMuted, RoundedCornerShape(4.dp)),
                )
                Spacer(Modifier.width(6.dp))
                Text(connection, style = IosType.footnote, color = ios.textSecondary)
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (query.isBlank()) "No chats yet" else "Nothing found",
                        style = IosType.headline,
                        color = ios.textPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (query.isBlank()) "Start a conversation from Contacts" else "Try a different search",
                        style = IosType.subhead,
                        color = ios.textSecondary,
                    )
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
                if (pinned.isNotEmpty()) {
                    item { SectionLabel("Pinned") }
                    items(pinned, key = { it.id }) { chat -> ChatRow(chat, onOpenChat) }
                }
                if (rest.isNotEmpty()) {
                    item { SectionLabel(if (query.isBlank()) "All Chats" else "Results") }
                    items(rest, key = { it.id }) { chat -> ChatRow(chat, onOpenChat) }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val ios = LocalIos.current
    Text(
        text = text,
        style = IosType.sectionHeader,
        color = ios.textSecondary,
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
    )
}

@Composable
private fun ChatRow(chat: TdlibClient.ChatUi, onOpenChat: (Long) -> Unit) {
    val ios = LocalIos.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 900f),
        label = "rowScale",
    )
    val rowBackground by animateColorAsState(
        targetValue = if (pressed) ios.accent.copy(alpha = 0.06f) else ios.background,
        label = "rowBg",
    )

    Column(Modifier.background(rowBackground)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(interactionSource = interaction, indication = null) { onOpenChat(chat.id) }
                .padding(start = 14.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.padding(vertical = 9.dp)) {
                Avatar(
                    initials = chat.initials,
                    gradient = Format.gradientFor(chat.id),
                    size = 54.dp,
                )
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chat.title,
                        style = IosType.title.copy(color = ios.textPrimary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (chat.pinned) {
                        Spacer(Modifier.width(5.dp))
                        Text("📌", style = IosType.caption)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.lastOutgoing && chat.lastMessageId != 0L) {
                        Ticks(read = true, color = ios.accent)
                        Spacer(Modifier.width(3.dp))
                    }
                    Text(
                        text = chat.preview.ifBlank { "No messages yet" },
                        style = IosType.subhead.copy(color = ios.textSecondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Format.chatTime(chat.date),
                    style = IosType.caption.copy(fontSize = 12.5.sp),
                    color = if (chat.unread > 0) ios.accent else ios.textSecondary,
                    maxLines = 1,
                )
                Spacer(Modifier.height(5.dp))
                if (chat.unread > 0) {
                    UnreadBadge(chat.unread, muted = false)
                } else {
                    Spacer(Modifier.height(22.dp))
                }
            }
        }
        Hairline(Modifier.padding(start = 79.dp))
    }
}
