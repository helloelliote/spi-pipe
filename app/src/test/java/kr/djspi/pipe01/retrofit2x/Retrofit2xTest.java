package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

public class Retrofit2xTest {

    public static SetServiceStep newBuilder() {
        return new Steps();
    }

    private Retrofit2xTest() {

    }

    public interface SetServiceStep {
        SetQueryStep setService(ServiceStrategy strategy);
    }

    public interface SetQueryStep {
        BuildStep setQuery(JsonObject jsonObject);
    }

    public interface BuildStep {
        RetrofitCoreTest run(RetrofitCoreTest.OnRetrofitListenerTest listenerTest);
    }

    private static class Steps implements SetServiceStep, SetQueryStep, BuildStep {

        private ServiceStrategy service;
        private JsonObject jsonQuery;

        @Override
        public SetQueryStep setService(ServiceStrategy service) {
            this.service = service;
            return this;
        }

        @Override
        public BuildStep setQuery(JsonObject jsonObject) {
            this.jsonQuery = jsonObject;
            return this;
        }

        @Override
        public RetrofitCoreTest run(RetrofitCoreTest.OnRetrofitListenerTest listenerTest) {
            RetrofitCoreTest test = new RetrofitCoreTest();
            test.setService(service);
            test.setQuery("");
            return test;
        }
    }
}
