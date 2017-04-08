package photato.controllers;

import java.nio.file.Path;
import java.util.Map;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

public class LoadingHandler extends PhotatoHandler {

    private final Path folderRoot;

    public LoadingHandler(Path folderRoot) {
        super("", new String[]{"GET"});
        this.folderRoot = folderRoot;
    }

    @Override
    protected Response getResponse(String path, Map<String, String> query) throws Exception {
        return new Response(200, new FileEntity(folderRoot.resolve("loading.html").toFile(), ContentType.create("text/html; charset=UTF-8")));
    }

}
