package kr.djspi.pipe01;

import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

public class NaverMapActivityTest {

    @Before
    public void setUp() {
    }

    @Test
    public void onCreate() {
    }

    @Test
    public void onMapReady() {
    }

    @Test
    public void onBackPressed() {
    }

    @Test
    public void onDestroy() {
    }

    @Test
    public void onLocationUpdate() {
    }

    @Test
    public void getSpiData() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("request", "spi-get");

        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("sy", 35.869429);
        jsonData.addProperty("sx", 128.614516);
        jsonData.addProperty("ny", 35.870643);
        jsonData.addProperty("nx", 128.615828);
        jsonObject.add("data", jsonData);

        System.out.println(jsonObject);
    }
}