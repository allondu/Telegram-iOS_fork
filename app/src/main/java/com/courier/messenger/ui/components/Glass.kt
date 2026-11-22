package com.courier.messenger.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.courier.messenger.ui.theme.IosType
import com.courier.messenger.ui.theme.LocalIos
import kotlin.math.sin

/**
 * Translucent "liquid glass" panel: layered tint, specular rim and a soft
 * top highlight. True backdrop blur needs platform support, so the rim and
 * translucency carry the effect on every API level.
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    tint: Color = Color.White,
    strength: Float = 0.26f,
    content: @Composable BoxScope.() -> Unit,
) {
    val ios = LocalIos.current
    val base = if (ios.isDark) Color(0xFF2C2C2E) else tint
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        base.copy(alpha = strength + 0.10f),
                        base.copy(alpha = strength * 0.55f),
                        base.copy(alpha = strength * 0.85f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = if (ios.isDark) 0.34f else 0.75f),
                        Color.White.copy(alpha = 0.05f),
                        Color.White.copy(alpha = if (ios.isDark) 0.18f else 0.35f),
                    ),
                ),
                shape = shape,
            ),
        content = content,
    )
}

/** Animated colour blobs behind the content — the "liquid" in liquid glass. */
@Composable
fun LiquidBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val ios = LocalIos.current
    val transition = rememberInfiniteTransition(label = "backdrop")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "phase",
    )
    Box(modifier) {
        Canvas(Modifier.fillMaxSize().blur(64.dp)) {
            val w = size.width
            val h = size.height
            val drift = sin(phase * Math.PI.toFloat())
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(ios.accent.copy(alpha = if (ios.isDark) 0.42f else 0.30f), Color.Transparent),
                ),
                radius = w * 0.62f,
                center = Offset(w * (0.16f + 0.10f * drift), h * (0.18f + 0.04f * drift)),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF8E7CFF).copy(alpha = if (ios.isDark) 0.36f else 0.24f), Color.Transparent),
                ),
                radius = w * 0.55f,
                center = Offset(w * (0.84f - 0.12f * drift), h * (0.30f + 0.06f * drift)),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF4FD1C5).copy(alpha = if (ios.isDark) 0.26f else 0.18f), Color.Transparent),
                ),
                radius = w * 0.6f,
                center = Offset(w * (0.3f + 0.1f * drift), h * (0.86f - 0.05f * drift)),
            )
        }
        content()
    }
}

@Composable
fun GlassInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    prefix: String? = null,
    textStyle: TextStyle = IosType.body,
) {
    val ios = LocalIos.current
    GlassPanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        strength = 0.18f,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            if (prefix != null) {
                Text(prefix, style = textStyle, color = ios.textSecondary)
                Spacer(Modifier.size(8.dp))
            }
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(placeholder, style = textStyle, color = ios.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = textStyle.copy(color = ios.textPrimary),
                    cursorBrush = SolidColor(ios.accent),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun GlassButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val ios = LocalIos.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 780f),
        label = "buttonScale",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    if (enabled) listOf(ios.outgoingTop, ios.outgoingBottom)
                    else listOf(ios.textTertiary, ios.textTertiary),
                ),
            )
            .border(
                1.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.05f))),
                RoundedCornerShape(26.dp),
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text, style = IosType.headline.copy(fontSize = 17.sp), color = Color.White)
        }
    }
}
