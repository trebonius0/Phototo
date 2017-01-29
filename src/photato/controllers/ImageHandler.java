package photato.controllers;

import photato.Photato;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import photato.helpers.FileHelper;
import photato.helpers.SafeSimpleDateFormat;
import java.util.Map;
import org.apache.http.entity.FileEntity;
import photato.core.PhotatoFilesManager;
import photato.core.entities.PhotatoPicture;
import photato.core.resize.fullscreen.IFullScreenImageGetter;

public class ImageHandler extends FileHandler {

    private static final SafeSimpleDateFormat expiresDateFormat;
    private final IFullScreenImageGetter fullScreenImageEntityGetter;
    private final PhotatoFilesManager photatoFilesManager;

    static {
        expiresDateFormat = new SafeSimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        expiresDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        expiresDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public ImageHandler(Path folderRoot, String prefix, IFullScreenImageGetter fullScreenEntityGetter, PhotatoFilesManager photatoFilesManager) {
        super(folderRoot, prefix, Photato.supportedPictureExtensions);

        this.fullScreenImageEntityGetter = fullScreenEntityGetter;
        this.photatoFilesManager = photatoFilesManager;
    }

    @Override
    protected String getContentType(String extension) {
        return "image/" + extension;
    }

    @Override
    protected Header[] getHeaders() {
        int maxAge = 30 * 86400;

        return new Header[]{
            new BasicHeader("Cache-Control", "max-age=" + maxAge + ", public"),
            new BasicHeader("Expires", expiresDateFormat.format(new Date(System.currentTimeMillis() + maxAge * 1000L)))
        };
    }

    @Override
    protected HttpEntity getEntity(String path, Map<String, String> query, File localFile) throws IOException {
        String extension = FileHelper.getExtension(path);
        ContentType contentType = ContentType.create(getContentType(extension.toLowerCase()));

        PhotatoPicture picture = this.photatoFilesManager.getPicture(localFile.toPath());

        if (this.fullScreenImageEntityGetter != null) {
            return this.fullScreenImageEntityGetter.getImage(localFile.toPath(), contentType, picture);
        } else {
           return new FileEntity(localFile, contentType);
        }
    }

}
