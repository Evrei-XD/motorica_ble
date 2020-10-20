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

package com.skydoves.waterdays.ui.fragments.main

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.skydoves.waterdays.R
import com.skydoves.waterdays.WDApplication
import com.skydoves.waterdays.ble.ConstantManager
import com.skydoves.waterdays.ble.SampleGattAttributes.*
import com.skydoves.waterdays.persistence.sqlite.SqliteManager
import com.skydoves.waterdays.ui.activities.main.MainActivity
import com.skydoves.waterdays.utils.DateUtils
import kotlinx.android.synthetic.main.layout_chart.*
import javax.inject.Inject

/**
 * Created by skydoves on 2016-10-15.
 * Updated by skydoves on 2017-08-17.
 * Copyright (c) 2017 skydoves rights reserved.
 */

class ChartFragment : Fragment(), OnChartValueSelectedListener {

  @Inject
  lateinit var sqliteManager: SqliteManager

  private var rootView: View? = null
  private var dateCount = 0
  private var main: MainActivity? = null
  var graphThread: Thread? = null
  var graphThreadFlag = false
  private var plotData = false
  var objectAnimator: ObjectAnimator? = null
  var objectAnimator2: ObjectAnimator? = null


  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val rootView = inflater.inflate(R.layout.layout_chart, container, false)
    WDApplication.component.inject(this)
    this.rootView = rootView
    if (activity != null) { main = activity as MainActivity? }
    return rootView
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    // set dateCount
    dateCount = -DateUtils.getDateDay(DateUtils.getFarDay(0), DateUtils.dateFormat)
//    initializeChart(DateUtils.getDateDay("2020-10-16", DateUtils.dateFormat))//2020-10-14  DateUtils.getFarDay(0)

    ////////initialized graph
    initializedSensorGraph()


    val shutdownCurrentTv = rootView!!.findViewById(R.id.shutdown_current_tv) as TextView
    val startUpStepTv = rootView!!.findViewById(R.id.start_up_step_tv) as TextView
    val startUpTimeTv = rootView!!.findViewById(R.id.start_up_time_tv) as TextView
    val deadZoneTv = rootView!!.findViewById(R.id.dead_zone_tv) as TextView
    val sensitivityTv = rootView!!.findViewById(R.id.sensitivity_tv) as TextView
    val brakeMotorTv = rootView!!.findViewById(R.id.brake_motor_tv) as TextView
    val scale = resources.displayMetrics.density
    val limit_CH1 = rootView!!.findViewById(R.id.limit_CH1) as ImageView
//    val limit_CH2 = rootView!!.findViewById(R.id.limit_CH2) as ImageView


    close_btn.setOnTouchListener(OnTouchListener { v, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        main?.BleCommand(byteArrayOf(0x01, 0x00), CLOSE_MOTOR_HDLE, WRITE)
      }
      if (event.action == MotionEvent.ACTION_UP) {
        main?.BleCommand(byteArrayOf(0x00, 0x00), CLOSE_MOTOR_HDLE, WRITE)
      }
      false
    })
    open_btn.setOnTouchListener(OnTouchListener { v, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        main?.BleCommand(byteArrayOf(0x01, 0x00), OPEN_MOTOR_HDLE, WRITE)
      }
      if (event.action == MotionEvent.ACTION_UP) {
        main?.BleCommand(byteArrayOf(0x00, 0x00), OPEN_MOTOR_HDLE, WRITE)
      }
      false
    })
    shutdown_current_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        shutdownCurrentTv.text = seekBar.progress.toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.BleCommand(byteArrayOf(seekBar.progress.toByte()), SHUTDOWN_CURRENT_HDLE, WRITE)
      }
    })
    start_up_step_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        startUpStepTv.text = seekBar.progress.toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.BleCommand(byteArrayOf(seekBar.progress.toByte()), START_UP_STEP_HDLE, WRITE)
      }
    })
    start_up_time_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        startUpTimeTv.text = seekBar.progress.toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.BleCommand(byteArrayOf(seekBar.progress.toByte()), START_UP_TIME_HDLE, WRITE)
      }
    })
    dead_zone_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        deadZoneTv.text = (seekBar.progress + 30).toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.BleCommand(byteArrayOf((seekBar.progress + 30).toByte()), DEAD_ZONE_HDLE, WRITE)
      }
    })
    sensitivity_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        sensitivityTv.text = (seekBar.progress + 1).toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.BleCommand(byteArrayOf((seekBar.progress + 1).toByte()), SENSITIVITY_HDLE, WRITE)
      }
    })
    CH1_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//        sensitivityTv.text = (seekBar.progress + 1).toString()
        System.err.println("CH1" + seekBar.progress)
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
//        main?.DelaiGriaz(byteArrayOf((seekBar.progress + 1).toByte()), SENSITIVITY_HDLE)
        objectAnimator = ObjectAnimator.ofFloat(limit_CH1, "y", 300 * scale + 10f - (seekBar.progress * scale * 1.04f))
        objectAnimator?.duration = 200
        objectAnimator?.start()
      }
    })
    CH2_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//        sensitivityTv.text = (seekBar.progress + 1).toString()
        System.err.println("CH2" + seekBar.progress)
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
//        main?.DelaiGriaz(byteArrayOf((seekBar.progress + 1).toByte()), SENSITIVITY_HDLE)
      }
    })
    brake_motor_sb.setOnClickListener(View.OnClickListener {
      if (brake_motor_sb.isChecked) {
        brakeMotorTv.text = 1.toString()
        main?.BleCommand(byteArrayOf(0x01), BRAKE_MOTOR_HDLE, WRITE)
      } else {
        brakeMotorTv.text = 0.toString()
        main?.BleCommand(byteArrayOf(0x00), BRAKE_MOTOR_HDLE, WRITE)
      }
    })
  }

  override fun onResume() {
    super.onResume()
    System.err.println("ChartFragment onResume")
    graphThreadFlag = true
    startGraphEnteringDataThread()
  }

  override fun onPause() {
    super.onPause()
    graphThreadFlag = false
    System.err.println("ChartFragment onPause")
  }

  //////////////////////////////////////////////////////////////////////////////
  /**                          работа с графиками                            **/
  //////////////////////////////////////////////////////////////////////////////
  private fun createSet(): LineDataSet? {
    val set = LineDataSet(null, null)
    set.axisDependency = YAxis.AxisDependency.LEFT //.AxisDependency.LEFT
    set.lineWidth = 2f
    set.color = Color.rgb(255, 171, 0)
    set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    set.setCircleColor(Color.TRANSPARENT)
    set.setCircleColorHole(Color.TRANSPARENT)
    set.fillColor = ColorTemplate.getHoloBlue()
    set.highLightColor = Color.rgb(244, 117, 177)
    set.valueTextColor = Color.TRANSPARENT
    return set
  }
  private fun createSet2(): LineDataSet? {
    val set2 = LineDataSet(null, null)
    set2.axisDependency = YAxis.AxisDependency.LEFT //.AxisDependency.LEFT
    set2.lineWidth = 2f
    set2.color = Color.WHITE
    set2.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    set2.setCircleColor(Color.TRANSPARENT)
    set2.setCircleColorHole(Color.TRANSPARENT)
    set2.fillColor = ColorTemplate.getHoloBlue()
    set2.highLightColor = Color.rgb(244, 117, 177)
    set2.valueTextColor = Color.TRANSPARENT
    return set2
  }
  private fun addEntry(sens1: Int, sens2: Int) {
    val data: LineData = chart_mainchart?.data!!
    if (data != null) {
      var set = data.getDataSetByIndex(0)
      var set2 = data.getDataSetByIndex(1)
      if (set == null) {
        set = createSet()
        set2 = createSet2()
        data.addDataSet(set)
        data.addDataSet(set2)
      }

      data.addEntry(Entry(set!!.entryCount.toFloat(), sens1.toFloat()), 0)
      data.addEntry(Entry(set2!!.entryCount.toFloat(), sens2.toFloat()), 1)
      data.notifyDataChanged()
      chart_mainchart.notifyDataSetChanged()
      chart_mainchart.setVisibleXRangeMaximum(50f)
      chart_mainchart.moveViewToX(set.entryCount - 50.toFloat()) //data.getEntryCount()
    }
  }
  private fun initializedSensorGraph() {
    chart_mainchart.contentDescription
    chart_mainchart.setTouchEnabled(false)
    chart_mainchart.setDragEnabled(false)
    chart_mainchart.isDragDecelerationEnabled = false
    chart_mainchart.setScaleEnabled(false)
    chart_mainchart.setDrawGridBackground(false)
    chart_mainchart.setPinchZoom(false)
    chart_mainchart.setBackgroundColor(Color.TRANSPARENT)
    chart_mainchart.getHighlightByTouchPoint(1f, 1f)
    val data = LineData()
    val data2 = LineData()
    chart_mainchart.data = data
    chart_mainchart.data = data2
    chart_mainchart.legend.isEnabled = false
    chart_mainchart.description.textColor = Color.TRANSPARENT
    chart_mainchart.animateY(700)

    val x: XAxis = chart_mainchart.xAxis
    x.textColor = Color.TRANSPARENT
    x.setDrawGridLines(false)
    x.axisMaximum = 4000000f
    x.setAvoidFirstLastClipping(true)
    x.position = XAxis.XAxisPosition.BOTTOM

    val y: YAxis = chart_mainchart.axisLeft
    y.textColor = Color.WHITE
    y.mAxisMaximum = 255f
    y.mAxisMinimum = 0f
    y.textSize = 12f
    y.setDrawGridLines(true)
    y.setDrawAxisLine(false)
    y.setStartAtZero(true)
    y.gridColor = Color.WHITE
    chart_mainchart.axisRight.axisLineColor = Color.TRANSPARENT
    chart_mainchart.axisRight.textColor = Color.TRANSPARENT
  }

  private fun startGraphEnteringDataThread() {
    graphThread = Thread {
//      var i = 0
      while (graphThreadFlag) {
        main?.runOnUiThread(Runnable {
//          if (i == 0) {
            addEntry(0, 255)
//            i = 1
//          } else {
//            addEntry(100, 120)
//            i = 0
//          }
        })
        try {
          Thread.sleep(ConstantManager.GRAPH_UPDATE_DELAY.toLong())
        } catch (ignored: Exception) {
        }
      }
    }
    graphThread?.start()
  }

  override fun onValueSelected(e: Entry?, h: Highlight?) {
    TODO("Not yet implemented")
  }

  override fun onNothingSelected() {

  }
}
