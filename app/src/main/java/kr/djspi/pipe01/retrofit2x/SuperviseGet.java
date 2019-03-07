package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.RetrofitCore.BUILDER;

/**
 * 웹서비스를 클래스 형태로 추가하고, ServiceStrategy 인터페이스를 통해 참조시킨다
 *
 * @see RetrofitCore#BUILDER
 * @see RetrofitCore#jsonQuery
 * @see RetrofitCore#setService(ServiceStrategy)
 */
public final class SuperviseGet implements ServiceStrategy {

    private static String url;

    public SuperviseGet(String url) {
        SuperviseGet.url = url;
    }

    @Override
    public Call<JsonObject> getServiceRequest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("request", "supervise-get");
        final String query = jsonObject.toString();
        return BUILDER.baseUrl(url).build()
                .create(RetrofitService.class).getSupervise(query);
    }
}
