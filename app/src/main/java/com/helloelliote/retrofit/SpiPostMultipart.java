package com.helloelliote.retrofit;

import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

import static com.helloelliote.retrofit.ApiKey.API_PIPE_SET;
import static com.helloelliote.retrofit.RetrofitCore.BUILDER;
import static com.helloelliote.retrofit.RetrofitCore.fileQuery;
import static com.helloelliote.retrofit.RetrofitCore.stringQuery;

public class SpiPostMultipart implements ServiceStrategy {

    private static String url;
    private static final MediaType MULTIPART = MediaType.parse("multipart/form-data");

    public SpiPostMultipart(String url) {
        SpiPostMultipart.url = url;
    }

    @Override
    public Call<JsonObject> getServiceCall() {
        String query = String.format("{\"request\":%s,\"data\":%s}", API_PIPE_SET, stringQuery);
        RequestBody jsonRequestBody = RequestBody.create(MULTIPART, query);

        RequestBody fileRequestBody = RequestBody.create(MULTIPART, fileQuery);
        // MultipartBody.Part is used to send also the actual fileQuery name
        MultipartBody.Part fileMultipartBody = MultipartBody.Part.createFormData("picture", fileQuery.getName(), fileRequestBody);

        return BUILDER.baseUrl(url).build()
                .create(RetrofitService.class).postSpiMultipart(jsonRequestBody, fileMultipartBody);
    }
}
