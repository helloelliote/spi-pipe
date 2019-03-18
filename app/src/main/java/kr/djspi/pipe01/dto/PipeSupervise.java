package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PipeSupervise implements DataItem, Serializable {

    /**
     * enum 먼저 생성해서 서버에서 관리처 id (Pipe.class 의 supervise_id) 와 id 에 물린 관리처명을 받아온다.
     */
    private int id;
    /**
     * 관리 기관과 관리기관 연락처
     */
    private String supervise;
}
