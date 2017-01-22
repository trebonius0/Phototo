package photato.controllers;

import photato.helpers.FileHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

public abstract class FileHandler extends PhotatoHandler {

    private final Path folderRoot;
    private final String[] allowedExtensions;

    public FileHandler(Path folderRoot, String prefix, String allowedExtension) {
        this(folderRoot, prefix, new String[]{allowedExtension});
    }

    public FileHandler(Path folderRoot, String prefix, String[] allowedExtensions) {
        super(prefix, new String[]{"GET"});
        this.folderRoot = folderRoot;
        this.allowedExtensions = allowedExtensions;
    }

    @Override
    protected Response getResponse(String path, Map<String,String> query) throws Exception {
        final Path wantedLocally = this.folderRoot.resolve(path);
        if (!wantedLocally.startsWith(this.folderRoot) || Arrays.stream(allowedExtensions).noneMatch(FileHelper.getExtension(path).toLowerCase()::equals)) {
            return PhotatoHandler.http403;
        } else {
            File wantedLocallyFile = wantedLocally.toFile();
            if (!wantedLocallyFile.exists()) {
                return PhotatoHandler.http404;
            } else if (!wantedLocallyFile.canRead() || wantedLocallyFile.isDirectory()) {
                return PhotatoHandler.http403;
            } else {
                return new Response(HttpStatus.SC_OK, this.getEntity(path, query, wantedLocallyFile), this.getHeaders());
            }
        }
    }

    protected abstract String getContentType(String extension);

    protected Header[] getHeaders() {
        return new Header[0];
    }

    protected HttpEntity getEntity(String path, Map<String,String> query, File localFile) throws IOException {
        String extension = FileHelper.getExtension(path);
        return new FileEntity(localFile, ContentType.create(getContentType(extension.toLowerCase())));
    }

}
