package kr.djspi.pipe01.sql

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Supervise::class], version = 2)
abstract class SuperviseDatabase : RoomDatabase() {
    abstract fun dao(): SuperviseDao
}
