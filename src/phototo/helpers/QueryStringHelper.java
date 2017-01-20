package phototo.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class QueryStringHelper {

    public static Map<String, String> splitQuery(String query) {
        if (query == null || query.isEmpty()) {
            return new HashMap<>();
        } else {
            try {
                return URLEncodedUtils.parse(new URI(query), "UTF-8").stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
            } catch (URISyntaxException ex) {
                return new HashMap<>();
            }
        }
    }
}
