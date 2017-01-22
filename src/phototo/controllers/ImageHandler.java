package phototo.controllers;

import phototo.Phototo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHeader;
import phototo.helpers.FileHelper;
import phototo.helpers.SafeSimpleDateFormat;
import phototo.core.fullscreen.IFullScreenImageEntityGetter;
import java.util.Map;

public class ImageHandler extends FileHandler {

    private static final SafeSimpleDateFormat expiresDateFormat;
    private final IFullScreenImageEntityGetter fullScreenImageEntityGetter;

    static {
        expiresDateFormat = new SafeSimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        expiresDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        expiresDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public ImageHandler(Path folderRoot, String prefix, IFullScreenImageEntityGetter fullScreenEntityGetter) {
        super(folderRoot, prefix, Phototo.supportedPictureExtensions);

        this.fullScreenImageEntityGetter = fullScreenEntityGetter;
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
    protected HttpEntity getEntity(String path, Map<String, String> query, File localFile) {
        String extension = FileHelper.getExtension(path);
        ContentType contentType = ContentType.create(getContentType(extension.toLowerCase()));

        if (query.containsKey("height") && query.containsKey("width") && query.containsKey("rotationId")) {
            int height = Integer.parseInt(query.get("height"));
            int width = Integer.parseInt(query.get("width"));
            int rotationId = Integer.parseInt(query.get("rotationId"));

            if (this.fullScreenImageEntityGetter != null) {
                return this.fullScreenImageEntityGetter.getImage(localFile, contentType, height, width, rotationId);
            } else {
                throw new IllegalStateException();
            }
        } else {
            return new FileEntity(localFile, contentType);
        }

    }

}
