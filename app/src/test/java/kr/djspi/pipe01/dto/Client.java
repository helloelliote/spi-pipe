package kr.djspi.pipe01.dto;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class Client {

    private Gson gson;
    private Spi spi;
    private ArrayList<Spi> spiArrayList, resultArrayList;

    @Before

    public void setUp() {
        gson = new Gson();
        spi = new Spi(354);
        spiArrayList = new ArrayList<>();
        resultArrayList = new ArrayList<>();
    }

    @Test
    public void doTest() {
        SpiSupervise spiSupervise = new SpiSupervise();
        spiSupervise.setSupervise("대진기술정보");
        spiSupervise.setContact("053-424-9547");
        String json = gson.toJson(spiSupervise);
        System.out.println(json);
        // {"supervise":"대진기술정보","contact":"053-424-9547"}

        spi.add(spiSupervise);
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

    private String spiString;

    @Test
    public void doTest3() {
        Spi spi_1 = new Spi(234);

        SpiMaterial spiMaterial = new SpiMaterial();
        spiMaterial.setMaterial("알루미늄");
        spi_1.add(spiMaterial);

        SpiSupervise spiSupervise = new SpiSupervise();
        spiSupervise.setSupervise("대진기술정보");
        spiSupervise.setContact("053-424-9547");
        spi_1.add(spiSupervise);

        SpiGeoLocation spiGeoLocation = new SpiGeoLocation();
        spiGeoLocation.setLatitude(36.3333);
        spiGeoLocation.setLongitude(128.4434);
        spi_1.add(spiGeoLocation);

        spiArrayList.add(spi_1);

        Spi spi_2 = new Spi(5567);
        spiMaterial = new SpiMaterial();
        spiMaterial.setMaterial("주철");
        spiSupervise = new SpiSupervise();
        spiSupervise.setSupervise("경동");
        spiSupervise.setContact("053-424-9547");
        spiGeoLocation = new SpiGeoLocation();
        spiGeoLocation.setLatitude(36.1233);
        spiGeoLocation.setLongitude(129.174);
        spi_2.add(spiMaterial).add(spiSupervise).add(spiGeoLocation);

        spiArrayList.add(spi_2);

        JsonArray jsonArray = new JsonArray();

        spiString = gson.toJson(spiArrayList);
//        System.out.println(spiString);
        // [{"material":"알루미늄"},{"supervise":"대진기술정보","contact":"053-424-9547"}]
    }

    @Test
    public void doTest4() {
        doTest3();
        String testString = spiString;
        System.err.println(testString);

        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();

    }
}
