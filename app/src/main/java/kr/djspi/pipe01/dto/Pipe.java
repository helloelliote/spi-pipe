package kr.djspi.pipe01.dto;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Pipe implements PipeData {

    private static final String TAG = Pipe.class.getSimpleName();
    private static String json;
    public final int id;
    public final int type_id;
    public final int spi_id;
    public final int supervise_id;
    public final int construction_id;
    public final int spec;
    public double depth;
    public String material;
    /**
     * 관로 종류(pipe)에 따라 '관경' 또는 '전압' 등으로 바뀌는 헤더값
     */
    private String header;
    /**
     * 관로 종류: PipeType (enum) 클래스에서 선택된 관로 종류
     *
     * @see PipeType
     */
    private String pipe;
    /**
     * 관로 종류(pipe)에 따라 'mm' 또는 '코어' 등으로 바뀌는 단위값
     */
    private String unit;

    public Pipe(int id, int type_id, int spi_id, int supervise_id, int construction_id, int spec) {
        this.id = id;
        this.type_id = type_id;
        this.spi_id = spi_id;
        this.supervise_id = supervise_id;
        this.construction_id = construction_id;
        this.spec = spec;
        toJson();
    }

    private void toJson() {
        json = new Gson().toJson(this);
    }

    public static List<Pipe> initPipeEntryList() {
        ArrayList<String> list = new ArrayList<>();
        list.add(json);
        Type listType = new TypeToken<ArrayList<Pipe>>() {
        }.getType();
        return new Gson().fromJson(list.toString(), listType);
    }
}
