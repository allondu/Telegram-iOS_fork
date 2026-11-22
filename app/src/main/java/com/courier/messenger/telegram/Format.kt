package com.courier.messenger.telegram

import androidx.compose.ui.graphics.Color
import org.drinkless.tdlib.TdApi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object Format {

    /** 8 iOS-like avatar gradients, picked deterministically from the chat id. */
    private val gradients: List<List<Color>> = listOf(
        listOf(Color(0xFF5EB5F7), Color(0xFF3B8DE0)),
        listOf(Color(0xFF7ED97E), Color(0xFF3CB371)),
        listOf(Color(0xFFF7B26A), Color(0xFFE8894A)),
        listOf(Color(0xFFC48AF0), Color(0xFF8E5BD6)),
        listOf(Color(0xFFF58FA6), Color(0xFFD9587A)),
        listOf(Color(0xFF63D3CE), Color(0xFF2FA8A3)),
        listOf(Color(0xFFB8BCC4), Color(0xFF8E939B)),
        listOf(Color(0xFF9AB8F5), Color(0xFF5C7FD6)),
    )

    fun gradientFor(id: Long): List<Color> {
        val index = (abs(id) % gradients.size).toInt()
        return gradients[index]
    }

    fun initials(title: String): String {
        val words = title.trim().split(" ").filter { it.isNotBlank() }
        if (words.isEmpty()) return "#"
        if (words.size == 1) return words[0].take(2).uppercase()
        return (words[0].take(1) + words[1].take(1)).uppercase()
    }

    fun normalizePhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return if (digits.isEmpty()) "" else "+$digits"
    }

    fun chatTime(epochSeconds: Int): String {
        if (epochSeconds <= 0) return ""
        val then = Calendar.getInstance().apply { timeInMillis = epochSeconds * 1000L }
        val now = Calendar.getInstance()
        val sameDay = then.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            then.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
        if (sameDay) return SimpleDateFormat("HH:mm", Locale.getDefault()).format(then.time)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = then.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            then.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
        if (isYesterday) return "Yesterday"

        val daysAgo = (now.timeInMillis - then.timeInMillis) / 86_400_000L
        return if (daysAgo in 0..6) SimpleDateFormat("EEE", Locale.getDefault()).format(then.time)
        else SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(then.time)
    }

    fun messageTime(epochSeconds: Int): String =
        if (epochSeconds <= 0) "" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochSeconds * 1000L))

    fun senderName(message: TdApi.Message, users: Map<Long, TdApi.User>): String? {
        val sender = message.senderId
        if (sender is TdApi.MessageSenderUser) {
            val user = users[sender.userId] ?: return null
            val name = listOf(user.firstName, user.lastName).filter { it.isNotBlank() }.joinToString(" ")
            return name.ifBlank { null }
        }
        return null
    }

    fun preview(message: TdApi.Message?): String {
        if (message == null) return ""
        val body = contentText(message.content)
        return if (message.isOutgoing) "You: $body" else body
    }

    fun contentText(content: TdApi.MessageContent?): String = when (content) {
        null -> ""
        is TdApi.MessageText -> content.text.text
        is TdApi.MessagePhoto -> content.caption.text.ifBlank { "Photo" }
        is TdApi.MessageVideo -> content.caption.text.ifBlank { "Video" }
        is TdApi.MessageAnimation -> content.caption.text.ifBlank { "GIF" }
        is TdApi.MessageDocument -> content.caption.text.ifBlank { "Document" }
        is TdApi.MessageAudio -> content.caption.text.ifBlank { "Audio" }
        is TdApi.MessageVoiceNote -> content.caption.text.ifBlank { "Voice message" }
        is TdApi.MessageVideoNote -> "Video message"
        is TdApi.MessageSticker -> content.sticker.emoji.ifBlank { "Sticker" }
        is TdApi.MessageLocation -> "Location"
        is TdApi.MessageContact -> "Contact"
        is TdApi.MessagePoll -> content.poll.question.text.ifBlank { "Poll" }
        is TdApi.MessageDice -> content.emoji
        is TdApi.MessageCall -> "Call"
        is TdApi.MessageBasicGroupChatCreate -> "Group created"
        is TdApi.MessageChatAddMembers -> "Members added"
        is TdApi.MessageChatChangeTitle -> "Group renamed"
        is TdApi.MessageChatChangePhoto -> "Photo changed"
        is TdApi.MessageChatDeletePhoto -> "Photo removed"
        is TdApi.MessageChatJoinByLink -> "Joined by link"
        is TdApi.MessageChatJoinByRequest -> "Joined by request"
        is TdApi.MessagePinMessage -> "Pinned message"
        else -> "Message"
    }
}
