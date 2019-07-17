package kr.djspi.pipe01.network

import com.google.gson.JsonObject
import retrofit2.Call

class Retrofit2x : RetrofitService {
    override fun searchPlaces(query: String, coordinate: String): Call<JsonObject> {
        return createRetrofit(URL_SEARCH_PLACES).searchPlaces(
            query,
            coordinate
        )
    }

    override fun reverseGeocode(coords: String, orders: String, output: String): Call<JsonObject> {
        return createRetrofit(URL_REVERSE_GEOCODE).reverseGeocode(
            coords, // default 값(기본값)은 위경도 좌표계(epsg:4326)
            orders, // 좌표 to 도로명 주소(새주소)
            output // default 값(기본값)은 xml
        )
    }

    override fun getSuperviseDatabase(jsonString: String): Call<JsonObject> {
        return createRetrofit(URL_SPI).getSuperviseDatabase(
            jsonString
        )
    }

    override fun getServerData(jsonString: String): Call<JsonObject> {
        return createRetrofit(URL_SPI).getServerData(jsonString)
    }

    override fun getSpi(jsonString: String): Call<JsonObject> {
        return createRetrofit(URL_SPI).getSpi(jsonString)
    }

    companion object {
        internal const val URL_SPI = "http://35.200.109.228/"
        internal const val URL_SEARCH_PLACES = "https://naveropenapi.apigw.ntruss.com/map-place/v1/"
        internal const val URL_REVERSE_GEOCODE =
            "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/"
    }
}