package com.duyha.ble

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.duyha.blelib.BleCentral
import com.duyha.blelib.BleCentralListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app), BleCentralListener {

    private val _messageId = MutableLiveData<Int>()
    val messageId: LiveData<Int> = _messageId

    var bleCentral: BleCentral = BleCentral(app, this)

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                bleCentral.scan()
            }
        }
    }

    fun onBluetoothResult() {
    }

    override fun onInitializeBleFailed() {
        _messageId.postValue(R.string.msg_scan_failed)
    }

    override fun onScanFailed(errorCode: Int) {
        _messageId.postValue(R.string.msg_scan_failed)
    }

    override fun onWriteCharacteristicFailed() {
        _messageId.postValue(R.string.msg_scan_failed)
    }

    override fun onScanSuccess() {
        _messageId.postValue(R.string.msg_scan_success)
    }

    override fun onServicesDiscovered() {
        _messageId.postValue(R.string.msg_service_discovered)
    }

    override fun onWriteRED() {
        _messageId.postValue(R.string.msg_send_red)
    }

    override fun onWriteGREEN() {
        _messageId.postValue(R.string.msg_send_green)
    }

    override fun onDisconnected() {
        _messageId.postValue(R.string.msg_disconnect)
    }
}