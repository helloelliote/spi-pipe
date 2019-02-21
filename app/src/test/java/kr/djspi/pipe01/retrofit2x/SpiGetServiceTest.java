package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.RetrofitCore.BUILDER;

public class SpiGetServiceTest implements ServiceStrategy {

    private static final String URL_SPI = "http://192.168.0.33";

    @Before
    public void setUp() {
        jsonBounds = new JsonObject();
//        final LatLngBounds bounds = naverMap.getContentBounds();
//        jsonBounds.addProperty("sy", Math.round(bounds.getSouthLatitude() * 1000000d) / 1000000d);
//        jsonBounds.addProperty("sx", Math.round(bounds.getWestLongitude() * 1000000d) / 1000000d);
//        jsonBounds.addProperty("ny", Math.round(bounds.getNorthLatitude() * 1000000d) / 1000000d);
//        jsonBounds.addProperty("nx", Math.round(bounds.getEastLongitude() * 1000000d) / 1000000d);
        jsonBounds.addProperty("sy", 35.869429);
        jsonBounds.addProperty("sx", 128.614516);
        jsonBounds.addProperty("ny", 35.870643);
        jsonBounds.addProperty("nx", 128.615828);
    }

    private JsonObject jsonBounds;

    @Test
    public Call<JsonObject> getRequest() {
        JsonObject jsonQuery = new JsonObject();
        jsonQuery.addProperty("request", "spi-get");
        jsonQuery.add("data", jsonBounds);
        final String query = jsonQuery.toString();
        return BUILDER.baseUrl(URL_SPI).build()
                .create(RetrofitService.class).getSpiRequest(query);
    }
}