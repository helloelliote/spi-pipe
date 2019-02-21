package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import kr.djspi.pipe01.retrofit2x.RetrofitCoreTest.OnRetrofitListenerTest;

public class Client {

    @Before
    public void setUp() {

    }

    /**
     * RetrofitCoreTest 는 클래스 내부 빌더 패턴(return this;)을 사용해 서비스와 쿼리를 지정해준다.
     * 이는 빌더 패턴의 편리함은 있으나, 빌더를 통해 최종적으로 run() 을 실행하는 과정에서
     * #setService() 나 #setQuery() 등이 누락되어도 검증할 방법이 없어 런타임 예외가 발생한다.
     * @see RetrofitCoreTest#get()
     *
     * 이를 해결하기 위해 새로운 Step SetQuery 패턴을 활용한다.
     * http://rdafbn.blogspot.com/2012/07/step-builder-pattern_28.html
     * @see Retrofit2x#newBuilder()
     * @see RetrofitCore
     */
    @Test
    public void doTest() {
        RetrofitCoreTest.get()
                .setService(new SpiPostServiceTest())
                .setQuery("")
                .run(new OnRetrofitListenerTest() {
                    @Override
                    public void onResponse(JsonObject response) {
                        System.out.println(response.toString());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                    }
                });
    }

    @Test
    public void doTest2() {
        JsonObject jsonBounds = new JsonObject();
        jsonBounds.addProperty("sy", 35.869429);
        jsonBounds.addProperty("sx", 128.614516);
        jsonBounds.addProperty("ny", 35.870643);
        jsonBounds.addProperty("nx", 128.615828);
        System.out.println(jsonBounds);
    }

    @Test
    public void doTest3() {
        RetrofitCore retrofit2x =
                Retrofit2x.newBuilder()
                .setService(new SpiGetServiceTest())
                .setQuery(new JsonObject())
                .build();
        retrofit2x.run(new RetrofitCore.OnRetrofitListener() {
                    @Override
                    public void onResponse(JsonObject response) {

                    }

                    @Override
                    public void onFailure(Throwable throwable) {

                    }
                });
//        Assert.assertThat();
    }
}
