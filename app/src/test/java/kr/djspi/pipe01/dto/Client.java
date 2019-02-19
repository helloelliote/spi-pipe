package kr.djspi.pipe01.dto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
        SpiSupervise spiSupervise = new SpiSupervise();
        spiSupervise.setSupervise("대진기술정보");
        spiSupervise.setContact("053-424-9547");
        String json = gson.toJson(spiSupervise);
        System.out.println(json);
        // {"supervise":"대진기술정보","contact":"053-424-9547"}

        spi.setData(spiSupervise);
        System.err.println(spi.toString());
        // [SpiSupervise(supervise=대진기술정보, contact=053-424-9547)]
        System.out.println(spiSupervise.getSupervise());
        // 대진기술정보
    }

    @Test
    public void doTest2() {
        SpiSupervise spiSupervise = new SpiSupervise();
        spiSupervise.setSupervise("대진기술정보");
        spiSupervise.setContact("053-424-9547");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("supervise", spiSupervise.getSupervise());
        jsonObject.addProperty("contact", spiSupervise.getContact());
        String json2 = gson.toJson(jsonObject);
        System.out.println(json2);
        // {"supervise":"대진기술정보","contact":"053-424-9547"}
    }

    @Test
    public void getSpi() {
        spi = new Spi(234);

        SpiMaterial spiMaterial = new SpiMaterial();
        spiMaterial.setMaterial("알루미늄");
        spi.setData(spiMaterial);

        SpiSupervise spiSupervise = new SpiSupervise();
        spiSupervise.setSupervise("대진기술정보");
        spiSupervise.setContact("053-424-9547");
        spi.setData(spiSupervise);

        SpiGeoLocation spiGeoLocation = new SpiGeoLocation();
        spiGeoLocation.setLatitude(36.3333);
        spiGeoLocation.setLongitude(128.4434);
        spi.setData(spiGeoLocation);

        spiArrayList.add(spi);

        Spi spi_2 = new Spi(5567);
        spiMaterial = new SpiMaterial();
        spiMaterial.setMaterial("주철");
        spiSupervise = new SpiSupervise();
        spiSupervise.setSupervise("경동");
        spiSupervise.setContact("053-424-9547");
        spiGeoLocation = new SpiGeoLocation();
        spiGeoLocation.setLatitude(36.1233);
        spiGeoLocation.setLongitude(129.174);
        spi_2.setData(spiMaterial);
        spi_2.setData(spiSupervise);
        spi_2.setData(spiGeoLocation);

        spiArrayList.add(spi_2);

        String spiString = gson.toJson(spiArrayList);
        System.out.println(spiString);
        // [{"material":"알루미늄"},{"supervise":"대진기술정보","contact":"053-424-9547"}]
    }
}
