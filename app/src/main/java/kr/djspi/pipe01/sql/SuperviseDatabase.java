package kr.djspi.pipe01.sql;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Supervise.class}, version = 1)
public abstract class SuperviseDatabase extends RoomDatabase {
    public abstract SuperviseDao dao();
}
