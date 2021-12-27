package id.tpusk.headpumpcalculator.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DataEntity (
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "title") val title: String?,
        @ColumnInfo(name = "time") val time: String?,
        @ColumnInfo(name = "data") val data: String
        )