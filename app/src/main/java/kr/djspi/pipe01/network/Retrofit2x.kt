package kr.djspi.pipe01.network

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.djspi.pipe01.network.RetrofitCreator.createRetrofit
import retrofit2.Call

object Retrofit2x {

    private const val URL_SPI = "http://35.200.109.228/"
    private const val URL_SEARCH_PLACES = "https://naveropenapi.apigw.ntruss.com/map-place/v1/"
    private const val URL_REVERSE_GEOCODE =
        "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/"

    fun searchPlaces(query: String, coordinate: String): Call<JsonObject> {
        return createRetrofit(URL_SEARCH_PLACES).searchPlaces(
            query,
            coordinate
        )
    }

    fun reverseGeocode(coords: String, orders: String, output: String): Call<JsonObject> {
        return createRetrofit(URL_REVERSE_GEOCODE).reverseGeocode(
            coords, // default 값(기본값)은 위경도 좌표계(epsg:4326)
            orders, // 좌표 to 도로명 주소(새주소)
            output // default 값(기본값)은 xml
        )
    }

    fun getSuperviseDatabase(): Call<JsonObject> {
        val jsonObject = JsonObject()
        jsonObject.addProperty("request", "supervise-get")
        return createRetrofit(URL_SPI).getSuperviseDatabase(jsonObject.toString())
    }

    fun getSpi(api: String, jsonQuery: JsonElement): Call<JsonObject> {
        val jsonObject = JsonObject()
        jsonObject.addProperty("request", api)
        jsonObject.add("data", jsonQuery)
        return createRetrofit(URL_SPI).getSpi(jsonObject.toString())
    }
}