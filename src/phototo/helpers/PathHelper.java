package phototo.helpers;

import java.util.HashMap;
import java.util.Map;

public class PathHelper {

    public static Tuple<String, Map<String, String>> splitPathAndQuery(String pathAndQuery) {
        String path;
        String query;
        int p = pathAndQuery.indexOf("?");
        if (p == -1) {
            path = pathAndQuery;
            query = null;
        } else {
            path = pathAndQuery.substring(0, p);
            query = pathAndQuery.substring(p + 1);
        }

        return new Tuple<>(path, PathHelper.splitQuery(query));
    }

    private static Map<String, String> splitQuery(String query) {
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
