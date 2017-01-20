package phototo.controllers;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import phototo.helpers.PathHelper;
import phototo.helpers.Tuple;

public abstract class PhototoHandler implements HttpRequestHandler {

    protected static final Response http403 = new Response(403, new StringEntity("<html><body><h1>Forbidden/h1></body></html>", ContentType.create("text/html", "UTF-8")));
    protected static final Response http404 = new Response(404, new StringEntity("<html><body><h1>Not found</h1></body></html>", ContentType.create("text/html", "UTF-8")));
    protected static final Response http500 = new Response(500, new StringEntity("<html><body><h1>Server error</h1></body></html>", ContentType.create("text/html", "UTF-8")));

    protected static class Response {

        public final int responseCode;
        public final HttpEntity entity;
        public final Header[] headers;

        public Response(int responseCode, HttpEntity entity, Header... headers) {
            this.responseCode = responseCode;
            this.entity = entity;
            this.headers = headers;
        }

    }
    protected final String prefix;
    private final Set<String> allowedVerbs;

    public PhototoHandler(String prefix, String[] allowedVerbs) {
        this.prefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        this.allowedVerbs = new HashSet<>(Arrays.asList(allowedVerbs));
    }

    @Override
    public final void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!this.allowedVerbs.contains(request.getRequestLine().getMethod().toUpperCase())) {
            response.setStatusCode(http403.responseCode);
            response.setEntity(http403.entity);
        } else {
            String target = URLDecoder.decode(request.getRequestLine().getUri(), "UTF-8");
            Tuple<String, Map<String, String>> pathAndQueryTuple = PathHelper.splitPathAndQuery(target.substring(this.prefix.length()));
            String path = pathAndQueryTuple.o1;

            Map<String, String> query = pathAndQueryTuple.o2;

            if (this.isAuthorized(path, query)) {
                try {
                    Response res = getResponse(path, query);
                    response.setStatusCode(res.responseCode);
                    response.setEntity(res.entity);
                } catch (Exception ex) {
                    response.setStatusCode(http500.responseCode);
                    response.setEntity(http500.entity);
                    ex.printStackTrace();
                }
            } else {
                response.setStatusCode(http403.responseCode);
                response.setEntity(http403.entity);
            }
        }
    }

    protected boolean isAuthorized(String path, Map<String, String> query) {
        return true;
    }

    protected abstract Response getResponse(String path, Map<String, String> query) throws Exception;

    
}
