package kr.djspi.pipe01.retrofit2x;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;

import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.RetrofitUtil.BUILDER;
import static kr.djspi.pipe01.retrofit2x.RetrofitUtil.jsonQuery;

/**
 * 웹서비스를 클래스 형태로 추가하고, ServiceStrategy 인터페이스를 통해 참조시킨다
 *
 * @see RetrofitUtil#BUILDER
 * @see RetrofitUtil#jsonQuery
 * @see RetrofitUtil#setService(ServiceStrategy)
 */
public final class SpiPostService implements ServiceStrategy {

    private static final String URL_SPI = "https://ispi.kr/";
    private static final String URL_TEST = "http://192.168.0.33/";

    @Override
    public Call<JsonObject> getRequest() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("request", "spi-set");
        hashMap.put("data", jsonQuery);
        final String query = new Gson().toJson(hashMap);
        return BUILDER.baseUrl(URL_TEST).build()
                .create(RetrofitService.class).postSpiRequest(query);
    }
}
