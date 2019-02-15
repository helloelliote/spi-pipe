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
public final class SearchPlacesService implements ServiceStrategy {

    private static final String URL_SEARCH_PLACES = "https://naveropenapi.apigw.ntruss.com/map-place/v1/";

    @Override
    public Call<JsonObject> setRequest() {
        return BUILDER.baseUrl(URL_SEARCH_PLACES).build()
                .create(RetrofitService.class).getSearchPlacesRequest(queryList.get(0), queryList.get(1));
    }
}
