package kr.djspi.pipe01.sql

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SuperviseDao {

    @Query("SELECT * FROM Supervise ORDER BY supervise ASC")
    fun getAll(): List<Supervise>

    @Query("SELECT supervise FROM Supervise WHERE id = :userId LIMIT 1")
    fun selectById(userId: Int): String

    @Query("SELECT id FROM Supervise WHERE supervise = :supervise LIMIT 1")
    fun selectBySupervise(supervise: String?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg entity: Supervise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: Supervise)
}
