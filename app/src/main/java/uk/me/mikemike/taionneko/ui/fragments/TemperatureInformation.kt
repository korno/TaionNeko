package uk.me.mikemike.taionneko.ui.fragments

import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import uk.me.mikemike.taionneko.R
import kotlinx.android.synthetic.main.fragment_temperature_information.*
import org.threeten.bp.OffsetDateTime
import uk.me.mikemike.taionneko.MainActivityViewModel
import uk.me.mikemike.taionneko.TemperatureEntry

class TemperatureCustomEntry(temp: TemperatureEntry, x:Float) : Entry(x, temp.value) {
    val tempEntry = temp
}

class TemperatureInformation : Fragment() {

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_temperature_information, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(MainActivityViewModel::class.java)

        temperatureChart.xAxis.labelRotationAngle = 90f;
        temperatureChart.xAxis.granularity=1F


        viewModel.averageTemperature.observe(viewLifecycleOwner,
            Observer { textAverageTemperature.text = it.toString() })
        viewModel.recentEntries.observe(viewLifecycleOwner, Observer {
            updateGraph(it)
        })
        viewModel.recentEntryDates.observe(viewLifecycleOwner, Observer {
            temperatureChart.xAxis.valueFormatter = IndexAxisValueFormatter(it)
            temperatureChart.xAxis.setLabelCount(it.size)
            temperatureChart.invalidate()
        })

        viewModel.deleteAll()
    }

    private fun updateGraph(data: List<TemperatureEntry>) {
        if (temperatureChart.data == null) {
            var chartData = ArrayList<Entry>()
            data.forEachIndexed { index, entry ->
                chartData.add(
                    Entry(
                        index.toFloat(),
                        entry.value
                    )
                )
            }
            var dataSet = LineDataSet(chartData, "Temperature").apply {
                axisDependency = YAxis.AxisDependency.LEFT
            }
            var lineDataSets = ArrayList<ILineDataSet>()
            lineDataSets.add(dataSet)
            var d = LineData(lineDataSets)
            temperatureChart.data = d
            temperatureChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        } else {
            var chartData = temperatureChart.data.getDataSetByIndex(0)
            chartData.clear()
            data.forEachIndexed { index, entry ->
                temperatureChart.data.addEntry(
                    TemperatureCustomEntry(
                        entry,
                        index.toFloat()
                    ), 0
                )
            }

            temperatureChart.data.notifyDataChanged()
            temperatureChart.notifyDataSetChanged()
            temperatureChart.axisLeft.resetAxisMaximum()
            temperatureChart.axisLeft.resetAxisMinimum()
            temperatureChart.invalidate()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            TemperatureInformation()
    }
}