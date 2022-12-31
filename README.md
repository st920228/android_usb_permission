# android_usb_permission
 
這是簡單的USB權限要求小程式

程式內容不包含USB溝通傳遞的功能

網路上找了很多關於USB權限的文章

android的USB權限主要分成手動黨跟自動黨

手動黨是透過以下程式碼動態請求權限

            val permissionIntent =
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(ACTION_USB_PERMISSION),
                    PendingIntent.FLAG_IMMUTABLE
                )
            usbManager.requestPermission(usbDevice, permissionIntent)
            
自動黨則是在manifest中新增intent-filter

            <intent-filter>
                <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
            </intent-filter>
            <meta-data
                android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
                android:resource="@xml/device_filter" />
                
resouce中填寫以下內容

            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <!--decimal-->
                <usb-device vendor-id="1234" product-id="5678" />
            </resources>

問題是一般的手動黨只要裝置重新插拔或重開APP就要重新請求權限

要永久記錄權限必須透過自動黨的intent-filter跟activity做綁定

但綁定後會導致APP剛下載還沒開啟時一插入USB裝置就跳視窗詢問是否開啟APP控制裝置

這時就可以透過將intent-filter移到activity-alias並設android:enabled="false"來解決

等到需要請求權限時再用以下程式碼開啟intent-filter

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
        
這邊要注意的是開啟後再關閉會遺忘USB權限，之後要重問一次

最後APP要避免被重複開啟創建

記得要在manifest的activity中加android:launchMode="singleTop"

這需求可能蠻冷門的

只是我找了一個禮拜資料才找到這種實現方式

紀錄一下順便希望能幫到有同樣需求的人

取之於網用之於網
