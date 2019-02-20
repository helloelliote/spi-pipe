package kr.djspi.pipe01.dto;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class Client {

    private Gson gson;
    private Spi spi;
    private ArrayList<Spi> spiArrayList;

    @Before

    public void setUp() {
        gson = new Gson();
        spi = new Spi(354);
        spiArrayList = new ArrayList<>();
    }

    @Test
    public void doTest() {
        Spi spi = new Spi(2445);
        SpiType spiType = new SpiType();
        spiType.setType("표지판");
        spi.addSpiData(spiType);
        Pipe sangsu = new Pipe();
        sangsu.addPipeData(new PipeConstruction());
        spi.addPipe(sangsu);
    }

    @Test
    public void doTest2() {

    }

    @Test
    public void getSpi() {
        String spiString = gson.toJson(spiArrayList);
        System.out.println(spiString);
        // [{"material":"알루미늄"},{"supervise":"대진기술정보","contact":"053-424-9547"}]
    }
}
