package kr.djspi.pipe01.dto;

import java.util.ArrayList;

import lombok.Data;

@Data
public class Pipe <T> implements PipeObject, PipeData {

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
    private ArrayList<PipeData> pipeDataList = new ArrayList<>();

    private Spi spi;

    private String header;
    /**
     * 관로 종류: PipeType (enum) 클래스에서 선택된 관로 종류
     * @see PipeType
     */
    private String pipe;
    /**
     * 관로 종류(pipe)에 따라 'mm' 또는 '코어' 등으로 바뀌는 단위값
     */
    private String unit;

    private static PipeType[] pipeTypes;

    public Pipe() {
        if (pipeTypes != null) pipeTypes = PipeType.values();
    }

    public void addPipeData(PipeData pipeData) {
        pipeData.setId(id);
        pipeDataList.add(pipeData);
    }

    public void removePipeData(PipeData pipeData) {
        pipeDataList.remove(pipeData);
    }
}
