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

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import uk.me.mikemike.taionneko.R
import kotlinx.android.synthetic.main.fragment_temperature_information.*
import uk.me.mikemike.taionneko.ui.activities.MainActivityViewModel
import uk.me.mikemike.taionneko.TemperatureEntry
import uk.me.mikemike.taionneko.utils.applyTheme
import uk.me.mikemike.taionneko.utils.setTemperatureData
import uk.me.mikemike.taionneko.utils.updateGraphXAxisLabels

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

    private fun setupViewModelObservers() {
        viewModel = ViewModelProvider(activity!!).get(MainActivityViewModel::class.java)
        viewModel.searchResultsAverageTemp.observe(viewLifecycleOwner,
            Observer {
                textAverageTemperature.text =
                    getString(R.string.fragment_temperature_information_average_temp, it)
            })

        viewModel.searchResults.observe(viewLifecycleOwner, Observer {
            temperatureChart.setTemperatureData(it, requireContext(), this::calculateLineColors, this::calculateCircleColors)
        })
        viewModel.searchResultDatesStrings.observe(viewLifecycleOwner, Observer {
            temperatureChart.updateGraphXAxisLabels(it)
        })
    }


    private fun setupChartDesign() {
        // Yes yes, I know setting UI stuff in the code is awful but I was
        // unable to set these in the XML layout
        temperatureChart.applyTheme(requireContext(), R.string.graph_no_data_for_last_seven_days)
        temperatureChart.setTouchEnabled(true)

    }

    private fun calculateLineColors(ents: List<TemperatureEntry>): List<Int> {
        val c = ContextCompat.getColor(requireContext(), R.color.color_s)
        return ArrayList<Int>().apply {
            ents.forEach {
                add(c)
            }
        }
    }

    private fun calculateCircleColors(ents: List<TemperatureEntry>): List<Int>{

        val hotColor = ContextCompat.getColor(requireContext(), R.color.hot_temperature_graph_color)
        val normalColor = ContextCompat.getColor(requireContext(), R.color.hot_temperature_graph_color)
        val coldColor = ContextCompat.getColor(requireContext(), R.color.hot_temperature_graph_color)
        return ArrayList<Int>().apply{
            ents.forEach {
                add(hotColor)
            }
        }
    }
}