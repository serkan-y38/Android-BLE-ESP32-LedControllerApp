package com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.yilmaz.ledcontroller.R
import com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen.componets.HomeAlertDialog

@Composable
fun HomeScreen(
    @Suppress("UNUSED_PARAMETER") navHostController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                viewModel.updateIsBluetoothEnabled(true)
                viewModel.isPaired()
            }
        }
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            !state.isBluetoothEnabled -> {
                HomeAlertDialog(
                    title = "Please enable bluetooth",
                    body = "Enable bluetooth to connect esp32 module",
                    actionText = "Enable",
                    action = {
                        enableBluetoothLauncher.launch(
                            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        )
                    }
                )
            }

            !state.isPaired -> {
                HomeAlertDialog(
                    title = "Pair device",
                    body = "Your device is not paired with the ESP32 module",
                    actionText = "Pair",
                    action = {viewModel.pair()}
                )
            }

            state.isPairing -> {
                HomeAlertDialog(
                    title = "Pairing...",
                    body = "Please wait while pairing devices",
                    actionText = "OK",
                    action = { }
                )
            }
        }
        Icon(
            modifier = Modifier
                .width(196.dp)
                .height(196.dp)
                .align(Alignment.Center),
            painter = painterResource(R.drawable.baseline_lightbulb_24),
            contentDescription = "img"
        )
    }
}