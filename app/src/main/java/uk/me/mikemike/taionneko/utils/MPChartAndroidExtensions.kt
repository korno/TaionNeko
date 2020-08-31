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
package uk.me.mikemike.taionneko.utils






import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_temperature_information.*
import uk.me.mikemike.taionneko.R
import uk.me.mikemike.taionneko.TemperatureEntry



// The Entry class used by the chart only allows for two float values for data (x and y axis) and we need access to
// the date a temperature was added,  while we could map the chart's entry based on it's index back to the original data (they are stored in the same order in both lists)
// it seems a little nicer just to extend the chart data class and include the data in it, then we can forget about the orignal data when
// we do any formatting in the chart
class TemperatureCustomChartEntry(temp: TemperatureEntry, x:Float) : Entry(x, temp.value) {
    val tempEntry = temp
}

typealias  colorsGenFun = (items: List<TemperatureEntry>) -> List<Int>

fun LineChart.applyTheme(c: Context, noDataTextID: Int){
    this.apply {
        xAxis.apply {
            labelRotationAngle = 90F
            granularity = 1F
            position = XAxis.XAxisPosition.BOTTOM
        }
        legend.isEnabled = false
        description.isEnabled = false
        axisRight.isEnabled = false
        setNoDataText(c.resources.getString(noDataTextID))
    }
}

fun LineChart.setTemperatureData(data: List<TemperatureEntry>, context: Context, lineColorsFun: colorsGenFun,
                circleColorsFun: colorsGenFun){
    // FIX for displaying the chart empty data message; if the data is empty set the chart data to null
    // this forces the chart to show the empty data text
    if (data.isEmpty()) {
        this.data = null
        return
    }

    // We need to create the data the first time - subsquent times we can just
    // reuse the data object and fill the actual data (leaving the styling alone)
    if (this.data == null) {
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
            lineWidth = ResourcesCompat.getFloat(
                context.resources,
                R.dimen.line_graph_line_width
            )
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.chart_fill)
        }
        this.data = LineData(ArrayList<ILineDataSet>().apply { add(dataSet) })
    } else {
        val chartData = this.data.getDataSetByIndex(0) as LineDataSet
        chartData.clear()
        data.forEachIndexed { index, entry ->
            this.data.addEntry(
                TemperatureCustomChartEntry(
                    entry,
                    index.toFloat()
                ), 0
            )
        }
    }
    (this.data.getDataSetByIndex(0) as LineDataSet).setColors(lineColorsFun(data))
    (this.data.getDataSetByIndex(0) as LineDataSet).setCircleColors(circleColorsFun(data))
    this.forceChartRefresh()
}


fun LineChart.updateGraphXAxisLabels(vals: List<String>) {
    this.xAxis.valueFormatter = IndexAxisValueFormatter(vals)
    this.xAxis.setLabelCount(vals.size)
    this.invalidate()
}


fun LineChart.forceChartRefresh(){
    // Black magic, a random combination will force the chart to redraw itself
    // call the functions, all the functions!
    data.notifyDataChanged()
    notifyDataSetChanged()
    axisLeft.resetAxisMaximum()
    axisLeft.resetAxisMinimum()
    invalidate()
    fitScreen()
}