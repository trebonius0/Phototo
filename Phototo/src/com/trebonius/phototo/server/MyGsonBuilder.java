package com.trebonius.phototo.server;

import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;

public class MyGsonBuilder {

    public static Gson getGson() {
        return new GsonFireBuilder().enableExposeMethodResult().createGsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }
}
