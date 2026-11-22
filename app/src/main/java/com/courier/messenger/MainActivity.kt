package com.courier.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.courier.messenger.ui.App
import com.courier.messenger.ui.theme.IosTheme
import com.courier.messenger.ui.theme.LocalIos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IosTheme {
                App(modifier = Modifier.fillMaxSize().background(LocalIos.current.background))
            }
        }
    }
}
