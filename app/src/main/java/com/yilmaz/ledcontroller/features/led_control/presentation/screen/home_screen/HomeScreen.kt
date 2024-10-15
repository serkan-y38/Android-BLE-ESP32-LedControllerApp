package com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen.componets.ConnectToESPScreen
import com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen.componets.HomeAlertDialog
import com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen.componets.LedControlScreen

@Composable
fun HomeScreen(
    @Suppress("UNUSED_PARAMETER") navHostController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val hostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(key1 = state.message) {
        state.message?.let { message ->
            hostState.showSnackbar(message)
        }
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                viewModel.updateIsBluetoothEnabled(true)
                viewModel.isPaired()
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = hostState) }
    ) { innerPadding ->
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
                    action = { viewModel.pair() }
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

            state.isConnected -> {
                LedControlScreen(
                    innerPadding = innerPadding,
                    onDisconnect = { viewModel.disconnect() },
                    deviceName = state.deviceName
                )
            }

            else -> {
                ConnectToESPScreen(
                    innerPadding = innerPadding,
                    onConnectToLedModule = { viewModel.connect() }
                )
            }
        }
    }
}

