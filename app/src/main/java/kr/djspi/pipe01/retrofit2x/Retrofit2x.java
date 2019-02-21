package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

public final class Retrofit2x {

    private Retrofit2x() {
    }

    public static SetBuilder newBuilder() {
        return new BuildSteps();
    }

    public interface SetBuilder {
        SetService setService(ServiceStrategy service);
    }

    public interface SetService {
        SetQuery setQuery(JsonObject jsonObject);
    }

    public interface SetQuery {
        RetrofitCore build();
    }

    private static final class BuildSteps implements SetBuilder, SetService, SetQuery {

        private ServiceStrategy service;
        private JsonObject jsonQuery;

        @Override
        public SetService setService(ServiceStrategy service) {
            this.service = service;
            return this;
        }

        @Override
        public SetQuery setQuery(JsonObject jsonQuery) {
            this.jsonQuery = jsonQuery;
            return this;
        }

        @Override
        public RetrofitCore build() {
            RetrofitCore core = RetrofitCore.get();
            return !core.setService(service) || !core.setQuery(jsonQuery) ? null : core;
        }
    }
}
