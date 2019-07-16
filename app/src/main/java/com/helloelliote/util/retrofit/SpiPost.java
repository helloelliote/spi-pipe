package com.helloelliote.util.retrofit;

import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

import static com.helloelliote.util.retrofit.ApiKey.API_PIPE_SET;
import static com.helloelliote.util.retrofit.RetrofitCore.BUILDER;
import static com.helloelliote.util.retrofit.RetrofitCore.multipartBody;
import static com.helloelliote.util.retrofit.RetrofitCore.stringQuery;

public class SpiPost implements ServiceStrategy {

    private static String url;

    public SpiPost(String url) {
        SpiPost.url = url;
    }

    @Override
    public Call<JsonObject> getServiceCall() {
        String query = String.format("{\"request\":%string,\"data\":%string}", API_PIPE_SET, stringQuery);
        RequestBody jsonRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), query);

        return BUILDER.baseUrl(url).build()
                .create(RetrofitService.class).postSpi(jsonRequestBody, multipartBody);
    }
}
