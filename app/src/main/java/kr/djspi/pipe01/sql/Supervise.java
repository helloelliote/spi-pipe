package kr.djspi.pipe01.sql;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"id"}, unique = true)})
public class Supervise {

    @PrimaryKey
    private int id;
    @ColumnInfo(name = "supervise")
    private String supervise;

    public Supervise(int id, String supervise) {
        this.id = id;
        this.supervise = supervise;
    }

    public int getId() {
        return id;
    }

    public String getSupervise() {
        return supervise;
    }
}
