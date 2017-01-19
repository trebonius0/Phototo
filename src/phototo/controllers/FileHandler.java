package phototo.controllers;

import phototo.helpers.FileHelper;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public abstract class FileHandler implements HttpRequestHandler {

    private final Path folderRoot;
    private final String prefix;
    private final String[] allowedExtensions;

    public FileHandler(Path folderRoot, String prefix, String allowedExtension) {
        this(folderRoot, prefix, new String[]{allowedExtension});
    }

    public FileHandler(Path folderRoot, String prefix, String[] allowedExtensions) {
        this.folderRoot = folderRoot;
        this.prefix = prefix.endsWith("/") ? prefix : (prefix + "/");
        this.allowedExtensions = allowedExtensions;
    }

    @Override
    public final void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {

        String method = request.getRequestLine().getMethod().toUpperCase();
        if (!method.equals("GET")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }

        String target = URLDecoder.decode(request.getRequestLine().getUri(), "UTF-8");

        if (!target.startsWith(this.prefix)) {
            throw new HttpException("Incorrect route");
        }

        String pathAndQuery = target.substring(this.prefix.length());
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

        final Path wantedLocally = this.folderRoot.resolve(path);
        if (!wantedLocally.startsWith(this.folderRoot) || Arrays.stream(allowedExtensions).noneMatch(FileHelper.getExtension(path).toLowerCase()::equals)) {
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            StringEntity entity = new StringEntity("<html><body><h1>Forbidden</h1></body></html>", ContentType.create("text/html", "UTF-8"));
            response.setEntity(entity);
        } else {
            File wantedLocallyFile = wantedLocally.toFile();
            if (!wantedLocallyFile.exists()) {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                StringEntity entity = new StringEntity("<html><body><h1>File " + wantedLocallyFile.getPath() + " not found</h1></body></html>", ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
            } else if (!wantedLocallyFile.canRead() || wantedLocallyFile.isDirectory()) {
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                StringEntity entity = new StringEntity("<html><body><h1>Forbidden</h1></body></html>", ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
            } else {
                    HttpEntity body = this.getEntity(path, query, wantedLocallyFile);
                    response.setStatusCode(HttpStatus.SC_OK);
                    response.setHeaders(this.getHeaders());
                    response.setEntity(body);
            }
        }
    }

    protected abstract String getContentType(String extension);

    protected Header[] getHeaders() {
        return new Header[0];
    }

    protected HttpEntity getEntity(String path, String query, File localFile) throws IOException {
        String extension = FileHelper.getExtension(path);
        return new FileEntity(localFile, ContentType.create(getContentType(extension.toLowerCase())));
    }

}
