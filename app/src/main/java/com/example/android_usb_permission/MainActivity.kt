package com.example.android_usb_permission

import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.android_usb_permission.ui.theme.Android_usb_permissionTheme

class MainActivity : ComponentActivity() {
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intentFilter.addAction(ACTION_USB_PERMISSION)
        return intentFilter
    }

    private val mGattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> findUSB()
                ACTION_USB_PERMISSION -> {
                    val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

                    synchronized(this) {
                        var hasPermission = false
                        if (usbDevice != null) {
                            if (usbManager.hasPermission(usbDevice)) {
                                hasPermission = true
                                //TODO setting your device
                            }
                        }
                        if (!hasPermission) {
                            Toast.makeText(
                                context,
                                "Permission denied for device",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        findUSB()
        setContent {
            Android_usb_permissionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(mGattUpdateReceiver)
        super.onDestroy()
    }

    var usbDevice: UsbDevice? = null
    fun findUSB() {

        val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceIterator: Iterator<UsbDevice> = usbManager.deviceList.values.iterator()

        while (deviceIterator.hasNext()) {
            val device: UsbDevice = deviceIterator.next()
            //decimal
            if (device.vendorId == 1234 && device.productId == 5678) {
                usbDevice = device
            }
        }

        if (usbDevice != null) {
            manifestIntentFilterSetting(true)

            val permissionIntent =
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(ACTION_USB_PERMISSION),
                    PendingIntent.FLAG_IMMUTABLE
                )
            usbManager.requestPermission(usbDevice, permissionIntent)
        }
    }

    fun manifestIntentFilterSetting(boolean: Boolean) {
        val pm = applicationContext.packageManager
        val compName = ComponentName(packageName, "$packageName.MainAliasActivity")
        pm.setComponentEnabledSetting(
            compName,
            if (boolean) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP
        )
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Android_usb_permissionTheme {
        Greeting("Android")
    }
}