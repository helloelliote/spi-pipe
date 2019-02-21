package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import kr.djspi.pipe01.dto.Spi;
import retrofit2.Call;

public class SpiPostServiceTest implements ServiceStrategy {

    private static final String URL_SPI = "http://192.168.0.33/";

    @Before
    public void setUp() {

    }

    private Spi spi;

    @Test
    public Call<JsonObject> getServiceRequest() {
        return null;
    }
}