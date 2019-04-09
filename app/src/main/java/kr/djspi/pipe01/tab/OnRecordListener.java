package kr.djspi.pipe01.tab;

import android.net.Uri;

import com.google.gson.JsonObject;

public interface OnRecordListener {
    JsonObject getJsonObjectRecord();

    Uri getPhotoUri();

    void onRecord(String tag, int result);
}
