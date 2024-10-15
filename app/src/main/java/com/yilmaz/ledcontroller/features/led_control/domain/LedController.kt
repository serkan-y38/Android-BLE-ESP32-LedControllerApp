package com.yilmaz.ledcontroller.features.led_control.domain

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface LedController {
    val isPaired: StateFlow<Boolean>
    val isPairing: StateFlow<Boolean>
    val isBluetoothEnabled: StateFlow<Boolean>
    val deviceName: StateFlow<String>
    val message: SharedFlow<String>

    fun isPaired()

    fun pair()
}