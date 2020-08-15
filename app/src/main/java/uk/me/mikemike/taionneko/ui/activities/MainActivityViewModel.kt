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
package uk.me.mikemike.taionneko.ui.activities

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import uk.me.mikemike.taionneko.TaionNekoDatabase
import uk.me.mikemike.taionneko.TaionNekoRepository
import uk.me.mikemike.taionneko.TemperatureEntry

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repos: TaionNekoRepository


    val recentEntries: LiveData<List<TemperatureEntry>>
    val recentEntryDates: LiveData<List<String>>


    var averageTemperature: LiveData<Float>
    var selectedDate: MutableLiveData<OffsetDateTime>

    var fromSearchDate: MutableLiveData<OffsetDateTime>
    var searchResults: LiveData<List<TemperatureEntry>>
    val searchResultDatesStrings: LiveData<List<String>>

    val dashboardRecordLimit: Int = application.getSharedPreferences("GlobalPrefs", MODE_PRIVATE).getInt("DashboardRecordSize", 14)

    init {
        selectedDate = MutableLiveData(OffsetDateTime.now())
        repos = TaionNekoRepository(
            TaionNekoDatabase.getInstance(
                application
            ), dashboardRecordLimit
        )
        recentEntries = repos.recentEntries
        averageTemperature = Transformations.map(recentEntries){
            calculateAverageTemperature(it)
        }

      recentEntryDates = Transformations.map(recentEntries){generateDateStrings(it)}


        fromSearchDate = MutableLiveData(OffsetDateTime.now().minusWeeks(1))
        searchResults = Transformations.switchMap(fromSearchDate){ currentDate -> repos.getEntriesSince(currentDate, dashboardRecordLimit)}
        searchResultDatesStrings = Transformations.map(searchResults){generateDateStrings(it)}
    }



    private fun generateDateStrings(vals: List<TemperatureEntry>): List<String>{
        val v=ArrayList<String>()
        vals.forEach { v.add(it.insertDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))) }
        return v
    }

    private fun calculateAverageTemperature(vals: List<TemperatureEntry>): Float{
        if(vals.isEmpty()) return 0f
        val s = vals.sumByDouble { it.value.toDouble() }
        return s.toFloat() / vals.size
    }


     fun addNewTemperatureEntry(entry: TemperatureEntry): LiveData<Long> {
        val r = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
             r.postValue(repos.add(entry))
        }
        return r
    }

    fun deleteAll(){
        viewModelScope.launch (Dispatchers.IO ){
            repos.deleteAll()
        }
    }

}