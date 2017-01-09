package com.trebonius.phototo.core.thumbnails;

import java.io.IOException;
import java.nio.file.Path;

public interface IThumbnailGenerator {

    void generateThumbnail(Path originalFilename, long lastModifiedTimestamp) throws IOException;

    void deleteThumbnail(Path originalFilename, long lastModifiedTimestamp) throws IOException;

    void cleanOutdated() throws IOException;

    String getThumbnailUrl(Path originalFilename, long lastModifiedTimestamp);

    int getThumbnailWidth(int originalWidth, int originalHeight);

    int getThumbnailHeight(int originalWidth, int originalHeight);
}
