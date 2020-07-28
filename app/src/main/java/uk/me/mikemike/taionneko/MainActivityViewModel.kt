package uk.me.mikemike.taionneko

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repos: TaionNekoRepository

    val recentEntries: LiveData<List<TemperatureEntry>>
    val recentEntryDates: LiveData<List<String>>
    var averageTemperature: LiveData<Float>
    var selectedDate: MutableLiveData<OffsetDateTime>


    init {
        val recentSize = application.getSharedPreferences("GlobalPrefs", MODE_PRIVATE).getInt("RecentRecordSize", 7)
        selectedDate = MutableLiveData(OffsetDateTime.now())
        repos = TaionNekoRepository(TaionNekoDatabase.getInstance(application), recentSize)
        recentEntries = repos.recentEntries
        averageTemperature = Transformations.map(recentEntries){
            calculateAverageTemperature(it)
        }
        recentEntryDates = Transformations.map(recentEntries){generateRecentEntryDates(it)}
    }

    fun generateRecentEntryDates(vals: List<TemperatureEntry>): List<String>{
        val v=ArrayList<String>()
        vals.forEach { v.add(it.insertDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))) }
        return v
    }

    fun calculateAverageTemperature(vals: List<TemperatureEntry>): Float{
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