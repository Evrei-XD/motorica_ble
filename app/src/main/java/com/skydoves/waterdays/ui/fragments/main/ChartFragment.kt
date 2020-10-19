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

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
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
import com.skydoves.waterdays.ui.customViews.MyMarkerView
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
  private var plotData = true


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
    initializedGraphForChannel1()
    graphThreadFlag = true
    startGraphEnteringDataThread()


    val shutdownCurrentTv = rootView!!.findViewById(R.id.shutdown_current_tv) as TextView
    val startUpStepTv = rootView!!.findViewById(R.id.start_up_step_tv) as TextView
    val startUpTimeTv = rootView!!.findViewById(R.id.start_up_time_tv) as TextView
    val deadZoneTv = rootView!!.findViewById(R.id.dead_zone_tv) as TextView
    val sensitivityTv = rootView!!.findViewById(R.id.sensitivity_tv) as TextView
    val brakeMotorTv = rootView!!.findViewById(R.id.brake_motor_tv) as TextView


    close_btn.setOnTouchListener(OnTouchListener { v, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        main?.DelaiGriaz(byteArrayOf(0x01, 0x00), CLOSE_MOTOR_HDLE)
      }
      if (event.action == MotionEvent.ACTION_UP) {
        main?.DelaiGriaz(byteArrayOf(0x00, 0x00), CLOSE_MOTOR_HDLE)
      }
      false
    })
    open_btn.setOnTouchListener(OnTouchListener { v, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        main?.DelaiGriaz(byteArrayOf(0x01, 0x00), OPEN_MOTOR_HDLE)
      }
      if (event.action == MotionEvent.ACTION_UP) {
        main?.DelaiGriaz(byteArrayOf(0x00, 0x00), OPEN_MOTOR_HDLE)
      }
      false
    })
    shutdown_current_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        shutdownCurrentTv.text = seekBar.progress.toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.DelaiGriaz(byteArrayOf(seekBar.progress.toByte()), SHUTDOWN_CURRENT_HDLE)
      }
    })
    start_up_step_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        startUpStepTv.text = seekBar.progress.toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.DelaiGriaz(byteArrayOf(seekBar.progress.toByte()), START_UP_STEP_HDLE)
      }
    })
    start_up_time_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        startUpTimeTv.text = seekBar.progress.toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.DelaiGriaz(byteArrayOf(seekBar.progress.toByte()), START_UP_TIME_HDLE)
      }
    })
    dead_zone_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        deadZoneTv.text = (seekBar.progress + 30).toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.DelaiGriaz(byteArrayOf((seekBar.progress + 30).toByte()), DEAD_ZONE_HDLE)
      }
    })
    sensitivity_sb.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        sensitivityTv.text = (seekBar.progress + 1).toString()
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {
        main?.DelaiGriaz(byteArrayOf((seekBar.progress + 1).toByte()), SENSITIVITY_HDLE)
      }
    })
    brake_motor_sb.setOnClickListener(View.OnClickListener {
      if (brake_motor_sb.isChecked) {
        brakeMotorTv.text = 1.toString()
        main?.DelaiGriaz(byteArrayOf(0x01), BRAKE_MOTOR_HDLE)
      } else {
        brakeMotorTv.text = 0.toString()
        main?.DelaiGriaz(byteArrayOf(0x00), BRAKE_MOTOR_HDLE)
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
    set.color = Color.GREEN
    set.cubicIntensity = 3f
    set.setCircleColor(Color.GREEN)
    set.fillAlpha = 65
    set.fillColor = ColorTemplate.getHoloBlue()
    set.highLightColor = Color.rgb(244, 117, 177)
    set.valueTextColor = Color.WHITE
    set.valueTextSize = 1f
    return set
  }
  private fun createSet2(): LineDataSet? {
    val set2 = LineDataSet(null, null)
    set2.axisDependency = YAxis.AxisDependency.LEFT //.AxisDependency.LEFT
    set2.lineWidth = 2f
    set2.color = Color.BLUE
    set2.cubicIntensity = 3f
    set2.setCircleColor(Color.YELLOW)
    set2.fillAlpha = 65
    set2.fillColor = ColorTemplate.getHoloBlue()
    set2.highLightColor = Color.rgb(244, 117, 177)
    set2.valueTextColor = Color.WHITE
    set2.valueTextSize = 1f
    return set2
  }
  private fun addEntry(sens1: Int, sens2: Int) {
    val data: LineData = chart_mainchart?.data!!
    if (data != null) {
      var set = data.getDataSetByIndex(0)
      var set2 = data.getDataSetByIndex(1)
      if (set == null) {
        set = createSet()
        data.addDataSet(set)
        set2 = createSet2()
        data.addDataSet(set2)
      }
      data.addEntry(Entry(set!!.entryCount.toFloat(), sens1.toFloat()), 0)
      data.addEntry(Entry(set2!!.entryCount.toFloat(), sens2.toFloat()), 1)
      data.notifyDataChanged()
//      data.notifyDataChanged()
      chart_mainchart.notifyDataSetChanged()
      chart_mainchart.setVisibleXRangeMaximum(50f)
      chart_mainchart.moveViewToX(set.entryCount - 50.toFloat()) //data.getEntryCount()
    }
  }
  fun initializedGraphForChannel1() {
    chart_mainchart.contentDescription//getDescription().setEnabled(true)
    chart_mainchart.setTouchEnabled(false)
    chart_mainchart.setDragEnabled(false)
    chart_mainchart.isDragDecelerationEnabled = false//setDragXEnabled(false)
    chart_mainchart.setScaleEnabled(false)
    chart_mainchart.setDrawGridBackground(false)
    chart_mainchart.setPinchZoom(false)
    chart_mainchart.setBackgroundColor(Color.BLACK)
    chart_mainchart.getHighlightByTouchPoint(1f, 1f)
    val data = LineData()
    val data2 = LineData()
    chart_mainchart.setData(data)
    chart_mainchart.setData(data2)
    val legend: Legend = chart_mainchart.getLegend()
    legend.form = Legend.LegendForm.CIRCLE
    val x1: XAxis = chart_mainchart.getXAxis()
    x1.textColor = Color.BLACK
    x1.setDrawGridLines(false)
    x1.setAxisMaximum(4000000f) //x1.resetAxisMaximum();
    x1.setAvoidFirstLastClipping(true)
    val y1: YAxis = chart_mainchart.getAxisLeft()
    y1.textColor = Color.WHITE
    y1.mAxisMaximum = 255f
    y1.mAxisMinimum = 0f
    y1.gridColor = Color.BLACK
    y1.setDrawGridLines(false)
    chart_mainchart.getAxisRight().setEnabled(false)

//    chart_mainchart.description.text = ""
//    chart_mainchart.description.textSize = 16f
//    chart_mainchart.description.textColor = Color.TRANSPARENT
//    chart_mainchart.legend.isEnabled = false
//    chart_mainchart.legend.isWordWrapEnabled = false
//    chart_mainchart.legend.textColor = Color.TRANSPARENT
////    chart_mainchart.legend.setCustom =computed, label)
//
//    chart_mainchart.setDrawGridBackground(false)
//    chart_mainchart.axisLeft.setDrawGridLines(true)
//    chart_mainchart.axisLeft.setDrawAxisLine(true)
//    chart_mainchart.axisLeft.gridColor = Color.WHITE
//    chart_mainchart.axisRight.setDrawGridLines(false)
//    chart_mainchart.axisRight.textColor = Color.TRANSPARENT
//    chart_mainchart.xAxis.setDrawGridLines(false)
//
//    chart_mainchart.setPinchZoom(false)
//    chart_mainchart.isDragEnabled = true //здесь можно сделать изменение масштаба только по оси х
//    chart_mainchart.setScaleEnabled(true)//и перетаскивание по ней же если поставить в обоих этих строчках true
//    chart_mainchart.setScaleMinima(2f, 0f)//здесь можно увеличить начальный масштаб 2f = 2x
//    chart_mainchart.setVisibleXRange(4f, 24f)//здесь можно настроить минимальный и максимальный диапазон увеличения
//    chart_mainchart.xAxis.labelRotationAngle = 45f
//    chart_mainchart.animateY(700)
//
//    val mv = context?.let { MyMarkerView(it, R.layout.custom_marker_view) }
//    chart_mainchart.markerView = mv
//
//    // X - axis settings
//    val xAxis = chart_mainchart.xAxis
//    xAxis.textSize = 12f
////    xAxis.spaceBetweenLabels = 4
//    xAxis.position = XAxis.XAxisPosition.BOTTOM
//    xAxis.textColor = Color.rgb(255, 255, 255)
//    xAxis.axisLineColor = Color.WHITE
//
//
//    // Y - axis settings
//    val leftAxis = chart_mainchart.axisLeft
//    leftAxis.textColor = Color.rgb(255, 255, 255)
//    leftAxis.textSize = 12f
//    leftAxis.axisLineColor = Color.TRANSPARENT
//    leftAxis.setStartAtZero(true)
//    leftAxis.mAxisMaximum = 255f
//    leftAxis.mAxisMinimum = 0f
//
//    // Y2 - axis settings
//    val rightAxis = chart_mainchart.axisRight
//    rightAxis.axisLineColor = Color.TRANSPARENT
  }

  fun startGraphEnteringDataThread() {
    graphThread = Thread {
      while (graphThreadFlag) {
        if (plotData) {
          addEntry(50, 250)
          plotData = false
        }
        main?.runOnUiThread(Runnable { addEntry(10, 255) })
        try {
          Thread.sleep(ConstantManager.GRAPH_UPDATE_DELAY.toLong())
        } catch (ignored: Exception) {
        }
      }
    }
    graphThread?.start()
  }

  private fun initializeChart(dayCount: Int) {
    var TotalAmount = 0f
    var Max = 0f
    var sumCount = 0f
    val entries = ArrayList<Entry>()
    for (i in 0..dayCount) {
      val daySum = sqliteManager.getDayDrinkAmount(DateUtils.getFarDay(dateCount + i))

      // get total sum
      TotalAmount += daySum.toFloat()

      // get max
      if (i == 0)
        Max = daySum.toFloat()
      else if (Max < daySum) Max = daySum.toFloat()

      // count
      if (daySum != 0)
        sumCount++

      // add entry
      entries.add(Entry(daySum.toFloat(), i.toFloat()))
    }

    val labels = ArrayList<String>()
    labels.add("1:00")
    labels.add("2:00")
    labels.add("3:00")
    labels.add("4:00")
    labels.add("5:00")
    labels.add("6:00")
    labels.add("7:00")


    val dataset = LineDataSet(entries, "количество выпитой воды")
//    val data = LineData(labels, dataset)
//    chart_mainchart.data = data
    chart_mainchart.setOnChartValueSelectedListener(this)

    val computed = intArrayOf(Color.TRANSPARENT)
    val label = arrayOf("")
//    chart_mainchart.setDescription("")
//    chart_mainchart.setDescriptionTextSize(16f)
//    chart_mainchart.setDescriptionColor(Color.TRANSPARENT)
    chart_mainchart.legend.isEnabled = false
    chart_mainchart.legend.isWordWrapEnabled = false
    chart_mainchart.legend.textColor = Color.TRANSPARENT
//    chart_mainchart.legend.setCustom(computed, label)

    chart_mainchart.setDrawGridBackground(false)
    chart_mainchart.axisLeft.setDrawGridLines(true)
    chart_mainchart.axisLeft.setDrawAxisLine(true)
    chart_mainchart.axisLeft.gridColor = Color.WHITE
    chart_mainchart.axisRight.setDrawGridLines(false)
    chart_mainchart.axisRight.textColor = Color.TRANSPARENT
    chart_mainchart.xAxis.setDrawGridLines(false)

    chart_mainchart.setPinchZoom(false)
    chart_mainchart.isDragEnabled = true //здесь можно сделать изменение масштаба только по оси х
    chart_mainchart.setScaleEnabled(true)//и перетаскивание по ней же если поставить в обоих этих строчках true
    chart_mainchart.setScaleMinima(2f, 0f)//здесь можно увеличить начальный масштаб 2f = 2x
    chart_mainchart.setVisibleXRange(4f, 24f)//здесь можно настроить минимальный и максимальный диапазон увеличения
    chart_mainchart.xAxis.labelRotationAngle = 45f
    chart_mainchart.animateY(700)

    val mv = context?.let { MyMarkerView(it, R.layout.custom_marker_view) }
    chart_mainchart.markerView = mv

    // X - axis settings
    val xAxis = chart_mainchart.xAxis
    xAxis.textSize = 12f
//    xAxis.spaceBetweenLabels = 4
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.textColor = Color.rgb(255, 255, 255)
    xAxis.axisLineColor = Color.WHITE


    // Y - axis settings
    val leftAxis = chart_mainchart.axisLeft
    leftAxis.textColor = Color.rgb(255, 255, 255)
    leftAxis.textSize = 12f
    leftAxis.axisLineColor = Color.TRANSPARENT
    leftAxis.setStartAtZero(true)
    leftAxis.mAxisMaximum = 255f
    leftAxis.mAxisMinimum = 0f
    leftAxis.spaceTop = 10f
//    leftAxis.valueFormatter = YAxisValueFormatter()

    // Y2 - axis settings
    val rightAxis = chart_mainchart.axisRight
    rightAxis.axisLineColor = Color.TRANSPARENT

     //set max Y-Axis & chart message
//    val tv_chartMessage = rootView!!.findViewById(R.id.chart_tv_message) as TextView
//    if (TotalAmount > 0)
//      tv_chartMessage.visibility = View.INVISIBLE
//    else
//      tv_chartMessage.visibility = View.VISIBLE

     //dataSet settings
    dataset.setDrawFilled(true)
    dataset.circleSize = 3f
    dataset.valueTextSize = 13f
    dataset.valueTextColor = Color.TRANSPARENT
    dataset.enableDashedHighlightLine(10f, 1f, 0f)
//    dataset.valueFormatter = DataSetValueFormatter()
  }

  override fun onValueSelected(e: Entry?, h: Highlight?) {
    TODO("Not yet implemented")
  }

  /**
   * YAxis : Water Y-Value Formatter
   */
//  private inner class YAxisValueFormatter : com.github.mikephil.charting.formatter.YAxisValueFormatter {
//    override fun getFormattedValue(value: Float, yAxis: YAxis): String {
//      return Math.round(value).toString() + " ед"
//    }
//  }

  /**
   * Water DataSet-Value Formatter
   */
//  private inner class DataSetValueFormatter : ValueFormatter {
//    override fun getFormattedValue(value: Float, entry: Entry, dataSetIndex: Int, viewPortHandler: ViewPortHandler): String {
//      return Math.round(value).toString() + ""
//    }
//  }

//  override fun onValueSelected(e: Entry, dataSetIndex: Int, h: Highlight) {
//    System.err.println("подпись: " + DateUtils.getIndexOfDayName(e.xIndex))
//
//    System.err.println("подпись: " + e.`val` + " ml")
//  }

  override fun onNothingSelected() {

  }
}
