package com.courier.messenger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.courier.messenger.telegram.Format
import com.courier.messenger.telegram.TdlibClient
import com.courier.messenger.ui.components.Avatar
import com.courier.messenger.ui.components.ChevronRight
import com.courier.messenger.ui.components.GlassPanel
import com.courier.messenger.ui.components.Hairline
import com.courier.messenger.ui.theme.IosType
import com.courier.messenger.ui.theme.LocalIos

@Composable
fun SettingsScreen(
    profile: TdlibClient.ProfileUi?,
    connection: String,
    chatCount: Int,
    onLogout: () -> Unit,
) {
    val ios = LocalIos.current
    var confirmLogout by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(ios.groupedBackground)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Settings", style = IosType.largeTitle, color = ios.textPrimary)
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 20.dp)) {
            item {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    strength = 0.34f,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(
                            initials = profile?.initials ?: "#",
                            gradient = Format.gradientFor(profile?.userId ?: 1L),
                            size = 62.dp,
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                profile?.name ?: "Signed in",
                                style = IosType.title.copy(fontSize = 20.sp),
                                color = ios.textPrimary,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                profile?.phone?.ifBlank { "Telegram account" } ?: "Telegram account",
                                style = IosType.subhead,
                                color = ios.textSecondary,
                            )
                        }
                        ChevronRight(ios.textTertiary, size = 14.dp)
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                GlassPanel(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(18.dp),
                    strength = 0.24f,
                ) {
                    Column {
                        SettingRow("Connection", connection)
                        Hairline(Modifier.padding(start = 16.dp))
                        SettingRow("Chats synced", chatCount.toString())
                        Hairline(Modifier.padding(start = 16.dp))
                        SettingRow("Encryption", "End-to-end (Telegram)")
                        Hairline(Modifier.padding(start = 16.dp))
                        SettingRow("Engine", "TDLib 1.8 · native")
                    }
                }
            }

            item {
                Spacer(Modifier.height(14.dp))
                GlassPanel(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(18.dp),
                    strength = 0.24f,
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { confirmLogout = true }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Log out",
                                style = IosType.body.copy(fontSize = 17.sp),
                                color = ios.danger,
                                modifier = Modifier.weight(1f),
                            )
                            ChevronRight(ios.textTertiary, size = 14.dp)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Courier 1.0 (1) · Android\nA TDLib-powered client with the iOS interface language.\nYour messages travel through Telegram's servers, not ours.",
                    style = IosType.footnote,
                    color = ios.textSecondary,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 18.dp),
                )
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    onLogout()
                }) {
                    Text("Log out", color = ios.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text("Cancel", color = ios.accent)
                }
            },
            title = { Text("Log out of Courier?") },
            text = { Text("TDLib will remove this session's local data. You can log in again at any time.") },
            containerColor = ios.background,
        )
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    val ios = LocalIos.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = IosType.body.copy(fontSize = 16.sp), color = ios.textPrimary)
        Spacer(Modifier.width(12.dp))
        Text(value, style = IosType.subhead, color = ios.textSecondary)
    }
}
