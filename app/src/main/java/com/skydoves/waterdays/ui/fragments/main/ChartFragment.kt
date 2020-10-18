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
import android.bluetooth.BluetoothGattCharacteristic
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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ViewPortHandler
import com.skydoves.waterdays.R
import com.skydoves.waterdays.WDApplication
import com.skydoves.waterdays.persistence.sqlite.SqliteManager
import com.skydoves.waterdays.ui.activities.main.MainActivity
import com.skydoves.waterdays.ui.customViews.MyMarkerView
import com.skydoves.waterdays.utils.DateUtils
import kotlinx.android.synthetic.main.layout_chart.*
import java.util.*
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
    System.err.println("ChartFragment: дата dateCount=" + dateCount)
    initializeChart(DateUtils.getDateDay("2020-10-16", DateUtils.dateFormat))//2020-10-14  DateUtils.getFarDay(0)
    System.err.println("ChartFragment: дата getDateDay=" + DateUtils.getDateDay(DateUtils.getFarDay(0), DateUtils.dateFormat))

    close_btn.setOnTouchListener(OnTouchListener { v, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        main?.DelaiGriaz(byteArrayOf(0x01, 0x00))
      }
      if (event.action == MotionEvent.ACTION_UP) {
        main?.DelaiGriaz(byteArrayOf(0x00, 0x00))
      }
      false
    })
//    basal_fab.setOnClickListener  { onBasalFab() }
//    diary_fab.setOnClickListener  { onDiaryFab() }
//    search_fab.setOnClickListener { onSearchFab() }
//    play_stop_fab.setOnClickListener { onPlayStopFab() }
  }

//  private fun onClose(): Boolean {
//    main?.DelaiGriaz()
//  }

  private fun onBolusFab() {
    main?.DelaiGriaz(byteArrayOf(0x01, 0x00))
    Toast.makeText(context, getString(R.string.bolus), Toast.LENGTH_SHORT).show()
  }
  private fun onBasalFab() {
    Toast.makeText(context, getString(R.string.basal), Toast.LENGTH_SHORT).show()
  }
  private fun onDiaryFab() {
    Toast.makeText(context, getString(R.string.diary), Toast.LENGTH_SHORT).show()
  }
  private fun onSearchFab() {
    Toast.makeText(context, getString(R.string.search), Toast.LENGTH_SHORT).show()
  }
  private fun onPlayStopFab() {
    Toast.makeText(context, getString(R.string.play_stop), Toast.LENGTH_SHORT).show()
  }


  private fun initializeChart(dayCount: Int) {
    var TotalAmount = 0f
    var Max = 0f
    var sumCount = 0f
    val entries = ArrayList<Entry>()
    for (i in 0..dayCount) {
      val daySum = sqliteManager!!.getDayDrinkAmount(DateUtils.getFarDay(dateCount + i))

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
      entries.add(Entry(daySum.toFloat(), i))
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
    val data = LineData(labels, dataset)
    chart_mainchart.data = data
    chart_mainchart.setOnChartValueSelectedListener(this)

    val computed = intArrayOf(Color.TRANSPARENT)
    val label = arrayOf("")
    chart_mainchart.setDescription("")
    chart_mainchart.setDescriptionTextSize(16f)
    chart_mainchart.setDescriptionColor(Color.TRANSPARENT)
    chart_mainchart.legend.isEnabled = false
    chart_mainchart.legend.isWordWrapEnabled = false
    chart_mainchart.legend.textColor = Color.TRANSPARENT
    chart_mainchart.legend.setCustom(computed, label)

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
    xAxis.spaceBetweenLabels = 4
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.textColor = Color.rgb(255, 255, 255)
    xAxis.axisLineColor = Color.WHITE


    // Y - axis settings
    val leftAxis = chart_mainchart.axisLeft
    leftAxis.textColor = Color.rgb(255, 255, 255)
    leftAxis.textSize = 12f
    leftAxis.axisLineColor = Color.TRANSPARENT
    leftAxis.setStartAtZero(true)
    leftAxis.spaceTop = 10f
    leftAxis.valueFormatter = YAxisValueFormatter()

    // Y2 - axis settings
    val rightAxis = chart_mainchart.axisRight
    rightAxis.axisLineColor = Color.TRANSPARENT

    // set max Y-Axis & chart message
    val tv_chartMessage = rootView!!.findViewById(R.id.chart_tv_message) as TextView
    if (TotalAmount > 0)
      tv_chartMessage.visibility = View.INVISIBLE
    else
      tv_chartMessage.visibility = View.VISIBLE

    // dataSet settings
    dataset.setDrawFilled(true)
    dataset.circleSize = 3f
    dataset.valueTextSize = 13f
    dataset.valueTextColor = Color.TRANSPARENT
    dataset.enableDashedHighlightLine(10f, 1f, 0f)
    dataset.valueFormatter = DataSetValueFormatter()
  }

  /**
   * YAxis : Water Y-Value Formatter
   */
  private inner class YAxisValueFormatter : com.github.mikephil.charting.formatter.YAxisValueFormatter {
    override fun getFormattedValue(value: Float, yAxis: YAxis): String {
      return Math.round(value).toString() + " ед"
    }
  }

  /**
   * Water DataSet-Value Formatter
   */
  private inner class DataSetValueFormatter : ValueFormatter {
    override fun getFormattedValue(value: Float, entry: Entry, dataSetIndex: Int, viewPortHandler: ViewPortHandler): String {
      return Math.round(value).toString() + ""
    }
  }

  override fun onValueSelected(e: Entry, dataSetIndex: Int, h: Highlight) {
    System.err.println("подпись: " + DateUtils.getIndexOfDayName(e.xIndex))

    System.err.println("подпись: " + e.`val` + " ml")
  }

  override fun onNothingSelected() {

  }
}
