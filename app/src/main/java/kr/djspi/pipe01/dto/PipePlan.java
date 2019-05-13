package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PipePlan implements DataItem, Serializable {

    private int id = -1;
    private int pipe_id = -1;
    /**
     * 평면도(plane) 와 단면도(section) 도면 정보: 아래의 사용자 입력값에 따라 변화되는 값
     */
    private String file_plane;
    private String file_section;
}
