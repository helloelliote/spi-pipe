package kr.djspi.pipe01.retrofit2x;

import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitUtilTest {

    private static ServiceStrategy service;
    public static ArrayList<Object> queryListTest = new ArrayList<>();

    @Before
    public void setUp() {
    }

    public interface OnRetrofitListenerTest {
        void onResponse(JsonObject response);

        void onFailure(Throwable throwable);
    }

    private static class LazyHolder {
        static final RetrofitUtilTest INSTANCE = new RetrofitUtilTest();
    }

    @Contract(pure = true)
    public static RetrofitUtilTest get() {
        return RetrofitUtilTest.LazyHolder.INSTANCE;
    }

    @Test
    public RetrofitUtilTest setService(ServiceStrategy service) {
        RetrofitUtilTest.service = service;
        return this;
    }

    @Test
    public RetrofitUtilTest setQuery(Object... objects) {
        queryListTest.clear();
        queryListTest.addAll(Arrays.asList(objects));
        return this;
    }

    @Test
    public void run(OnRetrofitListenerTest listener) {
        if (service == null || queryListTest.isEmpty()) {
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