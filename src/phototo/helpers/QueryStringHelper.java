package phototo.helpers;

import java.util.HashMap;
import java.util.Map;

public class QueryStringHelper {

    public static Map<String, String> splitQuery(String query) {
        if (query == null || query.isEmpty()) {
            return new HashMap<>();
        } else {
            String[] params = query.split("&");
            Map<String, String> map = new HashMap<>();
            for (String param : params) {
                String[] split = param.split("=");
                map.put(split[0], split.length > 1 ? split[1] : "");
            }
            return map;
        }
    }
}
