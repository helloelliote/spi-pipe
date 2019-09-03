package kr.djspi.pipe01.sql

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["id"], unique = true)])
class Supervise(
    @field:PrimaryKey
    val id: Int,
    @field:ColumnInfo(name = "supervise")
    val supervise: String
)
