/**Copyright 2020 Michael Hall

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.**/
package uk.me.mikemike.taionneko.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import uk.me.mikemike.taionneko.R
import kotlinx.android.synthetic.main.fragment_temperature_information.*
import uk.me.mikemike.taionneko.ui.activities.MainActivityViewModel
import uk.me.mikemike.taionneko.TemperatureEntry

// The Entry class used by the chart only allows for two float values for data (x and y axis) and we need access to
// the date a temperature was added,  while we could map the chart's entry based on it's index back to the original data (they are stored in the same order in both lists)
// it seems a little nicer just to extend the chart data class and include the data in it, then we can forget about the orignal data when
// we do any formatting in the chart
class TemperatureCustomChartEntry(temp: TemperatureEntry, x:Float) : Entry(x, temp.value) {
    val tempEntry = temp
}

class TemperatureInformation : Fragment() {

    private lateinit var viewModel: MainActivityViewModel

    companion object {
        @JvmStatic
        fun newInstance() =
            TemperatureInformation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_temperature_information, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        setupChartDesign()
        setupViewModelObservers()

    }

    private fun setupViewModelObservers(){
        viewModel = ViewModelProvider(activity!!).get(MainActivityViewModel::class.java)
        viewModel.averageTemperature.observe(viewLifecycleOwner,
            // TODO: Format this float value to a couple of significant digits - it looks weird now
            Observer { textAverageTemperature.text = it.toString() })

        viewModel.recentEntries.observe(viewLifecycleOwner, Observer {
            updateGraph(it)
        })
        viewModel.recentEntryDates.observe(viewLifecycleOwner, Observer {
            updateGraphXAxisLabels(it)
        })
    }


    private fun setupChartDesign(){
        // Yes yes, I know setting UI stuff in the code is awful but I was
        // unable to set these in the XML layout
        temperatureChart.apply {
                xAxis.apply {
                    labelRotationAngle =90F
                    granularity=1F
                    position = XAxis.XAxisPosition.BOTTOM
                }
            legend.isEnabled = false
            description.isEnabled = false
            axisRight.isEnabled = false
            setTouchEnabled(true)
        }
    }

    private fun updateGraphXAxisLabels(vals: List<String>){
        temperatureChart.xAxis.valueFormatter = IndexAxisValueFormatter(vals)
        temperatureChart.xAxis.setLabelCount(vals.size)
        temperatureChart.invalidate()
    }

    private fun forceChartRefresh(){
        // Black magic, a random combination will force the chart to redraw itself
        // call the functions, all the functions!
        temperatureChart.data.notifyDataChanged()
        temperatureChart.notifyDataSetChanged()
        temperatureChart.axisLeft.resetAxisMaximum()
        temperatureChart.axisLeft.resetAxisMinimum()
        temperatureChart.invalidate()
    }


    private fun updateGraph(data: List<TemperatureEntry>) {
        // We need to create the data the first time
        if (temperatureChart.data == null) {
            val chartData = ArrayList<Entry>()
            data.forEachIndexed { index, entry ->
                chartData.add(
                    TemperatureCustomChartEntry(
                        entry,
                        index.toFloat()
                    )
                )
            }
            val dataSet = LineDataSet(chartData, "Temperature").apply {
                axisDependency = YAxis.AxisDependency.LEFT
                lineWidth = 3F
                setDrawFilled(true)
                fillColor = Color.BLUE
            }
            temperatureChart.data = LineData(ArrayList<ILineDataSet>().apply { add(dataSet) })
        } else {
            val chartData = temperatureChart.data.getDataSetByIndex(0)
            chartData.clear()
            data.forEachIndexed { index, entry ->
                temperatureChart.data.addEntry(
                    TemperatureCustomChartEntry(
                        entry,
                        index.toFloat()
                    ), 0
                )
            }
        }
        forceChartRefresh()
    }


}