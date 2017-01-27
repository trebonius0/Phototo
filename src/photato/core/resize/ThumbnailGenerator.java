package photato.core.resize;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import photato.Routes;

public class ThumbnailGenerator extends ResizedImageGenerator {

    private final int thumbailHeight;

    public ThumbnailGenerator(FileSystem fileSystem, Path rootFolder, String thumbnailsFolderName, int thumbnailHeight, int thumbnailQuality) throws IOException {
        super(fileSystem, rootFolder, thumbnailsFolderName, thumbnailQuality, true, Routes.thumbnailRootUrl);
        this.thumbailHeight = thumbnailHeight;
    }

    @Override
    public int getResizedPictureWidth(int originalWidth, int originalHeight) {
        return originalWidth * thumbailHeight / Math.max(1, originalHeight);
    }

    @Override
    public int getResizedPictureHeight(int originalWidth, int originalHeight) {
        return thumbailHeight;
    }

}
