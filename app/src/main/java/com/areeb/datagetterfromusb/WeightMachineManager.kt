package com.areeb.datagetterfromusb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.util.Log

class WeightMachineManager(private val context: Context) {
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var usbDevice: UsbDevice? = null
    private var usbInterface: UsbInterface? = null
    private var usbEndpoint: UsbEndpoint? = null
    private var usbDeviceConnection: UsbDeviceConnection? = null

    private val usbPermissionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    val granted: Boolean =
                        intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (granted) {
                        device?.let {
                            openConnection(it)
                        }
                    }
                }
            }
        }
    }

    fun startListening() {
        val permissionIntent =
            PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        context.registerReceiver(usbPermissionReceiver, filter)

        val usbDevices: HashMap<String, UsbDevice>? = usbManager.deviceList
        usbDevices?.forEach { (_, device) ->
            if (isWeightMachine(device)) {
                if (usbManager.hasPermission(device)) {
                    openConnection(device)
                } else {
                    usbManager.requestPermission(device, permissionIntent)
                }
            }
        }
    }

    private fun openConnection(device: UsbDevice) {
        usbDevice = device
        usbInterface = device.getInterface(0)
        usbEndpoint = usbInterface!!.getEndpoint(0)
        usbDeviceConnection = usbManager.openDevice(device)
        // Start reading weight data from the USB endpoint
        readWeightData()
    }

    private fun readWeightData() {
        val buffer = ByteArray(1024)
        while (true) {
            Log.e("usb_end_point", usbDevice.toString())
            val bytesRead =
                usbDeviceConnection?.bulkTransfer(usbEndpoint, buffer, buffer.size, TIMEOUT)
            if (bytesRead != null && bytesRead > 0) {
                val weightData = String(buffer, 0, bytesRead)
                // Process the weight data as needed
                Log.e(TAG, "Received weight data: $weightData")
            } else {
                Log.e(TAG, "byte data is empty")
            }
        }
    }

    private fun isWeightMachine(device: UsbDevice): Boolean {
        // Implement your own logic to determine if the device is your weight machine
        // For example, you can check the device's vendor and product IDs
        Log.e("vendor_id", device.vendorId.toString())
        Log.e("product_id", device.productId.toString())
        return device.vendorId == YOUR_WEIGHT_MACHINE_VENDOR_ID && device.productId == YOUR_WEIGHT_MACHINE_PRODUCT_ID
    }

    fun stopListening() {
        context.unregisterReceiver(usbPermissionReceiver)
        closeConnection()
    }

    private fun closeConnection() {
        usbDeviceConnection?.close()
        usbDeviceConnection = null
        usbEndpoint = null
        usbInterface = null
        usbDevice = null
    }

    companion object {
        private const val TAG = "WeightMachineManager"
        private const val ACTION_USB_PERMISSION = "com.areeb.datagetterfromusb.USB_PERMISSION"
        private const val TIMEOUT = 1000 // Timeout in milliseconds
        private const val YOUR_WEIGHT_MACHINE_VENDOR_ID =
            3768 // Replace with your weight machine's vendor ID
        private const val YOUR_WEIGHT_MACHINE_PRODUCT_ID = 61440
        // Replace with your weight machine's product ID
    }
}
