package kr.djspi.pipe01.network

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kr.djspi.pipe01.network.RetrofitCreator.createRetrofit
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import retrofit2.Call

object Retrofit2x : RequestBody() {

    private const val URL_SPI = "http://35.200.109.228/"
    // 2020년 6월 1일 NAVER "Search Places" 서비스 종료로, kakao 의 "키워드로 장소 검색" 서비스로 대체
    // https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-keyword
    // App 등록: https://developers.kakao.com/console/app/445410
    private const val URL_SEARCH_PLACES = "https://dapi.kakao.com/v2/local/search/"
    private const val URL_REVERSE_GEOCODE =
        "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/"

    fun searchPlaces(query: String, x: String, y: String, size: Int = 5): Call<JsonObject> {
        return createRetrofit(URL_SEARCH_PLACES).searchPlaces(
            query,
            x,
            y,
            size
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

    fun postSpi(query: String, part: MultipartBody.Part? = null): Call<JsonObject> {
        val stringQuery = "{\"request\":\"pipe-set\",\"data\":$query}"
        val requestBody: RequestBody = create(MediaType.parse("multipart/form-data"), stringQuery)
        return createRetrofit(URL_SPI).postSpi(requestBody, part)
    }

    override fun contentType(): MediaType? {
        return null
    }

    override fun writeTo(sink: BufferedSink) {
    }
}
