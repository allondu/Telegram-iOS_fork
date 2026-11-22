package com.courier.messenger.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.courier.messenger.telegram.TdlibClient
import com.courier.messenger.ui.components.BubbleGlyph
import com.courier.messenger.ui.components.GlassButton
import com.courier.messenger.ui.components.GlassInput
import com.courier.messenger.ui.components.GlassPanel
import com.courier.messenger.ui.components.LiquidBackdrop
import com.courier.messenger.ui.theme.IosType
import com.courier.messenger.ui.theme.LocalIos

@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    val ios = LocalIos.current
    val appear by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 220f),
        label = "appear",
    )
    LiquidBackdrop(Modifier.fillMaxSize().background(ios.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(48.dp))
            LogoBadge()
            Spacer(Modifier.height(22.dp))
            Text(
                text = title,
                style = IosType.largeTitle.copy(fontSize = 28.sp, lineHeight = 34.sp),
                color = ios.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = IosType.subhead,
                color = ios.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(26.dp))
            Box(Modifier.fillMaxWidth().scale(0.96f + 0.04f * appear)) {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    strength = 0.22f,
                ) {
                    Column(Modifier.padding(18.dp)) {
                        content()
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Powered by TDLib · end-to-end encrypted Telegram",
                style = IosType.caption,
                color = ios.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun LogoBadge() {
    val ios = LocalIos.current
    val pulse by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 90f),
        label = "logo",
    )
    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(ios.accent)
            .scale(pulse),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(46.dp)) {
            BubbleGlyph(Color.White, filled = true, size = 46.dp)
        }
    }
}

@Composable
fun SetupScreen(
    initialId: String,
    initialHash: String,
    onSave: (String, String) -> Unit,
) {
    var apiId by remember { mutableStateOf(initialId) }
    var apiHash by remember { mutableStateOf(initialHash) }
    val canSave = apiId.trim().toIntOrNull() != null && apiHash.trim().length >= 10

    AuthScaffold(
        title = "Connect your account",
        subtitle = "Courier talks to Telegram directly through TDLib. Enter the api_id and api_hash of your own Telegram application.",
    ) {
        GlassInput(
            value = apiId,
            onValueChange = { apiId = it },
            placeholder = "api_id (e.g. 1234567)",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
        )
        Spacer(Modifier.height(12.dp))
        GlassInput(
            value = apiHash,
            onValueChange = { apiHash = it },
            placeholder = "api_hash",
            isPassword = true,
        )
        Spacer(Modifier.height(16.dp))
        GlassButton(
            text = "Continue",
            enabled = canSave,
            onClick = { onSave(apiId.trim(), apiHash.trim()) },
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Create one at my.telegram.org → API development tools.\nThese keys stay on this device.",
            style = IosType.footnote,
            color = LocalIos.current.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun PhoneScreen(auth: TdlibClient.AuthUi, onContinue: (String) -> Unit) {
    val ios = LocalIos.current
    var phone by remember { mutableStateOf("") }
    val canContinue = phone.count { it.isDigit() } >= 5 && !auth.busy

    AuthScaffold(
        title = "Your phone number",
        subtitle = "Please confirm your country code and enter your phone number.",
    ) {
        GlassInput(
            value = phone,
            onValueChange = { phone = it },
            placeholder = "Phone number",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
            prefix = "+",
        )
        if (auth.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(auth.error, style = IosType.footnote, color = ios.danger)
        }
        Spacer(Modifier.height(16.dp))
        GlassButton(
            text = if (auth.stage == TdlibClient.Stage.Starting) "Connecting…" else "Next",
            enabled = canContinue,
            loading = auth.busy || auth.stage == TdlibClient.Stage.Starting,
            onClick = { onContinue("+" + phone.filter { it.isDigit() }) },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Telegram will send you a login code by SMS or in-app message.",
            style = IosType.footnote,
            color = ios.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun CodeScreen(
    auth: TdlibClient.AuthUi,
    onContinue: (String) -> Unit,
    onResend: () -> Unit,
) {
    val ios = LocalIos.current
    var code by remember { mutableStateOf("") }
    val canContinue = code.length >= auth.codeLength.coerceAtLeast(4) && !auth.busy

    AuthScaffold(
        title = "Enter the code",
        subtitle = "We sent a ${auth.codeLength}-digit code to your Telegram account.",
    ) {
        GlassInput(
            value = code,
            onValueChange = { input -> code = input.filter { it.isDigit() }.take(8) },
            placeholder = "•".repeat(auth.codeLength.coerceIn(3, 8)),
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            textStyle = IosType.body.copy(fontSize = 22.sp, letterSpacing = 6.sp),
        )
        if (auth.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(auth.error, style = IosType.footnote, color = ios.danger)
        }
        Spacer(Modifier.height(16.dp))
        GlassButton(
            text = "Sign in",
            enabled = canContinue,
            loading = auth.busy,
            onClick = { onContinue(code) },
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Resend code",
            style = IosType.body.copy(fontWeight = FontWeight.Medium),
            color = ios.accent,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onResend() }
                .padding(10.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PasswordScreen(
    auth: TdlibClient.AuthUi,
    onContinue: (String) -> Unit,
) {
    val ios = LocalIos.current
    var password by remember { mutableStateOf("") }
    val canContinue = password.isNotBlank() && !auth.busy

    AuthScaffold(
        title = "Two-step verification",
        subtitle = auth.passwordHint?.takeIf { it.isNotBlank() }
            ?: "Your account is protected with an additional password.",
    ) {
        GlassInput(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password",
            isPassword = true,
        )
        if (auth.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(auth.error, style = IosType.footnote, color = ios.danger)
        }
        Spacer(Modifier.height(16.dp))
        GlassButton(
            text = "Unlock",
            enabled = canContinue,
            loading = auth.busy,
            onClick = { onContinue(password) },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "The password is checked locally by TDLib using Telegram's SRP protocol.",
            style = IosType.footnote,
            color = ios.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
