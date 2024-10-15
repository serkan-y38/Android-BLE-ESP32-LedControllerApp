package com.yilmaz.ledcontroller.features.led_control.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.yilmaz.ledcontroller.features.led_control.data.receiver.PairDeviceReceiver
import com.yilmaz.ledcontroller.features.led_control.domain.LedController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class LedControllerImpl(
    private val context: Context
) : LedController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _isPaired = MutableStateFlow(false)
    override val isPaired: StateFlow<Boolean>
        get() = _isPaired.asStateFlow()

    private val _isPairing = MutableStateFlow(false)
    override val isPairing: StateFlow<Boolean>
        get() = _isPairing.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(false)
    override val isBluetoothEnabled: StateFlow<Boolean>
        get() = _isBluetoothEnabled.asStateFlow()

    private val _deviceName = MutableStateFlow("")
    override val deviceName: StateFlow<String>
        get() = _deviceName.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    override val message: SharedFlow<String>
        get() = _message.asSharedFlow()

    private var isScanning = false
    private var isPairDeviceReceiverRegistered = false

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (result.device.address == DEVICE_ADDRESS) {
                if (!hasBluetoothConnectPermission()) return
                val device = bluetoothAdapter?.getRemoteDevice(result.device.address)

                device?.let {
                    try {
                        device.createBond()
                    } catch (e: Exception) {
                        setMessage(("Failed to pair with device: ${e.message}"))
                    }
                } ?: run {
                    setMessage("Device not found with address: ${result.device.address}")
                }
            }
        }
    }

    private val pairDeviceReceiver = PairDeviceReceiver(
        onPairRequest = {
            _isPairing.update { true }
            _isPaired.update { true }
        },
        onPairedSuccessfully = { device ->
            setMessage("Connected to ${device?.address}")
            _isPaired.update { true }
            _isPairing.update { false }
            stopScan()
        },
        onPairingError = { device ->
            setMessage("Cannot connected to device ${device?.address}")
            _isPairing.update { false }
            _isPaired.update { false }
            stopScan()
        },
        onPairing = {
            _isPairing.update { true }
            _isPaired.update { true }
        }
    )

    init {
        isPaired()
        isBluetoothEnabled()
    }

    override fun isPaired() {
        if (!hasBluetoothConnectPermission()) {
            setMessage("No bluetooth connect permission")
            return
        }

        val result = bluetoothAdapter
            ?.bondedDevices
            ?.filter { bluetoothDevice -> bluetoothDevice.address == DEVICE_ADDRESS }
            ?.size == 1

        _isPaired.update { result }
    }

    override fun pair() {
        startScan()
    }

    private fun startScan() {
        if (!hasBluetoothScanPermission()) {
            setMessage("No bluetooth permission")
            return
        }

        if (!isBluetoothEnabled()) {
            setMessage("Bluetooth not enabled")
            return
        }

        if (isScanning) return

        if (!isPairDeviceReceiverRegistered) {
            registerPairDeviceReceiver()
            isPairDeviceReceiverRegistered = true
        }

        bluetoothAdapter?.bluetoothLeScanner?.startScan(leScanCallback)
        isScanning = true
    }

    private fun stopScan() {
        if (!hasBluetoothScanPermission()) {
            setMessage("No bluetooth permission")
            return
        }

        if (!isBluetoothEnabled()) {
            setMessage("Bluetooth not enabled")
            return
        }

        if (!isScanning) return

        bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
        isScanning = false
    }

    private fun isBluetoothEnabled(): Boolean {
        val result = bluetoothAdapter?.isEnabled == true
        _isBluetoothEnabled.update { result }
        return result
    }

    private fun setMessage(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _message.emit(text)
        }
    }

    private fun registerPairDeviceReceiver() {
        context.registerReceiver(
            pairDeviceReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            }
        )
    }

    private fun hasBluetoothScanPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                return false
            }
        } else {
            if (!hasPermission(Manifest.permission.BLUETOOTH)) {
                return false
            }
        }
        return true
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                return false
            }
        } else {
            if (!hasPermission(Manifest.permission.BLUETOOTH)) {
                return false
            }
        }
        return true
    }

    private fun hasPermission(permission: String) =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val DEVICE_ADDRESS = ""
    }
}