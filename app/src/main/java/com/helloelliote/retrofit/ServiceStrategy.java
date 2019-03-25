package com.helloelliote.retrofit;

import com.google.gson.JsonObject;

import retrofit2.Call;

public interface ServiceStrategy {
    Call<JsonObject> getServiceCall();
}
