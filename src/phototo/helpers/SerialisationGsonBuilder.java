package phototo.helpers;

import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;

public class SerialisationGsonBuilder {

    public static Gson getGson() {
        return new GsonFireBuilder().enableExposeMethodResult().createGsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }
}
