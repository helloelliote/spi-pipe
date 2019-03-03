package kr.djspi.pipe01.retrofit2x;

import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Retrofit2x {

    private Retrofit2x() {
    }

    @NotNull
    @Contract(" -> new")
    public static SetBuilder newBuilder() {
        return new BuildSteps();
    }

    public interface SetBuilder {
        SetService setService(@NonNull ServiceStrategy service);
    }

    public interface SetService {
        SetQuery setQuery(@NonNull JsonObject jsonQuery);
    }

    public interface SetQuery {
        @NonNull
        RetrofitCore build() throws NullPointerException;
    }

    private static final class BuildSteps implements SetBuilder, SetService, SetQuery {

        private ServiceStrategy service;
        private JsonObject jsonQuery;

        @Contract("_ -> this")
        @Override
        public SetService setService(@NonNull ServiceStrategy service) {
            this.service = service;
            return this;
        }

        @Contract("_ -> this")
        @Override
        public SetQuery setQuery(@NonNull JsonObject jsonQuery) {
            this.jsonQuery = jsonQuery;
            return this;
        }

        @Override
        public @NonNull
        RetrofitCore build() {
            RetrofitCore core = RetrofitCore.get();
            if (!core.setService(service) || !core.setQuery(jsonQuery)) {
                throw new NullPointerException();
            } else return core;
        }
    }
}
