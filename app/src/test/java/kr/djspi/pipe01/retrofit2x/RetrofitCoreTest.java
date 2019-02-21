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

/**
 * RetrofitCoreTest 는 클래스 내부 빌더 패턴(return this;)을 사용해 서비스와 쿼리를 지정해준다.
 * 이는 빌더 패턴의 편리함은 있으나, 빌더를 통해 최종적으로 run() 을 실행하는 과정에서
 * #setService() 나 #setQuery() 등이 누락되어도 검증할 방법이 없어 런타임 예외가 발생한다.
 *
 * 이를 해결하기 위해 새로운 Step SetQuery 패턴을 활용한다.
 * http://rdafbn.blogspot.com/2012/07/step-builder-pattern_28.html
 */
@Deprecated
public class RetrofitCoreTest {

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
        static final RetrofitCoreTest INSTANCE = new RetrofitCoreTest();
    }

    @Contract(pure = true)
    public static RetrofitCoreTest get() {
        return RetrofitCoreTest.LazyHolder.INSTANCE;
    }

    @Test
    public RetrofitCoreTest setService(ServiceStrategy service) {
        RetrofitCoreTest.service = service;
        return this;
    }

    /**
     * @param strings 이전 버전: 쿼리를 String 으로 콤마로 구분해 입력
     *                현재 버전: 쿼리를 클라이언트에서 JsonObject#addProperty() 로 추가한 다음,
     *                단일 JsonObject 객체로 입력
     */
    @Test
    public RetrofitCoreTest setQuery(String ... strings) {
        queryListTest.clear();
        queryListTest.addAll(Arrays.asList(strings));
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
        return service.getServiceRequest();
    }
}