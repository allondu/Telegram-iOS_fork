package com.courier.messenger.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.courier.messenger.ui.components.Hairline
import com.courier.messenger.ui.theme.IosType
import com.courier.messenger.ui.theme.LocalIos

@Composable
fun ContactsScreen(
    chats: List<TdlibClient.ChatUi>,
    onOpenChat: (Long) -> Unit,
) {
    val ios = LocalIos.current
    var query by remember { mutableStateOf("") }

    val contacts = remember(chats, query) {
        val private = chats.filter { it.isPrivate }
        if (query.isBlank()) private else private.filter { it.title.contains(query, ignoreCase = true) }
    }
    val grouped = remember(contacts) {
        contacts.groupBy { it.title.firstOrNull()?.uppercaseChar() ?: '#' }.toSortedMap()
    }

    Column(Modifier.fillMaxSize().background(ios.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Contacts", style = IosType.largeTitle, color = ios.textPrimary)
        }
        Box(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
            GlassInput(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search",
                modifier = Modifier.fillMaxWidth(),
                textStyle = IosType.subhead,
            )
        }
        if (contacts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No private chats yet",
                    style = IosType.subhead,
                    color = ios.textSecondary,
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
                grouped.forEach { (letter, people) ->
                    item(key = "header-$letter") {
                        Text(
                            text = letter.toString(),
                            style = IosType.sectionHeader,
                            color = ios.textSecondary,
                            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 12.dp, bottom = 4.dp),
                        )
                    }
                    items(people, key = { it.id }) { contact ->
                        ContactRow(contact, onOpenChat)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: TdlibClient.ChatUi, onOpenChat: (Long) -> Unit) {
    val ios = LocalIos.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 900f),
        label = "contactScale",
    )
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(interactionSource = interaction, indication = null) { onOpenChat(contact.id) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                initials = contact.initials,
                gradient = Format.gradientFor(contact.id),
                size = 44.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    contact.title,
                    style = IosType.body.copy(fontSize = 16.sp),
                    color = ios.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    "Telegram chat",
                    style = IosType.caption.copy(fontSize = 13.sp),
                    color = ios.textSecondary,
                    maxLines = 1,
                )
            }
        }
        Hairline(Modifier.padding(start = 72.dp))
    }
}
