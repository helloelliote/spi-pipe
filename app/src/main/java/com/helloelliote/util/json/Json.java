package com.helloelliote.util.json;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class Json {

    public static String s(@NonNull JsonObject jsonObject, String s) {
        return jsonObject.get(s).getAsString();
    }

    public static int i(@NonNull JsonObject jsonObject, String i) {
        return jsonObject.get(i).getAsInt();
    }

    public static double d(@NonNull JsonObject jsonObject, String d) {
        return jsonObject.get(d).getAsDouble();
    }

    public static float f(@NonNull JsonObject jsonObject, String f) {
        return jsonObject.get(f).getAsFloat();
    }

    public static JsonObject o(@NonNull JsonObject jsonObject, String obj) {
        return jsonObject.get(obj).getAsJsonArray().get(0).getAsJsonObject();
    }

    public static JsonObject o(@NonNull JsonObject jsonObject, String obj, int index) {
        return jsonObject.get(obj).getAsJsonArray().get(index).getAsJsonObject();
    }

    public static JsonArray a(@NonNull JsonObject jsonObject, String a) {
        return jsonObject.get(a).getAsJsonArray();
    }

    public static boolean isNull(@NonNull JsonObject jsonObject, String n) {
        return jsonObject.get(n).isJsonNull();
    }
}
