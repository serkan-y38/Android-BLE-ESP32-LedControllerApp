package com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen

data class HomeState(
    val message: String? = null,
    val deviceName: String = "",
    val isPairing: Boolean = false,
    val isPaired: Boolean = false,
    val isConnected: Boolean = false,
    val isBluetoothEnabled: Boolean = false
)
