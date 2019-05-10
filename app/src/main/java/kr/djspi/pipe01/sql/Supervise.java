package kr.djspi.pipe01.sql;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"id", "supervise"}, unique = true)})
public class Supervise {

    @PrimaryKey
    private int id;
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
