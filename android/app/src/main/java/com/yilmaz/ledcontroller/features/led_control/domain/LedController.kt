package com.yilmaz.ledcontroller.features.led_control.domain

import com.yilmaz.ledcontroller.features.led_control.domain.model.LedControlModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface LedController {
    val isPaired: StateFlow<Boolean>
    val isPairing: StateFlow<Boolean>
    val isConnected: StateFlow<Boolean>
    val isBluetoothEnabled: StateFlow<Boolean>
    val deviceName: StateFlow<String>
    val message: SharedFlow<String>

    fun isPaired()

    fun pair()

    fun connect()

    fun disconnect()

    fun release()

    fun updateLedMode(ledControlModel: LedControlModel)
}