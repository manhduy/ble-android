package com.duyha.blelib

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.*
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*

class BleCentral(
    private val context: Context,
    private val listener: BleCentralListener
) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null
    private var bleGatt: BluetoothGatt? = null

    init {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    suspend fun scan() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            withContext(Dispatchers.Main) {
                listener.onInitializeBleFailed()
            }
            Timber.d("onInitializeBleFailed")
        } else {
            bleScanner = bluetoothAdapter!!.bluetoothLeScanner
            bleScanner?.let {
                val scanFilter = ScanFilter.Builder().build()
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
                it.startScan(listOf(scanFilter), settings, scanCallback)
                Timber.d("startScan")
            }
        }
    }

    fun writeCharacteristicAndDisconnect(gatt: BluetoothGatt) {
        GlobalScope.launch {
            writeCharacteristic(gatt, CHAR_VAL_RED)
            listener.onWriteRED()
            delay(1000L)
            /*writeCharacteristic(gatt, CHAR_VAL_GREEN)
            listener.onWriteGREEN()
            delay(1000L)
            gatt.disconnect()
            listener.onDisconnect()*/
        }
    }

    fun writeCharacteristicAndDisconnect1(gatt: BluetoothGatt) {
        GlobalScope.launch {
            val service = gatt.getService(UUID_Service)
            service?.let {
                Timber.d("writeCharacteristic")
                val characteristic = service.getCharacteristic(UUID_characteristic)
                characteristic.value = CHAR_VAL_RED.toByteArray()
                Timber.d("writeCharacteristic RED")
                gatt.writeCharacteristic(characteristic)
                listener.onWriteRED()
                delay(3000L)
                characteristic.value = CHAR_VAL_GREEN.toByteArray()
                gatt.writeCharacteristic(characteristic)
                Timber.d("writeCharacteristic GREEN")
                listener.onWriteGREEN()
                delay(3000L)
                //gatt.disconnect()
                //listener.onDisconnect()
            }
        }
    }

    private fun writeCharacteristic(gatt: BluetoothGatt, value: String) {
        val service = gatt.getService(UUID_Service)
        service?.let {
            Timber.d("writeCharacteristic $value")
            val characteristic = service.getCharacteristic(UUID_characteristic)
            characteristic.value = value.toByteArray()
            gatt.writeCharacteristic(characteristic)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            listener.onScanFailed(errorCode)
            println("onScanFailed errorCode $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                if (it.device.name == PERIPHERAL_NAME) {
                    listener.onScanSuccess()
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
            Timber.d("onConnectionStateChange status $status")
            Timber.d("onConnectionStateChange newState $newState")
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                listener.onDisconnected()
                GlobalScope.launch {
                    scan()
                }
            } else if (newState != status && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Timber.d("onServicesDiscovered status $status")
            listener.onServicesDiscovered()
            GlobalScope.launch {
                writeCharacteristic(gatt, CHAR_VAL_RED)
                listener.onWriteRED()
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Timber.d("onCharacteristicWrite status $status")
            val value = characteristic.getStringValue(0)
            Timber.d("onCharacteristicWrite value $value")
            if (value == CHAR_VAL_RED) {
                GlobalScope.launch {
                    delay(1000L)
                    writeCharacteristic(gatt, CHAR_VAL_GREEN)
                    listener.onWriteGREEN()
                }
            } else {
                GlobalScope.launch {
                    delay(1000L)
                    gatt.disconnect()
                }
            }
        }
    }

    companion object {
        private const val RC_ENABLE_BLUETOOTH = 1
        private const val RC_ACCESS_FINE_LOCATION = 2
        private const val SERVICE_UUID = "00000000-0000-0000-0000-000000000000"
        private val UUID_Service: UUID = UUID.fromString(SERVICE_UUID)
        private val UUID_characteristic: UUID = UUID.fromString(SERVICE_UUID)
        private const val PERIPHERAL_NAME =  "BLEPeripheral"
        private const val TAG = "BleCentral"
        private const val CHAR_VAL_RED = "RED"
        private const val CHAR_VAL_GREEN = "GREEN"
    }


}