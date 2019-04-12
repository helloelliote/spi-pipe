package com.helloelliote.util.retrofit;

import com.google.gson.JsonObject;

import retrofit2.Call;

import static com.helloelliote.util.retrofit.ApiKey.API_SPI_INIT;
import static com.helloelliote.util.retrofit.RetrofitCore.BUILDER;
import static com.helloelliote.util.retrofit.RetrofitCore.stringQuery;

/**
 * 웹서비스를 클래스 형태로 추가하고, ServiceStrategy 인터페이스를 통해 참조시킨다
 *
 * 특이사항: 초기화 데이터가 정상적으로 등록된 후, Response Body 에 해당하는 데이터가
 * 에는 반드시
 *
 * @see RetrofitCore#BUILDER
 * @see RetrofitCore#jsonQuery
 * @see RetrofitCore#setService(ServiceStrategy)
 */
public final class SpiInit implements ServiceStrategy {

    private static String url;

    public SpiInit(String url) {
        SpiInit.url = url;
    }

    @Override
    public Call<JsonObject> getServiceCall() {
        String query = String.format("{\"request\":%s,\"data\":%s}", API_SPI_INIT, stringQuery);
        return BUILDER.baseUrl(url).build()
                .create(RetrofitService.class).initSpi(query);
    }
}
