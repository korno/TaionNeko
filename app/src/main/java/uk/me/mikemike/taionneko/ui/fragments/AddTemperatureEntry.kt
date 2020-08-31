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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_temperature_entry.*
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import uk.me.mikemike.taionneko.ui.activities.MainActivityViewModel
import uk.me.mikemike.taionneko.R
import uk.me.mikemike.taionneko.TemperatureEntry
import uk.me.mikemike.taionneko.utils.hideSoftKeyboard



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
        viewModel.selectedDate.observe(viewLifecycleOwner, Observer {
            editTextDate.editText!!.setText(
                it.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            )
            editTextTime.editText!!.setText(
                it.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            )
        })

        // Unable to set this in the layout xml annoyingly...
        // Although we are using text boxes for the date and time,
        // instead of allowing typing we will hijack the click event and bring up
        // a dialog box to allow date and time selection. The date and time look better
        // in a text box than a simple label (they actually look editable)
        editTextDate.editText!!.inputType = InputType.TYPE_NULL
        editTextTime.editText!!.inputType = InputType.TYPE_NULL

        bindUIEvents()

    }

    private fun bindUIEvents(){

        buttonAddTemperature.setOnClickListener {
            addEntry()
        }


        buttonToday.setOnClickListener {
            OffsetDateTime.now().let {
                setSavedDate(it.year, it.monthValue, it.dayOfMonth)
            }
        }

        buttonNow.setOnClickListener {
            OffsetDateTime.now().let {
                setSavedTime(it.hour, it.minute)
            }
        }


        editTextDate.editText!!.setOnClickListener {
            val theTime = getSavedDateOrNow()
            // Android's date picker uses 0 as January while the Time API uses 1, hence the need for the -1 and +1 when dealing
            // with the values returned from and sent to the dialog
            DatePickerDialog(requireContext(), DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                this.setSavedDate(year, monthOfYear+1, dayOfMonth)
            }, theTime.year, theTime.monthValue-1, theTime.dayOfMonth).show()
        }

        editTextTime.editText!!.setOnClickListener {
            val theTime = getSavedDateOrNow()
            TimePickerDialog(requireContext(), TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                this.setSavedTime(hourOfDay, minute)
            }, theTime.hour, theTime.minute, true).show()
        }

        // Limit the max an minimum values for the temperature input
       // TODO : Does this actually work well? Disabled for now as the app may be used for non human temperatures. Check requirements.
        /*
        editTextTemperature.onFocusChangeListener =
            MinMaxTextWatcher(30F, 45F,this::onTemperatureValueToLow, this::onTemperatureValueToHigh)
         */
    }

    /* see above todo
    private fun onTemperatureValueToLow(value: Float){
        Toast.makeText(requireContext(), "The temperature value was too low. Set to minimum value instead.", Toast.LENGTH_SHORT).show()
    }

    private fun onTemperatureValueToHigh(value: Float){
        Toast.makeText(requireContext(), "The temperature value was too high. Set to maximum value instead.", Toast.LENGTH_SHORT).show()
    }*/

    private fun getSavedDateOrNow(): OffsetDateTime{
        return  if(viewModel.selectedDate.value != null) viewModel.selectedDate.value!! else OffsetDateTime.now()!!

    }

    private fun setSavedTime(hours: Int, minutes: Int){
        viewModel.selectedDate.value?.let {
            viewModel.selectedDate.postValue(OffsetDateTime.of(it.year, it.monthValue, it.dayOfMonth, hours, minutes, 0, 0, it.offset))
        }
    }

    private fun setSavedDate(year: Int, month: Int, day: Int){
        viewModel.selectedDate.value?.let {
            viewModel.selectedDate.postValue(OffsetDateTime.of(year, month, day, it.hour, it.minute, 0, 0, it.offset ))
        }
    }

    private fun addEntry(){
        // Ugh, extension methods really don't like inheritence
        (activity as AppCompatActivity).hideSoftKeyboard(editTextTemperature)
        editTextTemperature.editText!!.text.toString().toFloatOrNull()?.let {
            editTextTemperature.error = null
            viewModel.addNewTemperatureEntry(TemperatureEntry(0, it, getSavedDateOrNow())).observe(viewLifecycleOwner, Observer { handleTemperatureEntry()})
            editTextTemperature.editText!!.setText("");
            return
        }
        editTextTemperature.setError(resources.getString(R.string.invalid_temperature_value))
        Toast.makeText(requireContext(), R.string.invalid_temperature_value, Toast.LENGTH_SHORT).show()
    }

    private fun handleTemperatureEntry(){

        val s = Snackbar.make(
            buttonAddTemperature,
            R.string.temperature_entry_added,
            Snackbar.LENGTH_SHORT
        )

        // FIX: Force the snackbar to anchor above the navigation bar if one is present in the
        // parent activity AS LONG AS it has the id bottomNavigation
        // this is really hacky but will fix the problem of the snackbar covering the navigation
        val bottomNavigation = activity!!.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        if(bottomNavigation != null) {
           s.setAnchorView(bottomNavigation).show()
        }
        else{
            s.show()
        }
    }


}