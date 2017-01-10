package com.trebonius.phototo.server;

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
import com.trebonius.phototo.core.fullscreen.IFullScreenResizeGenerator;
import com.trebonius.phototo.helpers.FileHelper;
import com.trebonius.phototo.helpers.SafeSimpleDateFormat;

public class ImageHandler extends FileHandler {

    private static final Pattern heightPattern = Pattern.compile("height=([0-9]*)");
    private static final Pattern widthPattern = Pattern.compile("width=([0-9]*)");
    private static final SafeSimpleDateFormat expiresDateFormat;
    private final IFullScreenResizeGenerator fullScreenResizeGenerator;

    static {
        expiresDateFormat = new SafeSimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        expiresDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        expiresDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public ImageHandler(String prefix, Path folderRoot, IFullScreenResizeGenerator fullScreenResizeGenerator) {
        super(prefix, folderRoot);

        this.fullScreenResizeGenerator = fullScreenResizeGenerator;
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
    protected HttpEntity getEntity(String path, String query, File localFile) throws Exception {
        String extension = FileHelper.getExtension(path);
        ContentType contentType = ContentType.create(getContentType(extension.toLowerCase()));

        if (query != null) {
            Matcher mHeight = heightPattern.matcher(query);
            Matcher mWidth = widthPattern.matcher(query);
            if (mHeight.find() && mWidth.find()) {
                int height = Integer.parseInt(mHeight.group(1));
                int width = Integer.parseInt(mWidth.group(1));

                if (this.fullScreenResizeGenerator != null) {
                    return this.fullScreenResizeGenerator.getImage(localFile, contentType, height, width);
                } else {
                    throw new IllegalStateException();
                }
            } else {
                return new FileEntity(localFile, contentType);
            }
        } else {
            return new FileEntity(localFile, contentType);
        }

    }

}
