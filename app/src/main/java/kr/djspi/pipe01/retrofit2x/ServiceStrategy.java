package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import retrofit2.Call;

interface ServiceStrategy {
    Call<JsonObject> getServiceRequest();
}
