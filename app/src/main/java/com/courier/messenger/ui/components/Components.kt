package com.courier.messenger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.courier.messenger.ui.theme.IosType
import com.courier.messenger.ui.theme.LocalIos

/** 0.5dp table separator, the way iOS draws its hairlines. */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = LocalIos.current.separator) {
    Box(modifier.fillMaxWidth().height(0.5.dp).background(color))
}

@Composable
fun Avatar(
    initials: String,
    gradient: List<Color>,
    size: Dp = 54.dp,
    online: Boolean = false,
) {
    val ios = LocalIos.current
    Box(Modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center,
        ) {
            if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    color = Color.White,
                    style = TextStyle(
                        fontSize = (size.value * 0.36f).sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                )
            }
        }
        if (online) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(15.dp)
                    .background(ios.background, CircleShape)
                    .padding(2.5.dp)
                    .background(ios.online, CircleShape),
            )
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val ios = LocalIos.current
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ios.searchField)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = ios.textSecondary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = IosType.subhead,
                    color = ios.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = IosType.subhead.copy(color = ios.textPrimary),
                cursorBrush = SolidColor(ios.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun UnreadBadge(count: Int, muted: Boolean) {
    val ios = LocalIos.current
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
            .background(if (muted) ios.badgeMuted else ios.badge, CircleShape)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = Color.White,
            style = IosType.caption.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
        )
    }
}

/** Read receipts, drawn the way the iOS client renders them inside bubbles. */
@Composable
fun Ticks(color: Color, read: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier.size(if (read) 16.dp else 10.dp, 10.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val first = Path().apply {
            moveTo(w * 0.04f, h * 0.52f)
            lineTo(w * 0.26f, h * 0.84f)
            lineTo(w * 0.62f, h * 0.16f)
        }
        drawPath(first, color, style = stroke)
        if (read) {
            val second = Path().apply {
                moveTo(w * 0.40f, h * 0.52f)
                lineTo(w * 0.62f, h * 0.84f)
                lineTo(w * 0.98f, h * 0.16f)
            }
            drawPath(second, color, style = stroke)
        }
    }
}

@Composable
fun MutedGlyph(color: Color, size: Dp = 13.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawArc(
            color = color,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.22f, h * 0.14f),
            size = Size(w * 0.56f, h * 0.56f),
            style = Stroke(1.1.dp.toPx(), cap = StrokeCap.Round),
        )
        drawLine(color, Offset(w * 0.3f, h * 0.62f), Offset(w * 0.7f, h * 0.62f), 1.1.dp.toPx(), StrokeCap.Round)
        drawLine(color, Offset(w * 0.5f, h * 0.62f), Offset(w * 0.5f, h * 0.82f), 1.1.dp.toPx(), StrokeCap.Round)
        drawLine(color, Offset(w * 0.14f, h * 0.9f), Offset(w * 0.86f, h * 0.1f), 1.3.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    val ios = LocalIos.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ios.background)
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text, style = IosType.sectionHeader, color = ios.textSecondary)
    }
}

/** Blue bubble used by the tab bar for "Chats". */
@Composable
fun BubbleGlyph(color: Color, filled: Boolean, size: Dp = 26.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.09f)
            cubicTo(w * 0.8f, h * 0.09f, w * 0.95f, h * 0.28f, w * 0.95f, h * 0.49f)
            cubicTo(w * 0.95f, h * 0.7f, w * 0.78f, h * 0.85f, w * 0.5f, h * 0.85f)
            cubicTo(w * 0.42f, h * 0.85f, w * 0.35f, h * 0.84f, w * 0.28f, h * 0.81f)
            lineTo(w * 0.1f, h * 0.93f)
            lineTo(w * 0.15f, h * 0.71f)
            cubicTo(w * 0.08f, h * 0.65f, w * 0.05f, h * 0.57f, w * 0.05f, h * 0.49f)
            cubicTo(w * 0.05f, h * 0.28f, w * 0.2f, h * 0.09f, w * 0.5f, h * 0.09f)
            close()
        }
        if (filled) {
            drawPath(path, color)
        } else {
            drawPath(path, color, style = Stroke(1.7.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun ChevronLeft(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(12.dp, 20.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.85f, h * 0.08f)
            lineTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.85f, h * 0.92f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
fun ChevronRight(color: Color, modifier: Modifier = Modifier, size: Dp = 12.dp) {
    Canvas(modifier.size(size * 0.62f, size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.08f)
            lineTo(w * 0.8f, h * 0.5f)
            lineTo(w * 0.15f, h * 0.92f)
        }
        drawPath(path, color, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun MicGlyph(color: Color, size: Dp = 26.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.38f, h * 0.1f),
            size = Size(w * 0.24f, h * 0.46f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(1.7.dp.toPx()),
        )
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.24f, h * 0.26f),
            size = Size(w * 0.52f, h * 0.42f),
            style = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round),
        )
        drawLine(color, Offset(w * 0.5f, h * 0.68f), Offset(w * 0.5f, h * 0.84f), 1.7.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
fun AttachGlyph(color: Color, size: Dp = 26.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(color, radius = w * 0.4f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(1.5.dp.toPx()))
        drawLine(color, Offset(w * 0.5f, h * 0.3f), Offset(w * 0.5f, h * 0.7f), 1.7.dp.toPx(), StrokeCap.Round)
        drawLine(color, Offset(w * 0.3f, h * 0.5f), Offset(w * 0.7f, h * 0.5f), 1.7.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
fun SmileGlyph(color: Color, size: Dp = 24.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(color, radius = w * 0.4f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(1.4.dp.toPx()))
        drawCircle(color, radius = w * 0.045f, center = Offset(w * 0.37f, h * 0.4f))
        drawCircle(color, radius = w * 0.045f, center = Offset(w * 0.63f, h * 0.4f))
        drawArc(
            color = color,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.3f, h * 0.36f),
            size = Size(w * 0.4f, h * 0.34f),
            style = Stroke(1.4.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
fun SendGlyph(color: Color, size: Dp = 26.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.86f)
            lineTo(w * 0.5f, h * 0.2f)
            moveTo(w * 0.24f, h * 0.45f)
            lineTo(w * 0.5f, h * 0.18f)
            lineTo(w * 0.76f, h * 0.45f)
        }
        drawPath(path, color, style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun PhoneGlyph(color: Color, size: Dp = 24.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.26f, h * 0.14f)
            lineTo(w * 0.4f, h * 0.14f)
            lineTo(w * 0.46f, h * 0.38f)
            lineTo(w * 0.34f, h * 0.46f)
            cubicTo(w * 0.38f, h * 0.62f, w * 0.52f, h * 0.74f, w * 0.68f, h * 0.78f)
            lineTo(w * 0.74f, h * 0.66f)
            lineTo(w * 0.94f, h * 0.72f)
            lineTo(w * 0.94f, h * 0.86f)
            cubicTo(w * 0.94f, h * 0.9f, w * 0.9f, h * 0.94f, w * 0.86f, h * 0.94f)
            cubicTo(w * 0.46f, h * 0.94f, w * 0.14f, h * 0.62f, w * 0.14f, h * 0.22f)
            cubicTo(w * 0.14f, h * 0.18f, w * 0.18f, h * 0.14f, w * 0.22f, h * 0.14f)
            close()
        }
        drawPath(path, color, style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** iOS-style navigation bar: 44dp tall, centered title, blue actions. */
@Composable
fun IosNavBar(
    title: String,
    modifier: Modifier = Modifier,
    left: @Composable (() -> Unit)? = null,
    right: @Composable (() -> Unit)? = null,
) {
    val ios = LocalIos.current
    Column(modifier.fillMaxWidth().background(ios.navBar)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = IosType.headline,
                color = ios.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 72.dp),
            )
            Row(
                Modifier.align(Alignment.CenterStart).fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box { left?.invoke() }
                Box { right?.invoke() }
            }
        }
        Hairline(color = ios.navHairline)
    }
}
