package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PipePlan implements DataItem, Serializable {

    public int id = -1;
    public int pipe_id = -1;
    /**
     * 평면도(plane) 와 단면도(section) 도면 정보: 아래의 사용자 입력값에 따라 변화되는 값
     */
    public String file_plane;
    public String file_section;
}
