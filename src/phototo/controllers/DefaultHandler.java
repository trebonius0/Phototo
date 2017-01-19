package phototo.controllers;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class DefaultHandler implements HttpRequestHandler {

    private final Path folderRoot;

    public DefaultHandler(Path folderRoot) {
        this.folderRoot = folderRoot;
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {

        response.setStatusCode(HttpStatus.SC_OK);
        FileEntity body = new FileEntity(folderRoot.resolve("index.html").toFile(), ContentType.create("text/html; charset=UTF-8"));
        response.setEntity(body);
    }

}
