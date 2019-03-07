package kr.djspi.pipe01.dto;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Pipe2 implements DataItem {

    private static String json;
    private int id;
    private int type_id;
    private int spi_id;
    private int supervise_id;
    private int construction_id;
    private int spec;
    private double depth;
    private String material;
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

    public Pipe2() {
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
