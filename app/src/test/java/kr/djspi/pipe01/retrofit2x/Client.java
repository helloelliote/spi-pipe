package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import kr.djspi.pipe01.dto.PipeSupervise;
import kr.djspi.pipe01.dto.Spi;
import kr.djspi.pipe01.dto.SpiLocation;
import kr.djspi.pipe01.retrofit2x.RetrofitUtilTest.OnRetrofitListenerTest;

public class Client {

    private Spi spi;

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

    @Test
    public void doTest() {
        RetrofitUtilTest.get()
                .setService(new SpiPostServiceTest())
                .run(new OnRetrofitListenerTest() {
                    @Override
                    public void onResponse(JsonObject response) {
                        System.out.println(response.toString());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                    }
                });
    }

    @Test
    public void doTest2() {
        JsonObject jsonBounds = new JsonObject();
        jsonBounds.addProperty("sy", 35.869429);
        jsonBounds.addProperty("sx", 128.614516);
        jsonBounds.addProperty("ny", 35.870643);
        jsonBounds.addProperty("nx", 128.615828);

        RetrofitUtil.get()
                .setService(new SpiGetService())
                .setQuery(jsonBounds)
                .run(new RetrofitUtil.OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {
                        System.out.println(response);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.getMessage();
                    }
                });

    }
}
