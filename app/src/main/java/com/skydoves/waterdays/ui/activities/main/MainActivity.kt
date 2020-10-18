/*
 * Copyright (C) 2016 skydoves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.waterdays.ui.activities.main

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ExpandableListView
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.SimpleExpandableListAdapter
import com.skydoves.waterdays.R
import com.skydoves.waterdays.ble.BluetoothLeService
import com.skydoves.waterdays.ble.ConstantManager
import com.skydoves.waterdays.ble.SampleGattAttributes
import com.skydoves.waterdays.ble.SampleGattAttributes.CLOSE_MOTOR_HDLE
import com.skydoves.waterdays.compose.BaseActivity
import com.skydoves.waterdays.compose.qualifiers.RequirePresenter
import com.skydoves.waterdays.consts.IntentExtras
import com.skydoves.waterdays.events.rx.RxUpdateMainEvent
import com.skydoves.waterdays.presenters.MainPresenter
import com.skydoves.waterdays.services.receivers.AlarmBootReceiver
import com.skydoves.waterdays.services.receivers.LocalWeatherReceiver
import com.skydoves.waterdays.ui.adapters.SectionsPagerAdapter
import com.skydoves.waterdays.utils.NavigationUtils
import com.skydoves.waterdays.viewTypes.MainActivityView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_chart.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by skydoves on 2016-10-15.
 * Updated by skydoves on 2017-08-17.
 * Copyright (c) 2017 skydoves rights reserved.
 */

@RequirePresenter(MainPresenter::class)
class MainActivity : BaseActivity<MainPresenter, MainActivityView>(), MainActivityView {

  private var nAdapter: NfcAdapter? = null
  private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
  private var mDeviceName: String? = null
  private var mDeviceAddress: String? = null
  private var mBluetoothLeService: BluetoothLeService? = null
  private var mGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()
  private var mGattServicesList: ExpandableListView? = null
  private var mConnectView: View? = null
  private var mDisconnectView: View? = null
  private var mConnected = false
  private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
  private var mCharacteristic: BluetoothGattCharacteristic? = null


  private val LIST_NAME = "NAME"
  private val LIST_UUID = "UUID"
  private val myBuffer = byteArrayOf(0x00, 0x00)
  private val lockServiceSettings = false

  // Code to manage Service lifecycle.
  private val mServiceConnection: ServiceConnection = object : ServiceConnection {
    override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
      mBluetoothLeService = (service as BluetoothLeService.LocalBinder).getService()
      if (!mBluetoothLeService?.initialize()!!) {
        Timber.e("Unable to initialize Bluetooth")
        finish()
      }
      // Automatically connects to the device upon successful start-up initialization.
      mBluetoothLeService?.connect(mDeviceAddress)
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
      mBluetoothLeService = null
    }
  }

  // Handles various events fired by the Service.
  // ACTION_GATT_CONNECTED: connected to a GATT server.
  // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
  // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
  // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
  //                        or notification operations.
  private val mGattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action
      if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
        //connected state
        mConnected = true
        mConnectView!!.visibility = View.VISIBLE
        mDisconnectView!!.visibility = View.GONE
        System.err.println("DeviceControlActivity-------> момент индикации коннекта")
        invalidateOptionsMenu()
      } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
        //disconnected state
        mConnected = false
        mConnectView!!.visibility = View.GONE
        mDisconnectView!!.visibility = View.VISIBLE
        System.err.println("DeviceControlActivity-------> момент индикации коннекта")
        invalidateOptionsMenu()
        clearUI()
      } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
        // Show all the supported services and characteristics on the user interface.
        displayGattServices(mBluetoothLeService!!.supportedGattServices)
      } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//        displayData(intent.getByteArrayExtra(BluetoothLeService.MIO_DATA)) //вывод на график данных из характеристики показаний пульса
      }
    }
  }

  // If a given GATT characteristic is selected, check for supported features.  This sample
  // demonstrates 'Read' 'Write' and 'Notify' features.  See
  // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
  // list of supported characteristic features.
  private val servicesListClickListener = OnChildClickListener { parent, v, groupPosition, childPosition, id ->
    if (mGattCharacteristics != null) {
      mCharacteristic = mGattCharacteristics[groupPosition][childPosition]
      System.err.println("groupPosition=$groupPosition")
      System.err.println("childPosition=$childPosition")
      System.err.println("mCharacteristic=$mCharacteristic")
      val charaProp: Int = mCharacteristic?.getProperties()!!
      var properties = ""
      if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
        if (mNotifyCharacteristic != null) {
          mBluetoothLeService!!.setCharacteristicNotification(
                  mNotifyCharacteristic, false)
          mNotifyCharacteristic = null
        }
        mBluetoothLeService!!.readCharacteristic(mCharacteristic)
        properties = properties + "R " //= properties+"R ";
      }
      if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
        properties = properties + "W "
      }
      if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
        mNotifyCharacteristic = mCharacteristic
        mBluetoothLeService!!.setCharacteristicNotification(
                mCharacteristic, true)
        properties = properties + "N "
      }
      if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
        properties = properties + "I "
      }
      if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
        properties = properties + "WWR "
      }
      if (charaProp and BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE > 0) {
        properties = properties + "ASW "
      }
      if (charaProp and BluetoothGattCharacteristic.PROPERTY_BROADCAST > 0) {
        properties = properties + "BROAD "
      }
      System.err.println("uygwefyubhkcbqjwe" + properties)
      return@OnChildClickListener true
    }
    false
  }

  private fun clearUI() {
    mGattServicesList!!.setAdapter(null as SimpleExpandableListAdapter?)
    enableInterface(false)
  }

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    initBaseView(this)

    val intent = intent
    mDeviceName = intent.getStringExtra(ConstantManager.EXTRAS_DEVICE_NAME)
    mDeviceAddress = intent.getStringExtra(ConstantManager.EXTRAS_DEVICE_ADDRESS)

    // Sets up UI references.

    // Sets up UI references.
    mGattServicesList = findViewById(R.id.gatt_services_list)
    mGattServicesList?.setOnChildClickListener(servicesListClickListener)
    mConnectView = findViewById(R.id.connect_view)
    mDisconnectView = findViewById(R.id.disconnect_view)

    val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)


    nAdapter = NfcAdapter.getDefaultAdapter(this)
    getNFCData(intent)

    // auto weather alarm
    weatherAlarm()

    // set boot receiver
    val receiver = ComponentName(this, AlarmBootReceiver::class.java)
    val pm = packageManager
    pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)

    RxUpdateMainEvent.getInstance().observable
        .compose(bindToLifecycle<Boolean>())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { flag ->
//          if (!flag) showBadge(0)
          mSectionsPagerAdapter.notifyDataSetChanged()
        }
  }

  override fun initializeUI() {
    mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
    mainactivity_viewpager.adapter = mSectionsPagerAdapter
    mainactivity_viewpager.offscreenPageLimit = 5
  }


  private fun getNFCData(intent: Intent) {
    if (NfcAdapter.ACTION_NDEF_DISCOVERED == getIntent().action) {
      val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
      if (rawMsgs != null) {
        val messages = arrayOfNulls<NdefMessage>(rawMsgs.size)

        for (i in rawMsgs.indices)
          messages[i] = rawMsgs[i] as NdefMessage
        val payload = messages[0]!!.getRecords()[0].payload

        presenter.addRecord(String(payload))
        mSectionsPagerAdapter!!.notifyDataSetChanged()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    val filter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
    try {
      filter.addDataType("waterdays_nfc/*")
    } catch (e: Exception) {
      e.printStackTrace()
    }

    val i = Intent(this, javaClass)
    i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    val pIntent = PendingIntent.getActivity(this, 0, i, 0)

    val filters = arrayOf(filter)
//    nAdapter!!.enableForegroundDispatch(this, pIntent, filters, null)

    //BLE
    registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
    if (mBluetoothLeService != null) {
      val result = mBluetoothLeService!!.connect(mDeviceAddress)
    }
  }

  override fun onPause() {
    super.onPause()
    unregisterReceiver(mGattUpdateReceiver)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    getNFCData(getIntent())
  }

  private fun weatherAlarm() {
    if (!presenter.weatherAlarm) {
      val mCalendar = GregorianCalendar()
      val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
      alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, mCalendar.timeInMillis, (1200 * 1000).toLong(), pendingIntent(IntentExtras.ALARM_PENDING_REQUEST_CODE))
      presenter.weatherAlarm = true
    }
  }

  private fun pendingIntent(requestCode: Int): PendingIntent {
    val intent = Intent(this, LocalWeatherReceiver::class.java)
    intent.putExtra(IntentExtras.ALARM_PENDING_REQUEST, requestCode)
    return PendingIntent.getBroadcast(this, requestCode, intent, 0)
  }

  // Demonstrates how to iterate through the supported GATT Services/Characteristics.
  // In this sample, we populate the data structure that is bound to the ExpandableListView
  // on the UI.
  private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
    System.err.println("DeviceControlActivity-------> момент начала выстраивания списка параметров")
    if (gattServices == null) return
    var uuid: String? = null
    val unknownServiceString = resources.getString(R.string.unknown_service)
    val unknownCharaString = resources.getString(R.string.unknown_characteristic)
    val gattServiceData = ArrayList<HashMap<String, String?>>()
    val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String?>>>()
    mGattCharacteristics = java.util.ArrayList()


    // Loops through available GATT Services.
    for (gattService in gattServices) {
      val currentServiceData = HashMap<String, String?>()
      uuid = gattService.uuid.toString()
      currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownServiceString)
      currentServiceData[LIST_UUID] = uuid
      gattServiceData.add(currentServiceData)
      val gattCharacteristicGroupData = ArrayList<HashMap<String, String?>>()
      val gattCharacteristics = gattService.characteristics
      val charas = ArrayList<BluetoothGattCharacteristic>()

      // Loops through available Characteristics.
      for (gattCharacteristic in gattCharacteristics) {
        charas.add(gattCharacteristic)
        val currentCharaData = HashMap<String, String?>()
        uuid = gattCharacteristic.uuid.toString()
        currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownCharaString)
        currentCharaData[LIST_UUID] = uuid
        gattCharacteristicGroupData.add(currentCharaData)
      }
      mGattCharacteristics.add(charas)
      gattCharacteristicData.add(gattCharacteristicGroupData)
    }
    val gattServiceAdapter = SimpleExpandableListAdapter(
            this,
            gattServiceData,
            android.R.layout.simple_expandable_list_item_2, arrayOf(LIST_NAME, LIST_UUID), intArrayOf(android.R.id.text1, android.R.id.text2),
            gattCharacteristicData,
            android.R.layout.simple_expandable_list_item_2, arrayOf(LIST_NAME, LIST_UUID), intArrayOf(android.R.id.text1, android.R.id.text2))
    mGattServicesList!!.setAdapter(gattServiceAdapter)
    enableInterface(true)
  }
  private fun enableInterface(enabled: Boolean) {
    close_btn.isEnabled = enabled
    open_btn.isEnabled = enabled
//    mOpenBtn.setEnabled(enabled)
//    mCloseBtn.setEnabled(enabled)
//    mFirstByteSeekBar.setEnabled(enabled)
//    mSecondByteSeekBar.setEnabled(enabled)
  }

  fun DelaiGriaz (byteArray: ByteArray){
    for (i in mGattCharacteristics.indices) {
      for (j in mGattCharacteristics[i].indices) {
        if (mGattCharacteristics[i][j].uuid.toString() == CLOSE_MOTOR_HDLE) {
          mCharacteristic = mGattCharacteristics[i][j]
          if (mCharacteristic?.properties!! and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
            val massage = byteArray
            mCharacteristic?.value = massage
            mBluetoothLeService?.writeCharacteristic(mCharacteristic)
//              mBluetoothLeService?.readCharacteristic(mCharacteristic)
          }
        }
      }
    }
  }

  private fun makeGattUpdateIntentFilter(): IntentFilter? {
    val intentFilter = IntentFilter()
    intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
    intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
    intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
    intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
    return intentFilter
  }
}