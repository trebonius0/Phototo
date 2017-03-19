package photato.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import photato.core.PhotatoFilesManager;
import photato.core.entities.PhotatoMedia;
import photato.core.resize.fullscreen.IFullScreenImageGetter;
import photato.helpers.FileHelper;

public class FullScreenImageHandler extends ImageHandler {

    private final IFullScreenImageGetter fullScreenImageEntityGetter;
    private final PhotatoFilesManager photatoFilesManager;

    public FullScreenImageHandler(Path folderRoot, String prefix, IFullScreenImageGetter fullScreenImageEntityGetter, PhotatoFilesManager photatoFilesManager) {
        super(folderRoot, prefix);
        this.fullScreenImageEntityGetter = fullScreenImageEntityGetter;
        this.photatoFilesManager = photatoFilesManager;
    }

    @Override
    protected Response getResponse(String path, Map<String, String> query, File wantedLocallyFile) throws Exception {
        if (!wantedLocallyFile.exists()) {
            HttpEntity entity = this.getEntity(path, query, wantedLocallyFile);

            if (entity == null) {
                return PhotatoHandler.http404;
            } else {
                return new Response(HttpStatus.SC_OK, entity, this.getHeaders());
            }
        } else if (!wantedLocallyFile.canRead() || wantedLocallyFile.isDirectory()) {
            return PhotatoHandler.http403;
        } else {
            return new Response(HttpStatus.SC_OK, this.getEntity(path, query, wantedLocallyFile), this.getHeaders());
        }
    }

    @Override
    protected HttpEntity getEntity(String path, Map<String, String> query, File localFile) throws IOException {
        String extension = FileHelper.getExtension(path);
        ContentType contentType = ContentType.create(getContentType(extension.toLowerCase()));

        PhotatoMedia picture = this.photatoFilesManager.getMediaFromHashUrl(path);

        return this.fullScreenImageEntityGetter.getImage(picture, contentType);

    }

}
