package kr.djspi.pipe01.retrofit2x;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import kr.djspi.pipe01.dto.PipeSupervise;
import kr.djspi.pipe01.dto.Spi;
import kr.djspi.pipe01.dto.SpiLocation;
import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.RetrofitUtil.BUILDER;

public class SpiPostServiceTest implements ServiceStrategy {

    private static final String URL_SPI = "http://192.168.0.33/";

    @Before
    public void setUp() {
        spi = new Spi(234);

        PipeMaterial pipeMaterial = new PipeMaterial();
        pipeMaterial.setMaterial("알루미늄");
        spi.addSpiData(pipeMaterial);

        PipeSupervise pipeSupervise = new PipeSupervise();
        pipeSupervise.setSupervise("대진기술정보");
        pipeSupervise.setContact("053-424-9547");
        spi.addSpiData(pipeSupervise);

        SpiLocation spiLocation = new SpiLocation();
        spiLocation.setLatitude(36.3333);
        spiLocation.setLongitude(128.4434);
        spi.addSpiData(spiLocation);
    }

    private Spi spi;

    @Test
    public Call<JsonObject> getRequest() {
        spi = new Spi(234);

        PipeMaterial pipeMaterial = new PipeMaterial();
        pipeMaterial.setMaterial("알루미늄");
        spi.addSpiData(pipeMaterial);

        PipeSupervise pipeSupervise = new PipeSupervise();
        pipeSupervise.setSupervise("대진기술정보");
        pipeSupervise.setContact("053-424-9547");
        spi.addSpiData(pipeSupervise);

        SpiLocation spiLocation = new SpiLocation();
        spiLocation.setLatitude(36.3333);
        spiLocation.setLongitude(128.4434);
        spi.addSpiData(spiLocation);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("request", "spi-set");
        hashMap.put("data", spi);
        final String query = new Gson().toJson(hashMap);
        return BUILDER.baseUrl(URL_SPI).build()
                .create(RetrofitService.class).postSpiRequest(query);
    }
}