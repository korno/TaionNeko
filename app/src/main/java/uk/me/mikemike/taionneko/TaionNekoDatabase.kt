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

        const val DATABASE_NAME = "taionneko_db"
        private var instance: TaionNekoDatabase? = null

        fun getInstance(context: Context): TaionNekoDatabase{
            // assigning to a temp variable avoids the need to mark the return as nullable
            var temp  = instance
            if(temp == null){
                temp = databaseBuilder(context.applicationContext, TaionNekoDatabase::class.java, DATABASE_NAME).fallbackToDestructiveMigration().build()
                instance = temp
            }
            return temp;
        }

    }

}


class TaionNekoRepository(private val database: TaionNekoDatabase, limit: Int) {

    val recentEntries: LiveData<List<TemperatureEntry>> = database.temperatureEntryDao().getMostRecent(limit)

    suspend fun add(entry: TemperatureEntry): Long{
        return database.temperatureEntryDao().add(entry)
    }

    suspend fun deleteAll(){
        database.clearAllTables()
    }

    fun getEntriesSince(since: OffsetDateTime, limit: Int): LiveData<List<TemperatureEntry>> {
        return database.temperatureEntryDao().getEntriesSince(since, limit)
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
       return a.format(dateFormatter)
    }
}