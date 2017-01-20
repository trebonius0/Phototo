package phototo.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        try {
            return new Tuple<>(URLDecoder.decode(path, "UTF-8"), PathHelper.splitQuery(query));
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    private static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        if (query == null || query.isEmpty()) {
            return new HashMap<>();
        } else {
            String[] params = query.split("&");
            Map<String, String> map = new HashMap<>();
            for (String param : params) {
                String[] split = param.split("=");
                map.put(URLDecoder.decode(split[0], "UTF-8"), URLDecoder.decode(split.length > 1 ? split[1] : "", "UTF-8"));
            }
            return map;
        }
    }
}
