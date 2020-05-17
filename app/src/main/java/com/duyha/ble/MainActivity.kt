package com.duyha.ble

import android.Manifest
import android.bluetooth.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.messageId.observe(this, Observer {
            tvMsg.text = getString(it)
        })
        viewModel.blePerNotGranted.observe(this, Observer {
            viewError.visibility = View.VISIBLE
            requestBlePermission()
        })
        viewModel.bleNotEnabled.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                if (it) {
                    viewError.visibility = View.VISIBLE
                    enableBluetooth()
                }
            }
        })
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, RC_ENABLE_BLUETOOTH)
    }

    private fun requestBlePermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            RC_ACCESS_FINE_LOCATION_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_ENABLE_BLUETOOTH) {
            viewError.visibility = View.GONE
            viewModel.startBleCentral()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_ACCESS_FINE_LOCATION_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    viewError.visibility = View.GONE
                    viewModel.startBleCentral()
                } else {
                    // permission denied
                    viewError.visibility = View.VISIBLE
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun onTryAgainClick() {
        viewError.visibility = View.GONE
        viewModel.startBleCentral()
    }

    override fun onDestroy() {
        viewModel.stopBleCentral()
        super.onDestroy()
    }


    companion object {
        private const val RC_ENABLE_BLUETOOTH = 1
        private const val RC_ACCESS_FINE_LOCATION_PERMISSION = 2
    }

}
