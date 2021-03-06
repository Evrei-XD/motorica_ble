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

package com.skydoves.waterdays.ui.activities.intro

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.skydoves.waterdays.R
import com.skydoves.waterdays.WDApplication
import com.skydoves.waterdays.ble.ConstantManager
import com.skydoves.waterdays.compose.BaseView
import com.skydoves.waterdays.persistence.preference.PreferenceKeys
import com.skydoves.waterdays.persistence.preference.PreferenceManager
import com.skydoves.waterdays.persistence.sqlite.SqliteManager
import com.skydoves.waterdays.ui.activities.main.MainActivity
import com.skydoves.waterdays.ui.fragments.intro.SlideFragment
import javax.inject.Inject


@Suppress("NAME_SHADOWING")
class StartActivity : AppIntro(), BaseView {
  val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
  val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"

  private var mDeviceName: String? = null
  private var mDeviceAddress: String? = null
  @Inject
  lateinit var preferenceManager: PreferenceManager
  @Inject
  lateinit var sqliteManager: SqliteManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WDApplication.component.inject(this)

    val intent = intent
    mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME)
    mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS)
    preferenceManager.putBoolean(PreferenceKeys.NEWBE.first, false)//для выключения интро при последующем запуске

    if (!preferenceManager.getBoolean(PreferenceKeys.NEWBE.first, PreferenceKeys.NEWBE.second)) {
      val intent = Intent(this, MainActivity::class.java)
      intent.putExtra(ConstantManager.EXTRAS_DEVICE_NAME, mDeviceName)
      intent.putExtra(ConstantManager.EXTRAS_DEVICE_ADDRESS, mDeviceAddress)
      startActivity(intent)
      finish()
      return
    }

    initializeUI()
  }

  override fun initializeUI() {
    addSlide(SlideFragment.newInstance(R.layout.intro1))
    addSlide(SlideFragment.newInstance(R.layout.intro2))
    addSlide(SlideFragment.newInstance(R.layout.intro3))
    addSlide(SlideFragment.newInstance(R.layout.intro4))
    setDoneText(getString(R.string.start))
  }

  override fun onSkipPressed(currentFragment: Fragment?) {
    super.onSkipPressed(currentFragment)
    activityStart()
  }

  override fun onDonePressed(currentFragment: Fragment?) {
    super.onDonePressed(currentFragment)
    activityStart()
  }

  private fun activityStart() {
//    startActivity(Intent(this, SetWeightActivity::class.java))//было
    val intent = Intent(this, MainActivity::class.java)
    intent.putExtra(ConstantManager.EXTRAS_DEVICE_NAME, mDeviceName)
    intent.putExtra(ConstantManager.EXTRAS_DEVICE_ADDRESS, mDeviceAddress)
    startActivity(intent)
    preferenceManager.putBoolean(PreferenceKeys.NEWBE.first, false)//для выключения интро при последующем запуске
    finish()
  }
}
