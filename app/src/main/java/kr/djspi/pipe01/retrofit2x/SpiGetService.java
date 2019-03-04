package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.RetrofitCore.BUILDER;
import static kr.djspi.pipe01.retrofit2x.RetrofitCore.jsonQuery;

/**
 * 웹서비스를 클래스 형태로 추가하고, ServiceStrategy 인터페이스를 통해 참조시킨다
 *
 * @see RetrofitCore#BUILDER
 * @see RetrofitCore#jsonQuery
 * @see RetrofitCore#setService(ServiceStrategy)
 */
public final class SpiGetService implements ServiceStrategy {

    private static String url;

    public SpiGetService(String url) {
        SpiGetService.url = url;
    }

    @Override
    public Call<JsonObject> getServiceRequest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("request", "spi-get");
        jsonObject.add("data", jsonQuery);
        final String query = jsonObject.toString();
        return BUILDER.baseUrl(url).build()
                .create(RetrofitService.class).getSpi(query);
    }
}
