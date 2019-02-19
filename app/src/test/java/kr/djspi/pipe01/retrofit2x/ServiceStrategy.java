package kr.djspi.pipe01.retrofit2x;

import com.google.gson.JsonObject;

import retrofit2.Call;

public interface ServiceStrategy {
    Call<JsonObject> getRequest();
}
