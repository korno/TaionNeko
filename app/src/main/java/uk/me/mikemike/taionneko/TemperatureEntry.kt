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

import androidx.lifecycle.LiveData
import androidx.room.*
import org.threeten.bp.OffsetDateTime



@Entity(tableName = TemperatureEntryDao.TABLE_NAME)
data class TemperatureEntry (

    @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = TEMPERATURE_FIELD) val value: Float,
        @ColumnInfo(name = INSERT_DATE_FIELD) val insertDate: OffsetDateTime
){
    companion object{
        const val INSERT_DATE_FIELD = "inserted_date"
        const val TEMPERATURE_FIELD = "temperature_value"
    }
}


@Dao
interface TemperatureEntryDao {
    companion object{
        const val TABLE_NAME  = "TEMPERATURE_ENTRY"
    }

    @Insert
    suspend fun add(entry: TemperatureEntry): Long

    @Query("SELECT * FROM $TABLE_NAME ORDER BY datetime(${TemperatureEntry.INSERT_DATE_FIELD}) ASC LIMIT :count ")
    fun getMostRecent(count: Int): LiveData<List<TemperatureEntry>>

    @Query("SELECT * FROM (SELECT * FROM $TABLE_NAME  ORDER BY " +
            " datetime(${TemperatureEntry.INSERT_DATE_FIELD}) DESC LIMIT :limit) X  WHERE datetime(${TemperatureEntry.INSERT_DATE_FIELD}) > datetime(:since) ORDER BY datetime(${TemperatureEntry.INSERT_DATE_FIELD}) ASC")
    fun getEntriesSince(since: OffsetDateTime, limit: Int): LiveData<List<TemperatureEntry>>

}
