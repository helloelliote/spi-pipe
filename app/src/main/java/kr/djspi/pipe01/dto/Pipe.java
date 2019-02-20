package kr.djspi.pipe01.dto;

import java.util.ArrayList;

import lombok.Data;

@Data
public class Pipe <T> implements ListInterface, PipeData {

    private int id;
    /**
     * 관로 종류(pipe)에 따라 '관경' 또는 '전압' 등으로 바뀌는 헤더값
     */
    private ArrayList<PipeData> dataList = new ArrayList<>();

    private Spi spi;

    private double depth;
    private String material;

    private String header;
    /**
     * 관로 종류: PipeType (enum) 클래스에서 선택된 관로 종류
     * @see PipeType
     */
    private String pipe;
    /**
     * 관로 종류(pipe)에 따라 '관경(단위 mm)' 또는 '코어 수' 등으로 바뀌는 정보값
     * 추후 '본관 & 지관' 관경 등을 따로 입력받을 경우 'spec_2' 등의 필드 추가 필요
     * mm 단위를 사용하므로 double 이 아닌 int 형으로 입력
     */
    private int spec;
//    private String spec_2;
    /**
     * 관로 종류(pipe)에 따라 'mm' 또는 '코어' 등으로 바뀌는 단위값
     */
    private String unit;

    private static PipeType[] pipeTypes;

    public Pipe() {
        pipeTypes = PipeType.values();
    }

    public void addPipeData(PipeData pipeData) {
        dataList.add(pipeData);
    }

    public void removePipeData(PipeData pipeData) {
        dataList.remove(pipeData);
    }
}
