package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * (중요) DTO 클래스는 Proguard 난독화 대상에서 제외시켜야 Json 파싱 과정에서 각종 변수명들이 유지된다.
 * proguard-rules.pro: -keep class kr.djspi.pipe01.dto.** { *; }
 */
@Data
public class Entry implements Serializable {
    private final Spi spi;
    private final SpiType spi_type;
    private final SpiLocation spi_location;
    private final SpiMemo spi_memo;
    //    private final SpiPhoto spi_photo;
    private final Pipe pipe;
    private final PipeType pipe_type;
    private final PipeShape pipe_shape;
    private final PipePosition pipe_position;
    private final PipePlan pipe_plan;
    private final PipeSupervise pipe_supervise;
}
