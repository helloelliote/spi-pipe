package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.ApiKey.API_PIPE_SET;
import static kr.djspi.pipe01.retrofit2x.RetrofitCore.BUILDER;
import static kr.djspi.pipe01.retrofit2x.RetrofitCore.stringQuery;

/**
 * 웹서비스를 클래스 형태로 추가하고, ServiceStrategy 인터페이스를 통해 참조시킨다
 *
 * @see RetrofitCore#BUILDER
 * @see RetrofitCore#jsonQuery
 * @see RetrofitCore#setService(ServiceStrategy)
 */
public final class SpiPost implements ServiceStrategy {

    private static String url;

    public SpiPost(String url) {
        SpiPost.url = url;
    }

    @Override
    public Call<JsonObject> getServiceRequest() {
        String query = String.format("{\"request\":%s,\"data\":%s}", API_PIPE_SET, stringQuery);
        return BUILDER.baseUrl(url).build()
                .create(RetrofitService.class).postSpi(query);
    }
}
