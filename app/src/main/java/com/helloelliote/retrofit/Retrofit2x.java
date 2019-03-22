package com.helloelliote.retrofit;

import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Retrofit2x {

    private Retrofit2x() {
    }

    @NotNull
    @Contract(" -> new")
    public static SetBuilder builder() {
        return new BuildSteps();
    }

    public interface SetBuilder {
        SetService setService(@NonNull ServiceStrategy service);
    }

    public interface SetService {
        SetQuery setQuery(@NonNull JsonObject jsonQuery);

        SetQuery setQuery(@NonNull String stringQuery);
    }

    public interface SetQuery {
        RetrofitCore build() throws NullPointerException;
    }

    private static final class BuildSteps implements SetBuilder, SetService, SetQuery {

        private ServiceStrategy service;
        private JsonObject jsonQuery;
        private String stringQuery;

        @Override
        @Contract("_ -> this")
        public SetService setService(@NonNull ServiceStrategy service) {
            this.service = service;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public SetQuery setQuery(@NonNull JsonObject jsonQuery) {
            this.jsonQuery = jsonQuery;
            return this;
        }

        @Override
        @Contract("_ -> this")
        public SetQuery setQuery(@NonNull String stringQuery) {
            this.stringQuery = stringQuery;
            return this;
        }

        @Override
        public RetrofitCore build() {
            RetrofitCore core = RetrofitCore.get();
            if (core.setService(service) && core.setQuery(jsonQuery) || core.setQuery(stringQuery)) {
                return core;
            } else throw new NullPointerException();
        }
    }
}
