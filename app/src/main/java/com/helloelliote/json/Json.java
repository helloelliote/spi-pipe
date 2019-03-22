package com.helloelliote.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

public final class Json {

    public static String s(@NotNull JsonObject jsonObject, String s) {
        return jsonObject.get(s).getAsString();
    }

    public static int i(@NotNull JsonObject jsonObject, String i) {
        return jsonObject.get(i).getAsInt();
    }

    public static double d(@NotNull JsonObject jsonObject, String d) {
        return jsonObject.get(d).getAsDouble();
    }

    public static JsonObject o(@NotNull JsonObject jsonObject, String obj) {
        return jsonObject.get(obj).getAsJsonArray().get(0).getAsJsonObject();
    }

    public static JsonArray a(@NotNull JsonObject jsonObject, String a) {
        return jsonObject.get(a).getAsJsonArray();
    }

    public static boolean isNull(@NotNull JsonObject jsonObject, String n) {
        return jsonObject.get(n).isJsonNull();
    }
}
