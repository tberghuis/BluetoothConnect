package com.example.bluetoothconnect

import android.bluetooth.*
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class KeyboardActivity : AppCompatActivity() {
    lateinit var bthid : BluetoothHidDevice
    val keyCode2KeyCode = KeyCode2KeyCode()
    val keyboardReport = KeyboardReport()
    val keyboardReport2 = KeyboardReport2()
    lateinit var btAdapter: BluetoothAdapter
    lateinit var TARGET_DEVICE_NAME: String
    var isCapsLockPressed = false
    var isAltPressed = false
    var isCtrlPressed = false
    var isShiftPressed = false
    var isShiftPressedWithOthers = false
    var isWindowPressed = false
    var isAltRepeat = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyboard)

        val intent = intent
        val name = intent.getSerializableExtra("name") as ArrayList<String>?
        TARGET_DEVICE_NAME = name!![0]
        val screenSize = name[1]

        // use BluetoothProfile.ServiceListener.onServiceConneted to get BluetoothHidDevice proxy object, no matter if bluetooth is connected or not
        //BluetoothHidDevice.Callback can listen bluetooth connection state change, pass it to BluetoothHidDevice.registerApp
        // after registerApp, once bluetooth connection is changed, callback will be called,
        // get BluetoothHidDevice from BluetoothProfile.ServiceListener, get BluetoothDevice from registerApp
        // send message need BluetoothHidDevice and BluetoothDevice with sendReport
        // registerApp contain sdp_record, there's keyboard report id in sdp_record's last parameter descriptor

        btAdapter = BluetoothAdapter.getDefaultAdapter()

        btAdapter.getProfileProxy(this,
                object : BluetoothProfile.ServiceListener{
                    override fun onServiceDisconnected(profile: Int) {
                        println("--- Disconnect, profile is $profile")
                    }
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                        println("--- connected, profile is $profile")
                        if (profile != BluetoothProfile.HID_DEVICE) {
                            return
                        }
                        // get bthid
                        bthid = proxy as BluetoothHidDevice
                        println("--- got hid proxy object ")
                        val btcallback = BluetoothCallback(this@KeyboardActivity,bthid, btAdapter,TARGET_DEVICE_NAME)
                        bthid.registerApp(sdpRecord, null, qosOut, {it.run()}, btcallback)
//                            bthid.registerApp(
//                                    Constants.SDP_RECORD, null, Constants.QOS_OUT, Executor { obj: Runnable -> obj.run() }, btcallback
//                            )
                    }
                }
                , BluetoothProfile.HID_DEVICE)

        fun sendAllKeyUp(){
            keyboardReport2.bytes.fill(0)
            if (!bthid.sendReport(ConnectedDevice.device, KeyboardReport2.ID, keyboardReport2.bytes)) {
                Log.e(" ", "Report wasn't sent")
            }
        }

//        fun sendKey(keyCode: Int){
//            var key = 0
//            if (keyCode2KeyCode.regularPhysicsKey.containsKey(keyCode)){
//                key = keyCode2KeyCode.keyMap.getOrDefault(keyCode2KeyCode.regularPhysicsKey.getOrDefault(keyCode,','),0)
//            }
//            if (keyCode2KeyCode.specialPhysicsKey.containsKey(keyCode)){
//                key = keyCode2KeyCode.scancode.getOrDefault(keyCode2KeyCode.specialPhysicsKey.getOrDefault(keyCode,"Space"),0)
//            }
//            keyboardReport2.key1=key.toByte()
//            bthid.sendReport(
//                ConnectedDevice.device,KeyboardReport2.ID,keyboardReport2.bytes
//            )
//
//            sendKeyUp()
//        }

        fun sendKey2(keyCode: Int){
            val key = KeyboardReport2.KeyEventMap.get(keyCode)
//            if (key != null){
//                val data = keyboardReport.setValue(0,key,0,0,0,0,0)
//                bthid.sendReport(
//                        ConnectedDevice.device,KeyboardReport2.ID,data
//                )
//                sendKeyUp()
//            }

            if (key != null){
                keyboardReport2.key1=key.toByte()
                bthid.sendReport(
                        ConnectedDevice.device,KeyboardReport2.ID,keyboardReport2.bytes
                )
                sendAllKeyUp()
            }
        }

        fun sendKeyDown3(keyCode: Int){
            if (isShiftPressed) {
                keyboardReport2.leftShift = true
                isShiftPressedWithOthers = true
            }
            if (isCtrlPressed) keyboardReport2.leftControl = true
            if (isAltPressed) keyboardReport2.leftAlt = true
            if (isWindowPressed) keyboardReport2.leftGui = true
            if (isCapsLockPressed) keyboardReport2.leftControl = true

            var key = 0

            // replace ALt Space to Alt Tab
            if (keyCode == KeyEvent.KEYCODE_SPACE && isAltPressed){
                key = KeyboardReport2.KeyEventMap.get(KeyEvent.KEYCODE_TAB) ?: 0
            }
            else{
                // if keyCode is 0, then key will be 0
                key = KeyboardReport2.KeyEventMap.get(keyCode) ?: 0
            }

            keyboardReport2.key1=key.toByte()
            bthid.sendReport(
                    ConnectedDevice.device,KeyboardReport2.ID,keyboardReport2.bytes
            )

            keyboardReport2.leftShift = false
            keyboardReport2.leftControl = false
//            keyboardReport2.leftAlt = false
            keyboardReport2.leftGui = false
//            isShiftPressed = false
////            isShiftPressedWithOthers = false
//            isCtrlPressed = false
//            isAltPressed =false
//            isWindowPressed = false
//            isCapsLockPressed = false

        }

        fun sendKeyUp3(keyCode: Int){
            var key = KeyboardReport2.KeyEventMap.get(keyCode) ?: 0
            // scan code is press code, press code + 0x80 will be release code
            key = key + 0x80
            keyboardReport2.key1=key.toByte()
            bthid.sendReport(
                    ConnectedDevice.device,KeyboardReport2.ID,keyboardReport2.bytes
            )
        }

        fun keyboardReportSendKey2(keyCode:Int, modifierKey: String){
            if (modifierKey == "Shift") keyboardReport2.leftShift = true
            if (modifierKey == "Ctrl") keyboardReport2.leftControl = true
            if (modifierKey == "Alt") keyboardReport2.leftAlt = true
            if (modifierKey == "Window") keyboardReport2.leftGui = true
            var key = 0
            // if keyCode is 0, then key will be 0
            key = KeyboardReport2.KeyEventMap.get(keyCode) ?: 0
            keyboardReport2.key1=key.toByte()
            bthid.sendReport(
                    ConnectedDevice.device,KeyboardReport2.ID,keyboardReport2.bytes
            )

            sendAllKeyUp()

            keyboardReport2.leftShift = false
            keyboardReport2.leftControl = false
            keyboardReport2.leftAlt = false
            keyboardReport2.leftGui = false
        }


        fun sendKeyDown(keyCode:Int){
            if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) keyboardReport2.leftAlt = true
            if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) keyboardReport2.leftControl = true
            if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) keyboardReport2.leftShift = true
            if (keyCode == KeyEvent.KEYCODE_WINDOW)  keyboardReport2.leftGui = true
            if (keyCode == KeyEvent.KEYCODE_CAPS_LOCK) keyboardReport2.leftControl = true

//            keyboardReport2.key1=0.toByte()
            bthid.sendReport(
                    ConnectedDevice.device,KeyboardReport2.ID,keyboardReport2.bytes
            )
        }

//        fun keyboardReportSendKey2(keyCode:Int, modifierKey: String){
//            if (modifierKey == "Shift") keyboardReport2.leftShift = true
//            if (modifierKey == "Ctrl") keyboardReport2.leftControl = true
//            if (modifierKey == "Alt") keyboardReport2.leftAlt = true
//            if (modifierKey == "Window") keyboardReport2.leftGui = true
//            var key = 0
//            // if keyCode is 0, then key will be 0
//            if (keyCode2KeyCode.regularPhysicsKey.containsKey(keyCode)){
//                key = keyCode2KeyCode.keyMap.getOrDefault(keyCode2KeyCode.regularPhysicsKey.getOrDefault(keyCode,','),0)
//            }
//            if (keyCode2KeyCode.specialPhysicsKey.containsKey(keyCode)){
//                key = keyCode2KeyCode.scancode.getOrDefault(keyCode2KeyCode.specialPhysicsKey.getOrDefault(keyCode,"Space"),0)
//            }
//            keyboardReport2.key1=key.toByte()
//            bthid.sendReport(
//                ConnectedDevice.device,KeyboardReport2.ID,keyboardReport2.bytes
//            )
//
//            sendKeyUp()
//
//            keyboardReport2.leftShift = false
//            keyboardReport2.leftControl = false
//            keyboardReport2.leftAlt = false
//            keyboardReport2.leftGui = false
//        }

//        fun keyboardReportSendKey(keyCode:Int, modifierKey: String){
//            var key = 0
//            if (keyCode2KeyCode.regularPhysicsKey.containsKey(keyCode)){
//                key = keyCode2KeyCode.keyMap.getOrDefault(keyCode2KeyCode.regularPhysicsKey.getOrDefault(keyCode,','),0)
//            }
//            if (keyCode2KeyCode.specialPhysicsKey.containsKey(keyCode)){
//                key = keyCode2KeyCode.scancode.getOrDefault(keyCode2KeyCode.specialPhysicsKey.getOrDefault(keyCode,"Space"),0)
//            }
//
//            var modifier = 0
//            if (modifierKey == "Ctrl") modifier = 1
//            if (modifierKey == "Shift") modifier = 2
//            if (modifierKey == "Alt") modifier = 4
//            if (modifierKey == "Window") modifier = 8
//            val data = keyboardReport.setValue(modifier,key,0,0,0,0,0)
//            bthid.sendReport(
//                ConnectedDevice.device,KeyboardReport2.ID,data
//            )
//            sendKeyUp()
//        }

        var latestSentTime = System.currentTimeMillis()

        // there should only one view in the layout.xml, otherwise, tab key would jump on other view, and tab key captured by android itself, not will send tab to other OS
        val editTextView = findViewById<EditText>(R.id.edit_text)
        editTextView?.requestFocus()

        editTextView?.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent? ->

            // windows key is search key in android, but there is no keycode for window key or fn key by otg wired keyboard input
//            if (event!!.isMetaPressed()){
//                val pressedTime = System.currentTimeMillis()
//                if ((pressedTime - latestSentTime) > 160L) {
//                    // shift space, it will send twice key event, reduce one
//                    keyboardReportSendKey(keyCode,"Window")
//                    println("keycode is $keyCode, with Window ")
//                    latestSentTime = pressedTime
//                }
//                return@setOnKeyListener true
//            }


            // switch window, normal behavior, alt key down, tab key down, tab key up with alt key down

            if (event?.action == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_CAPS_LOCK) { isCapsLockPressed = false }
                if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) { isAltPressed = false }
                if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) { isCtrlPressed = false }
                if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                   if (!isShiftPressedWithOthers) {keyboardReportSendKey2(0, "Shift")}
                    isShiftPressed = false
//                    if (!isShiftPressedWithOthers) {sendKeyDown3(0)}
//                    isShiftPressed = false
                }

                // alt tab switch window is alt key down, tab key down, tab up, alt up
               if (keyCode == KeyEvent.KEYCODE_SPACE && isAltPressed) {
//                   var key = KeyboardReport2.KeyEventMap.get(KeyEvent.KEYCODE_TAB) ?: 0
//                   key = key + 0x80
//                   keyboardReport2.key1=key.toByte()
//                   bthid.sendReport(
//                           ConnectedDevice.device,KeyboardReport2.ID,keyboardReport2.bytes
//                   )
                   sendKeyUp3(KeyEvent.KEYCODE_TAB)

               }
                else {
                   sendAllKeyUp()
                   if (isAltPressed) sendKeyDown(KeyEvent.KEYCODE_ALT_LEFT)
                   if (isCapsLockPressed) sendKeyDown(KeyEvent.KEYCODE_CTRL_LEFT)
                   if (isCtrlPressed) sendKeyDown(KeyEvent.KEYCODE_CTRL_LEFT)
               }
            }

            if (event?.action == KeyEvent.ACTION_DOWN) {
               if (bthid.connectedDevices != null){
//                   println("--- keycode is $keyCode")
                   if (keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
                       isCapsLockPressed = true
                       sendKeyDown(keyCode)
                       if (!isCapsLockPressed) sendKeyDown(keyCode)
                   }
                   if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
                       isAltPressed = true
                       // alt tab swith window, alt down, tab down, tab up, alt up, keey alt down
                       // alt key down
                       sendKeyDown(keyCode)
                       // hold alt, only send one alt key down
                       if (!isAltPressed) sendKeyDown(keyCode)
                   }
                   if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT){
                       isCtrlPressed = true
                       sendKeyDown(keyCode)
                       if (!isCtrlPressed) sendKeyDown(keyCode)
                   }
                   if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT){
                       // shift pressed and hold only send one shift key event when released for shift switch input method
                       isShiftPressed = true ;
                       isShiftPressedWithOthers = false
                   }

                   if (!listOf<Int>(KeyEvent.KEYCODE_CAPS_LOCK, KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT,
                           KeyEvent.KEYCODE_CTRL_LEFT,KeyEvent.KEYCODE_CTRL_RIGHT,KeyEvent.KEYCODE_SHIFT_LEFT,KeyEvent.KEYCODE_SHIFT_RIGHT).contains(keyCode))
                           sendKeyDown3(keyCode)
               }
                else {
                   btAdapter.getProfileProxy(this,
                       object : BluetoothProfile.ServiceListener{
                           override fun onServiceDisconnected(profile: Int) {
                               println("--- Disconnect, profile is $profile")
                           }
                           override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                               println("--- connected, profile is $profile")
                               if (profile == BluetoothProfile.HID_DEVICE) {
                                   // get bthid
                                   bthid = proxy as BluetoothHidDevice
                                   println("--- got hid proxy object ")
                                   val btcallback = BluetoothCallback(this@KeyboardActivity,bthid, btAdapter,TARGET_DEVICE_NAME)
                                   bthid.registerApp(sdpRecord, null, qosOut, {it.run()}, btcallback)
//                            bthid.registerApp(
//                                    Constants.SDP_RECORD, null, Constants.QOS_OUT, Executor { obj: Runnable -> obj.run() }, btcallback
//                            )
                               }
                           }
                       }
                       , BluetoothProfile.HID_DEVICE)

               }
            }
            return@setOnKeyListener false
        }





//        findViewById<Button>(R.id.button1).setOnClickListener{

            //KeyboardReport2 is faster than KeyboardReport

//            val keyboardReport = KeyboardReport()
//            val data = keyboardReport.setValue(0,4,0,0,0,0,0)
//            bthid.sendReport(
//                    ConnectedDevice.device,KeyboardReport2.ID,data
//            )
//            val data2 = keyboardReport.setValue(0,0,0,0,0,0,0)
//            if (!bthid.sendReport(ConnectedDevice.device, KeyboardReport2.ID, data2)) {
//                Log.e(" ", "Report wasn't sent")
//            }
//        }

    }

    override fun onRestart() {
        super.onRestart()
        btAdapter.getProfileProxy(this,
                object : BluetoothProfile.ServiceListener{
                    override fun onServiceDisconnected(profile: Int) {
                        println("--- Disconnect, profile is $profile")
                    }
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                        println("--- connected, profile is $profile")
                        if (profile == BluetoothProfile.HID_DEVICE) {
                            // get bthid
                            bthid = proxy as BluetoothHidDevice
                            println("--- got hid proxy object ")
                            val btcallback = BluetoothCallback(this@KeyboardActivity,bthid, btAdapter,TARGET_DEVICE_NAME)
                            bthid.registerApp(sdpRecord, null, qosOut, {it.run()}, btcallback)
//                            bthid.registerApp(
//                                    Constants.SDP_RECORD, null, Constants.QOS_OUT, Executor { obj: Runnable -> obj.run() }, btcallback
//                            )
                        }
                    }
                }
                , BluetoothProfile.HID_DEVICE)
    }

    private val sdpRecord by lazy {
        BluetoothHidDeviceAppSdpSettings(
            "Pixel HID1",
            "Mobile BController",
            "bla",
            BluetoothHidDevice.SUBCLASS1_COMBO,
                DescriptorCollection.MOUSE_KEYBOARD_COMBO
        )
    }

    private val qosOut by lazy {
        BluetoothHidDeviceAppQosSettings(
                BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
                800,
                9,
                0,
                11250,
                BluetoothHidDeviceAppQosSettings.MAX
        )
    }
}
