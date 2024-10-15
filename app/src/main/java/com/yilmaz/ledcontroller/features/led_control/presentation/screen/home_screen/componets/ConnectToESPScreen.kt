package com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen.componets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yilmaz.ledcontroller.R

@Composable
fun ConnectToESPScreen(innerPadding: PaddingValues, onConnectToLedModule: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clickable { onConnectToLedModule() }
    ) {
        Icon(
            modifier = Modifier
                .width(196.dp)
                .height(196.dp)
                .align(Alignment.Center),
            painter = painterResource(R.drawable.baseline_lightbulb_24),
            contentDescription = "img",
            tint = MaterialTheme.colorScheme.primaryContainer
        )
        Text(
            text = "Tap to connect ESP32 module",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}