package com.duyha.blelib

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.util.*

/**
 * A wrapper class that implement a Ble Central
 *
 * @property context Context
 * @property callback Callback handler that will receive callback events from BleCentral.
 */
class BleCentral(
    private val context: Context,
    private val callback: BleCentralCallback
) {

    private val scope = MainScope()

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null
    private var bleGatt: BluetoothGatt? = null


    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    /**
     * Start Bluetooth LE scan. The scan results will be delivered through the [callback].
     * It will automatically scan, connect, and communicate with the peripheral.
     *
     */
    fun start() {
        if (!hasBlePermission()) {
            callback.onInitializeBleFailed(INITIALIZE_FAILED_PERMISSION_NOT_GRANTED)
            return
        }
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            callback.onInitializeBleFailed(INITIALIZE_FAILED_BLUETOOTH_NOT_ENABLED)
            Log.d(TAG, "onInitializeBleFailed")
        } else {
            bleScanner = bluetoothAdapter!!.bluetoothLeScanner
            bleScanner?.let {
                val scanFilter = ScanFilter.Builder().build()
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
                it.startScan(listOf(scanFilter), settings, scanCallback)
                Log.d(TAG, "startScan")
            }
        }
    }

    private fun hasBlePermission(): Boolean {
        return ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Stop scanning and disconnect from the peripheral
     *
     */
    fun stop() {
        scope.cancel()
        bleScanner?.stopScan(scanCallback)
        bleGatt?.disconnect()

        bleGatt = null
        bleScanner = null
    }

    private fun writeCharacteristic(gatt: BluetoothGatt, value: String) {
        val service = gatt.getService(UUID_Service)
        service?.let {
            Log.d(TAG, "writeCharacteristic $value")
            val characteristic = service.getCharacteristic(UUID_characteristic)
            characteristic.value = value.toByteArray()
            gatt.writeCharacteristic(characteristic)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            callback.onScanFailed()
            Log.d(TAG, "onScanFailed errorCode $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                if (it.device.name == PERIPHERAL_NAME) {
                    callback.onScanSuccess()
                    bleScanner?.stopScan(this)
                    bleGatt = result.device.connectGatt(context, false, bleGattCallback)
                }
            }

            super.onScanResult(callbackType, result)
        }
    }

    private val bleGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.d(TAG, "onConnectionStateChange status $status")
            Log.d(TAG, "onConnectionStateChange newState $newState")
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                callback.onDisconnected()
                scope.launch {
                    start()
                }
            } else if (newState != status && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.d(TAG, "onServicesDiscovered status $status")
            callback.onServicesDiscovered()
            scope.launch {
                writeCharacteristic(gatt, CHAR_VAL_RED)
                callback.onWriteRED()
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d(TAG, "onCharacteristicWrite status $status")
            val value = characteristic.getStringValue(0)
            Log.d(TAG, "onCharacteristicWrite value $value")
            if (value == CHAR_VAL_RED) {
                scope.launch {
                    delay(1000L)
                    writeCharacteristic(gatt, CHAR_VAL_GREEN)
                    callback.onWriteGREEN()
                }
            } else {
                scope.launch {
                    delay(1000L)
                    gatt.disconnect()
                }
            }
        }
    }

    companion object {
        private const val SERVICE_UUID = "00000000-0000-0000-0000-000000000000"
        private val UUID_Service: UUID = UUID.fromString(SERVICE_UUID)
        private val UUID_characteristic: UUID = UUID.fromString(SERVICE_UUID)
        private const val PERIPHERAL_NAME =  "BLEPeripheral"
        private const val TAG = "BleCentral"
        private const val CHAR_VAL_RED = "RED"
        private const val CHAR_VAL_GREEN = "GREEN"

        /**
         * Error code return when permission ACCESS_FINE_LOCATION is not granted
         */
        const val INITIALIZE_FAILED_PERMISSION_NOT_GRANTED = 1

        /**
         * Error code return when bluetooth is not enabled
         */
        const val INITIALIZE_FAILED_BLUETOOTH_NOT_ENABLED = 2

    }
}