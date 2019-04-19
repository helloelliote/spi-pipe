package com.helloelliote.util.retrofit;

import com.google.gson.JsonObject;

import retrofit2.Call;

import static com.helloelliote.util.retrofit.ApiKey.API_SPI_DELETE;
import static com.helloelliote.util.retrofit.RetrofitCore.BUILDER;

public class SpiDel implements ServiceStrategy {

    private static String url;

    public SpiDel(String url) {
        SpiDel.url = url;
    }

    @Override
    public Call<JsonObject> getServiceCall() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("request", API_SPI_DELETE);
        jsonObject.add("data", RetrofitCore.jsonQuery);
        final String query = jsonObject.toString();
        return BUILDER.baseUrl(url).build()
                .create(RetrofitService.class).deleteSpi(query);
    }
}
