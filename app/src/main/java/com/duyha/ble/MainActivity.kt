package com.duyha.ble

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {


    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null
    private var bleGatt: BluetoothGatt? = null

    private var scanning = true

    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initBLE()
        btnScan.setOnClickListener {
            if (hasBLEPermission()) {
                scan()
            }
        }
    }

    private fun initBLE() {
        Log.d(TAG, "UUID_Service $UUID_Service")
        Log.d(TAG, "UUID_characteristic $UUID_characteristic")
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    private fun hasBLEPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (! ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    RC_ACCESS_FINE_LOCATION)
            }
            false
        } else {
            true
        }
    }

    private fun scan() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, RC_ENABLE_BLUETOOTH)
        } else {
            bleScanner = bluetoothAdapter!!.bluetoothLeScanner;
            bleScanner?.let {
                val scanFilter = ScanFilter.Builder().build()
                val settings = ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
                it.startScan(listOf(scanFilter), settings, scanCallback)
                Log.d(TAG, "startScan")
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "onScanFailed errorCode: $errorCode")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d(TAG, "onBatchScanResults")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                if (it.device.name == "BLE Device") {
                    bleScanner?.stopScan(this)
                    bleGatt = result.device.connectGatt(applicationContext, false, bleGattCallback)
                }
            }

            super.onScanResult(callbackType, result)
        }
    }

    private val bleGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            gatt.discoverServices()
            super.onConnectionStateChange(gatt, status, newState)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(UUID_Service)
            service?.let {
                val characteristic = service.getCharacteristic(UUID_characteristic)
                characteristic.value = "RED".toByteArray()
                gatt.writeCharacteristic(characteristic)
            }

            super.onServicesDiscovered(gatt, status)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val value = characteristic.getStringValue(0)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Characteristic: $value", Toast.LENGTH_LONG).show()
            }
            val service = gatt.getService(UUID_Service)
            super.onCharacteristicRead(gatt, characteristic, status)
        }
    }

    companion object {
        private const val RC_ENABLE_BLUETOOTH = 1
        private const val RC_ACCESS_FINE_LOCATION = 2
        private const val SERVICE_UUID = "00000000-0000-0000-0000-000000000000"
        private val UUID_Service: UUID = UUID(0L, 0L)
        private val UUID_characteristic: UUID = UUID(0L, 0L)
        private const val TAG = "MainActivityTAG"
    }
}
