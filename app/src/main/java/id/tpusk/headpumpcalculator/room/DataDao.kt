package id.tpusk.headpumpcalculator.room

import androidx.room.*


@Dao
interface DataDao {

    @Query("SELECT * FROM DataEntity ORDER BY id DESC")
    fun getAllData(): MutableList<DataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(dataList: DataEntity)

    @Delete
    suspend fun deleteData(dataList: DataEntity?)
}