package kr.djspi.pipe01.retrofit2x;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitCore {

    private static final String TAG = RetrofitCore.class.getSimpleName();
    private static ServiceStrategy service;
    private static final Gson gson = new GsonBuilder().setLenient().create();
    static final Retrofit.Builder BUILDER = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson));
    static JsonObject jsonQuery;
    static String stringQuery;

    private RetrofitCore() {
    }

    /**
     * LazyHolder 를 활용한 초기화 Singleton 패턴
     */
    private static class LazyHolder {
        static final RetrofitCore INSTANCE = new RetrofitCore();
    }

    @Contract(pure = true)
    public static RetrofitCore get() {
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
     * 사용자가 접근하려고 하는 네트워크 서비스를 클래스로 지정
     *
     * @param service 네트워크 서비스(클래스)
     * @see ServiceStrategy Strategy 패턴 인터페이스
     */
    final boolean setService(final ServiceStrategy service) {
        RetrofitCore.service = service;
        return service != null;
    }

    /**
     * 사용자 입력값(Json) 지정
     *
     * @param jsonQuery 사용자 입력값
     */
    final boolean setQuery(final JsonObject jsonQuery) {
        RetrofitCore.jsonQuery = null;
        RetrofitCore.jsonQuery = jsonQuery;
        return jsonQuery != null;
    }

    /**
     * 사용자 입력값(String) 지정
     *
     * @param stringQuery 사용자 입력값
     */
    final boolean setQuery(final String stringQuery) {
        RetrofitCore.stringQuery = null;
        RetrofitCore.stringQuery = stringQuery;
        return stringQuery != null;
    }

    /**
     * Retrofit Http 통신을 수행
     *
     * @param listener Response (또는 예외 Throwable) 리스너
     * @see OnRetrofitListener
     * @see ServiceStrategy#getServiceRequest 사용자가 지정한 네트워크 서비스에 맞추어 Request 를 생성
     */
    public final void run(OnRetrofitListener listener) {
        final Call<JsonObject> request = service.getServiceRequest();
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
}
