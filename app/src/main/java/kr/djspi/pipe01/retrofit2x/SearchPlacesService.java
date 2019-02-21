package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import retrofit2.Call;

import static kr.djspi.pipe01.retrofit2x.RetrofitCore.BUILDER;
import static kr.djspi.pipe01.retrofit2x.RetrofitCore.jsonQuery;

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
        final String place = jsonQuery.get("place").getAsString();
        final String coordinate = jsonQuery.get("coordinate").getAsString();
        return BUILDER.baseUrl(URL_SEARCH_PLACES).build()
                .create(RetrofitService.class).getSearchPlaces(place, coordinate);
    }
}
