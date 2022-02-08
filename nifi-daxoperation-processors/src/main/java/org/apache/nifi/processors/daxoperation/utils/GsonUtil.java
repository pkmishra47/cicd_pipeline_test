package org.apache.nifi.processors.daxoperation.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
    private static GsonBuilder gsonBuilder = new GsonBuilder();

    public static Gson getGson() {
        return gsonBuilder.serializeNulls().create();
    }
}
