package uk.me.mikemike.taionneko

import androidx.lifecycle.LiveData
import androidx.room.*
import org.threeten.bp.OffsetDateTime


/**
 * TemperatureEntry Entity
 */
@Entity(tableName = TemperatureEntryDao.TABLE_NAME)
data class TemperatureEntry (
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "temperature_value") val value: Float,
        @ColumnInfo(name = "inserted_date") val insertDate: OffsetDateTime
)

/**
 * TemperatureEntry Data Access Object
 */
@Dao
interface TemperatureEntryDao {
    companion object{
        const val TABLE_NAME  = "TEMPERATURE_ENTRY"
    }

    @Insert
    suspend fun add(entry: TemperatureEntry): Long

    @Query("SELECT * FROM $TABLE_NAME ORDER BY datetime(inserted_date) ASC LIMIT :count ")
    fun getMostRecent(count: Int): LiveData<List<TemperatureEntry>>

}
