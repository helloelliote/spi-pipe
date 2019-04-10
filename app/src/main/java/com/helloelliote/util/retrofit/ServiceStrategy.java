package com.helloelliote.util.retrofit;

import com.google.gson.JsonObject;

import retrofit2.Call;

interface ServiceStrategy {
    Call<JsonObject> getServiceCall();
}
