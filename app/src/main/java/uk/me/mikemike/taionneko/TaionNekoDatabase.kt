package uk.me.mikemike.taionneko

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter


@Database(entities = arrayOf(TemperatureEntry::class), version = 4)
@TypeConverters(TaionNekoConverters::class)
abstract class TaionNekoDatabase : RoomDatabase() {
    abstract fun temperatureEntryDao(): TemperatureEntryDao

    companion object {

        private var instance: TaionNekoDatabase? = null

        fun getInstance(context: Context): TaionNekoDatabase{

            var temp  = instance

            if(temp == null){
                temp = databaseBuilder(context.applicationContext, TaionNekoDatabase::class.java, "taioneko_db").fallbackToDestructiveMigration().build()
                instance = temp
            }

            return temp;
        }

    }

}


class TaionNekoRepository(private val database: TaionNekoDatabase, recentCount: Int) {

    val recentEntries: LiveData<List<TemperatureEntry>> = database.temperatureEntryDao().getMostRecent(recentCount)

    suspend fun add(entry: TemperatureEntry): Long{
        return database.temperatureEntryDao().add(entry)
    }

    suspend fun deleteAll(){
        database.clearAllTables()
    }
}


class TaionNekoConverters {

    private val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun toOffsetDateTime(a: String?): OffsetDateTime?{
        return a?.let{
            dateFormatter.parse(a, OffsetDateTime::from)
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(a:OffsetDateTime): String?{
       return a?.format(dateFormatter)
    }
}