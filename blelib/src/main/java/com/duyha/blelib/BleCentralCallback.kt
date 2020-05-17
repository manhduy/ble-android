package com.duyha.blelib

interface BleCentralCallback {
    fun onInitializeBleFailed(errorCode: Int)
    fun onScanFailed(errorCode: Int)
    fun onScanSuccess()
    fun onServicesDiscovered()
    fun onWriteRED()
    fun onWriteGREEN()
    fun onDisconnected()
}