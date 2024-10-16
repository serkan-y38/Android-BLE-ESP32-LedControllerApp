package com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen.componets

import android.content.res.Configuration
import android.provider.CalendarContract.Colors
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yilmaz.ledcontroller.R

@Composable
fun LedControlScreen(innerPadding: PaddingValues, onDisconnect: () -> Unit, deviceName: String) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = deviceName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onDisconnect() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Disconnect"
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 16.dp)
                ) {
                    LedItem(onTap = {}, color = Color.Red)
                    LedItem(onTap = {}, color = Color.Green)
                    LedItem(onTap = {}, color = Color.Blue)
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .wrapContentHeight()
                ) {
                    LedItem(onTap = {}, color = Color.Magenta)
                    LedItem(onTap = {}, color = Color.Yellow)
                    LedItem(onTap = {}, color = Color.Cyan)
                }
            }
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
            }
            item {
                Column {
                    AnimationItem(onTap = {}, animation = "Fade")
                    AnimationItem(onTap = {}, animation = "RGB")
                    AnimationItem(onTap = {}, animation = "Flash")
                    AnimationItem(onTap = {}, animation = "Rainbow")
                }
            }
        }
    }
}

@Composable
fun LedItem(onTap: () -> Unit, color: Color) {
    Card(
        modifier = Modifier.size(96.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
                    .align(Alignment.Center)
                    .clickable { onTap() },
                painter = painterResource(R.drawable.baseline_lightbulb_24),
                tint = color,
                contentDescription = "icon"
            )
        }
    }
}

@Composable
fun AnimationItem(onTap: () -> Unit, animation: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onTap() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_animation_24),
                contentDescription = "img"
            )
            Text(
                text = animation,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun Preview(modifier: Modifier = Modifier) {
    Scaffold {
        LedControlScreen(innerPadding = it, onDisconnect = {}, deviceName = "ESP32")
    }
}