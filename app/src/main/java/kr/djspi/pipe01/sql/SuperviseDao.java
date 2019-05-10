package kr.djspi.pipe01.sql;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SuperviseDao {

    @Query("SELECT * FROM Supervise")
    List<Supervise> getAll();

    @Query("SELECT * FROM Supervise WHERE id IN (:userId) LIMIT 1")
    Supervise loadById(int userId);

    @Query("SELECT * FROM Supervise WHERE supervise IN (:supervise) LIMIT 1")
    Supervise loadBySupervise(String supervise);

    @Update
    void update(Supervise supervise);

    @Insert
    void insertAll(Supervise... superviseEntities);

    @Insert
    void insert(Supervise superviseEntities);
}
