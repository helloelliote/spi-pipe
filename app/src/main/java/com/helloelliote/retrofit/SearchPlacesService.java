package com.helloelliote.retrofit;

import com.google.gson.JsonObject;

import retrofit2.Call;

import static com.helloelliote.retrofit.RetrofitCore.BUILDER;

/**
 * 웹서비스를 클래스 형태로 추가하고, ServiceStrategy 인터페이스를 통해 참조시킨다
 *
 * @see RetrofitCore#BUILDER
 * @see RetrofitCore#jsonQuery
 * @see RetrofitCore#setService(ServiceStrategy)
 */
public final class SearchPlacesService implements ServiceStrategy {

    private static final String URL_SEARCH_PLACES = "https://naveropenapi.apigw.ntruss.com/map-place/v1/";

    @Override
    public Call<JsonObject> getServiceRequest() {
        final String place = RetrofitCore.jsonQuery.get("place").getAsString();
        final String coordinate = RetrofitCore.jsonQuery.get("coordinate").getAsString();
        return BUILDER.baseUrl(URL_SEARCH_PLACES).build()
                .create(RetrofitService.class).getSearchPlaces(place, coordinate);
    }
}
