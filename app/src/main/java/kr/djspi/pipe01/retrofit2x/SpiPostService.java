package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.RetrofitUtil.BUILDER;

public class SpiPostService implements ServiceStrategy {

    private static final String URL_SPI = "https://ispi.kr/";

    @Override
    public Call<JsonObject> getRequest() {
        return BUILDER.baseUrl(URL_SPI).build()
                .create(RetrofitService.class).setSpiRequest();
    }
}
