package kr.djspi.pipe01.sql

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["id"], unique = true)])
data class Supervise(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "supervise") val supervise: String
)
