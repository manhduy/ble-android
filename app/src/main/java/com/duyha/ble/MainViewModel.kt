package com.duyha.ble

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.duyha.blelib.BleCentral
import com.duyha.blelib.BleCentralCallback
import kotlinx.coroutines.*

class MainViewModel(app: Application) : AndroidViewModel(app), BleCentralCallback {

    private val _messageId = MutableLiveData<Int>()
    val messageId: LiveData<Int>
        get() = _messageId

    private val _blePerNotGranted = MutableLiveData<Event<Boolean>>()
    val blePerNotGranted: LiveData<Event<Boolean>>
        get() = _blePerNotGranted

    private val _bleNotEnabled = MutableLiveData<Event<Boolean>>()
    val bleNotEnabled: LiveData<Event<Boolean>>
        get() = _bleNotEnabled

    var bleCentral: BleCentral = BleCentral(app, this)

    init {
        startBleCentral()
    }

    fun startBleCentral() {
        _messageId.postValue(R.string.msg_scanning)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                bleCentral.start()
            }
        }
    }

    fun stopBleCentral() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                bleCentral.stop()
            }
        }
    }

    override fun onInitializeBleFailed(errorCode: Int) {
        _messageId.postValue(R.string.msg_scan_failed)
        if (errorCode == BleCentral.INITIALIZE_FAILED_PERMISSION_NOT_GRANTED) {
            _blePerNotGranted.postValue(Event(true))
        } else {
            _bleNotEnabled.postValue(Event(true))
        }
    }

    override fun onScanFailed() {
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