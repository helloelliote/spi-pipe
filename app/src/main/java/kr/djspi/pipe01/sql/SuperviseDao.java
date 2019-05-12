package kr.djspi.pipe01.sql;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SuperviseDao {

    @Query("SELECT * FROM Supervise ORDER BY supervise ASC")
    List<Supervise> getAll();

    @Query("SELECT supervise FROM Supervise WHERE id IN (:userId) LIMIT 1")
    String selectById(int userId);

    @Query("SELECT id FROM Supervise WHERE supervise IN (:supervise) LIMIT 1")
    int selectBySupervise(String supervise);

    @Query("SELECT supervise FROM Supervise LIMIT 1 OFFSET (:row)")
    String selectByRow(int row);

    @Update
    void update(Supervise supervise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Supervise... entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Supervise entity);
}
