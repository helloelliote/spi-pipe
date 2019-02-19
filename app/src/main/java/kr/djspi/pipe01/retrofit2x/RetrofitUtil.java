package kr.djspi.pipe01.retrofit2x;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitUtil {

    private static final String TAG = RetrofitUtil.class.getSimpleName();
    private static ServiceStrategy service;
    static final Retrofit.Builder BUILDER = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create());
    static JsonObject jsonQuery = new JsonObject();

    private RetrofitUtil() {
    }

    /**
     * LazyHolder 를 활용한 초기화 Singleton 패턴
     */
    private static class LazyHolder {
        static final RetrofitUtil INSTANCE = new RetrofitUtil();
    }

    @Contract(pure = true)
    public static RetrofitUtil get() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 결과값인 Response (또는 예외 Throwable) 리스너
     */
    public interface OnRetrofitListener {
        void onResponse(JsonObject response);

        void onFailure(Throwable throwable);
    }

    /**
     * 사용자가 접근하려고 하는 웹서비스를 클래스로 지정
     *
     * @param service 웹서비스(클래스)
     * @return RetrofitUtil 자기 자신을 리턴하는 Builder 패턴
     * @see ServiceStrategy 웹서비스를 Strategy 패턴으로 참조하게 해주는 인터페이스
     */
    public final RetrofitUtil setService(final ServiceStrategy service) {
        RetrofitUtil.service = service;
        return this;
    }

    /**
     * 사용자 입력값: 웹서비스 종류에 따라 2개 이상의 String 을 참조
     *
     * @param jsonQuery 사용자 입력값
     * @return RetrofitUtil 자기 자신을 리턴하는 Builder 패턴
     */
    public final RetrofitUtil setQuery(final JsonObject jsonQuery) {
        RetrofitUtil.jsonQuery = null;
        RetrofitUtil.jsonQuery = jsonQuery;
        return this;
    }

    /**
     * Retrofit Http 통신을 수행
     *
     * @param listener Response (또는 예외 Throwable) 리스너
     * @see OnRetrofitListener
     * @see RetrofitUtil#getRequest 사용자가 지정한 웹서비스에 맞추어 Request 를 생성
     */
    public final void run(OnRetrofitListener listener) {
        if (service == null || jsonQuery == null) {
            Log.e(TAG, "Either Service or Query is Null");
            return;
        }
        Call<JsonObject> request = getRequest(service);
        if (request != null) {
            request.enqueue(new Callback<JsonObject>() {

                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (response.isSuccessful()) listener.onResponse(response.body());
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    listener.onFailure(t);
                }
            });
        }
    }

    private static Call<JsonObject> getRequest(@NonNull ServiceStrategy service) {
        return service.getRequest();
    }
}
