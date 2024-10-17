package com.yilmaz.ledcontroller.features.led_control.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.yilmaz.ledcontroller.features.led_control.data.receiver.PairDeviceReceiver
import com.yilmaz.ledcontroller.features.led_control.domain.LedController
import com.yilmaz.ledcontroller.features.led_control.domain.model.LedControlModel
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

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

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

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
    private var bluetoothGatt: BluetoothGatt? = null

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
        onPairedSuccessfully = { _ ->
            setMessage("Paired successfully")
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

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    setMessage("Successfully connected to ${gatt.device.address}")
                    _isConnected.update { true }
                    _deviceName.update { gatt.device.name }
                    gatt.discoverServices()

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    setMessage("Successfully disconnected from ${gatt.device.address}")
                    _isConnected.update { false }
                    _deviceName.update { "" }
                    gatt.close()
                }

            } else {
                setMessage("Error $status encountered for ${gatt.device.address}")
                _isConnected.update { false }
                _deviceName.update { "" }
                gatt.close()
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("gattCallback", "Change mode request sent successfully")
            } else {
                Log.i("gattCallback", "Failed to send led control model data -> status: $status")
            }
        }
    }

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

    override fun release() {
        disconnect()

        if (isPairDeviceReceiverRegistered) context.unregisterReceiver(pairDeviceReceiver)
    }

    override fun connect() {
        val device = bluetoothAdapter?.getRemoteDevice(DEVICE_ADDRESS)
        bluetoothGatt = device?.connectGatt(context, false, gattCallback, TRANSPORT_LE)

        stopScan()
    }

    override fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()

        _isConnected.update { false }
        _deviceName.update { "" }

        setMessage("Successfully disconnected from ${bluetoothGatt?.device?.address}")
    }

    override fun updateLedMode(ledControlModel: LedControlModel) {
        val service = bluetoothGatt?.getService(UUID.fromString(SERVICE_UUID))
        val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))

        characteristic?.let {
            val data = Json.encodeToString(ledControlModel).encodeToByteArray()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bluetoothGatt?.writeCharacteristic(
                    it,
                    data,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                @Suppress("DEPRECATION")
                it.value = data
                @Suppress("DEPRECATION")
                bluetoothGatt?.writeCharacteristic(it)
            }
        } ?: run {
            setMessage("Characteristic not found!")
        }
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
        private const val SERVICE_UUID = "22bf526e-1f59-40fb-a344-0bea8c1bfef2"
        private const val CHARACTERISTIC_UUID = "cdc7651d-88bd-4c0d-8c90-4572db5aa14b"
    }
}