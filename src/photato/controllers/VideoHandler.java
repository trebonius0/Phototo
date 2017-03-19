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

public class VideoHandler extends FileHandler {

    private static final SafeSimpleDateFormat expiresDateFormat;

    static {
        expiresDateFormat = new SafeSimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
        expiresDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        expiresDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public VideoHandler(Path folderRoot, String prefix) {
        super(folderRoot, prefix, Photato.supportedVideoExtensions);
    }

    @Override
    protected String getContentType(String extension) {
        return "video/" + extension;
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

        return new FileEntity(localFile, contentType);
    }

}
