package com.courier.messenger.telegram

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.util.concurrent.ConcurrentHashMap

/**
 * Thin, opinionated bridge over the official TDLib (org.drinkless.tdlib).
 *
 * All Telegram traffic happens inside TDLib; this object only translates its
 * update stream into StateFlows that Compose can render, and forwards user
 * intents (phone number, code, password, send message) back into TDLib.
 */
object TdlibClient {

    private const val TAG = "Courier/TDLib"

    enum class Stage { Idle, Starting, WaitPhone, WaitCode, WaitPassword, Ready, Error }

    data class AuthUi(
        val stage: Stage = Stage.Idle,
        val error: String? = null,
        val codeLength: Int = 5,
        val passwordHint: String? = null,
        val busy: Boolean = false,
    )

    data class ChatUi(
        val id: Long,
        val title: String,
        val preview: String,
        val date: Int,
        val unread: Int,
        val pinned: Boolean,
        val isPrivate: Boolean,
        val lastOutgoing: Boolean,
        val muted: Boolean,
        val lastMessageId: Long,
    ) {
        val initials: String get() = com.courier.messenger.telegram.Format.initials(title)
    }

    data class MessageUi(
        val id: Long,
        val chatId: Long,
        val text: String,
        val date: Int,
        val outgoing: Boolean,
        val pending: Boolean,
        val sender: String?,
    )

    data class ProfileUi(
        val name: String,
        val phone: String,
        val initials: String,
        val userId: Long,
    )

    private var client: Client? = null
    private var configuredId: Int = 0
    private var configuredHash: String = ""
    private var databaseDir: String = ""

    private val chatStore = ConcurrentHashMap<Long, TdApi.Chat>()
    private val userStore = ConcurrentHashMap<Long, TdApi.User>()
    private val messageStore = ConcurrentHashMap<Long, MutableList<TdApi.Message>>()
    private var myUserId: Long = 0
    private var openChatId: Long = 0

    private val _auth = MutableStateFlow(AuthUi())
    val auth: StateFlow<AuthUi> = _auth.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatUi>>(emptyList())
    val chats: StateFlow<List<ChatUi>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<Map<Long, List<MessageUi>>>(emptyMap())
    val messages: StateFlow<Map<Long, List<MessageUi>>> = _messages.asStateFlow()

    private val _profile = MutableStateFlow<ProfileUi?>(null)
    val profile: StateFlow<ProfileUi?> = _profile.asStateFlow()

    private val _connection = MutableStateFlow("Connecting…")
    val connection: StateFlow<String> = _connection.asStateFlow()

    private val _activeChat = MutableStateFlow<ChatUi?>(null)
    val activeChat: StateFlow<ChatUi?> = _activeChat.asStateFlow()

    /** Starts TDLib once, with the developer credentials the user supplied. */
    @Synchronized
    fun initialize(context: Context, apiId: Int, apiHash: String) {
        if (client != null && apiId == configuredId && apiHash == configuredHash) return
        configuredId = apiId
        configuredHash = apiHash
        databaseDir = "${context.filesDir.absolutePath}/tdlib"

        if (client == null) {
            client = try {
                Client.create(
                    Client.ResultHandler { update -> handleUpdate(update) },
                    Client.ExceptionHandler { e -> Log.e(TAG, "update exception", e) },
                    Client.ExceptionHandler { e -> Log.e(TAG, "default exception", e) },
                )
            } catch (t: Throwable) {
                Log.e(TAG, "TDLib could not start", t)
                _auth.update {
                    it.copy(stage = Stage.Error, error = "TDLib could not start on this device: ${t.message}")
                }
                null
            }
            if (client == null && _auth.value.stage != Stage.Error) {
                _auth.update {
                    it.copy(stage = Stage.Error, error = "TDLib native library is unavailable for this ABI")
                }
            }
        } else {
            _auth.update { it.copy(stage = Stage.Starting) }
        }
    }

    fun shutdown() {
        send(TdApi.Close())
    }

    // ---------------------------------------------------------------- auth

    fun submitPhone(raw: String) {
        val phone = Format.normalizePhone(raw)
        if (phone.length < 5) {
            _auth.update { it.copy(error = "Enter a valid phone number with country code") }
            return
        }
        _auth.update { it.copy(busy = true, error = null) }
        client?.send(
            TdApi.SetAuthenticationPhoneNumber(
                phone,
                TdApi.PhoneNumberAuthenticationSettings(
                    false, false, false, false, false, null, emptyArray<String>(),
                ),
            ),
        ) { result ->
            if (result is TdApi.Error) _auth.update { it.copy(busy = false, error = result.message) }
        }
    }

    fun submitCode(code: String) {
        _auth.update { it.copy(busy = true, error = null) }
        client?.send(TdApi.CheckAuthenticationCode(code.trim())) { result ->
            if (result is TdApi.Error) _auth.update { it.copy(busy = false, error = result.message) }
        }
    }

    fun submitPassword(password: String) {
        _auth.update { it.copy(busy = true, error = null) }
        client?.send(TdApi.CheckAuthenticationPassword(password)) { result ->
            if (result is TdApi.Error) _auth.update { it.copy(busy = false, error = result.message) }
        }
    }

    fun resendCode() {
        _auth.update { it.copy(busy = true, error = null) }
        client?.send(TdApi.ResendAuthenticationCode()) { result ->
            if (result is TdApi.Error) _auth.update { it.copy(busy = false, error = result.message) }
        }
    }

    fun logout() {
        chatStore.clear()
        userStore.clear()
        messageStore.clear()
        _chats.value = emptyList()
        _messages.value = emptyMap()
        _profile.value = null
        _activeChat.value = null
        send(TdApi.LogOut())
    }

    // -------------------------------------------------------------- chats

    fun refreshChats() {
        send(TdApi.GetChats(TdApi.ChatListMain(), 100))
        send(TdApi.LoadChats(TdApi.ChatListMain(), 100))
    }

    fun openChat(chatId: Long) {
        if (openChatId != 0L && openChatId != chatId) send(TdApi.CloseChat(openChatId))
        openChatId = chatId
        _activeChat.value = chatStore[chatId]?.let { toChatUi(it) }
        send(TdApi.OpenChat(chatId))
        client?.send(TdApi.GetChatHistory(chatId, 0L, 0, 60, false)) { result ->
            if (result is TdApi.Messages) {
                val ordered = result.messages.sortedBy { it.id }
                messageStore[chatId] = ordered.toMutableList()
                publishMessages(chatId)
                if (ordered.isNotEmpty()) {
                    send(TdApi.ViewMessages(chatId, ordered.map { it.id }.toLongArray(), null, true))
                }
            }
        }
    }

    fun closeChat() {
        if (openChatId != 0L) send(TdApi.CloseChat(openChatId))
        openChatId = 0
        _activeChat.value = null
    }

    fun sendText(chatId: Long, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val content = TdApi.InputMessageText(TdApi.FormattedText(clean, emptyArray<TdApi.TextEntity>()), null, true)
        send(TdApi.SendMessage(chatId, null, null, null, null, content))
    }

    fun markRead(chatId: Long) {
        val list = messageStore[chatId] ?: return
        val ids = list.takeLast(20).map { it.id }.toLongArray()
        if (ids.isNotEmpty()) send(TdApi.ViewMessages(chatId, ids, null, true))
    }

    // ----------------------------------------------------------- internals

    private fun send(function: TdApi.Function<*>) {
        client?.send(function) { result ->
            if (result is TdApi.Error) Log.w(TAG, "TDLib error: ${result.message}")
        }
    }

    private fun handleUpdate(obj: TdApi.Object?) {
        when (val update = obj) {
            is TdApi.UpdateAuthorizationState -> onAuthorizationState(update.authorizationState)

            is TdApi.UpdateNewChat -> {
                chatStore[update.chat.id] = update.chat
                publishChats()
            }

            is TdApi.UpdateChatLastMessage -> {
                chatStore[update.chatId]?.let { chat ->
                    chat.lastMessage = update.lastMessage
                    chat.positions = update.positions
                    publishChats()
                    if (update.chatId == openChatId) publishMessages(update.chatId)
                }
            }

            is TdApi.UpdateChatTitle -> {
                chatStore[update.chatId]?.let { chat ->
                    chat.title = update.title
                    publishChats()
                    if (update.chatId == _activeChat.value?.id) {
                        _activeChat.value = toChatUi(chat)
                    }
                }
            }

            is TdApi.UpdateChatReadInbox -> {
                chatStore[update.chatId]?.let { chat ->
                    chat.unreadCount = update.unreadCount
                    publishChats()
                }
            }

            is TdApi.UpdateUser -> {
                userStore[update.user.id] = update.user
                if (update.user.id == myUserId) publishProfile(update.user)
                publishChats()
            }

            is TdApi.UpdateNewMessage -> {
                val message = update.message
                val list = messageStore[message.chatId]
                if (list != null) {
                    if (list.none { it.id == message.id }) {
                        list.add(message)
                        list.sortBy { it.id }
                    }
                    publishMessages(message.chatId)
                    if (message.chatId == openChatId && !message.isOutgoing) {
                        send(TdApi.ViewMessages(message.chatId, longArrayOf(message.id), null, true))
                    }
                }
            }

            is TdApi.UpdateMessageSendSucceeded -> {
                val oldId = update.oldMessageId
                val message = update.message
                messageStore[message.chatId]?.let { list ->
                    val index = list.indexOfFirst { it.id == oldId }
                    if (index >= 0) list[index] = message else if (list.none { it.id == message.id }) list.add(message)
                    list.sortBy { it.id }
                    publishMessages(message.chatId)
                }
            }

            is TdApi.UpdateMessageSendFailed -> {
                val failed = update.message
                messageStore[failed.chatId]?.let { list ->
                    val index = list.indexOfFirst { it.id == update.oldMessageId }
                    if (index >= 0) list.removeAt(index)
                    publishMessages(failed.chatId)
                }
                Log.w(TAG, "send failed: ${update.error.message}")
            }

            is TdApi.UpdateMessageContent -> {
                messageStore[update.chatId]?.let { list ->
                    val index = list.indexOfFirst { it.id == update.messageId }
                    if (index >= 0) {
                        val original = list[index]
                        original.content = update.newContent
                        publishMessages(update.chatId)
                    }
                }
            }

            is TdApi.UpdateDeleteMessages -> {
                messageStore[update.chatId]?.let { list ->
                    list.removeAll { msg -> update.messageIds.contains(msg.id) }
                    publishMessages(update.chatId)
                }
            }

            is TdApi.UpdateConnectionState -> {
                _connection.value = when (update.state) {
                    is TdApi.ConnectionStateReady -> "Connected"
                    is TdApi.ConnectionStateConnecting -> "Connecting…"
                    is TdApi.ConnectionStateConnectingToProxy -> "Connecting to proxy…"
                    is TdApi.ConnectionStateUpdating -> "Updating…"
                    is TdApi.ConnectionStateWaitingForNetwork -> "Waiting for network…"
                    else -> "Offline"
                }
            }

            is TdApi.UpdateOption -> Unit

            else -> Unit
        }
    }

    private fun onAuthorizationState(state: TdApi.AuthorizationState?) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                _auth.update { it.copy(stage = Stage.Starting) }
                send(
                    TdApi.SetTdlibParameters(
                        false,
                        "$databaseDir/database",
                        "$databaseDir/files",
                        ByteArray(0),
                        true,
                        true,
                        true,
                        false,
                        configuredId,
                        configuredHash,
                        "en",
                        "Courier for Android",
                        "Android ${Build.VERSION.RELEASE}",
                        "1.0",
                    ),
                )
            }

            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                _auth.update { it.copy(stage = Stage.WaitPhone, busy = false, error = null) }
            }

            is TdApi.AuthorizationStateWaitCode -> {
                _auth.update {
                    it.copy(
                        stage = Stage.WaitCode,
                        busy = false,
                        error = null,
                        codeLength = 5,
                    )
                }
            }

            is TdApi.AuthorizationStateWaitPassword -> {
                _auth.update {
                    it.copy(
                        stage = Stage.WaitPassword,
                        busy = false,
                        error = null,
                        passwordHint = state.passwordHint,
                    )
                }
            }

            is TdApi.AuthorizationStateReady -> {
                _auth.update { it.copy(stage = Stage.Ready, busy = false, error = null) }
                send(TdApi.GetMe())
                refreshChats()
            }

            is TdApi.AuthorizationStateLoggingOut -> {
                _auth.update { it.copy(stage = Stage.Starting, busy = true) }
            }

            is TdApi.AuthorizationStateClosed -> {
                _auth.update { it.copy(stage = Stage.Idle) }
                client = null
            }

            is TdApi.AuthorizationStateWaitOtherDeviceConfirmation -> {
                _auth.update { it.copy(stage = Stage.WaitPhone) }
            }

            else -> Unit
        }
    }

    private fun publishChats() {
        val list = chatStore.values
            .mapNotNull { chat ->
                val main = chat.positions.firstOrNull { it.list is TdApi.ChatListMain } ?: return@mapNotNull null
                toChatUi(chat) to main
            }
            .sortedWith(compareByDescending<Pair<ChatUi, TdApi.ChatPosition>> { it.second.isPinned }
                .thenByDescending { it.second.order })
            .map { it.first }
        _chats.value = list
        _activeChat.value?.let { active -> chatStore[active.id]?.let { _activeChat.value = toChatUi(it) } }
    }

    private fun toChatUi(chat: TdApi.Chat): ChatUi {
        val last = chat.lastMessage
        val position = chat.positions.firstOrNull { it.list is TdApi.ChatListMain }
        return ChatUi(
            id = chat.id,
            title = chat.title.ifBlank { "Chat" },
            preview = Format.preview(last),
            date = last?.date ?: 0,
            unread = chat.unreadCount,
            pinned = position?.isPinned == true,
            isPrivate = chat.type is TdApi.ChatTypePrivate,
            lastOutgoing = last?.isOutgoing == true,
            muted = false,
            lastMessageId = last?.id ?: 0L,
        )
    }

    private fun publishMessages(chatId: Long) {
        val list = messageStore[chatId] ?: return
        val mapped = list.map { message ->
            val content = message.content
            MessageUi(
                id = message.id,
                chatId = message.chatId,
                text = Format.contentText(content),
                date = message.date,
                outgoing = message.isOutgoing,
                pending = message.sendingState is TdApi.MessageSendingStatePending,
                sender = if (!message.isOutgoing) Format.senderName(message, userStore) else null,
            )
        }
        _messages.update { it + (chatId to mapped) }
    }

    private fun publishProfile(user: TdApi.User) {
        myUserId = user.id
        val name = listOf(user.firstName, user.lastName).filter { it.isNotBlank() }.joinToString(" ")
        _profile.value = ProfileUi(
            name = name.ifBlank { "Courier user" },
            phone = user.phoneNumber,
            initials = Format.initials(name),
            userId = user.id,
        )
    }
}
