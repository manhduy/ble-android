package com.duyha.blelib

interface BleCentralListener {
    fun onInitializeBleFailed()
    fun onScanFailed(errorCode: Int)
    fun onWriteCharacteristicFailed()
    fun onScanSuccess()
    fun onServicesDiscovered()
    fun onWriteRED()
    fun onWriteGREEN()
    fun onDisconnected()
}