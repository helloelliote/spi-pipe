package kr.djspi.pipe01.sql

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SuperviseDao {

    @get:Query("SELECT * FROM Supervise ORDER BY supervise ASC")
    val all: List<Supervise>

    @Query("SELECT supervise FROM Supervise WHERE id IN (:userId) LIMIT 1")
    fun selectById(userId: Int): String

    @Query("SELECT id FROM Supervise WHERE supervise IN (:supervise) LIMIT 1")
    fun selectBySupervise(supervise: String?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg entity: Supervise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: Supervise)
}
