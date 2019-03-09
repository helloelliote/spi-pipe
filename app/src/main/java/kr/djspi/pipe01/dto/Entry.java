package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Entry implements Serializable {
//    private final String request = "spi-set";
    private final Spi spi;
    private final SpiType spi_type;
    private final SpiLocation spi_location;
    private final SpiMemo spi_memo;
//    private final SpiPhoto spi_photo;
    private final Pipe pipe;
    private final PipeType pipe_type;
    private final PipeShape pipe_shape;
    private final PipePosition pipe_position;
    private final PipeSupervise pipe_supervise;
}
