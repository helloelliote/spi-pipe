package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.RetrofitUtil.BUILDER;
import static kr.djspi.pipe01.retrofit2x.RetrofitUtil.queryList;

/**
 * 웹서비스를 클래스 형태로 추가하고, ServiceStrategy 인터페이스를 통해 참조시킨다
 *
 * @see RetrofitUtil#BUILDER
 * @see RetrofitUtil#queryList
 * @see RetrofitUtil#setService(ServiceStrategy)
 */
public final class SpiGetService implements ServiceStrategy {

    private static final String URL_SPI = "https://ispi.kr/";

    @Override
    public Call<JsonObject> getRequest() {
        return BUILDER.baseUrl(URL_SPI).build()
                .create(RetrofitService.class).getSpiRequest(queryList.get(0));
    }
}
