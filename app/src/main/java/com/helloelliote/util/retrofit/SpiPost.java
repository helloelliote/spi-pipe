package com.helloelliote.util.retrofit;

import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

import static com.helloelliote.util.retrofit.ApiKey.API_PIPE_SET;
import static com.helloelliote.util.retrofit.RetrofitCore.BUILDER;
import static com.helloelliote.util.retrofit.RetrofitCore.fileQuery;
import static com.helloelliote.util.retrofit.RetrofitCore.stringQuery;

/**
 * 웹서비스를 클래스 형태로 추가하고, ServiceStrategy 인터페이스를 통해 참조시킨다
 *
 * @see RetrofitCore#BUILDER
 * @see RetrofitCore#jsonQuery
 * @see RetrofitCore#setService(ServiceStrategy)
 */
public class SpiPost implements ServiceStrategy {

    private static String url;
    private static final MediaType MULTIPART = MediaType.parse("multipart/form-data");

    public SpiPost(String url) {
        SpiPost.url = url;
    }

    @Override
    public Call<JsonObject> getServiceCall() {
        String query = String.format("{\"request\":%s,\"data\":%s}", API_PIPE_SET, stringQuery);
        RequestBody jsonRequestBody = RequestBody.create(MULTIPART, query);

        MultipartBody.Part fileMultipartBody = null;
        if (fileQuery != null) {
            RequestBody fileRequestBody = RequestBody.create(MULTIPART, fileQuery);
            // MultipartBody.Part is used to send also the actual fileQuery name
            fileMultipartBody = MultipartBody.Part.createFormData("picture", fileQuery.getName(), fileRequestBody);
        }

        return BUILDER.baseUrl(url).build()
                .create(RetrofitService.class).postSpi(jsonRequestBody, fileMultipartBody);
    }
}
