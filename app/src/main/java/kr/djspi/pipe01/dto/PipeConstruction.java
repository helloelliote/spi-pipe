package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class PipeConstruction implements DataItem {

    private int id;
    /**
     * (선택입력) 시공업체, 시공업체 연락처 정보: 글자수 제한 필요
     */
    private String construction;
    private String contact;
}
