package kr.djspi.pipe01;

import org.junit.Before;
import org.junit.Test;

import kr.djspi.pipe01.dto.Spi;
import kr.djspi.pipe01.dto.SpiLocation;
import kr.djspi.pipe01.dto.SpiMemo;
import kr.djspi.pipe01.dto.SpiType;

public class RecordInputActivityTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void setJsonTest() {
        // pipe, shape, horizontal, vertical, depth, spec, material,
        //            supervise, supervise_contact, spi_memo, construction, construction_contact

        Spi spi = new Spi(304);
        spi.setSerial("38:4F:HH:4F:D3:22:D9");

        SpiType spiType = new SpiType();
        spiType.setType("표지판");

        SpiLocation spiLocation = new SpiLocation();
        spiLocation.setLatitude(36.555552);
        spiLocation.setLongitude(128.445444);

        SpiMemo spiMemo = new SpiMemo();
        spiMemo.setMemo("테스트용 메모");
//        Pipe2 pipe = new Pipe2();
//        pipe.
    }
}