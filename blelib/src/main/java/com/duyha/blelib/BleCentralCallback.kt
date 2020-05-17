package com.duyha.blelib

/**
 * Callback events of BleCentral
 *
 */
interface BleCentralCallback {

    /**
     * Callback indicating when initialize the ble central failed
     *
     * @param errorCode Error code [BleCentral.INITIALIZE_FAILED_PERMISSION_NOT_GRANTED]}
     * or {@link [BleCentral.INITIALIZE_FAILED_BLUETOOTH_NOT_ENABLED].
     */
    fun onInitializeBleFailed(errorCode: Int)

    /**
     * Callback indicating when scan failed
     */
    fun onScanFailed()

    /**
     * Callback indicating when scan successfully
     */
    fun onScanSuccess()

    /**
     * Callback indicating when the service has been discovered
     */
    fun onServicesDiscovered()

    /**
     * Callback indicating when writing RED
     */
    fun onWriteRED()

    /**
     * Callback indicating when writing GREEN
     */
    fun onWriteGREEN()

    /**
     * Callback indicating when disconnected service
     */
    fun onDisconnected()
}