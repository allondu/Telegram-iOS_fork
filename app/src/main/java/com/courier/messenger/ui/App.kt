package com.courier.messenger.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.courier.messenger.telegram.AppPrefs
import com.courier.messenger.telegram.TdlibClient
import com.courier.messenger.ui.components.BubbleGlyph
import com.courier.messenger.ui.components.GlassPanel
import com.courier.messenger.ui.components.LiquidBackdrop
import com.courier.messenger.ui.theme.IosType
import com.courier.messenger.ui.theme.LocalIos

@Composable
fun App(modifier: Modifier = Modifier) {
    val ios = LocalIos.current
    val context = LocalContext.current
    var credentials by remember { mutableStateOf(AppPrefs.load(context)) }
    val auth by TdlibClient.auth.collectAsState()
    val chats by TdlibClient.chats.collectAsState()
    val profile by TdlibClient.profile.collectAsState()
    val connection by TdlibClient.connection.collectAsState()

    var tab by remember { mutableStateOf(1) }
    var openChatId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(credentials) {
        credentials?.let { safe ->
            TdlibClient.initialize(context.applicationContext, safe.apiId, safe.apiHash)
        }
    }

    val stage = when {
        credentials == null -> "setup"
        auth.stage == TdlibClient.Stage.WaitCode -> "code"
        auth.stage == TdlibClient.Stage.WaitPassword -> "password"
        auth.stage == TdlibClient.Stage.Ready -> "app"
        else -> "phone"
    }

    AnimatedContent(
        targetState = stage,
        modifier = modifier.fillMaxSize().background(ios.background),
        transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(180)) },
        label = "authStage",
    ) { current ->
        when (current) {
            "setup" -> SetupScreen(
                initialId = "",
                initialHash = "",
                onSave = { id, hash ->
                    val parsed = id.toIntOrNull()
                    if (parsed != null) {
                        AppPrefs.save(context, parsed, hash)
                        credentials = com.courier.messenger.telegram.ApiCredentials(parsed, hash)
                    }
                },
            )

            "phone" -> PhoneScreen(auth = auth, onContinue = { TdlibClient.submitPhone(it) })

            "code" -> CodeScreen(
                auth = auth,
                onContinue = { TdlibClient.submitCode(it) },
                onResend = { TdlibClient.resendCode() },
            )

            "password" -> PasswordScreen(auth = auth, onContinue = { TdlibClient.submitPassword(it) })

            else -> MainShell(
                chats = chats,
                profile = profile,
                connection = connection,
                tab = tab,
                onTab = { tab = it },
                openChatId = openChatId,
                onOpenChat = { openChatId = it },
                onCloseChat = { openChatId = null },
                onLogout = { TdlibClient.logout() },
            )
        }
    }
}

@Composable
private fun MainShell(
    chats: List<TdlibClient.ChatUi>,
    profile: TdlibClient.ProfileUi?,
    connection: String,
    tab: Int,
    onTab: (Int) -> Unit,
    openChatId: Long?,
    onOpenChat: (Long) -> Unit,
    onCloseChat: () -> Unit,
    onLogout: () -> Unit,
) {
    val ios = LocalIos.current
    LiquidBackdrop(Modifier.fillMaxSize().background(ios.background)) {
        AnimatedContent(
            targetState = openChatId,
            transitionSpec = {
                (slideInHorizontally(tween(280)) { it } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(tween(240)) { -it / 5 } + fadeOut(tween(160)))
            },
            label = "chatNavigation",
        ) { chatId ->
            if (chatId == null) {
                Column(Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = tab,
                        modifier = Modifier.weight(1f),
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(140)) },
                        label = "tabs",
                    ) { currentTab ->
                        when (currentTab) {
                            0 -> ContactsScreen(chats = chats, onOpenChat = onOpenChat)
                            1 -> ChatListScreen(chats = chats, connection = connection, onOpenChat = onOpenChat)
                            else -> SettingsScreen(
                                profile = profile,
                                connection = connection,
                                chatCount = chats.size,
                                onLogout = onLogout,
                            )
                        }
                    }
                    GlassTabBar(
                        selected = tab,
                        unread = chats.filter { it.unread > 0 }.sumOf { it.unread },
                        onSelect = onTab,
                    )
                }
            } else {
                ChatScreen(chatId = chatId, onBack = onCloseChat)
            }
        }
    }
}

@Composable
private fun GlassTabBar(selected: Int, unread: Int, onSelect: (Int) -> Unit) {
    val ios = LocalIos.current
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            strength = 0.30f,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabItem("Contacts", 0, selected, onSelect, Modifier.weight(1f)) { color ->
                    Icon(Icons.Filled.Person, contentDescription = null, tint = color, modifier = Modifier.size(25.dp))
                }
                TabItem("Chats", 1, selected, onSelect, Modifier.weight(1f), badge = unread) { color ->
                    BubbleGlyph(color, filled = selected == 1, size = 25.dp)
                }
                TabItem("Settings", 2, selected, onSelect, Modifier.weight(1f)) { color ->
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = color, modifier = Modifier.size(25.dp))
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    index: Int,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badge: Int = 0,
    icon: @Composable (Color) -> Unit,
) {
    val ios = LocalIos.current
    val isSelected = selected == index
    val color = if (isSelected) ios.accent else ios.textSecondary
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 520f),
        label = "tabScale",
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable { onSelect(index) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.scale(scale)) {
            icon(color)
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(start = 12.dp)
                        .size(15.dp)
                        .background(ios.danger, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (badge > 99) "99" else badge.toString(),
                        style = IosType.caption2.copy(fontSize = 9.sp),
                        color = Color.White,
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(text = label, style = IosType.tabLabel, color = color)
    }
}
