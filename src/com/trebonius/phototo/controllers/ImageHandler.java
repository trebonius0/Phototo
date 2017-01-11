package com.trebonius.phototo.controllers;

import com.trebonius.phototo.Phototo;
import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHeader;
import com.trebonius.phototo.helpers.FileHelper;
import com.trebonius.phototo.helpers.SafeSimpleDateFormat;
import com.trebonius.phototo.core.fullscreen.IFullScreenImageEntityGetter;
import com.trebonius.phototo.helpers.QueryStringHelper;
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
    protected HttpEntity getEntity(String path, String query, File localFile) {
        String extension = FileHelper.getExtension(path);
        ContentType contentType = ContentType.create(getContentType(extension.toLowerCase()));

        Map<String, String> queryStringParameters = QueryStringHelper.splitSearchQuery(query);
        if (queryStringParameters.containsKey("height") && queryStringParameters.containsKey("width")) {
            int height = Integer.parseInt(queryStringParameters.get("height"));
            int width = Integer.parseInt(queryStringParameters.get("width"));

            if (this.fullScreenImageEntityGetter != null) {
                return this.fullScreenImageEntityGetter.getImage(localFile, contentType, height, width);
            } else {
                throw new IllegalStateException();
            }
        } else {
            return new FileEntity(localFile, contentType);
        }

    }

}
