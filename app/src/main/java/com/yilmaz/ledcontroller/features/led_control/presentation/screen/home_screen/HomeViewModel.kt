package com.yilmaz.ledcontroller.features.led_control.presentation.screen.home_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yilmaz.ledcontroller.features.led_control.domain.LedController
import com.yilmaz.ledcontroller.features.led_control.domain.model.LedControlModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ledController: LedController
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state

    init {
        getMessage()
        getIsBluetoothEnabled()
        getIsPairing()
        getIsPaired()
        getIsConnected()
        getDeviceName()
    }

    private fun getMessage() {
        ledController.message.onEach { m ->
            _state.update { it.copy(message = m) }
        }.launchIn(viewModelScope)
    }

    private fun getIsPaired() {
        ledController.isPaired.onEach { isPaired ->
            _state.update { it.copy(isPaired = isPaired) }
        }.launchIn(viewModelScope)
    }

    private fun getIsPairing() {
        ledController.isPairing.onEach { isPairing ->
            _state.update { it.copy(isPairing = isPairing) }
        }.launchIn(viewModelScope)
    }

    private fun getIsConnected() {
        ledController.isConnected.onEach { c ->
            _state.update { it.copy(isConnected = c) }
        }.launchIn(viewModelScope)
    }

    private fun getIsBluetoothEnabled() {
        ledController.isBluetoothEnabled.onEach { isEnabled ->
            _state.update { it.copy(isBluetoothEnabled = isEnabled) }
        }.launchIn(viewModelScope)
    }

    private fun getDeviceName() {
        ledController.deviceName.onEach { deviceName ->
            _state.update { it.copy(deviceName = deviceName) }
        }.launchIn(viewModelScope)
    }

    fun updateIsBluetoothEnabled(enabled: Boolean) {
        _state.update { it.copy(isBluetoothEnabled = enabled) }
    }

    fun isPaired() {
        ledController.isPaired()
    }

    fun pair() {
        ledController.pair()
    }

    fun connect() {
        ledController.connect()
    }

    fun disconnect() {
        ledController.disconnect()
    }

    fun updateLedMode(ledControlModel: LedControlModel){
        ledController.updateLedMode(ledControlModel)
    }

    override fun onCleared() {
        super.onCleared()
        ledController.release()
    }
}