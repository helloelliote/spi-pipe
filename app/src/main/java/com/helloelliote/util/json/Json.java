package com.helloelliote.util.json;

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

    public static float f(@NotNull JsonObject jsonObject, String f) {
        return jsonObject.get(f).getAsFloat();
    }

    public static JsonObject o(@NotNull JsonObject jsonObject, String obj) {
        return jsonObject.get(obj).getAsJsonArray().get(0).getAsJsonObject();
    }

    public static JsonObject o(@NotNull JsonObject jsonObject, String obj, int index) {
        return jsonObject.get(obj).getAsJsonArray().get(index).getAsJsonObject();
    }

    public static JsonArray a(@NotNull JsonObject jsonObject, String a) {
        return jsonObject.get(a).getAsJsonArray();
    }

    public static boolean isNull(@NotNull JsonObject jsonObject, String n) {
        return jsonObject.get(n).isJsonNull();
    }
}