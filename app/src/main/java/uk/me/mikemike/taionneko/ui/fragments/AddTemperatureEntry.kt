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


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_temperature_entry.*
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import uk.me.mikemike.taionneko.MainActivityViewModel
import uk.me.mikemike.taionneko.R
import uk.me.mikemike.taionneko.TemperatureEntry


class AddTemperatureEntry : Fragment() {

    companion object {
        fun newInstance() = AddTemperatureEntry()
    }

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_temperature_entry, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(MainActivityViewModel::class.java)

        buttonAddTemperature.setOnClickListener {
                addEntry()
        }


        buttonToday.setOnClickListener {
            OffsetDateTime.now().let {
                setCurrentDate(it.year, it.monthValue, it.dayOfMonth)
            }
        }

        buttonNow.setOnClickListener {
            OffsetDateTime.now().let {
                setCurrentTime(it.hour, it.minute)
            }
        }

        viewModel.selectedDate.observe(viewLifecycleOwner, Observer {
            editTextDate.setText(
                it.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            )
            editTextTime.setText(
                it.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            )
        })

        // Unable to set this in the layout xml annoyingly...
        // Although we are using text boxes for the date and time,
        // instead of allowing typing we will hijack the click event and bring up
        // a dialog box to allow date and time selection. The date and time look better
        // in a text box than a simple label (they actually look editable)
        editTextDate.inputType = InputType.TYPE_NULL
        editTextTime.inputType = InputType.TYPE_NULL

        editTextDate.setOnClickListener {
            val theTime = getSavedDateOrNow()
            // Android's date picker uses 0 as January while the Time API uses 1, hence the need for the -1 and +1 when dealing
            // with the values returned from and sent to the dialog
            val dpd = DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                this.setCurrentDate(year, monthOfYear+1, dayOfMonth)
            }, theTime.year, theTime.monthValue-1, theTime.dayOfMonth)
            dpd.show()
        }

        editTextTime.setOnClickListener {
            val theTime = getSavedDateOrNow()
            val timeDialog = TimePickerDialog(requireContext(), TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                this.setCurrentTime(hourOfDay, minute)
            }, theTime.hour, theTime.minute, true)
            timeDialog.show()
        }



    }


    fun getSavedDateOrNow(): OffsetDateTime{
        return  if(viewModel.selectedDate.value != null) viewModel.selectedDate.value!! else OffsetDateTime.now()!!

    }

    fun setCurrentTime(hours: Int, minutes: Int){
        viewModel.selectedDate.value?.let {
            viewModel.selectedDate.postValue(OffsetDateTime.of(it.year, it.monthValue, it.dayOfMonth, hours, minutes, 0, 0, it.offset))
        }
    }

    fun setCurrentDate(year: Int, month: Int, day: Int){
        viewModel.selectedDate.value?.let {
            viewModel.selectedDate.postValue(OffsetDateTime.of(year, month, day, it.hour, it.minute, 0, 0, it.offset ))
        }
    }

    fun addEntry(){
        editTextTemperature.text.toString().toFloatOrNull()?.let {
            viewModel.addNewTemperatureEntry(TemperatureEntry(0, it, getSavedDateOrNow())).observe(viewLifecycleOwner, Observer { handleTemperatureEntry()})
            return
        }
        Toast.makeText(requireContext(), "Please Enter a valid temperature", Toast.LENGTH_SHORT).show()
    }

    fun handleTemperatureEntry(){
        Toast.makeText(activity, "Temperature Entry Added", Toast.LENGTH_SHORT).show()
    }


}