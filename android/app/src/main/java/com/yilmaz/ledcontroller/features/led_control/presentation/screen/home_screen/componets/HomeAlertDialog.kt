package com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen.componets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun HomeAlertDialog(action: () -> Unit, actionText: String, title: String, body: String) {
    AlertDialog(
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text(text = title) },
        text = { Text(text = body) },
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = { action() }
            ) { Text(actionText) }
        }
    )
}