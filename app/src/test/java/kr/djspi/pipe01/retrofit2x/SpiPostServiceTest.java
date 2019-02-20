package kr.djspi.pipe01.retrofit2x;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import kr.djspi.pipe01.dto.Spi;
import kr.djspi.pipe01.dto.SpiLocation;
import kr.djspi.pipe01.dto.SpiMaterial;
import kr.djspi.pipe01.dto.SpiSupervise;
import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.RetrofitUtil.BUILDER;

public class SpiPostServiceTest implements ServiceStrategy {

    private static final String URL_SPI = "http://192.168.0.33/";

    @Before
    public void setUp() {
        spi = new Spi(234);

        SpiMaterial spiMaterial = new SpiMaterial();
        spiMaterial.setMaterial("알루미늄");
        spi.setData(spiMaterial);

        SpiSupervise spiSupervise = new SpiSupervise();
        spiSupervise.setSupervise("대진기술정보");
        spiSupervise.setContact("053-424-9547");
        spi.setData(spiSupervise);

        SpiLocation spiLocation = new SpiLocation();
        spiLocation.setLatitude(36.3333);
        spiLocation.setLongitude(128.4434);
        spi.setData(spiLocation);
    }

    private Spi spi;

    @Test
    public Call<JsonObject> getRequest() {
        spi = new Spi(234);

        SpiMaterial spiMaterial = new SpiMaterial();
        spiMaterial.setMaterial("알루미늄");
        spi.setData(spiMaterial);

        SpiSupervise spiSupervise = new SpiSupervise();
        spiSupervise.setSupervise("대진기술정보");
        spiSupervise.setContact("053-424-9547");
        spi.setData(spiSupervise);

        SpiLocation spiLocation = new SpiLocation();
        spiLocation.setLatitude(36.3333);
        spiLocation.setLongitude(128.4434);
        spi.setData(spiLocation);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("request", "spi-set");
        hashMap.put("data", spi);
        final String query = new Gson().toJson(hashMap);
        return BUILDER.baseUrl(URL_SPI).build()
                .create(RetrofitService.class).postSpiRequest(query);
    }
}